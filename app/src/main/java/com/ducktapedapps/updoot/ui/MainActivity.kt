package com.ducktapedapps.updoot.ui

import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.viewpager2.widget.ViewPager2.ORIENTATION_HORIZONTAL
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.UpdootApplication
import com.ducktapedapps.updoot.backgroundWork.cacheCleanUp.enqueueCleanUpWork
import com.ducktapedapps.updoot.databinding.ActivityMainBinding
import com.ducktapedapps.updoot.ui.comments.CommentsFragment
import com.ducktapedapps.updoot.ui.explore.ExploreFragment
import com.ducktapedapps.updoot.ui.navDrawer.NavDrawerPagerAdapter
import com.ducktapedapps.updoot.ui.navDrawer.ScrimVisibilityAdjuster
import com.ducktapedapps.updoot.ui.navDrawer.ToolbarMenuSwapper
import com.ducktapedapps.updoot.ui.settings.SettingsFragment
import com.ducktapedapps.updoot.ui.subreddit.SubredditFragment
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
            navDrawerVisibility.observe(this@MainActivity) { visibile ->
                binding.bottomNavigationDrawer.apply {
                    if (visibile) showWihAnimation() else hideWithAnimation()
                }
            }
        }
    }

    private fun setUpStatusBarColors() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.color_primary)
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
                    adapter = NavDrawerPagerAdapter(this@MainActivity)
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
        supportFragmentManager
                .beginTransaction()
                .addToBackStack(null)
                .setCustomAnimations(R.anim.enter_from_bottom, R.anim.exit_to_top, R.anim.enter_from_top, R.anim.exit_to_bottom)
                .replace(R.id.fragment_container, SettingsFragment())
                .commit()
    }

    fun collapseBottomNavDrawer() = binding.bottomNavigationDrawer.collapse()

    override fun currentAccountChanged() = viewModel.reloadContent()

    private fun setUpWorkers() = enqueueCleanUpWork(this)

    private fun getCurrentDestinationMenu(): Int? =
            when (supportFragmentManager.findFragmentById(R.id.fragment_container)?.javaClass?.simpleName) {
                SubredditFragment::class.java.simpleName -> R.menu.subreddit_screen_menu
                CommentsFragment::class.java.simpleName -> R.menu.comment_screen_menu
                else -> null
            }
}