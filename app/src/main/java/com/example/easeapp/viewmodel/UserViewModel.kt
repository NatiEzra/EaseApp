package com.example.ease.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.ease.model.User

class UserViewModel : ViewModel() {

    private val userRepo = User()

    private val _createUserResult = MutableLiveData<Result<Unit>>()
    val createUserResult: LiveData<Result<Unit>> = _createUserResult

    private val _profileImage = MutableLiveData<String?>()
    val profileImage: LiveData<String?> = _profileImage

    private val _user = MutableLiveData<Map<String, Any>?>()
    val user: LiveData<Map<String, Any>?> get() = _user


    private val _editUserResult = MutableLiveData<Result<Unit>>()
    val editUserResult: LiveData<Result<Unit>> = _editUserResult

    fun createUser(name: String, email: String, bitmap: Bitmap?) {
        userRepo.createUser(name, email, bitmap) { success, error ->
            if (success) {
                _createUserResult.postValue(Result.success(Unit))
            } else {
                _createUserResult.postValue(Result.failure(Throwable(error)))
            }
        }
    }

    fun getProfileImage() {
        userRepo.getProfileImage { url ->
            _profileImage.postValue(url)
        }
    }

    fun editUser(currentPassword: String, name: String, newPassword: String, image: Bitmap?) {
        userRepo.editUser(currentPassword, name, newPassword, image) { success, error ->
            if (success) {
                _editUserResult.postValue(Result.success(Unit))
            } else {
                _editUserResult.postValue(Result.failure(Throwable(error)))
            }
        }
    }
    fun fetchUser() {
        userRepo.getUser { userData ->
            _user.postValue(userData)
        }
    }

}
