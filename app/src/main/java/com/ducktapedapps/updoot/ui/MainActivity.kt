package com.ducktapedapps.updoot.ui

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.UpdootApplication
import com.ducktapedapps.updoot.databinding.ActivityMainBinding
import com.ducktapedapps.updoot.model.LinkData
import com.ducktapedapps.updoot.ui.navDrawer.BottomNavDrawerFragment
import com.ducktapedapps.updoot.ui.navDrawer.OnStateChangeAction
import com.ducktapedapps.updoot.utils.Constants
import com.ducktapedapps.updoot.utils.accountManagement.UserManager
import com.ducktapedapps.updoot.utils.accountManagement.UserManager.AccountChangeListener
import com.ducktapedapps.updoot.utils.getCompactCountAsString
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_HIDDEN
import javax.inject.Inject

class MainActivity : AppCompatActivity(), AccountChangeListener {
    private val viewModel: ActivityVM by lazy { ViewModelProvider(this).get(ActivityVM::class.java) }
    private lateinit var binding: ActivityMainBinding
    private val navController by lazy { findNavController(R.id.nav_host_fragment) }

    @Inject
    lateinit var userManager: UserManager

    private val bottomNavDrawerFragment: BottomNavDrawerFragment by lazy {
        supportFragmentManager.findFragmentById(R.id.bottom_nav_drawer) as BottomNavDrawerFragment
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.item_settings -> {
                navController.navigate(R.id.SettingsDestination)
                bottomNavDrawerFragment.toggleState()
                true
            }
            else -> false
        }
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
            bottomNavDrawerFragment.addOnStateChangeAction(object : OnStateChangeAction {
                override fun onStateChange(newState: Int) {
                    when (newState) {
                        STATE_HIDDEN -> {
                            when (navController.currentDestination?.id) {
                                R.id.SubredditDestination -> bottomAppBar.replaceMenu(R.menu.subreddit_screen_menu)
                                R.id.CommentsDestination -> bottomAppBar.replaceMenu(R.menu.comment_screen_menu)
                                else -> bottomAppBar.menu.clear()
                            }
                            fab.show()
                        }
                        else -> {
                            if (navController.currentDestination?.id != R.id.SettingsDestination)
                                bottomAppBar.replaceMenu(R.menu.bottom_navigation_menu)
                            else bottomAppBar.menu.clear()
                            fab.hide()
                        }
                    }
                }
            })
        }
        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            bottomNavDrawerFragment.hide()
            when (destination.id) {
                R.id.SubredditDestination -> {
                    binding.textToolbarTitle.text = arguments?.getString("r/subreddit")
                            ?: "Frontpage"
                }
                R.id.CommentsDestination -> {
                    binding.textToolbarTitle.text = String.format(
                            "%s comments",
                            getCompactCountAsString(arguments?.getParcelable<LinkData>("SubmissionData")?.commentsCount?.toLong()
                                    ?: 0L))
                }
                R.id.SettingsDestination -> {
                    binding.apply {
                        textToolbarTitle.text = getString(R.string.settings)
                        bottomAppBar.menu.clear()
                    }
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