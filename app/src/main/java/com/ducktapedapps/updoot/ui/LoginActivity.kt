package com.ducktapedapps.updoot.ui

import android.accounts.AccountAuthenticatorActivity
import android.accounts.AccountManager
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.UpdootApplication
import com.ducktapedapps.updoot.api.remote.AuthAPI
import com.ducktapedapps.updoot.api.remote.RedditAPI
import com.ducktapedapps.updoot.databinding.ActivityLoginBinding
import com.ducktapedapps.updoot.model.Token
import com.ducktapedapps.updoot.utils.Constants
import com.ducktapedapps.updoot.utils.accountManagement.RedditClient
import com.ducktapedapps.updoot.utils.accountManagement.TokenInterceptor
import kotlinx.coroutines.*
import java.util.*
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class LoginActivity : AccountAuthenticatorActivity(), CoroutineScope {
    @Inject
    lateinit var redditClient: RedditClient

    @Inject
    lateinit var redditAPI: RedditAPI

    @Inject
    lateinit var authAPI: AuthAPI

    @Inject
    lateinit var interceptor: TokenInterceptor

    @Inject
    lateinit var accountManager: AccountManager

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    private lateinit var job: Job

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    private val authUrl: String
        get() {
            val state = UUID.randomUUID().toString()
            return Uri.Builder()
                    .scheme("https")
                    .authority("www.reddit.com")
                    .appendPath("api")
                    .appendPath("v1")
                    .appendPath("authorize.compact")
                    .appendQueryParameter("client_id", Constants.client_id)
                    .appendQueryParameter("response_type", "code")
                    .appendQueryParameter("state", state)
                    .appendQueryParameter("redirect_uri", Constants.redirect_uri)
                    .appendQueryParameter("duration", "permanent")
                    .appendQueryParameter("scope", Constants.scopes)
                    .build()
                    .toString()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityLoginBinding = DataBindingUtil.setContentView(this, R.layout.activity_login)

        (application as UpdootApplication).updootComponent.inject(this)

        job = Job()

        CookieManager.getInstance().removeAllCookies(null)
        CookieManager.getInstance().flush()

        val webView = binding.webView
        val progressBar = binding.loginProgress

        webView.loadUrl(authUrl)

        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                val uri = Uri.parse(url)
                uri?.let {
                    if (it.host == Uri.parse(Constants.redirect_uri)?.host) {
                        if (uri.getQueryParameter("error") == null) {

                            webView.stopLoading()
                            webView.visibility = View.GONE
                            progressBar.visibility = View.VISIBLE

                            val code = uri.getQueryParameter("code")
                            if (code != null) {
                                launch(Dispatchers.IO) {
                                    fetchToken(code)?.let { fetchedToken ->
                                        fetchUserDetailsAndSave(fetchedToken)
                                    }
                                }

                            }
                        } else finish()
                    }
                }
            }
        }
    }

    private suspend fun fetchToken(code: String): Token? {
        return try {
            val token: Token = authAPI.getUserToken(code = code)
            token.setAbsoluteExpiry()
            interceptor.sessionToken = token.access_token
            token
        } catch (exception: Exception) {
            Log.e(TAG, "unable to fetch user token : ", exception)
            null
        }
    }

    private suspend fun fetchUserDetailsAndSave(token: Token) {
        try {
            val account = redditAPI.userIdentity()
            account.let { fetchedAccountDetails ->
                withContext(Dispatchers.Main) {
                    redditClient.createUserAccountAndSetItAsCurrent(
                            fetchedAccountDetails.name,
                            fetchedAccountDetails.icon_img,
                            token
                    )
                }
            }
        } catch (exception: Exception) {
            Log.e(TAG, "unable to fetch user details: ", exception)
            withContext(Dispatchers.Main) {
                Toast.makeText(this@LoginActivity, "Unable to login", Toast.LENGTH_SHORT).show()
            }
        } finally {
            withContext(Dispatchers.Main) { finish() }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    companion object {
        private const val TAG = "LoginActivity"
    }
}