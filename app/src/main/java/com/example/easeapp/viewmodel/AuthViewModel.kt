package com.example.ease.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ease.repositories.AuthRepository
import kotlinx.coroutines.launch

class AuthViewModel(

    private val authRepo: AuthRepository = AuthRepository.shared

) : ViewModel() {

    private val _authState = MutableLiveData<Result<Boolean>>()
    val authState: LiveData<Result<Boolean>> get() = _authState

    fun login(email: String, password: String) {
        viewModelScope.launch {
            authRepo.loginUser(email, password) { success, error ->
                if (success) {
                    _authState.postValue(Result.success(true))
                } else {
                    _authState.postValue(Result.failure(Throwable(error)))
                }
            }
        }
    }


    fun register(email: String, password: String) {
        viewModelScope.launch {
            authRepo.registerUser(email, password) { success, error ->
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
