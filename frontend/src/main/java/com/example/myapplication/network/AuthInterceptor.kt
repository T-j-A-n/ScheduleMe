package com.example.myapplication.network

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.Response

interface AuthApi {
    @POST("/auth/register")
    suspend fun register(@Body request: AuthRequest): Response<Void>

    @POST("/auth/login")
    suspend fun login(@Body request: AuthRequest): Response<AuthResponse>
}
