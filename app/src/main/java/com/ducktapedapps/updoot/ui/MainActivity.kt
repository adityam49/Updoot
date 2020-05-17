package com.ducktapedapps.updoot.ui

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.UpdootApplication
import com.ducktapedapps.updoot.databinding.ActivityMainBinding
import com.ducktapedapps.updoot.model.LinkData
import com.ducktapedapps.updoot.utils.Constants
import com.ducktapedapps.updoot.utils.accountManagement.UserManager
import com.ducktapedapps.updoot.utils.accountManagement.UserManager.AccountChangeListener
import com.ducktapedapps.updoot.utils.getCompactCountAsString
import javax.inject.Inject

class MainActivity : AppCompatActivity(), AccountChangeListener {
    private val viewModel: ActivityVM by lazy { ViewModelProvider(this).get(ActivityVM::class.java) }
    private lateinit var binding: ActivityMainBinding
    private val navController by lazy { findNavController(R.id.nav_host_fragment) }

    @Inject
    lateinit var userManager: UserManager

    private val bottomNavDrawerFragment: NavDrawerFragment by lazy {
        supportFragmentManager.findFragmentById(R.id.bottom_nav_drawer) as NavDrawerFragment
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setUpViews()
        setUpStatusBarColors()
        (application as UpdootApplication).updootComponent.inject(this)
        userManager.attachListener(this)
    }

    private fun setUpStatusBarColors() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            window.statusBarColor = Color.BLACK
        }
    }

    private fun setUpViews() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        binding.apply {
            bottomAppBar.apply {
                setSupportActionBar(this)
                setupWithNavController(navController)
                setOnClickListener { bottomNavDrawerFragment.toggleState() }
            }
        }
        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            when (destination.id) {
                R.id.SubredditDestination -> {
                    binding.textToolbarTitle.text = arguments?.getString("r/subreddit")
                            ?: "Frontpage"
                }
                R.id.CommentsDestination -> {
                    binding.textToolbarTitle.text = String.format(
                            "%s comments",
                            getCompactCountAsString(arguments?.getParcelable<LinkData>("SubmissionData")?.commentsCount?.toLong()
                                    ?: 0L)
                    )
                }
                else -> Unit
            }
        }
    }

    override fun onCurrentAccountRemoved() = reloadContent()


    override fun onDestroy() {
        super.onDestroy()
        userManager.detachListener()
    }

    private fun reloadContent() = viewModel.setCurrentAccount(userManager.currentUser?.name)

    //Account switching after new login
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.ACCOUNT_LOGIN_REQUEST_CODE && resultCode == Activity.RESULT_OK) reloadContent()
    }
}