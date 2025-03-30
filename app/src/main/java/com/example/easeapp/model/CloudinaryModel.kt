package com.example.ease.model
import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.cloudinary.android.BuildConfig
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.cloudinary.android.policy.GlobalUploadPolicy
import com.example.ease.base.MyApplication
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Credentials
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.IOException
import java.io.File
import java.io.FileOutputStream


class CloudinaryModel {
    companion object {
        private var isInitialized = false
    }

    init{
        val config= mapOf(
            "cloud_name" to com.example.ease.BuildConfig.CLOUD_NAME,
            "api_key" to com.example.ease.BuildConfig.API_KEY,
            "api_secret" to com.example.ease.BuildConfig.API_SECRET
        )
        MyApplication.Globals.context?.let {
            if (!isInitialized) {
                MediaManager.init(it, config)
                MediaManager.get().globalUploadPolicy = GlobalUploadPolicy.defaultPolicy()
                isInitialized = true
            }
        }
    }
    fun uploadImage(bitmap: Bitmap, name : String, onSuccess: (String?) -> Unit, onError: (String?) -> Unit){
        //val context= MyApplication.Globals.context ?:return
        val context = MyApplication.Globals.context
        if (context == null){
            return
        }
        val file :File= bitmap.toFile( context,name )
        MediaManager.get().upload(file.path)
            .option("folder", "images")
            .callback(object: UploadCallback{

                override fun onStart(requestId: String?) {

                }

                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {

                }

                override fun onSuccess(requestId: String?, resultData: Map<*, *>) {
                    val url=resultData["secure_url"] as? String?: ""
                    onSuccess(url)
                }

                override fun onError(requestId: String?, error: ErrorInfo?) {
                    onError(error?.description?: "Unknown error")
                }

                override fun onReschedule(requestId: String?, error: ErrorInfo?) {

                }
            })
            .dispatch()
    }
  fun Bitmap.toFile (context: Context, name: String) : File{
        val file= File(context.cacheDir, "image_$name.jpg")
        FileOutputStream(file).use{ stream ->
            Log.d("Cloudinary", "uploadImage5 ofek ")
            this.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        }
        return file
    }

    fun deleteImage(previousImageUrl: String, onComplete: (Boolean, String?) -> Unit) {
        val publicId = extractPublicIdFromUrl(previousImageUrl)

        Thread {
            try {
                val result = MediaManager.get()
                    .cloudinary
                    .uploader()
                    .destroy(publicId, emptyMap<String, Any>())

                Handler(Looper.getMainLooper()).post {
                    if (result["result"] == "ok") {
                        onComplete(true, null)
                    } else {
                        onComplete(false, result["result"] as? String)
                    }
                }
            } catch (e: Exception) {
                Handler(Looper.getMainLooper()).post {
                    onComplete(false, e.message)
                }
            }
        }.start()
    }


    fun extractPublicIdFromUrl(url: String): String {
        val afterUpload = url.substringAfter("upload/")

        val afterVersion = afterUpload.substringAfter("/")

        return afterVersion.substringBeforeLast(".")
    }


}