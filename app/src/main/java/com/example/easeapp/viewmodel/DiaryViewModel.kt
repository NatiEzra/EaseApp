package com.example.easeapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.easeapp.model.DiaryRepo
import kotlinx.coroutines.launch

import android.content.Context
import android.widget.Toast
import com.example.easeapp.model.DiaryModel
import kotlinx.coroutines.Dispatchers

class DiaryViewModel : ViewModel() {

    private val _diarySaved = MutableLiveData<Result<Boolean>>()
    val diarySaved: LiveData<Result<Boolean>> get() = _diarySaved

    private val _userDiaries = MutableLiveData<List<DiaryModel>>()
    val userDiaries: LiveData<List<DiaryModel>> get() = _userDiaries

    private val _diaryDeleted = MutableLiveData<Result<Boolean>>()
    val diaryDeleted: LiveData<Result<Boolean>> get() = _diaryDeleted

//    fun deletePost(postId: String) {
//        viewModelScope.launch(Dispatchers.IO) {
//            postModel.deletePost(postId) { success, error ->
//                if (success) _postOperationState.postValue(Result.success(Unit))
//                else _postOperationState.postValue(Result.failure(Throwable(error)))
//            }
//        }
//    }

    fun deleteDiaryEntry(context: Context, diaryId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val diaryRepo = DiaryRepo(context)
            val result = diaryRepo.deleteDiary(diaryId)

            if (result == "Success") {
                _diaryDeleted.postValue(Result.success(true))
                Toast.makeText(context, "Diary entry deleted successfully", Toast.LENGTH_SHORT).show()
            } else {
                _diaryDeleted.postValue(Result.failure(Throwable(result.toString())))
            }
        }
    }
    fun addDiaryEntry(context: Context, text: String) {
        if (text.isBlank()) {
            _diarySaved.postValue(Result.failure(Throwable("Diary text cannot be empty")))
            return
        }

        viewModelScope.launch {
            val diaryRepo = DiaryRepo(context)
            val result = diaryRepo.addDiary(text)

            if (result == "Success" || result.contains("created successfully", ignoreCase = true)) {
                Toast.makeText(context, "Diary saved successfully", Toast.LENGTH_SHORT).show()
                _diarySaved.postValue(Result.success(true))
            } else {
                _diarySaved.postValue(Result.failure(Throwable(result)))
            }
        }
    }
    fun loadUserDiaries(context: Context) {
        viewModelScope.launch {
            val diaryRepo = DiaryRepo(context)
            val diaries = diaryRepo.getUserDiaries()
            _userDiaries.postValue(diaries)
        }
    }
}

