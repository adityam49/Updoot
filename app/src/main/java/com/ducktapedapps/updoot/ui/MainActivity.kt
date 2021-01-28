package com.ducktapedapps.updoot.ui

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate.*
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.databinding.ActivityMainBinding
import com.ducktapedapps.updoot.ui.comments.CommentsFragment
import com.ducktapedapps.updoot.ui.explore.ExploreFragment
import com.ducktapedapps.updoot.ui.login.LoginFragment
import com.ducktapedapps.updoot.ui.navDrawer.*
import com.ducktapedapps.updoot.ui.navDrawer.NavigationDestination.*
import com.ducktapedapps.updoot.ui.search.SearchVM
import com.ducktapedapps.updoot.ui.settings.SettingsFragment
import com.ducktapedapps.updoot.ui.subreddit.SubredditFragment
import com.ducktapedapps.updoot.utils.Constants
import com.ducktapedapps.updoot.utils.Constants.FRONTPAGE
import com.ducktapedapps.updoot.utils.ThemeType.*
import com.ducktapedapps.updoot.utils.accountManagement.IRedditClient
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var redditClient: IRedditClient

    private val viewModel: ActivityVM by viewModels()
    private val searchVM: SearchVM by viewModels()

    private lateinit var binding: ActivityMainBinding
    private val bottomNavBinding by lazy { binding.bottomNavigationDrawer.binding }

    private val subscriptionAdapter = SubscriptionsAdapter(object : SubscriptionsAdapter.ClickHandler {
        override fun goToSubreddit(subredditName: String) {
            supportFragmentManager
                    .beginTransaction()
                    .addToBackStack(null)
                    .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                    .replace(R.id.fragment_container, SubredditFragment.newInstance(subredditName))
                    .commit()
            bottomNavBinding.apply {
                searchView.clearFocus()
                root.post { collapseBottomNavDrawer() }
            }
        }
    })

    private val destinationAdapter = NavDrawerDestinationAdapter(object : NavDrawerDestinationAdapter.ClickHandler {
        override fun openDestination(destination: NavigationDestination) {
            when (destination) {
                AddAccount -> supportFragmentManager
                        .beginTransaction()
                        .addToBackStack(null)
                        .setCustomAnimations(R.anim.enter_from_bottom, R.anim.exit_to_top, R.anim.enter_from_top, R.anim.exit_to_bottom)
                        .replace(R.id.fragment_container, LoginFragment())
                        .commit()

                Search -> bottomNavBinding.searchView.apply {
                    expandBottomNavDrawer()
                    visibility = VISIBLE
                    requestFocus()
                    showKeyBoard(this.findFocus())
                }
                Explore -> {
                    supportFragmentManager
                            .beginTransaction()
                            .addToBackStack(null)
                            .setCustomAnimations(R.anim.enter_from_top, R.anim.exit_to_bottom, R.anim.enter_from_bottom, R.anim.exit_to_top)
                            .replace(R.id.fragment_container, ExploreFragment())
                            .commit()
                }
                History, Inbox, CreatePost -> Unit
                Exit -> showExitDialog()
            }
        }
    })

    private val accountsAdapter = AccountsAdapter(object : AccountsAdapter.AccountAction {
        override fun switch(accountName: String) {
            viewModel.setCurrentAccount(accountName)
            collapseBottomNavDrawer()
        }

        override fun logout(accountName: String) = viewModel.logout(accountName)
    })

    private val concatAdapter = ConcatAdapter(accountsAdapter, destinationAdapter)

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.item_settings -> {
                openSettings()
                true
            }
            R.id.item_sync_subscriptions -> {
                viewModel.enqueueSubscriptionSyncWork()
                true
            }
            else -> false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUpFragments()

        setUpViews()

        setUpViewModel()

        setUpStatusBarColors()
    }

    private fun setUpViewModel() {
        viewModel.apply {
            theme.onEach {
                setDefaultNightMode(when (it) {
                    DARK -> MODE_NIGHT_YES
                    LIGHT -> MODE_NIGHT_NO
                    AUTO -> MODE_NIGHT_FOLLOW_SYSTEM
                })
            }.launchIn(lifecycleScope)
            accounts.onEach {
                accountsAdapter.submitList(it)
            }.launchIn(lifecycleScope)
            navigationEntries.onEach {
                destinationAdapter.submitList(it)
            }.launchIn(lifecycleScope)
            searchVM.searchQueryLoading
                    .onEach { loading ->
                        bottomNavBinding.loadingIndicator.visibility = if (loading) VISIBLE else GONE
                    }
                    .onStart { delay(2_000) }
                    .launchIn(lifecycleScope)
            searchVM.results.onEach { subscriptionAdapter.submitList(it) }.launchIn(lifecycleScope)
        }
    }

    private fun setUpStatusBarColors() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.color_primary)
        }
    }

    override fun onBackPressed() {
        with(binding.bottomNavigationDrawer) {
            if (isInFocus()) {
                if (binding.searchView.hasFocus()) clearFocus()
                else collapse()
            } else {
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
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = concatAdapter
        }
        searchView.setOnQueryTextFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                concatAdapter.apply {
                    removeAdapter(accountsAdapter)
                    removeAdapter(destinationAdapter)
                    addAdapter(subscriptionAdapter)
                }
            } else {
                concatAdapter.apply {
                    removeAdapter(subscriptionAdapter)
                    addAdapter(accountsAdapter)
                    addAdapter(destinationAdapter)
                    view.visibility = GONE
                }
            }
        }
        lifecycleScope.launch {
            binding.bottomNavigationDrawer.binding.searchView.getQueryFlow()
                    .debounce(Constants.DEBOUNCE_TIME_OUT)
                    .filterNotNull()
                    .distinctUntilChanged()
                    .collectLatest {
                        searchVM.searchSubreddit(it)
                    }
        }
    }

    private fun SearchView.getQueryFlow(): StateFlow<String?> {
        val query = MutableStateFlow<String?>("")
        setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = true
            override fun onQueryTextChange(newText: String?): Boolean {
                query.value = newText
                return true
            }
        })
        return query
    }

    private fun openSettings() {
        supportFragmentManager
                .beginTransaction()
                .addToBackStack(null)
                .setCustomAnimations(R.anim.enter_from_top, R.anim.exit_to_bottom, R.anim.enter_from_bottom, R.anim.exit_to_top)
                .replace(R.id.fragment_container, SettingsFragment())
                .commit()
    }

    fun collapseBottomNavDrawer() = binding.bottomNavigationDrawer.collapse()

    fun expandBottomNavDrawer() = binding.bottomNavigationDrawer.expand()

    fun hideBottomNavDrawer() = binding.bottomNavigationDrawer.hideWithAnimation()

    fun showBottomNavDrawer() = binding.bottomNavigationDrawer.showWihAnimation()

    private fun getCurrentDestinationMenu(): Int? =
            when (supportFragmentManager.findFragmentById(R.id.fragment_container)) {
                is SubredditFragment -> R.menu.subreddit_screen_menu
                is CommentsFragment -> R.menu.comment_screen_menu
                else -> null
            }

    private fun showKeyBoard(focusView: View) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(focusView, 0)
    }

    private fun showExitDialog() = ExitDialogFragment().show(supportFragmentManager, null)
}