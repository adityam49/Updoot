package com.ducktapedapps.updoot.ui

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.UpdootApplication
import com.ducktapedapps.updoot.backgroundWork.cacheCleanUp.enqueueCleanUpWork
import com.ducktapedapps.updoot.databinding.ActivityMainBinding
import com.ducktapedapps.updoot.ui.navDrawer.BottomNavDrawerFragment
import com.ducktapedapps.updoot.ui.navDrawer.OnStateChangeAction
import com.ducktapedapps.updoot.utils.accountManagement.RedditClient
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_HIDDEN
import javax.inject.Inject

class MainActivity : AppCompatActivity(), RedditClient.AccountChangeListener, NavController.OnDestinationChangedListener {
    @Inject
    lateinit var redditClient: RedditClient

    @Inject
    lateinit var activityVMFactory: ActivityVMFactory
    private val viewModel by lazy { ViewModelProvider(this@MainActivity, activityVMFactory).get(ActivityVM::class.java) }

    private lateinit var binding: ActivityMainBinding
    private val navController by lazy { findNavController(R.id.nav_host_fragment) }
    private val bottomNavDrawer: BottomNavDrawerFragment by lazy {
        (supportFragmentManager.findFragmentById(R.id.bottom_nav_drawer) as BottomNavDrawerFragment).apply {
            addOnStateChangeAction(object : OnStateChangeAction {
                override fun onStateChange(newState: Int) {
                    binding.apply {
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
                }
            })
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.item_settings -> {
                navController.navigate(R.id.SettingsDestination)
                bottomNavDrawer.toggleState()
                true
            }
            else -> false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as UpdootApplication).updootComponent.inject(this)
        super.onCreate(savedInstanceState)
        redditClient.attachListener(this)
        setUpViews()
        setUpStatusBarColors()
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
                setOnClickListener { this@MainActivity.bottomNavDrawer.toggleState() }
            }
        }.run { findNavController(R.id.nav_host_fragment).addOnDestinationChangedListener(this@MainActivity) }
    }

    override fun onDestroy() {
        super.onDestroy()
        setUpWorkers()
        redditClient.detachListener()
    }

    override fun currentAccountChanged() = viewModel.reloadContent()

    override fun onDestinationChanged(controller: NavController, destination: NavDestination, arguments: Bundle?) {
        bottomNavDrawer.hide()
        when (destination.id) {
            R.id.SubredditDestination -> {
                setSubredditTitle(arguments)
                showPeripheralElements()
            }

            R.id.CommentsDestination -> {
                setCommentsTitle()
                showPeripheralElements()
            }

            R.id.SettingsDestination -> {
                showSettingsTitle()
                hidePeripheralElements()
            }

            R.id.ExploreDestination -> hidePeripheralElements()

            R.id.submissionOptionsBottomSheet -> Unit

            else -> hidePeripheralElements()
        }
    }

    private fun showSettingsTitle() {
        binding.textToolbarTitle.text = getString(R.string.settings)
    }

    private fun setCommentsTitle() = binding.apply {
        textToolbarTitle.text = "0 comments"
    }

    private fun setSubredditTitle(arg: Bundle?) = binding.apply {
        textToolbarTitle.text = arg?.getString("subreddit")
                ?: "Frontpage"
    }

    private fun hidePeripheralElements() {
        binding.apply {
            topAppBar.setExpanded(false)
            bottomAppBar.performHide()
            fab.apply {

                hide()
                visibility = View.GONE
            }
        }
    }

    private fun showPeripheralElements() {
        binding.apply {
            topAppBar.setExpanded(true)
            bottomAppBar.performShow()
            fab.apply {
                show()
                visibility = View.VISIBLE
            }
        }
    }

    private fun setUpWorkers() {
        enqueueCleanUpWork(this)
    }
}