package com.ducktapedapps.updoot.ui

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.viewpager2.widget.ViewPager2.ORIENTATION_HORIZONTAL
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.UpdootApplication
import com.ducktapedapps.updoot.backgroundWork.cacheCleanUp.enqueueCleanUpWork
import com.ducktapedapps.updoot.databinding.ActivityMainBinding
import com.ducktapedapps.updoot.ui.comments.CommentsFragment
import com.ducktapedapps.updoot.ui.explore.ExploreFragment
import com.ducktapedapps.updoot.ui.login.LoginActivity
import com.ducktapedapps.updoot.ui.navDrawer.NavDrawerPagerAdapter
import com.ducktapedapps.updoot.ui.navDrawer.ScrimVisibilityAdjuster
import com.ducktapedapps.updoot.ui.navDrawer.ToolbarMenuSwapper
import com.ducktapedapps.updoot.ui.navDrawer.accounts.AccountsAdapter
import com.ducktapedapps.updoot.ui.navDrawer.destinations.NavDrawerDestinationAdapter
import com.ducktapedapps.updoot.ui.navDrawer.subscriptions.SubscriptionsAdapter
import com.ducktapedapps.updoot.ui.settings.SettingsFragment
import com.ducktapedapps.updoot.ui.subreddit.SubredditFragment
import com.ducktapedapps.updoot.utils.Constants.FRONTPAGE
import com.ducktapedapps.updoot.utils.accountManagement.IRedditClient
import com.ducktapedapps.updoot.utils.accountManagement.RedditClient
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject


class MainActivity : AppCompatActivity(), IRedditClient.AccountChangeListener {
    @Inject
    lateinit var redditClient: RedditClient

    @Inject
    lateinit var activityVMFactory: ActivityVMFactory
    private val viewModel by lazy { ViewModelProvider(this@MainActivity, activityVMFactory).get(ActivityVM::class.java) }

    private lateinit var binding: ActivityMainBinding
    private val accountsAdapter = AccountsAdapter(object : AccountsAdapter.AccountAction {

        override fun login() = startActivity(Intent(this@MainActivity, LoginActivity::class.java))

        override fun switch(accountName: String) {
            viewModel.setCurrentAccount(accountName)
            if (bottomNavigationDrawer.isInFocus()) bottomNavigationDrawer.collapse()
        }

        override fun logout(accountName: String) = viewModel.logout(accountName)

        override fun toggleEntryMenu() = viewModel.expandOrCollapseAccountsMenu()
    })
    private val subscriptionAdapter = SubscriptionsAdapter(object : SubscriptionsAdapter.ClickHandler {
        override fun goToSubreddit(subredditName: String) {
            supportFragmentManager.beginTransaction().addToBackStack(null).replace(R.id.fragment_container, SubredditFragment.newInstance(subredditName)).commit()
            binding.bottomNavigationDrawer.collapse()
        }
    })
    private val navDrawerDestinationAdapter = NavDrawerDestinationAdapter(object : NavDrawerDestinationAdapter.ClickHandler {
        override fun openExplore() {
            supportFragmentManager.beginTransaction().addToBackStack(null).replace(R.id.fragment_container, ExploreFragment()).commit()
        }
    })

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.item_settings -> {
                openSettings()
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

        setUpFragments()

        observeScreen()

        setUpViews()

        setUpViewModel()

        redditClient.attachListener(this)

        setUpStatusBarColors()
    }

    private fun setUpViewModel() {
        viewModel.apply {
            accounts.observe(this@MainActivity) {
                accountsAdapter.submitList(it)
            }
            navigationEntries.observe(this@MainActivity) {
                navDrawerDestinationAdapter.submitList(it)
            }
            subredditSubscription.observe(this@MainActivity) {
                subscriptionAdapter.submitList(it)
            }
            navDrawerVisibility.observe(this@MainActivity) { visibile ->
                binding.bottomNavigationDrawer.apply {
                    if (visibile) show()
                    else hide()
                }
            }
        }
    }

    private fun setUpStatusBarColors() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            window.statusBarColor = Color.BLACK
        }
    }

    override fun onBackPressed() {
        with(binding.bottomNavigationDrawer) {
            if (isInFocus()) collapse()
            else super.onBackPressed()
        }
    }

    private fun setUpFragments() {
        with(supportFragmentManager) {
            if (findFragmentById(R.id.fragment_container) == null) {
                beginTransaction()
                        .add(R.id.fragment_container, SubredditFragment.newInstance(FRONTPAGE))
                        .commit()
            }
        }
    }

    private fun setUpViews() {
        binding.apply {
            scrimView.setOnClickListener { bottomNavigationDrawer.toggleState() }

            bottomNavigationDrawer.apply {

                addOnSlideAction(ScrimVisibilityAdjuster(scrimView))

                addOnStateChangeAction(ToolbarMenuSwapper(binding.toolbar, ::getCurrentDestinationMenu))

                binding.toolbar.apply {
                    setSupportActionBar(this)
                    setOnClickListener { bottomNavigationDrawer.toggleState() }
                }

                binding.viewPager.apply {
                    orientation = ORIENTATION_HORIZONTAL
                    adapter = NavDrawerPagerAdapter(
                            pageOneAdapter = listOf(accountsAdapter, navDrawerDestinationAdapter),
                            pageTwoAdapter = listOf(subscriptionAdapter)
                    )
                }
            }
        }
    }

    private fun observeScreen() {
        with(supportFragmentManager) {
            addOnBackStackChangedListener {
                when (findFragmentById(R.id.fragment_container)?.javaClass?.simpleName) {
                    SubredditFragment::class.java.simpleName,
                    CommentsFragment::class.java.simpleName -> viewModel.showBottomNavDrawer()

                    SettingsFragment::class.java.simpleName,
                    VideoPreviewFragment::class.java.simpleName,
                    ImagePreviewFragment::class.java.simpleName,
                    ExploreFragment::class.java.simpleName -> viewModel.hideBottomNavDrawer()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        setUpWorkers()
        redditClient.detachListener()
    }

    private fun openSettings() {
        supportFragmentManager.beginTransaction().addToBackStack(null).replace(R.id.fragment_container, SettingsFragment()).commit()
    }

    override fun currentAccountChanged() = viewModel.reloadContent()

    private fun setUpWorkers() = enqueueCleanUpWork(this)

    private fun getCurrentDestinationMenu(): Int? =
            when (supportFragmentManager.findFragmentById(R.id.fragment_container)?.javaClass?.simpleName) {
                SubredditFragment::class.java.simpleName -> R.menu.subreddit_screen_menu
                CommentsFragment::class.java.simpleName -> R.menu.comment_screen_menu
                else -> null
            }
}