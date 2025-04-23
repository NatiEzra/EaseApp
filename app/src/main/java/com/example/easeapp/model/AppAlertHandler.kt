package com.example.easeapp.model
import android.app.AlertDialog
import android.content.Context
import android.os.Handler
import android.os.Looper

object AppAlertHandler {
    private var currentDialog: AlertDialog? = null

    fun showGlobalDialog(context: Context, message: String) {
        // get the main thread’s Handler
        val mainHandler = Handler(Looper.getMainLooper())
        mainHandler.post {
            // now we are safely on the UI thread
            currentDialog?.dismiss()
            val dialog = AlertDialog.Builder(context)
                .setTitle("Notice")
                .setMessage(message)
                .setCancelable(true)
                .setPositiveButton("OK", null)
                .create()
            dialog.show()
            currentDialog = dialog
        }
    }
}
