package com.example.gitphos.data.remote

import com.example.gitphos.data.remote.model.AccessTokenResponse
import com.example.gitphos.data.remote.model.GithubRepoDto
import com.example.gitphos.data.remote.model.GithubUserDto
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface GithubApi {

    @GET("user")
    suspend fun getAuthenticatedUser(
        @Header("Authorization") token: String
    ): GithubUserDto

    @GET("user/repos?sort=updated&per_page=100")
    suspend fun getUserRepos(
        @Header("Authorization") token: String
    ): List<GithubRepoDto>

    @Headers("Accept: application/json")
    @POST("https://github.com/login/oauth/access_token")
    @FormUrlEncoded
    suspend fun getAccessToken(
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String,
        @Field("code") code: String
    ): AccessTokenResponse
}