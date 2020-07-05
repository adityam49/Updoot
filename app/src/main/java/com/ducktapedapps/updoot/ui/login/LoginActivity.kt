package com.ducktapedapps.updoot.ui.login

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.UpdootApplication
import com.ducktapedapps.updoot.databinding.ActivityLoginBinding
import com.ducktapedapps.updoot.ui.login.LoginState.*
import com.ducktapedapps.updoot.ui.login.ResultState.*
import javax.inject.Inject

class LoginActivity : AppCompatActivity() {

    @Inject
    lateinit var vmFactory: LoginVMFactory
    private lateinit var binding: ActivityLoginBinding
    private val viewModel by lazy { ViewModelProvider(this@LoginActivity, vmFactory).get(LoginViewModel::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as UpdootApplication).updootComponent.inject(this)
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        clearCookies()

        observeViewModel()

        binding.webView.apply {
            loadUrl(viewModel.authUrl)
            webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) = viewModel.parseUrl(Uri.parse(url))
            }
        }
    }

    private fun clearCookies() {
        CookieManager.getInstance().removeAllCookies(null)
        CookieManager.getInstance().flush()
    }

    private fun observeViewModel() {
        viewModel.apply {
            loginResult.observe(this@LoginActivity) { state ->
                when (state) {
                    is NotLoggedIn -> Unit
                    is Processing -> {
                        binding.webView.apply {
                            visibility = View.GONE
                            stopLoading()
                        }
                    }
                    is LoggedIn -> {
                        Toast.makeText(this@LoginActivity, "Logged In", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    is Error -> {
                        Toast.makeText(this@LoginActivity, state.errorMessage, Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            }
            accountNameState.observe(this@LoginActivity) { state ->
                with(binding) {
                    when (state) {
                        is Uninitiated -> {
                            userNameStatus.visibility = View.GONE
                            userNameStatusIcon.visibility = View.GONE
                        }
                        is Initiated -> {
                            userNameStatusIcon.apply {
                                visibility = View.VISIBLE
                                Glide.with(this@LoginActivity)
                                        .load(R.drawable.ic_account_circle_24dp)
                                        .apply(RequestOptions.circleCropTransform())
                                        .into(userNameStatusIcon)
                            }
                            userNameStatus.apply {
                                visibility = View.VISIBLE
                                text = context.getString(R.string.requesting_user_name)
                            }
                        }
                        is Finished -> {
                            userNameStatus.text = state.result.name
                            Glide.with(this@LoginActivity)
                                    .load(state.result.icon_img)
                                    .apply(RequestOptions.circleCropTransform())
                                    .into(userNameStatusIcon)
                        }
                    }
                }
            }
            subscribedSubreddits.observe(this@LoginActivity) { state ->
                with(binding) {
                    when (state) {
                        Uninitiated -> {
                            subredditStatus.visibility = View.GONE
                            subredditStatusIcon.visibility = View.GONE
                        }
                        Initiated -> {
                            subredditStatusIcon.apply {
                                visibility = View.VISIBLE
                                Glide.with(this@LoginActivity)
                                        .load(R.drawable.ic_subreddit_default_24dp)
                                        .into(this)
                            }
                            subredditStatus.apply {
                                visibility = View.VISIBLE
                                text = context.getString(R.string.subreddit_syncing)
                            }
                        }
                        is Finished -> {
                            subredditStatus.text = String.format("Synced %d subscribed subreddits", state.result)
                        }
                    }
                }
            }
        }
    }
}