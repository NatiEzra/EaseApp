package com.example.ease.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.ease.R
import com.example.ease.model.local.AppDatabase
import com.example.ease.model.local.UserEntity
import com.example.ease.viewmodel.AuthViewModel
import com.example.easeapp.model.RetrofitProvider.RetrofitProvider
import com.example.easeapp.model.SocketManager
import com.example.easeapp.model.requests.NotificationApi
import com.example.easeapp.repositories.NotificationRepository
import com.example.easeapp.viewmodel.NotificationsViewModel
import com.google.android.material.navigation.NavigationView
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.CropCircleTransformation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    val autViewModel: AuthViewModel by lazy { ViewModelProvider(this)[AuthViewModel::class.java] }

    private lateinit var navController: NavController
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var notificationCountTextView: TextView
    private lateinit var notificationRepository: NotificationRepository
    private lateinit var notificationsViewModel: NotificationsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.drawer_layout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val retrofit = RetrofitProvider.provideRetrofit(this)
        val api = retrofit.create(NotificationApi::class.java)
        notificationRepository = NotificationRepository(api)

        notificationsViewModel = ViewModelProvider(
            this,
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(NotificationsViewModel::class.java)) {
                        @Suppress("UNCHECKED_CAST")
                        return NotificationsViewModel(notificationRepository) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
        )[NotificationsViewModel::class.java]

        navController = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)?.findNavController()
            ?: throw IllegalStateException("NavController not found")

        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open_drawer, R.string.close_drawer)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        findViewById<ImageView>(R.id.menu_icon).setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.END)
        }

        val headerView = navView.getHeaderView(0)
        val badgeContainer = headerView.findViewById<FrameLayout>(R.id.notification_badge_container)
        badgeContainer.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            navController.navigate(R.id.notificationsFragment)
        }

        val tvCount = headerView.findViewById<TextView>(R.id.notification_count)
        notificationCountTextView = tvCount

        notificationsViewModel.unreadCount.observe(this) {
            forceRedrawNotificationBadge()
        }

        notificationsViewModel.loadInitialUnreadCount()

        findViewById<ImageView>(R.id.back_icon).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.homePageFragment, R.id.loginFragment, R.id.registerFragment ->
                    findViewById<ImageView>(R.id.back_icon).visibility = View.GONE
                else -> findViewById<ImageView>(R.id.back_icon).visibility = View.VISIBLE
            }
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        navView.setNavigationItemSelectedListener(this)

        // 📌 חיוני: קודם מגדירים את ה־callback לפני שה־socket מתחבר
        SocketManager.setOnNewNotificationCallback {
            Log.d("SocketUpdate", "🔥 Notification received via socket!")
            runOnUiThread {
                notificationsViewModel.refreshUnreadCountFromServer()
            }
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val userEntity = AppDatabase.getInstance(this@MainActivity).userDao().getCurrentUserBlocking()
            userEntity?.let {
                withContext(Dispatchers.Main) {
                    SocketManager.init(it._id, this@MainActivity)
                }
            }
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (navController.currentDestination?.id != R.id.homePageFragment) {
                    navController.navigateUp()
                } else {
                    finish()
                }
            }
        })

        refreshProfile(this)
    }

    private fun forceRedrawNotificationBadge() {
        val count = notificationsViewModel.unreadCount.value ?: 0
        if (count > 0) {
            notificationCountTextView.text = count.toString()
            notificationCountTextView.visibility = View.VISIBLE
        } else {
            notificationCountTextView.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        updateUnreadBadge()
    }

    fun updateUnreadBadge() {
        Log.d("BadgeDebug", "Fetching unread count...")
        lifecycleScope.launch {
            try {
                val all = notificationRepository.fetchAll()
                val unreadCount = all.count { !it.isRead }
                Log.d("BadgeDebug", "Unread count = $unreadCount")
                notificationsViewModel.setCount(unreadCount)
            } catch (e: Exception) {
                Log.e("BadgeDebug", "Failed to fetch notifications", e)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (toggle.onOptionsItemSelected(item)) true
        else super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> navController.navigate(R.id.homePageFragment)
            R.id.nav_profile -> navController.navigate(R.id.myProfileFragment)
            R.id.nav_privacy -> navController.navigate(R.id.privacyFragment)
            R.id.nav_logout -> {
                drawerLayout.closeDrawers()
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        AppDatabase.getInstance(this@MainActivity).userDao().clear()
                    }
                    SocketManager.disconnect()
                    autViewModel.signOut(this@MainActivity)
                    Toast.makeText(this@MainActivity, "You logged out, have a great day!", Toast.LENGTH_LONG).show()
                    navigateToLogin()
                }
            }
        }
        drawerLayout.closeDrawers()
        return true
    }

    fun editProfileButtonClicked() {
        navController.navigate(R.id.editProfileFragment)
    }

    fun myProfilePageButtonClicked() {
        navController.navigate(R.id.myProfileFragment)
    }

    fun diaryButtonClicked() {
        navController.navigate(R.id.diaryFragment)
    }

    fun navigateToLogin() {
        val intent = Intent(this, LoginRegisterActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    fun refreshProfile(context: Context) {
        val headerView = navView.getHeaderView(0)
        val headerProfileImage = headerView.findViewById<ImageView>(R.id.header_profile_image)
        val headerUserName = headerView.findViewById<TextView>(R.id.header_user_name)
        val headerUserEmail = headerView.findViewById<TextView>(R.id.header_user_email)

        lifecycleScope.launch {
            val user: UserEntity? = AppDatabase.getInstance(this@MainActivity).userDao().getCurrentUser()
            if (user != null) {
                SocketManager.init(user._id, context)

                headerUserName.text = user.name
                headerUserEmail.text = user.email

                if (!user.profileImageUrl.isNullOrEmpty()) {
                    val rawUrl = user.profileImageUrl
                    val fixedUrl = rawUrl.replace("localhost", "10.0.2.2") + "?t=" + System.currentTimeMillis()

                    Log.d("PROFILE_IMAGE", "Trying to load: $fixedUrl")

                    Picasso.get().invalidate(fixedUrl)
                    Picasso.get()
                        .load(fixedUrl)
                        .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE)
                        .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                        .transform(CropCircleTransformation())
                        .placeholder(R.drawable.ic_placeholder)

                        .into(headerProfileImage)
                } else {
                    headerProfileImage.setImageResource(R.drawable.ic_placeholder)
                }
            }
        }
    }

    fun fixImageUrl(oldUrl: String): String {
        return oldUrl.replace("http://localhost:", "http://10.0.2.2:")
    }
}
