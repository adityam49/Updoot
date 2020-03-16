package com.ducktapedapps.updoot.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.UpdootApplication
import com.ducktapedapps.updoot.databinding.ActivityMainBinding
import com.ducktapedapps.updoot.model.LinkData
import com.ducktapedapps.updoot.ui.subreddit.QASSubredditFragmentDirections
import com.ducktapedapps.updoot.ui.subreddit.QASSubredditVM
import com.ducktapedapps.updoot.ui.subreddit.QASSubredditVMFactory
import com.ducktapedapps.updoot.utils.Constants
import com.ducktapedapps.updoot.utils.QuickActionSheetBehavior
import com.ducktapedapps.updoot.utils.accountManagement.UserManager
import com.ducktapedapps.updoot.utils.accountManagement.UserManager.AccountChangeListener
import com.google.android.material.bottomsheet.BottomSheetBehavior
import javax.inject.Inject

class MainActivity : AppCompatActivity(), AccountChangeListener {
    private lateinit var viewModel: ActivityVM
    private lateinit var qasSubredditVM: QASSubredditVM

    @Inject
    lateinit var userManager: UserManager

    @Inject
    lateinit var qasSubredditVMFactory: QASSubredditVMFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as UpdootApplication).updootComponent.inject(this)

        setUpViewModels()

        setUpViews()

        userManager.attachListener(this)
    }

    private fun setUpViews() {
        val binding: ActivityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        val navController = Navigation.findNavController(this, R.id.nav_host_fragment)
        setSupportActionBar(binding.toolbar)

        val bottomNavigationView = binding.bottomNavigationBar
        bottomNavigationView.setupWithNavController(navController)

        val behavior: QuickActionSheetBehavior = BottomSheetBehavior.from(binding.qasContainer) as QuickActionSheetBehavior
        val qasController: NavController = findNavController(R.id.qas_fragment)

        navController.addOnDestinationChangedListener { _: NavController?, destination: NavDestination, arguments: Bundle? ->
            behavior.hideQAS()
            binding.apply {
                appbarLayout.setExpanded(true, false)
                bottomNavigationBar.visibility = View.VISIBLE
                qasContainer.visibility = View.VISIBLE
                toolbar.title = when (destination.id) {
                    R.id.SubredditDestination -> {
                        val subredditName = arguments?.getString("r/subreddit")
                        qasController.navigate(QASSubredditFragmentDirections.actionGlobalQASSubredditFragment().setSubredditName(subredditName))
                        qasSubredditVM.subredditName = subredditName
                        qasSubredditVM.loadInfo()
                        subredditName ?: getString(R.string.app_name)
                    }

                    R.id.CommentsDestination -> {
                        val data = arguments?.getParcelable<LinkData>("SubmissionData")
                        if (data != null && data.commentsCount != 0) {
                            if (data.commentsCount <= 999) data.commentsCount.toString() + " comments"
                            else (data.commentsCount / 1000).toString() + "k comments"
                        } else getString(R.string.Comments)
                    }

                    R.id.ExploreDestination -> getString(R.string.explore)

                    R.id.SettingsDestination -> getString(R.string.settings)

                    R.id.ImagePreviewDestination -> {
                        binding.apply {
                            appbarLayout.setExpanded(false, true)
                            bottomNavigationBar.visibility = View.GONE
                            qasContainer.visibility = View.GONE
                        }
                        ""
                    }

                    else -> getString(R.string.app_name)
                }
            }
        }
    }

    private fun setUpViewModels() {
        viewModel = ViewModelProvider(this).get(ActivityVM::class.java)
        qasSubredditVM = ViewModelProvider(this, qasSubredditVMFactory).get(QASSubredditVM::class.java)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(Constants.SCREEN_TITLE_KEY, this.supportActionBar?.title.toString())
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        this.supportActionBar?.title = savedInstanceState.getString(Constants.SCREEN_TITLE_KEY, getString(R.string.app_name))
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