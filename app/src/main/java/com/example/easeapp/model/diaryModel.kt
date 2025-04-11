package com.example.easeapp.model

import android.content.Context
import android.util.Log
import com.example.ease.model.local.AppDatabase
import com.example.easeapp.model.requests.DiaryApi
import com.example.easeapp.model.requests.RetrofitClientDiary
import com.example.easeapp.model.requests.UserApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date
import androidx.lifecycle.lifecycleScope

// נתוני המודל
data class DiaryModel(
    val context: String,
    val date: Date,
    val authorId: String
)

data class DiaryResponse(
    val text: String,
)


class DiaryRepo(private val context: Context) {

    private val diaryApi: DiaryApi by lazy {
        RetrofitClientDiary.create(context).create(DiaryApi::class.java)
    }

    fun addDiary(diaryText: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val currentUser = AppDatabase.getInstance(context).userDao().getCurrentUser()

                if (currentUser == null) {
                    Log.e("DiaryRepo", "No current user found")
                    return@launch
                }

                val token = "Bearer " + currentUser.accessToken?.trim()
                val request = DiaryModel(
                    context = diaryText,
                    date = Date(),
                    authorId = currentUser._id ?: ""
                )

                diaryApi.addDiary(request, token)
                    .enqueue(object : retrofit2.Callback<DiaryResponse> {
                        override fun onResponse(
                            call: retrofit2.Call<DiaryResponse>,
                            response: retrofit2.Response<DiaryResponse>
                        ) {
                            if (response.isSuccessful) {
                                Log.d("DiaryRepo", "Diary added: ${response.body()?.text}")
                            } else {
                                Log.e("DiaryRepo", "Failed: ${response.errorBody()?.string()}")
                            }
                        }

                        override fun onFailure(call: retrofit2.Call<DiaryResponse>, t: Throwable) {
                            Log.e("DiaryRepo", "Request failed", t)
                        }
                    })

            } catch (e: Exception) {
                Log.e("DiaryRepo", "Error preparing request", e)
            }
        }
    }
}


