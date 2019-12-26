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
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.*
import javax.inject.Inject

class LoginActivity : AppCompatActivity() {
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

    private var mToken: Token? = null

    private val disposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityLoginBinding = DataBindingUtil.setContentView(this, R.layout.activity_login)

        (application as UpdootApplication).updootComponent.inject(this)

        setSupportActionBar(binding.toolbar)

        CookieManager.getInstance().removeAllCookies(null)
        CookieManager.getInstance().flush()

        val webView = binding.webView
        val progressBar = binding.loginProgress

        webView.loadUrl(authUrl)
        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                Log.i(TAG, "onPageStarted: login $url")
                val uri = Uri.parse(url)
                if (uri?.host == Uri.parse(Constants.redirect_uri).host) {
                    if (uri.getQueryParameter("error") == null) {
                        webView.stopLoading()
                        webView.visibility = View.GONE
                        progressBar.visibility = View.VISIBLE
                        val code = uri.getQueryParameter("code")
                        if (code != null)
                            disposable.add(
                                    authAPI.getUserToken(code = code)
                                            .doOnSuccess { token: Token ->
                                                mToken = token
                                                token.setAbsoluteExpiry()
                                                interceptor.setSessionToken(token)
                                            }
                                            .doOnError { throwable: Throwable? -> Log.e(TAG, "onPageStarted: ", throwable) }
                                            .map { redditAPI }
                                            .flatMap(RedditAPI::userIdentity)
                                            .doOnSuccess { (name) ->
                                                sharedPreferences.edit().putString(Constants.LOGIN_STATE, name).apply()
                                                createAccount(name, mToken)
                                                setResult(Activity.RESULT_OK)
                                                finish()
                                            }
                                            .doOnError { throwable: Throwable? -> Log.e(TAG, "onPageStarted: ", throwable) }
                                            .subscribeOn(Schedulers.io())
                                            .subscribe()
                            )
                    } else {
                        Log.i(TAG, "onPageStarted: ")
                        finish()
                    }
                }
            }
        }
    }

    fun createAccount(username: String?, token: Token?) {
        val userAccount = Account(username, Constants.ACCOUNT_TYPE)
        val bundle = Bundle()
        bundle.putString(Constants.USER_TOKEN_REFRESH_KEY, token!!.refresh_token)
        accountManager.addAccountExplicitly(userAccount, null, bundle)
        accountManager.setAuthToken(userAccount, "full_access", token.access_token)
    }

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

    override fun onDestroy() {
        super.onDestroy()
        if (!disposable.isDisposed) {
            disposable.dispose()
        }
    }

    companion object {
        private const val TAG = "LoginActivity"
    }
}