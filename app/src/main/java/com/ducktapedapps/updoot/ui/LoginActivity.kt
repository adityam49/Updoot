package com.ducktapedapps.updoot.ui

import android.accounts.Account
import android.accounts.AccountManager
import android.app.Activity
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.UpdootApplication
import com.ducktapedapps.updoot.api.AuthAPI
import com.ducktapedapps.updoot.api.RedditAPI
import com.ducktapedapps.updoot.databinding.ActivityLoginBinding
import com.ducktapedapps.updoot.model.Token
import com.ducktapedapps.updoot.utils.Constants
import com.ducktapedapps.updoot.utils.accountManagement.TokenInterceptor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class LoginActivity : AppCompatActivity(), CoroutineScope {

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

        setSupportActionBar(binding.toolbar)

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
            val token: Token = authAPI.getUserTokenByCoroutine(code = code)
            token.setAbsoluteExpiry()
            interceptor.setSessionToken(token)
            token
        } catch (exception: Exception) {
            Log.i(TAG, "unable to fetch user token : ", exception.cause)
            null
        }
    }

    private suspend fun fetchUserDetailsAndSave(token: Token) {
        try {
            val account: com.ducktapedapps.updoot.model.Account = redditAPI.userIdentity()
            account.let { fetchedAccountDetails ->
                sharedPreferences.edit().putString(Constants.LOGIN_STATE, fetchedAccountDetails.name).apply()
                createAccount(fetchedAccountDetails.name, token)
                setResult(Activity.RESULT_OK)
                finish()
            }
        } catch (exception: Exception) {
            Log.i(com.ducktapedapps.updoot.ui.TAG, "unable to fetch user details: ", exception.cause)
        }
    }

    private fun createAccount(username: String, token: Token) {
        val userAccount = Account(username, Constants.ACCOUNT_TYPE)
        val bundle = Bundle()
        bundle.putString(Constants.USER_TOKEN_REFRESH_KEY, token.refresh_token)
        accountManager.addAccountExplicitly(userAccount, null, bundle)
        accountManager.setAuthToken(userAccount, "full_access", token.access_token)
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    companion object { private const val TAG = "LoginActivity" }
}