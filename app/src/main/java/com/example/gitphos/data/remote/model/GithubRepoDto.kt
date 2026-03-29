package com.example.gitphos.data.remote.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GithubRepoDto(
    @Json(name = "id") val id: Long,
    @Json(name = "name") val name: String,
    @Json(name = "clone_url") val cloneUrl: String,
    @Json(name = "default_branch") val defaultBranch: String?
)