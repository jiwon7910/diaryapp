package com.example.loginapp.models

import java.io.Serializable

data class Photo(
    val id: String = java.util.UUID.randomUUID().toString(),
    var imageUri: String = "",
    var memo: String = "",
    var date: String = "",
    val createdAt: Long = System.currentTimeMillis()
) : Serializable
