package com.example.ease.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ease.viewmodel.AuthViewModel

import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
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
import com.example.ease.base.MyApplication.Globals.context
import com.example.ease.model.local.AppDatabase
import com.example.ease.model.local.UserEntity
import com.example.ease.viewmodel.UserViewModel
import com.example.easeapp.model.RetrofitProvider.RetrofitProvider
import com.example.easeapp.model.SocketManager
import com.example.easeapp.model.requests.NotificationApi
import com.example.easeapp.repositories.NotificationRepository
import com.example.easeapp.viewmodel.NotificationsViewModel
import com.google.android.material.navigation.NavigationView
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import kotlinx.coroutines.launch
import jp.wasabeef.picasso.transformations.CropCircleTransformation
import kotlinx.coroutines.Dispatchers
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

        navController =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment)?.findNavController()
                ?: throw IllegalStateException("NavController not found")

        // Drawer
        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        toggle =
            ActionBarDrawerToggle(this, drawerLayout, R.string.open_drawer, R.string.close_drawer)
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

        notificationsViewModel.unreadCount.observe(this) { count ->
            if (count > 0) {
                notificationCountTextView.text = count.toString()
                notificationCountTextView.visibility = View.VISIBLE
            } else {
                notificationCountTextView.visibility = View.GONE
            }
        }

        notificationsViewModel.loadInitialUnreadCount()

        val backButton: ImageView = findViewById(R.id.back_icon)
        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.homePageFragment,
                R.id.loginFragment,
                R.id.registerFragment -> backButton.visibility = View.GONE
                else -> backButton.visibility = View.VISIBLE
            }
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        navView.setNavigationItemSelectedListener(this)

        SocketManager.setOnNewNotificationCallback {
            runOnUiThread { notificationsViewModel.incrementBy() }
        }
//roy ze hahelek shemakris
//        val userEntity: UserEntity? = AppDatabase
//            .getInstance(this)
//            .userDao()
//            .getCurrentUserBlocking()
//        userEntity?.let {
//            SocketManager.init(it._id, this)
//        }









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
    override fun onResume() {
        super.onResume()
        updateUnreadBadge()
    }

    fun updateUnreadBadge() {
        lifecycleScope.launch {
            try {
                // 1) Fetch all notifications
                val all = notificationRepository.fetchAll()
                // 2) Count how many are not read
                val unreadCount = all.count { !it.isRead }
                // 3) Show or hide the red circle
                if (unreadCount > 0) {
                    notificationCountTextView.text = unreadCount.toString()
                    notificationCountTextView.visibility = View.VISIBLE
                } else {
                    notificationCountTextView.visibility = View.GONE
                }
            } catch (e: Exception) {
                // If the fetch fails, just hide the badge
                notificationCountTextView.visibility = View.GONE
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
                drawerLayout.closeDrawers()          // close the drawer immediately

                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        AppDatabase.getInstance(this@MainActivity)
                            .userDao()
                            .clear()
                    }

                    SocketManager.disconnect()

                    autViewModel.signOut(this@MainActivity)

                    Toast.makeText(
                        this@MainActivity,
                        "You logged out, have a great day!",
                        Toast.LENGTH_LONG
                    ).show()

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
                SocketManager.init(user._id,context)
                headerUserName.text = user.name
                headerUserEmail.text = user.email
                if (!user.profileImageUrl.isNullOrEmpty()) {
                    val fixedUrl = fixImageUrl(user.profileImageUrl) + "?t=" + System.currentTimeMillis()
                    Picasso.get()
                        .invalidate(fixedUrl)
                    Picasso.get()
                        .load(fixedUrl)
                        .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE)
                        .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                        .transform(CropCircleTransformation())
                        .into(headerProfileImage)
                } else {
                    headerProfileImage.setImageResource(R.drawable.ic_placeholder)
                }
            }
        }
    }
}

    fun fixImageUrl(oldUrl: String): String {
        return oldUrl.replace("http://localhost:", "http://10.0.2.2:")
    }