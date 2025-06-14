package com.example.ease.base

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

class MyApplication: Application() {

    object Globals {
        var context: Context? = null
    }

    override fun onCreate() {
        super.onCreate()
        Globals.context = applicationContext
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }
}