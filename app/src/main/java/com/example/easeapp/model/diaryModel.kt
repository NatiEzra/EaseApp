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
        RetrofitClientDiary.create(context).create(diaryApi::class.java)
    }

    private val diaryList: MutableList<DiaryModel> = mutableListOf()

    fun addDiary(diaryModel: DiaryModel) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val currentUser = AppDatabase.getInstance(context).userDao().getCurrentUser()
                val token = "Bearer " + (currentUser?.accessToken?.trim() ?: "")

                val request = DiaryModel(
                    context = diaryModel.context,
                    date = Date(),
                    authorId = currentUser?._id  ?: ""
                )

                diaryApi.addDiary(request, token).enqueue(object : retrofit2.Callback<DiaryResponse> {
                    override fun onResponse(
                        call: retrofit2.Call<DiaryResponse>,
                        response: retrofit2.Response<DiaryResponse>
                    ) {
                        if (response.isSuccessful) {
                            Log.d("DiaryRepo", "Diary added successfully")
                        } else {
                            Log.e("DiaryRepo", "Failed to add diary: ${response.errorBody()?.string()}")
                        }
                    }

                    override fun onFailure(call: retrofit2.Call<DiaryResponse>, t: Throwable) {
                        Log.e("DiaryRepo", "Error adding diary", t)
                    }
                })

            } catch (e: Exception) {
                Log.e("DiaryRepo", "Error retrieving current user or sending diary", e)
            }
        }
    }
}
