package com.ducktapedapps.updoot.ui.login

import android.net.Uri
import androidx.lifecycle.*
import com.ducktapedapps.updoot.api.local.SubredditDAO
import com.ducktapedapps.updoot.api.remote.AuthAPI
import com.ducktapedapps.updoot.api.remote.RedditAPI
import com.ducktapedapps.updoot.model.Account
import com.ducktapedapps.updoot.model.Subreddit
import com.ducktapedapps.updoot.model.Token
import com.ducktapedapps.updoot.ui.login.LoginState.*
import com.ducktapedapps.updoot.ui.login.ResultState.*
import com.ducktapedapps.updoot.utils.Constants
import com.ducktapedapps.updoot.utils.accountManagement.RedditClient
import com.ducktapedapps.updoot.utils.accountManagement.TokenInterceptor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject

class LoginViewModel(
        private val redditClient: RedditClient,
        private val redditAPI: RedditAPI,
        private val authAPI: AuthAPI,
        private val interceptor: TokenInterceptor,
        private val subredditDAO: SubredditDAO
) : ViewModel() {

    private val _loginState: MutableLiveData<LoginState> = MutableLiveData(NotLoggedIn)
    val loginResult: LiveData<LoginState> = _loginState

    private val _accountNameState = MutableLiveData<ResultState<Account>>(Uninitiated)
    val accountNameState: LiveData<ResultState<Account>> = _accountNameState

    private val _subscribedSubreddits = MutableLiveData<ResultState<Int>>(Uninitiated)
    val subscribedSubreddits: LiveData<ResultState<Int>> = _subscribedSubreddits

    val authUrl: String
        get() {
            //TODO check state query param on redirect
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

    fun parseUrl(uri: Uri?) {
        if (uri != null && uri.host == Uri.parse(Constants.redirect_uri).host) {
            if (uri.getQueryParameter("error") == null) {
                _loginState.value = Processing
                getDetails(uri.getQueryParameter("code")!!)
            } else {
                _loginState.value = Error("Access denied")
            }
        }
    }

    private suspend fun loadUserSubscribedSubreddits() {
        try {
            _subscribedSubreddits.postValue(Initiated)
            var result = redditAPI.getSubscribedSubreddits(null)
            val allSubs = mutableListOf<Subreddit>().apply {
                addAll(result.subreddits)
            }
            var after: String? = result.after
            while (after != null) {
                result = redditAPI.getSubscribedSubreddits(after)
                allSubs.addAll(result.subreddits)
                after = result.after
            }
            allSubs.forEach { subredditDAO.insertSubreddit(it) }
            _subscribedSubreddits.postValue(Finished(allSubs.size))
        } catch (e: Exception) {
            e.printStackTrace()
            _loginState.postValue(Error("Unable to sync user subscribed subreddits"))
        }
    }

    private fun getDetails(code: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val fetchedToken = fetchToken(code)

                val account = loadUserName()

                loadUserSubscribedSubreddits()

                delay(2000)
                saveAccount(account!!, fetchedToken!!)

                _loginState.postValue(LoggedIn)

            } catch (e: Exception) {
                e.printStackTrace()
                _loginState.postValue(Error(e.message.toString()))
            }
        }
    }

    private suspend fun fetchToken(code: String): Token? =
            try {
                val token: Token = authAPI.getUserToken(code = code)
                token.setAbsoluteExpiry()
                interceptor.sessionToken = token.access_token
                token
            } catch (exception: Exception) {
                exception.printStackTrace()
                null
            }

    private suspend fun loadUserName(): Account? {
        return try {
            _accountNameState.postValue(Initiated)
            val account = redditAPI.userIdentity()
            _accountNameState.postValue(Finished(account))
            account
        } catch (exception: Exception) {
            exception.printStackTrace()
            _loginState.postValue(Error("Unable to load Username"))
            null
        }
    }

    private suspend fun saveAccount(account: Account, token: Token) =
            withContext(Dispatchers.Main) {
                redditClient.createUserAccountAndSetItAsCurrent(account.name, account.icon_img, token)
            }
}

sealed class ResultState<out T> {
    object Uninitiated : ResultState<Nothing>()
    object Initiated : ResultState<Nothing>()
    data class Finished<out T>(val result: T) : ResultState<T>()
}

sealed class LoginState {
    object NotLoggedIn : LoginState()
    object Processing : LoginState()
    data class Error(val errorMessage: String) : LoginState()
    object LoggedIn : LoginState()
}

class LoginVMFactory @Inject constructor(
        private val redditClient: RedditClient,
        private val redditAPI: RedditAPI,
        private val authAPI: AuthAPI,
        private val interceptor: TokenInterceptor,
        private val subredditDAO: SubredditDAO
) : ViewModelProvider.Factory {
    @Suppress("Unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
            if (modelClass.isAssignableFrom(LoginViewModel::class.java))
                LoginViewModel(
                        redditClient,
                        redditAPI,
                        authAPI,
                        interceptor,
                        subredditDAO
                ) as T
            else throw RuntimeException("Unsupported vm requested ${modelClass.simpleName}")
}