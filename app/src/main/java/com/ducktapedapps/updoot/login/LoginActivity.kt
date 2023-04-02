package com.ducktapedapps.updoot.login

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.core.widget.ContentLoadingProgressBar
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.ducktapedapps.updoot.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginActivity : ComponentActivity() {
    private val loginViewModel by viewModels<LoginViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

       val webView =  findViewById<WebView>(R.id.webv).apply {
            CookieManager.getInstance().removeAllCookies { }
            settings.javaScriptEnabled = true
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    request?.url?.let { view?.loadUrl(it.toString()) }
                    return true
                }

                override fun onPageFinished(view: WebView, url: String) {
                    super.onPageFinished(view, url)
                    loginViewModel.parseUrl(Uri.parse(url))
                }
            }
            loadUrl(loginViewModel.authUrl)
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                loginViewModel.loginState.collect {
                    when (it) {
                        is LoginState.NotLoggedIn,
                        is LoginState.ObservingUrl -> {
                            findViewById<ContentLoadingProgressBar>(R.id.progress_circular)?.hide()
                        }
                        is LoginState.Error -> {
                            Toast.makeText(
                                this@LoginActivity,
                                "Something went wrong",
                                Toast.LENGTH_SHORT
                            ).show()
                            finish()
                        }
                        is LoginState.FetchingToken,
                        is LoginState.FetchingUserName -> {
                            webView.visibility = View.INVISIBLE
                            findViewById<ContentLoadingProgressBar>(R.id.progress_circular)?.show()
                        }
                        LoginState.LoggedIn -> finish()
                    }
                }
            }
        }
    }
}