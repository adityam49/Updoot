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
import androidx.navigation.ui.setupWithNavController
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.UpdootApplication
import com.ducktapedapps.updoot.databinding.ActivityMainBinding
import com.ducktapedapps.updoot.utils.Constants
import com.ducktapedapps.updoot.utils.accountManagement.UserManager
import com.ducktapedapps.updoot.utils.accountManagement.UserManager.AccountChangeListener
import javax.inject.Inject

class MainActivity : AppCompatActivity(), AccountChangeListener {
    private lateinit var viewModel: ActivityVM

    @Inject
    lateinit var userManager: UserManager

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

        binding.bottomNavView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _: NavController?, destination: NavDestination, arguments: Bundle? ->
            when (destination.id) {
                R.id.ImagePreviewDestination -> binding.bottomNavView.visibility = View.GONE
                else -> binding.bottomNavView.visibility = View.VISIBLE
            }
        }
    }

    private fun setUpViewModels() {
        viewModel = ViewModelProvider(this).get(ActivityVM::class.java)
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