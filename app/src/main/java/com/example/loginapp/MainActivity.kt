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
import com.example.loginapp.adapters.AlbumAdapter
import com.example.loginapp.databinding.ActivityMainBinding
import com.example.loginapp.databinding.DialogAddAlbumBinding
import com.example.loginapp.models.PolaroidAlbum
import com.example.loginapp.utils.DataManager

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var albumAdapter: AlbumAdapter
    private lateinit var dataManager: DataManager
    private val albums = mutableListOf<PolaroidAlbum>()
    private var selectedImageUri: Uri? = null
    private var currentImagePreview: android.widget.ImageView? = null
    private var editingAlbum: PolaroidAlbum? = null
    private var editingPosition: Int = -1

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
            selectedImageUri = it
            currentImagePreview?.setImageURI(it)
        }
    }

    private val albumActivityLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.let { intent ->
                val updatedAlbum = intent.getSerializableExtra("album") as? PolaroidAlbum
                val position = intent.getIntExtra("position", -1)

                if (updatedAlbum != null && position != -1 && position < albums.size) {
                    albums[position] = updatedAlbum
                    albumAdapter.notifyItemChanged(position)
                    saveAlbums()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dataManager = DataManager(this)
        loadAlbums()
        
        setupRecyclerView()
        setupFab()
        updateEmptyState()
    }

    private fun loadAlbums() {
        albums.clear()
        albums.addAll(dataManager.loadAlbums())
    }

    private fun saveAlbums() {
        dataManager.saveAlbums(albums)
    }

    private fun setupRecyclerView() {
        albumAdapter = AlbumAdapter(
            albums,
            onAlbumClick = { album ->
                openAlbum(album)
            },
            onAlbumLongClick = { album, position ->
                showAlbumOptionsDialog(album, position)
            }
        )

        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(this@MainActivity, 2)
            adapter = albumAdapter
        }
    }

    private fun setupFab() {
        binding.fabAddAlbum.setOnClickListener {
            showAddAlbumDialog(null, -1)
        }
    }

    private fun showAddAlbumDialog(album: PolaroidAlbum?, position: Int) {
        val dialogBinding = DialogAddAlbumBinding.inflate(layoutInflater)
        selectedImageUri = null
        currentImagePreview = dialogBinding.previewImage
        editingAlbum = album
        editingPosition = position

        // If editing, populate fields
        album?.let {
            dialogBinding.editTitle.setText(it.title)
            dialogBinding.editDate.setText(it.date)
            dialogBinding.editMemo.setText(it.memo)
            if (it.coverImageUri.isNotEmpty()) {
                selectedImageUri = Uri.parse(it.coverImageUri)
                dialogBinding.previewImage.setImageURI(selectedImageUri)
            }
        }

        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()

        dialogBinding.btnSelectImage.setOnClickListener {
            checkPermissionAndPickImage()
        }

        dialogBinding.btnSave.setOnClickListener {
            val title = dialogBinding.editTitle.text.toString()
            val date = dialogBinding.editDate.text.toString()
            val memo = dialogBinding.editMemo.text.toString()

            if (title.isEmpty()) {
                Toast.makeText(this, "제목을 입력해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (album == null) {
                // Create new album
                val newAlbum = PolaroidAlbum(
                    title = title,
                    date = date,
                    memo = memo,
                    coverImageUri = selectedImageUri?.toString() ?: ""
                )
                albums.add(newAlbum)
                albumAdapter.notifyItemInserted(albums.size - 1)
                saveAlbums()
                Toast.makeText(this, "앨범이 생성되었습니다", Toast.LENGTH_SHORT).show()
            } else {
                // Update existing album
                album.title = title
                album.date = date
                album.memo = memo
                album.coverImageUri = selectedImageUri?.toString() ?: album.coverImageUri
                albumAdapter.notifyItemChanged(position)
                saveAlbums()
                Toast.makeText(this, "앨범이 수정되었습니다", Toast.LENGTH_SHORT).show()
            }

            updateEmptyState()
            dialog.dismiss()
        }

        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun openAlbum(album: PolaroidAlbum) {
        val position = albums.indexOf(album)
        val intent = Intent(this, AlbumActivity::class.java).apply {
            putExtra("album", album)
            putExtra("position", position)
        }
        albumActivityLauncher.launch(intent)
    }

    private fun showAlbumOptionsDialog(album: PolaroidAlbum, position: Int) {
        val options = arrayOf("수정", "삭제")
        AlertDialog.Builder(this)
            .setTitle(album.title)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        // Edit
                        showAddAlbumDialog(album, position)
                    }
                    1 -> {
                        // Delete
                        AlertDialog.Builder(this)
                            .setTitle("앨범 삭제")
                            .setMessage("'${album.title}' 앨범을 삭제하시겠습니까?\n(앨범 내 모든 사진이 삭제됩니다)")
                            .setPositiveButton("삭제") { _, _ ->
                                albumAdapter.removeAlbum(position)
                                saveAlbums()
                                updateEmptyState()
                                Toast.makeText(this, "앨범이 삭제되었습니다", Toast.LENGTH_SHORT).show()
                            }
                            .setNegativeButton("취소", null)
                            .show()
                    }
                }
            }
            .show()
    }

    private fun updateEmptyState() {
        if (albums.isEmpty()) {
            binding.emptyTextView.visibility = View.VISIBLE
            binding.recyclerView.visibility = View.GONE
        } else {
            binding.emptyTextView.visibility = View.GONE
            binding.recyclerView.visibility = View.VISIBLE
        }
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