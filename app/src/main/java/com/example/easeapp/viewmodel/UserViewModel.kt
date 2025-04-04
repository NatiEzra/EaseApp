package com.example.ease.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.ease.model.User
import com.example.ease.model.UserRepository

class UserViewModel : ViewModel() {

    private val userRepo = UserRepository()

    private val _createUserResult = MutableLiveData<Result<Unit>>()
    val createUserResult: LiveData<Result<Unit>> = _createUserResult

    private val _allDoctors = MutableLiveData<List<User>>()
    val allDoctors: LiveData<List<User>> = _allDoctors

    fun fetchAlldoctors() {
        userRepo.getAllDoctors { users ->
            _allDoctors.postValue(users)
        }
    }




    fun fetchUser() {

    }

}
