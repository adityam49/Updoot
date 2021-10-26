package com.ducktapedapps.updoot.login

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.ducktapedapps.updoot.backgroundWork.SubscriptionSyncWorker
import com.ducktapedapps.updoot.backgroundWork.enqueueOneOffSubscriptionsSyncFor
import com.ducktapedapps.updoot.data.remote.AuthAPI
import com.ducktapedapps.updoot.data.remote.RedditAPI
import com.ducktapedapps.updoot.data.remote.model.Account
import com.ducktapedapps.updoot.data.remote.model.Token
import com.ducktapedapps.updoot.login.LoginState.*
import com.ducktapedapps.updoot.login.ResultState.Finished
import com.ducktapedapps.updoot.login.ResultState.Running
import com.ducktapedapps.updoot.utils.Constants
import com.ducktapedapps.updoot.utils.accountManagement.TokenInterceptor
import com.ducktapedapps.updoot.utils.accountManagement.UpdootAccountManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject


@HiltViewModel
class LoginViewModel @Inject constructor(
    private val redditAPI: RedditAPI,
    private val authAPI: AuthAPI,
    private val interceptor: TokenInterceptor,
    private val updootAccountManager: UpdootAccountManager,
) : ViewModel() {

    private val _loginState: MutableStateFlow<LoginState> = MutableStateFlow(NotLoggedIn)
    val loginState: StateFlow<LoginState> = _loginState.onEach {
        when (it) {
            NotLoggedIn -> Unit
            ObservingUrl -> Unit
            is FetchingToken -> when (it.token) {
                Running -> Unit
                is Finished<Token> -> loadUserName(it.token.value)
            }
            is FetchingUserName -> when (it.account) {
                Running -> Unit
                is Finished -> _loginState.value = LoggedIn
            }
            is Error -> Unit
            LoggedIn -> delay(3_000)
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, NotLoggedIn)

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
                _loginState.value = ObservingUrl
                uri.getQueryParameter("code")?.let {
                    viewModelScope.launch { fetchToken(it) }
                }
            } else {
                _loginState.value = Error("Access denied")
            }
        }
    }

    private suspend fun fetchToken(code: String) {
        try {
            _loginState.value = FetchingToken(Running)
            val token: Token = authAPI.getUserToken(code = code)
            token.setAbsoluteExpiry()
            interceptor.sessionToken = token.access_token
            _loginState.value = FetchingToken(Finished(token))
        } catch (exception: Exception) {
            exception.printStackTrace()
            _loginState.value = Error(exception.message!!)
        }
    }

    private suspend fun loadUserName(token: Token) {
        try {
            _loginState.value = FetchingUserName(Running)
            val account = redditAPI.userIdentity()
            _loginState.value = FetchingUserName(Finished(account))
            saveAccount(account, token)
        } catch (exception: Exception) {
            exception.printStackTrace()
            _loginState.value = Error(exception.message!!)
        }
    }

    private suspend fun saveAccount(account: Account, token: Token) {
        updootAccountManager.createAccount(account.name, account.icon_img, token)
    }
}

sealed class LoginState {
    object NotLoggedIn : LoginState()
    object ObservingUrl : LoginState()
    data class FetchingToken(val token: ResultState<Token>) : LoginState()
    data class FetchingUserName(val account: ResultState<Account>) : LoginState()
    data class Error(val errorMessage: String) : LoginState()
    object LoggedIn : LoginState()
}

sealed class ResultState<out T> {
    object Running : ResultState<Nothing>()
    data class Finished<T>(val value: T) : ResultState<T>()
}