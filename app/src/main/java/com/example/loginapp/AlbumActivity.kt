package com.example.loginapp

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.example.loginapp.adapters.PhotoAdapter
import com.example.loginapp.databinding.ActivityAlbumBinding
import com.example.loginapp.databinding.DialogAddPhotoBinding
import com.example.loginapp.models.Photo
import com.example.loginapp.models.PolaroidAlbum

class AlbumActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAlbumBinding
    private lateinit var photoAdapter: PhotoAdapter
    private lateinit var album: PolaroidAlbum
    private var albumPosition: Int = -1
    private var selectedPhotoUri: Uri? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            pickImageLauncher.launch("image/*")
        } else {
            Toast.makeText(this, "사진 접근 권한이 필요합니다", Toast.LENGTH_SHORT).show()
        }
    }

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedPhotoUri = it
            currentPhotoPreview?.setImageURI(it)
        }
    }

    private var currentPhotoPreview: android.widget.ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlbumBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get album from intent
        album = intent.getSerializableExtra("album") as? PolaroidAlbum ?: run {
            finish()
            return
        }
        albumPosition = intent.getIntExtra("position", -1)

        setupToolbar()
        setupAlbumInfo()
        setupRecyclerView()
        setupFab()
        updateEmptyState()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finishWithResult()
        }
    }

    private fun setupAlbumInfo() {
        binding.albumTitleText.text = album.title
        binding.albumDateText.text = album.date

        if (album.memo.isNotEmpty()) {
            binding.albumMemoText.text = album.memo
            binding.albumMemoText.visibility = View.VISIBLE
        } else {
            binding.albumMemoText.visibility = View.GONE
        }
    }

    private fun setupRecyclerView() {
        photoAdapter = PhotoAdapter(
            album.photos,
            onPhotoClick = { photo ->
                // Could expand to show full-screen photo
                Toast.makeText(this, "사진: ${photo.memo}", Toast.LENGTH_SHORT).show()
            },
            onPhotoLongClick = { photo, position ->
                showPhotoOptionsDialog(photo, position)
            }
        )

        binding.photosRecyclerView.apply {
            layoutManager = GridLayoutManager(this@AlbumActivity, 2)
            adapter = photoAdapter
        }
    }

    private fun setupFab() {
        binding.fabAddPhoto.setOnClickListener {
            if (album.photos.size >= 20) {
                Toast.makeText(this, R.string.photo_limit, Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            showAddPhotoDialog()
        }
    }

    private fun showAddPhotoDialog() {
        val dialogBinding = DialogAddPhotoBinding.inflate(layoutInflater)
        selectedPhotoUri = null
        currentPhotoPreview = dialogBinding.photoPreview

        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()

        dialogBinding.btnSelectPhoto.setOnClickListener {
            checkPermissionAndPickImage()
        }

        dialogBinding.btnSavePhoto.setOnClickListener {
            val imageUri = selectedPhotoUri?.toString()
            if (imageUri.isNullOrEmpty()) {
                Toast.makeText(this, "사진을 선택해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val photo = Photo(
                imageUri = imageUri,
                memo = dialogBinding.editPhotoMemo.text.toString(),
                date = dialogBinding.editPhotoDate.text.toString()
            )

            album.photos.add(photo)
            photoAdapter.addPhoto(photo)
            updateEmptyState()

            dialog.dismiss()
            Toast.makeText(this, "사진이 추가되었습니다", Toast.LENGTH_SHORT).show()
        }

        dialogBinding.btnCancelPhoto.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showPhotoOptionsDialog(photo: Photo, position: Int) {
        val options = arrayOf("삭제")
        AlertDialog.Builder(this)
            .setTitle("사진 옵션")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        // Delete
                        AlertDialog.Builder(this)
                            .setTitle("사진 삭제")
                            .setMessage("이 사진을 삭제하시겠습니까?")
                            .setPositiveButton("삭제") { _, _ ->
                                photoAdapter.removePhoto(position)
                                updateEmptyState()
                                Toast.makeText(this, "사진이 삭제되었습니다", Toast.LENGTH_SHORT).show()
                            }
                            .setNegativeButton("취소", null)
                            .show()
                    }
                }
            }
            .show()
    }

    private fun updateEmptyState() {
        if (album.photos.isEmpty()) {
            binding.emptyPhotosText.visibility = View.VISIBLE
            binding.photosRecyclerView.visibility = View.GONE
        } else {
            binding.emptyPhotosText.visibility = View.GONE
            binding.photosRecyclerView.visibility = View.VISIBLE
        }
    }

    private fun finishWithResult() {
        val resultIntent = Intent().apply {
            putExtra("album", album)
            putExtra("position", albumPosition)
        }
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    override fun onBackPressed() {
        finishWithResult()
        super.onBackPressed()
    }

    private fun checkPermissionAndPickImage() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        when {
            ContextCompat.checkSelfPermission(
                this,
                permission
            ) == PackageManager.PERMISSION_GRANTED -> {
                pickImageLauncher.launch("image/*")
            }
            else -> {
                requestPermissionLauncher.launch(permission)
            }
        }
    }
}
