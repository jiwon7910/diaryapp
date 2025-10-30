package com.example.loginapp.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.loginapp.models.PolaroidAlbum
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class DataManager(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("PolaroidAlbums", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveAlbums(albums: List<PolaroidAlbum>) {
        val json = gson.toJson(albums)
        sharedPreferences.edit().putString("albums", json).apply()
    }

    fun loadAlbums(): MutableList<PolaroidAlbum> {
        val json = sharedPreferences.getString("albums", null)
        return if (json != null) {
            val type = object : TypeToken<MutableList<PolaroidAlbum>>() {}.type
            gson.fromJson(json, type)
        } else {
            mutableListOf()
        }
    }
}
