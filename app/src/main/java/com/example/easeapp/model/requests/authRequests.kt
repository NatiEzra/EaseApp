package com.example.easeapp.model.requests

// LOGIN
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
    val likedPosts: List<String>,
    val gender: String?,
    val dateOfBirth: String?,
    val phoneNumber: String?,
    val profilePicture: String?,
    val email: String
)

// REGISTER
data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String
)

data class RegisterResponse(
    val message: String,
    val user: RegisteredUser
)

data class RegisteredUser(
    val username: String,
    val email: String,
    val _id: String,
    val profilePicture: String,
    val role: String,
    val gender: String?,
    val dateOfBirth: String?,
    val phoneNumber: String?
)

// GET USER
data class UserProfileResponse(
    val user: UserDetails
)

data class UserDetails(
    val _id: String,
    val username: String,
    val email: String,
    val role: String,
    val profilePicture: String?,
    var accessToken: String,
    val phoneNumber: String?,
    val dateOfBirth: String?,
    val gender: String?
)

// UPDATE PROFILE
data class UpdateProfileResponse(
    val message: String,
    val user: UserDetails
)