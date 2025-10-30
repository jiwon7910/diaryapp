package com.example.loginapp.models

import java.io.Serializable
import java.util.Date

data class PolaroidAlbum(
    val id: String = java.util.UUID.randomUUID().toString(),
    var title: String = "",
    var date: String = "",
    var coverImageUri: String = "",
    var memo: String = "",
    val photos: MutableList<Photo> = mutableListOf(),
    val createdAt: Long = System.currentTimeMillis()
) : Serializable
