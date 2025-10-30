package com.example.loginapp.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.loginapp.databinding.ItemPhotoBinding
import com.example.loginapp.models.Photo

class PhotoAdapter(
    private val photos: MutableList<Photo>,
    private val onPhotoClick: (Photo) -> Unit,
    private val onPhotoLongClick: (Photo, Int) -> Unit
) : RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder>() {

    inner class PhotoViewHolder(private val binding: ItemPhotoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(photo: Photo, position: Int) {
            // Set photo image
            if (photo.imageUri.isNotEmpty()) {
                try {
                    binding.photoImage.setImageURI(Uri.parse(photo.imageUri))
                } catch (e: Exception) {
                    // Keep default background if image fails to load
                }
            }

            // Set memo
            if (photo.memo.isNotEmpty()) {
                binding.photoMemo.text = photo.memo
                binding.photoMemo.visibility = View.VISIBLE
            } else {
                binding.photoMemo.visibility = View.GONE
            }

            // Set date
            if (photo.date.isNotEmpty()) {
                binding.photoDate.text = photo.date
                binding.photoDate.visibility = View.VISIBLE
            } else {
                binding.photoDate.visibility = View.GONE
            }

            binding.root.setOnClickListener {
                onPhotoClick(photo)
            }

            binding.root.setOnLongClickListener {
                onPhotoLongClick(photo, position)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val binding = ItemPhotoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PhotoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        holder.bind(photos[position], position)
    }

    override fun getItemCount() = photos.size

    fun addPhoto(photo: Photo) {
        photos.add(photo)
        notifyItemInserted(photos.size - 1)
    }

    fun removePhoto(position: Int) {
        if (position in photos.indices) {
            photos.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}
