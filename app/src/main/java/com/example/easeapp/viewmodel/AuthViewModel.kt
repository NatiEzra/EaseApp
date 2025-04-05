package com.example.ease.viewmodel

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ease.repositories.AuthRepository
import com.example.easeapp.model.requests.UserDetails
import kotlinx.coroutines.launch

class AuthViewModel(

    private val authRepo: AuthRepository = AuthRepository.shared

) : ViewModel() {

    private val _authState = MutableLiveData<Result<Boolean>>()
    val authState: LiveData<Result<Boolean>> get() = _authState
    private val _loggedInUser = MutableLiveData<UserDetails>()
    val loggedInUser: LiveData<UserDetails> get() = _loggedInUser


    fun login(context: Context, email: String, password: String) {
        viewModelScope.launch {
            authRepo.loginUser(context, email, password) { success,  error, user ->
                if (success && user is UserDetails) {
                    _loggedInUser.postValue(user!!)
                    _authState.postValue(Result.success(true))
                } else {
                    _authState.postValue(Result.failure(Throwable(error)))
                }
            }
        }
    }


    fun register(context: Context, username: String, email: String, password: String, bitmap: Bitmap?) {
        viewModelScope.launch {
            authRepo.registerUser(context, username, email, password, bitmap) { success, error ->
                if (success) {
                    _authState.postValue(Result.success(true))
                } else {
                    _authState.postValue(Result.failure(Throwable(error)))
                }
            }
        }
    }

    fun signOut() {
        authRepo.signOut()
    }

    fun isUserLoggedIn(): Boolean = authRepo.isUserLoggedIn()
}
