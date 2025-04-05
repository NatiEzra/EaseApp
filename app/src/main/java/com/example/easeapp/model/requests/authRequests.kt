package com.example.easeapp.model.requests
//LOGIN
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
//REGISTER
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
    val role: String
)

//GET USER
data class UserProfileResponse(
    val user: UserDetails
)

data class UserDetails(
    val _id: String,
    val username: String,
    val email: String,
    val role: String,
    val profilePicture: String?,
    var accessToken: String
) {

}



