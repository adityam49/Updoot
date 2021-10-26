package com.ducktapedapps.updoot.login

import android.graphics.Bitmap
import android.net.Uri
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.ducktapedapps.navigation.Event
import com.ducktapedapps.navigation.Event.AuthEvent.NewAccountAdded
import com.ducktapedapps.updoot.common.PageLoading
import com.ducktapedapps.updoot.login.LoginState.*

private const val TAG = "LoginScreen"

@Composable
fun LoginScreen(publishEvent: (Event) -> Unit) {
    val viewModel = hiltViewModel<LoginViewModel>()
    val loginState = viewModel.loginState.collectAsState()
    when (val state = loginState.value) {
        is Error -> publishEvent(Event.ToastEvent(state.errorMessage))
        is FetchingToken, is FetchingUserName -> LoadingScreen()
        NotLoggedIn, ObservingUrl -> LoginScreen(
            loginState = loginState.value,
            parseUrl = viewModel::parseUrl,
            loginUrl = viewModel.authUrl
        )
        LoggedIn -> publishEvent(NewAccountAdded)
    }
}

@Composable
private fun LoadingScreen() {
    Box(contentAlignment = Alignment.Center) {
        Text("Fetching account details")
    }
}

@Composable
private fun LoginScreen(
    loginState: LoginState,
    parseUrl: (Uri?) -> Unit,
    loginUrl: String,
) {
    Box(Modifier.fillMaxSize()) {
        ComposableWebView(loginState = loginState, loginUrl = loginUrl, parseUrl = parseUrl)
    }
}

@Composable
fun ComposableWebView(
    loginState: LoginState,
    loginUrl: String,
    parseUrl: (Uri?) -> Unit
) {
    LaunchedEffect(loginUrl) {
        CookieManager.getInstance().apply {
            removeAllCookies(null)
            flush()
        }
    }

    AndroidView(
        modifier = Modifier
            .padding(bottom = 32.dp)
            .fillMaxSize(),
        factory = { context ->
            WebView(context).apply {
                loadUrl(loginUrl)
                webViewClient = object : WebViewClient() {
                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                        parseUrl(Uri.parse(url))
                    }
                }
                if (loginState is ObservingUrl) stopLoading()
            }
        },
    )
}