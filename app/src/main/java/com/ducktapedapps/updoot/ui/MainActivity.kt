package com.ducktapedapps.updoot.ui

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.lifecycleScope
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.databinding.ActivityMainBinding
import com.ducktapedapps.updoot.ui.comments.CommentsFragment
import com.ducktapedapps.updoot.ui.explore.ExploreFragment
import com.ducktapedapps.updoot.ui.imagePreview.ImagePreviewFragment
import com.ducktapedapps.updoot.ui.login.LoginFragment
import com.ducktapedapps.updoot.ui.navDrawer.NavigationDestination.*
import com.ducktapedapps.updoot.ui.search.SearchFragment
import com.ducktapedapps.updoot.ui.settings.SettingsFragment
import com.ducktapedapps.updoot.ui.subreddit.PostMedia.ImageMedia
import com.ducktapedapps.updoot.ui.subreddit.SubredditFragment
import com.ducktapedapps.updoot.ui.user.UserFragment
import com.ducktapedapps.updoot.utils.Constants.FRONTPAGE
import com.ducktapedapps.updoot.utils.ThemeType.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: ActivityVM by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUpFragments()

        setUpViewModel()

        setUpStatusBarColors()
    }

    private fun setUpViewModel() {
        viewModel
            .theme
            .onEach {
                setDefaultNightMode(
                    when (it) {
                        DARK -> MODE_NIGHT_YES
                        LIGHT -> MODE_NIGHT_NO
                        AUTO -> MODE_NIGHT_FOLLOW_SYSTEM
                    }
                )
            }.launchIn(lifecycleScope)
        viewModel
            .navigationRequest
            .onEach {
                when (it) {
                    AddAccount -> openLoginScreen()
                    Exit -> showExitDialog()
                    Settings -> openSettings()
                    Search -> openSearch()
                    Explore -> openExplore()
                    else -> Unit
                }
            }.launchIn(lifecycleScope)
    }

    private fun openExplore() {
        supportFragmentManager
            .beginTransaction()
            .addToBackStack(null)
            .replace(R.id.fragment_container, ExploreFragment())
            .commit()
    }

    private fun openSearch() {
        supportFragmentManager
            .beginTransaction()
            .addToBackStack(null)
            .replace(R.id.fragment_container, SearchFragment())
            .commit()
    }

    private fun setUpStatusBarColors() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.color_status_bar)
        }
    }

    override fun onBackPressed() {
        if (onBackPressedDispatcher.hasEnabledCallbacks()) super.onBackPressed()
        else showExitDialog()
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

    private fun openSettings() {
        supportFragmentManager
            .beginTransaction()
            .addToBackStack(null)
            .replace(R.id.fragment_container, SettingsFragment())
            .commit()
    }


    private fun openLoginScreen() {
        supportFragmentManager
            .beginTransaction()
            .addToBackStack(null)
            .replace(R.id.fragment_container, LoginFragment())
            .commit()
    }

    fun openSubreddit(name: String) {
        supportFragmentManager
            .beginTransaction()
            .addToBackStack(null)
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            .replace(R.id.fragment_container, SubredditFragment.newInstance(name))
            .commit()
    }

    fun openUser(name: String) {
        supportFragmentManager
            .beginTransaction()
            .addToBackStack(null)
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            .replace(R.id.fragment_container, UserFragment.newInstance(name))
            .commit()
    }

    fun openComments(subreddit: String, id: String) {
        supportFragmentManager
            .beginTransaction()
            .addToBackStack(null)
            .replace(R.id.fragment_container, CommentsFragment.newInstance(subreddit, id))
            .commit()
    }

    fun openLink(link: String) = startActivity(Intent().apply {
        action = Intent.ACTION_VIEW
        data = Uri.parse(link)
    })

    fun openImage(media: ImageMedia) {
        supportFragmentManager
            .beginTransaction()
            .addToBackStack(null)
            .add(
                R.id.fragment_container,
                ImagePreviewFragment.newInstance(media.url, media.url)
            ) //TODO : put high res url in UI model
            .commit()
    }

    fun openVideo(videoUrl: String) {
        supportFragmentManager
            .beginTransaction()
            .addToBackStack(null)
            .add(R.id.fragment_container, VideoPreviewFragment.newInstance(videoUrl))
            .commit()
    }

    private fun showExitDialog() = ExitDialogFragment().show(supportFragmentManager, null)
}