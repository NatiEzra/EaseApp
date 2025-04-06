package com.example.ease.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.ease.R
import com.example.ease.model.local.AppDatabase
import com.example.ease.model.local.UserEntity
import com.example.ease.viewmodel.UserViewModel
import com.google.android.material.navigation.NavigationView
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import kotlinx.coroutines.launch
import jp.wasabeef.picasso.transformations.CropCircleTransformation

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var navController: NavController
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.drawer_layout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        navController =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment)?.findNavController()
                ?: throw IllegalStateException("NavController not found")

        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        toggle =
            ActionBarDrawerToggle(this, drawerLayout, R.string.open_drawer, R.string.close_drawer)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        findViewById<ImageView>(R.id.menu_icon).setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.END)
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        navView.setNavigationItemSelectedListener(this)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (navController.currentDestination?.id != R.id.homePageFragment) {
                    navController.navigate(R.id.homePageFragment)
                } else {
                    finish()
                }
            }
        })

        refreshProfile()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (toggle.onOptionsItemSelected(item)) true
        else super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> navController.navigate(R.id.homePageFragment)
            R.id.nav_profile -> navController.navigate(R.id.myProfileFragment)
            R.id.nav_privacy -> {
                // TODO: navController.navigate(R.id.privacyFragment)
            }

            R.id.nav_about -> {
                // TODO: navController.navigate(R.id.aboutFragment)
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

    fun navigateToLogin() {
        val intent = Intent(this, LoginRegisterActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    fun refreshProfile() {
        val headerView = navView.getHeaderView(0)
        val headerProfileImage = headerView.findViewById<ImageView>(R.id.header_profile_image)
        val headerUserName = headerView.findViewById<TextView>(R.id.header_user_name)
        val headerUserEmail = headerView.findViewById<TextView>(R.id.header_user_email)

        lifecycleScope.launch {
            val user: UserEntity? = AppDatabase.getInstance(this@MainActivity).userDao().getCurrentUser()
            if (user != null) {
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