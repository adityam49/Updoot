package com.ducktapedapps.updoot.ui

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.MergeAdapter
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.UpdootApplication
import com.ducktapedapps.updoot.backgroundWork.cacheCleanUp.enqueueCleanUpWork
import com.ducktapedapps.updoot.databinding.ActivityMainBinding
import com.ducktapedapps.updoot.ui.navDrawer.ScrimVisibilityAdjuster
import com.ducktapedapps.updoot.ui.navDrawer.ToolbarMenuSwapper
import com.ducktapedapps.updoot.ui.navDrawer.accounts.AccountsAdapter
import com.ducktapedapps.updoot.utils.accountManagement.RedditClient
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject


class MainActivity : AppCompatActivity(), RedditClient.AccountChangeListener, NavController.OnDestinationChangedListener {
    @Inject
    lateinit var redditClient: RedditClient

    @Inject
    lateinit var activityVMFactory: ActivityVMFactory
    private val viewModel by lazy { ViewModelProvider(this@MainActivity, activityVMFactory).get(ActivityVM::class.java) }

    private lateinit var binding: ActivityMainBinding
    private val navController by lazy { findNavController(R.id.nav_host_fragment) }
    private val accountsAdapter = AccountsAdapter(object : AccountsAdapter.AccountAction {

        override fun login() = navController.navigate(R.id.loginActivity)

        override fun switch(accountName: String){
            viewModel.setCurrentAccount(accountName)
            if(bottomNavigationDrawer.isInFocus()) bottomNavigationDrawer.hide()
        }

        override fun logout(accountName: String) = viewModel.logout(accountName)

        override fun toggleEntryMenu() = viewModel.expandOrCollapseAccountsMenu()
    })

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.item_settings -> {
                navController.navigate(R.id.SettingsDestination)
                true
            }
            else -> false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as UpdootApplication).updootComponent.inject(this)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUpViews()

        setUpViewModel()

        redditClient.attachListener(this)

        setUpStatusBarColors()
    }

    private fun setUpViewModel() {
        viewModel.accounts.observe(this) {
            accountsAdapter.submitList(it)
        }
    }

    private fun setUpStatusBarColors() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            window.statusBarColor = Color.BLACK
        }
    }

    override fun onBackPressed() {
        with(binding.bottomNavigationDrawer) {
            if (isInFocus()) hide()
            else super.onBackPressed()
        }
    }

    private fun setUpViews() {
        binding.apply {
            navController.addOnDestinationChangedListener(this@MainActivity)

            scrimView.setOnClickListener { bottomNavigationDrawer.toggleState() }

            bottomNavigationDrawer.apply {

                addOnSlideAction(ScrimVisibilityAdjuster(scrimView))

                addOnStateChangeAction(ToolbarMenuSwapper(binding.toolbar, ::getCurrentDestinationMenu))

                binding.toolbar.apply {
                    setSupportActionBar(this)
                    setupWithNavController(navController)
                    setOnClickListener { bottomNavigationDrawer.toggleState() }
                }

                binding.recyclerView.apply {
                    adapter = MergeAdapter(accountsAdapter)
                    layoutManager = LinearLayoutManager(this@MainActivity)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        setUpWorkers()
        redditClient.detachListener()
    }

    override fun currentAccountChanged() = viewModel.reloadContent()

    override fun onDestinationChanged(controller: NavController, destination: NavDestination, arguments: Bundle?) {
        navController.currentDestination?.label = when (destination.id) {
            R.id.SubredditDestination -> arguments?.getString("subreddit").run { if (isNullOrEmpty()) "Updoot" else this }
            R.id.CommentsDestination -> "Comments"
            R.id.SettingsDestination -> "Settings"
            R.id.ExploreDestination -> "Explore"
            else -> "Updoot"
        }
        bottomNavigationDrawer.post { /*to let behaviour be initialized*/
            if (bottomNavigationDrawer.isInFocus()) bottomNavigationDrawer.hide()
        }
    }

    private fun setUpWorkers() = enqueueCleanUpWork(this)

    private fun getCurrentDestinationMenu(): Int? =
            when (navController.currentDestination?.id) {
                R.id.SubredditDestination -> R.menu.subreddit_screen_menu
                R.id.CommentsDestination -> R.menu.comment_screen_menu
                else -> null
            }
}