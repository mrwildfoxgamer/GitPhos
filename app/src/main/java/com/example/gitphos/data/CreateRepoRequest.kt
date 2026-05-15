package com.example.gitphos.data.remote.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CreateRepoRequest(
    @Json(name = "name") val name: String,
    @Json(name = "private") val private: Boolean = true,
    @Json(name = "auto_init") val autoInit: Boolean = true
)