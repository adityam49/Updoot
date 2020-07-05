package com.ducktapedapps.updoot.ui.login

import android.net.Uri
import androidx.lifecycle.*
import com.ducktapedapps.updoot.api.remote.AuthAPI
import com.ducktapedapps.updoot.api.remote.RedditAPI
import com.ducktapedapps.updoot.model.Token
import com.ducktapedapps.updoot.ui.login.LoginState.*
import com.ducktapedapps.updoot.utils.Constants
import com.ducktapedapps.updoot.utils.accountManagement.RedditClient
import com.ducktapedapps.updoot.utils.accountManagement.TokenInterceptor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject

class LoginViewModel(
        private val redditClient: RedditClient,
        private val redditAPI: RedditAPI,
        private val authAPI: AuthAPI,
        private val interceptor: TokenInterceptor
) : ViewModel() {

    private val _loginState: MutableLiveData<LoginState> = MutableLiveData(NotLoggedIn)
    val loginResult: LiveData<LoginState> = _loginState

    val authUrl: String
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

    private fun getDetails(code: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val fetchedToken = fetchToken(code)
                fetchUserDetailsAndSave(fetchedToken!!)
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
            exception.printStackTrace()
        }
    }
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
        private val interceptor: TokenInterceptor
) : ViewModelProvider.Factory {
    @Suppress("Unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
            if (modelClass.isAssignableFrom(LoginViewModel::class.java))
                LoginViewModel(
                        redditClient,
                        redditAPI,
                        authAPI,
                        interceptor
                ) as T
            else throw RuntimeException("Unsupported vm requested ${modelClass.simpleName}")
}