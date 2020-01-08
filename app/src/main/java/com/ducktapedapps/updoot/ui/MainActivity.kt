package com.ducktapedapps.updoot.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.Navigation
import androidx.navigation.ui.setupWithNavController
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.UpdootApplication
import com.ducktapedapps.updoot.databinding.ActivityMainBinding
import com.ducktapedapps.updoot.model.LinkData
import com.ducktapedapps.updoot.ui.AccountsBottomSheetDialogFragment.BottomSheetListener
import com.ducktapedapps.updoot.utils.Constants
import com.ducktapedapps.updoot.utils.accountManagement.UserManager
import com.ducktapedapps.updoot.utils.accountManagement.UserManager.AccountChangeListener
import javax.inject.Inject

class MainActivity : AppCompatActivity(), BottomSheetListener, AccountChangeListener {
    private lateinit var viewModel: ActivityVM
    @Inject
    lateinit var userManager: UserManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as UpdootApplication).updootComponent.inject(this)

        val binding: ActivityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        val navController = Navigation.findNavController(this, R.id.nav_host_fragment)
        val bottomNavigationView = binding.bottomNavigationBar

        setSupportActionBar(binding.toolbar)

        navController.addOnDestinationChangedListener { _: NavController?, destination: NavDestination, arguments: Bundle? ->
            when (destination.id) {
                R.id.SubredditDestination -> {
                    val title = arguments?.getString("r/subreddit")
                    binding.toolbar.title = title ?: getString(R.string.app_name)
                }

                R.id.CommentsDestination -> {
                    val data = arguments?.getParcelable<LinkData>("SubmissionData")
                    if (data != null && data.commentsCount != 0) {
                        if (data.commentsCount <= 999) binding.toolbar.title = data.commentsCount.toString() + " comments"
                        else binding.toolbar.title = (data.commentsCount / 1000).toString() + "k comments"
                    } else binding.toolbar.title = getString(R.string.Comments)
                }
            }

            bottomNavigationView.setupWithNavController(navController)
            userManager.attachListener(this)
            viewModel = ViewModelProvider(this).get(ActivityVM::class.java)
        }
    }

    //for bottomSheet Account switching
    override fun onButtonClicked(text: String?) {
        if (text == "Add Account") {
            val intent = Intent(this, LoginActivity::class.java)
            startActivityForResult(intent, Constants.ACCOUNT_LOGIN_REQUEST_CODE)
        } else {
            userManager.setCurrentUser(text, null)
            viewModel.setCurrentAccount(userManager.currentUser?.name)
        }
    }

    override fun onCurrentAccountRemoved() {
        reloadContent()
    }

    override fun onDestroy() {
        super.onDestroy()
        userManager.detachListener()
    }

    private fun reloadContent() {
        viewModel.setCurrentAccount(userManager.currentUser?.name)
    }

    //Account switching after new login
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.ACCOUNT_LOGIN_REQUEST_CODE && resultCode == Activity.RESULT_OK) reloadContent()
    }
}