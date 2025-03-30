package com.example.ease.ui.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.example.ease.R
import com.example.ease.model.User
import com.example.ease.model.local.AppDatabase
import com.example.ease.model.local.UserEntity
import com.example.ease.ui.auth.editProfileFragmentDirections
import com.example.ease.ui.auth.myProfileFragmentDirections
import com.example.ease.ui.posts.FeedFragmentDirections
import com.example.ease.ui.posts.MyPostsFragmentDirections
import com.example.ease.ui.rest_apis.articlesFragmentDirections
import com.example.ease.viewmodel.UserViewModel
import kotlinx.coroutines.launch
import com.example.ease.ui.rest_apis.MapFragment
import com.example.ease.ui.rest_apis.MapFragmentDirections

class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavController
    private lateinit var progressBar: ProgressBar
    private var profileName: String = ""
    private var userEmail: String = ""
    private val userViewModel: UserViewModel by viewModels()




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
            onBackPressedDispatcher.addCallback(
                this,
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        val currentDestinationId = navController.currentDestination?.id
                        if (currentDestinationId == R.id.feedFragment) {
                            // If we're already at feedFragment, close the app
                            finish()
                        } else {
                            // Otherwise navigate to feedFragment and clear intermediate backstack
                            navController.navigate(
                                R.id.feedFragment,
                                null,
                                navOptions {
                                    popUpTo(R.id.feedFragment) {
                                        inclusive = false // Keep feedFragment in the stack
                                    }
                                    launchSingleTop = true
                                }
                            )
                        }
                    }
                }
            )


//        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
//            override fun handleOnBackPressed() {
//                    val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
//
//                    if (currentFragment !is FeedFragment) {
//                        homePageButtonClicked()
//                    } else {
//                        finishAffinity()
//                    }
//            }
//        })

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        navController = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)?.findNavController()
            ?: throw IllegalStateException("NavController not found")

        // Set up bottom navigation (if needed)
        // HOME (Feed)
        findViewById<ImageView>(R.id.home_icon).setOnClickListener {
            // If we are already at Feed, do nothing or re-navigate with pop if you want.
            if (navController.currentDestination?.id != R.id.feedFragment) {
                navController.navigate(
                    R.id.feedFragment,
                    null,
                    navOptions {
                        // Pop everything up to feedFragment so it's the only one on the stack
                        popUpTo(R.id.feedFragment) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                )
            }
        }

// ARTICLES
        findViewById<ImageView>(R.id.articles_icon).setOnClickListener {
            if (navController.currentDestination?.id != R.id.articlesFragment) {
                navController.navigate(
                    R.id.articlesFragment,
                    null,
                    navOptions {
                        popUpTo(R.id.feedFragment) {
                            inclusive = false
                        }
                        launchSingleTop = true
                    }
                )
            }
        }

// MY POSTS
        findViewById<ImageView>(R.id.my_posts_icon).setOnClickListener {
            if (navController.currentDestination?.id != R.id.myPostsFragment) {
                navController.navigate(
                    R.id.myPostsFragment,
                    null,
                    navOptions {
                        popUpTo(R.id.feedFragment) {
                            inclusive = false
                        }
                        launchSingleTop = true
                    }
                )
            }
        }

// ADD POST
        findViewById<ImageView>(R.id.add_icon).setOnClickListener {
            val currentDestination = navController.currentDestination?.id
            val action = when (currentDestination) {
                R.id.feedFragment -> FeedFragmentDirections.actionFeedFragmentToAddPostFragment(null, false)
                R.id.myPostsFragment -> MyPostsFragmentDirections.actionMyPostsFragmentToAddPostFragment(null, false)
                R.id.myProfileFragment -> myProfileFragmentDirections.actionMyProfileFragmentToAddPostFragment(null, false)
                R.id.articlesFragment -> articlesFragmentDirections.actionArticlesFragmentToAddPostFragment(null, false)
                R.id.editProfileFragment -> editProfileFragmentDirections.actionEditProfileFragmentToAddPostFragment(null, false)
                R.id.mapFragment -> MapFragmentDirections.actionMapFragmentToAddPostFragment(null, false)
                else -> return@setOnClickListener
            }
            navController.navigate(action, navOptions {
                popUpTo(R.id.feedFragment) {
                    inclusive = false
                }
                launchSingleTop = true
            })
        }


        findViewById<ImageView>(R.id.map_icon).setOnClickListener {
            if (navController.currentDestination?.id != R.id.mapFragment) {
                navController.navigate(
                    R.id.mapFragment,
                    null,
                    navOptions {
                        popUpTo(R.id.feedFragment) {
                            inclusive = false
                        }
                        launchSingleTop = true
                    }
                )
            }
        }

// PROFILE
        findViewById<ImageView>(R.id.profile_icon).setOnClickListener {
            if (navController.currentDestination?.id != R.id.myProfileFragment) {
                navController.navigate(
                    R.id.myProfileFragment,
                    null,
                    navOptions {
                        popUpTo(R.id.feedFragment) {
                            inclusive = false
                        }
                        launchSingleTop = true
                    }
                )
            }
        }



        //progressBar = findViewById(R.id.progressBar)
        //progressBar.findViewById<ProgressBar>(R.id.progressBar)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // הגדרת הפרגמנט הראשוני

        /*  if (savedInstanceState == null) {
              replaceFragment(FeedFragment())
          }
          */

        //navController.navigate(R.id.feedFragment)

//        val addPostButton = findViewById<ImageView>(R.id.add_icon)
//        addPostButton.setOnClickListener {
//            addPostButtonClicked(false, null) // Ensure AddPostFragment exists
//            //addtobackstack
//
//        }
//
//
//        val homePageButton = findViewById<ImageView>(R.id.home_icon)
//        homePageButton.setOnClickListener {
//            homePageButtonClicked()
//        }
//
//        val profilePageButton=findViewById<ImageView>(R.id.profile_icon)
//        profilePageButton.setOnClickListener{
//            myProfilePageButtonClicked()
//        }
//        val myPostsbutton=findViewById<ImageView>(R.id.my_posts_icon)
//        myPostsbutton.setOnClickListener{
//            MyPostsButtonClicked()
//        }


        userViewModel.fetchUser()

        userViewModel.user.observe(this) { user ->
            if (user != null) {
                val name = user["name"].toString()
                val email = user["email"].toString()
                val image = user["image"] as? String

                lifecycleScope.launch {
                    val userDao = AppDatabase.getInstance(applicationContext).userDao()
                    userDao.clear() // optional
                    userDao.insert(UserEntity(email = email, name = name, profileImageUrl = image))
                }
            }
        }
//        val articlesButton=findViewById<ImageView>(R.id.articles_icon)
//        articlesButton.setOnClickListener{
//            articlesButtonClicked()
//        }

    }



    fun MyPostsButtonClicked() {
        navController.navigate(R.id.myPostsFragment)
    }
    suspend fun getCachedUser(): UserEntity? {
        return AppDatabase.getInstance(applicationContext).userDao().getCurrentUser()
    }
    fun getUserName(callback: (String?) -> Unit) {
        lifecycleScope.launch {
            val user = getCachedUser()
            callback(user?.name)
        }
    }

    fun getUserEmail(callback: (String?) -> Unit) {
        lifecycleScope.launch {
            val user = getCachedUser()
            callback(user?.email)
        }
    }
    //    fun getUserName(): String {
//        return profileName;
//    }
//    fun getUserEmail(): String {
//        return userEmail;
//    }
    fun refreshProfile() {
        userViewModel.fetchUser()

        userViewModel.user.observe(this) { user ->
            if (user != null) {
                val name = user["name"].toString()
                val email = user["email"].toString()
                val image = user["image"] as? String

                lifecycleScope.launch {
                    val userDao = AppDatabase.getInstance(applicationContext).userDao()
                    userDao.clear() // Optional
                    userDao.insert(UserEntity(email = email, name = name, profileImageUrl = image))
                }
            }
        }
        /*
        val userServer = User.shared
        userServer.getUser { user ->
            if (user != null) {
                val name = user["name"].toString()
                val email = user["email"].toString()
                val image= user["image"] as? String

                // Save to Room
                lifecycleScope.launch {
                    val userDao = AppDatabase.getInstance(applicationContext).userDao()
                    userDao.clear() // Optional: clear old user
                    userDao.insert(UserEntity(email = email, name = name, profileImageUrl = image))
                }
            }
        }*/
    }
    fun articlesButtonClicked(){
        navController.navigate(R.id.articlesFragment)
    }

// CHECK


    fun openInBrowser(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }
    fun homePageButtonClicked() {

        navController.navigate(R.id.feedFragment)

    }
    fun myProfilePageButtonClicked() {

        navController.navigate(R.id.myProfileFragment)



    }

    fun editProfileButtonClicked(){
        navController.navigate(R.id.editProfileFragment)

    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container, fragment)
        fragmentTransaction.commit()
    }

    fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
    fun editPost(postId: String?) {
        val action = MyPostsFragmentDirections.actionMyPostsFragmentToAddPostFragment(isEdit = true, postId = postId)
        navController.navigate(action)
    }
    fun navigateToLogin() {
        val intent = Intent(this, LoginRegisterActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }





}