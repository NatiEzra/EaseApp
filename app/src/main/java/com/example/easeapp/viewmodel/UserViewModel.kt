package com.example.ease.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.ease.model.User
import com.example.ease.model.UserRepository

class UserViewModel(application: Application) : AndroidViewModel(application) {

    private val userRepo = UserRepository.getInstance(application.applicationContext)

    private val _createUserResult = MutableLiveData<Result<Unit>>()
    val createUserResult: LiveData<Result<Unit>> = _createUserResult

    private val _allDoctors = MutableLiveData<List<User>>()
    val allDoctors: LiveData<List<User>> = _allDoctors

    fun fetchAlldoctors(context: Context) {
        userRepo.getAllDoctors(context) { users ->
            _allDoctors.postValue(users)
        }
    }
}