package com.example.loginapp.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.loginapp.databinding.ItemPolaroidAlbumBinding
import com.example.loginapp.models.PolaroidAlbum

class AlbumAdapter(
    private val albums: MutableList<PolaroidAlbum>,
    private val onAlbumClick: (PolaroidAlbum) -> Unit,
    private val onAlbumLongClick: (PolaroidAlbum, Int) -> Unit
) : RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder>() {

    inner class AlbumViewHolder(private val binding: ItemPolaroidAlbumBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(album: PolaroidAlbum, position: Int) {
            binding.albumTitle.text = album.title.ifEmpty { "제목 없음" }
            binding.albumDate.text = album.date.ifEmpty { "" }

            // Set cover image
            if (album.coverImageUri.isNotEmpty()) {
                try {
                    binding.albumCoverImage.setImageURI(Uri.parse(album.coverImageUri))
                } catch (e: Exception) {
                    // Keep default background if image fails to load
                }
            }

            binding.root.setOnClickListener {
                onAlbumClick(album)
            }

            binding.root.setOnLongClickListener {
                onAlbumLongClick(album, position)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
        val binding = ItemPolaroidAlbumBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AlbumViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
        holder.bind(albums[position], position)
    }

    override fun getItemCount() = albums.size

    fun updateAlbums(newAlbums: List<PolaroidAlbum>) {
        albums.clear()
        albums.addAll(newAlbums)
        notifyDataSetChanged()
    }

    fun removeAlbum(position: Int) {
        if (position in albums.indices) {
            albums.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}
