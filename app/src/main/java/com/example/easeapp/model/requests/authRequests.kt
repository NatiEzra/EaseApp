package com.example.easeapp.model.requests

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val _id: String,
    val username: String,
    val role: String,
    val isAuthenticated: Boolean,
    val likedPosts: List<String>
)
