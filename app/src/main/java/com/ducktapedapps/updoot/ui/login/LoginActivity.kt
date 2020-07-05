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
import com.ducktapedapps.updoot.UpdootApplication
import com.ducktapedapps.updoot.databinding.ActivityLoginBinding
import com.ducktapedapps.updoot.ui.login.LoginState.*
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

    private fun observeViewModel() = viewModel.loginResult.observe(this) { state ->
        when (state) {
            is NotLoggedIn -> Unit
            is Processing -> {
                binding.apply {
                    webView.apply {
                        stopLoading()
                        visibility = View.GONE
                    }
                    loginProgress.visibility = View.VISIBLE
                }
            }
            is LoggedIn -> {
                Toast.makeText(this, "Logged In", Toast.LENGTH_SHORT).show()
                finish()
            }
            is Error -> {
                Toast.makeText(this, "Something went wrong. Try again later ${state.errorMessage}", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}