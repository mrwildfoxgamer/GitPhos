package com.example.gitphos.data.remote

import com.example.gitphos.data.remote.model.GithubUserDto
import retrofit2.http.GET
import retrofit2.http.Header

interface GithubApi {

    @GET("user")
    suspend fun getAuthenticatedUser(
        @Header("Authorization") token: String
    ): GithubUserDto
}