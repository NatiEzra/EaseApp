package com.example.easeapp.model

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.ease.model.local.AppDatabase
import com.example.easeapp.model.requests.DiaryApi
import com.example.easeapp.model.requests.RetrofitClientDiary
import com.example.easeapp.model.requests.UserApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.ease.R
import com.example.ease.ui.activities.MainActivity
import kotlinx.coroutines.withContext

// נתוני המודל
data class DiaryModel(
    val context: String,
    val date: Date,
    val authorId: String
)

data class DiaryResponse(
    val message: String

)


class DiaryRepo(
private val context: Context
) {
    private val api: DiaryApi by lazy {
        RetrofitClientDiary.create(context).create(DiaryApi::class.java)
    }

    private val db: AppDatabase by lazy {
        AppDatabase.getInstance(context)
    }

    suspend fun addDiary(diaryText: String): String = withContext(Dispatchers.IO) {
        try {
            val currentUser = db.userDao().getCurrentUser()
            if (currentUser == null) return@withContext "User not found"

            val token = "Bearer ${currentUser.accessToken?.trim()}"
            val request = DiaryModel(
                context = diaryText,
                date = Date(),
                authorId = currentUser._id ?: ""
            )

            val response = api.addDiary(request, token)

            return@withContext if (response.isSuccessful) {
                response.body()?.message ?: "Success"
            } else {
                val error = response.errorBody()?.string()
                "Error: ${error ?: "Unknown error"}"
            }

        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext "Error: ${e.message}"
        }
    }
    suspend fun getUserDiaries(): List<DiaryModel> = withContext(Dispatchers.IO) {
        try {
            val currentUser = db.userDao().getCurrentUser()
            if (currentUser == null) return@withContext emptyList()

            val token = "Bearer ${currentUser.accessToken?.trim()}"
            val response = api.getUserDiaries(token, currentUser._id ?: "")

            if (response.isSuccessful) {
                return@withContext response.body() ?: emptyList()
            } else {
                return@withContext emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext emptyList()
        }
    }

   suspend fun deleteDiary(diaryId: String): String = withContext(Dispatchers.IO){
       val currentUser = db.userDao().getCurrentUser()
         if (currentUser == null) return@withContext "User not found"
        val token = "Bearer ${currentUser.accessToken?.trim()}"
        val response = api.deleteDiary(token,diaryId)
        if (response.isSuccessful) {
            return@withContext response.body()?.message ?: "Success"
        } else {
            val error = response.errorBody()?.string()
            return@withContext "Error: ${error ?: "Unknown error"}"
        }
   }

}


