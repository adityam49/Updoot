package com.ducktapedapps.updoot.ui

import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.UpdootApplication
import com.ducktapedapps.updoot.backgroundWork.enqueueCleanUpWork
import com.ducktapedapps.updoot.backgroundWork.enqueueSubscriptionSyncWork
import com.ducktapedapps.updoot.databinding.ActivityMainBinding
import com.ducktapedapps.updoot.ui.comments.CommentsFragment
import com.ducktapedapps.updoot.ui.explore.ExploreFragment
import com.ducktapedapps.updoot.ui.imagePreview.ImagePreviewFragment
import com.ducktapedapps.updoot.ui.login.LoginFragment
import com.ducktapedapps.updoot.ui.navDrawer.NavDrawerScreen
import com.ducktapedapps.updoot.ui.navDrawer.ScrimVisibilityAdjuster
import com.ducktapedapps.updoot.ui.navDrawer.ToolbarMenuSwapper
import com.ducktapedapps.updoot.ui.settings.SettingsFragment
import com.ducktapedapps.updoot.ui.subreddit.SubredditFragment
import com.ducktapedapps.updoot.ui.theme.UpdootTheme
import com.ducktapedapps.updoot.utils.Constants.FRONTPAGE
import com.ducktapedapps.updoot.utils.accountManagement.IRedditClient
import com.ducktapedapps.updoot.utils.accountManagement.RedditClient
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Inject


@FlowPreview
@ExperimentalCoroutinesApi
class MainActivity : AppCompatActivity(), IRedditClient.AccountChangeListener {
    @Inject
    lateinit var redditClient: RedditClient

    @Inject
    lateinit var activityVMFactory: ActivityVMFactory
    private val viewModel by lazy { ViewModelProvider(this@MainActivity, activityVMFactory).get(ActivityVM::class.java) }

    private lateinit var binding: ActivityMainBinding
    private val bottomNavBinding by lazy { binding.bottomNavigationDrawer.binding }

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

    private fun setUpViewModel() = viewModel.apply {
        navDrawerVisibility.observe(this@MainActivity, { visible ->
            binding.bottomNavigationDrawer.apply {
                if (visible) showWihAnimation() else hideWithAnimation()
            }
        })
    }

    private fun setUpStatusBarColors() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.color_primary)
        }
    }

    override fun onBackPressed() {
        with(binding.bottomNavigationDrawer) {
            if (isInFocus()) collapse()
            else {
                if (onBackPressedDispatcher.hasEnabledCallbacks()) super.onBackPressed()
                else showExitDialog()
            }
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
            }
        }
        setUpBottomNavViews()
    }

    private fun setUpBottomNavViews() = bottomNavBinding.apply {
        composeView.setContent {
            UpdootTheme {
                NavDrawerScreen(
                        viewModel = viewModel,
                        onLogin = ::openLoginScreen,
                        onRemoveAccount = ::logout,
                        onSwitchAccount = ::switchAccount,
                        onToggleAccountMenu = viewModel::toggleAccountsMenuList,
                        onExit = ::showExitDialog,
                        onExplore = ::openExplore
                )
            }
        }
    }

    private fun observeScreen() {
        with(supportFragmentManager) {
            addOnBackStackChangedListener {
                when (findFragmentById(R.id.fragment_container)) {
                    is SubredditFragment,
                    is CommentsFragment -> viewModel.showBottomNavDrawer()

                    is LoginFragment,
                    is SettingsFragment,
                    is VideoPreviewFragment,
                    is ImagePreviewFragment,
                    is ExploreFragment -> viewModel.hideBottomNavDrawer()
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
        supportFragmentManager
                .beginTransaction()
                .addToBackStack(null)
                .setCustomAnimations(R.anim.enter_from_top, R.anim.exit_to_bottom, R.anim.enter_from_bottom, R.anim.exit_to_top)
                .replace(R.id.fragment_container, SettingsFragment())
                .commit()
    }

    private fun collapseBottomNavDrawer() = binding.bottomNavigationDrawer.collapse()

    override fun currentAccountChanged() = viewModel.reloadContent()

    private fun setUpWorkers() {
        enqueueSubscriptionSyncWork()
        enqueueCleanUpWork()
    }

    private fun getCurrentDestinationMenu(): Int? =
            when (supportFragmentManager.findFragmentById(R.id.fragment_container)) {
                is SubredditFragment -> R.menu.subreddit_screen_menu
                is CommentsFragment -> R.menu.comment_screen_menu
                else -> null
            }

    private fun logout(userName: String) {
        viewModel.logout(userName)
        collapseBottomNavDrawer()
    }

    private fun switchAccount(userName: String) {
        viewModel.setCurrentAccount(userName)
        collapseBottomNavDrawer()
    }

    private fun openLoginScreen() {
        supportFragmentManager
                .beginTransaction()
                .addToBackStack(null)
                .setCustomAnimations(R.anim.enter_from_bottom, R.anim.exit_to_top, R.anim.enter_from_top, R.anim.exit_to_bottom)
                .replace(R.id.fragment_container, LoginFragment())
                .commit()
        collapseBottomNavDrawer()
    }

    private fun openExplore() {
        supportFragmentManager
                .beginTransaction()
                .addToBackStack(null)
                .setCustomAnimations(R.anim.enter_from_top, R.anim.exit_to_bottom, R.anim.enter_from_bottom, R.anim.exit_to_top)
                .replace(R.id.fragment_container, ExploreFragment())
                .commit()
    }

    private fun showExitDialog() = ExitDialogFragment().show(supportFragmentManager, null)
}