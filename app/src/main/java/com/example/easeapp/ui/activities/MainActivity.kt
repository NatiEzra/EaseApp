package com.example.ease.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.ease.R
import com.example.ease.model.local.AppDatabase
import com.example.ease.model.local.UserEntity
import com.example.ease.viewmodel.UserViewModel
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope
import android.view.View
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private val userViewModel: UserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }

        navController = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)?.findNavController()
            ?: throw IllegalStateException("NavController not found")

        // כפתור מעבר לפרופיל (אם קיים)
//        findViewById<ImageView>(R.id.profile_icon).setOnClickListener {
//            if (navController.currentDestination?.id != R.id.myProfileFragment) {
//                navController.navigate(R.id.myProfileFragment)
//            }
//        }

        // back button → חוזר למסך פרופיל
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (navController.currentDestination?.id != R.id.myProfileFragment) {
                    navController.navigate(R.id.myProfileFragment)
                } else {
                    finish()
                }
            }
        })

        // טוען משתמש מקאש
        userViewModel.fetchUser()
        userViewModel.user.observe(this) { user ->
            if (user != null) {
                val name = user["name"].toString()
                val email = user["email"].toString()
                val image = user["image"] as? String
                lifecycleScope.launch {
                    val userDao = AppDatabase.getInstance(applicationContext).userDao()
                    userDao.clear()
                    userDao.insert(UserEntity(email = email, name = name, profileImageUrl = image))
                }
            }
        }
    }

    fun editProfileButtonClicked() {
        navController.navigate(R.id.editProfileFragment)
    }

    fun myProfilePageButtonClicked() {
        navController.navigate(R.id.myProfileFragment)
    }

    fun navigateToLogin() {
        val intent = Intent(this, LoginRegisterActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    fun refreshProfile() {
        userViewModel.fetchUser()
        userViewModel.user.observe(this) { user ->
            if (user != null) {
                val name = user["name"].toString()
                val email = user["email"].toString()
                val image = user["image"] as? String
                lifecycleScope.launch {
                    val userDao = AppDatabase.getInstance(applicationContext).userDao()
                    userDao.clear()
                    userDao.insert(UserEntity(email = email, name = name, profileImageUrl = image))
                }
            }
        }
    }
}
