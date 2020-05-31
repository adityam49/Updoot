package com.ducktapedapps.updoot.utils.accountManagement

import android.util.Log
import com.ducktapedapps.updoot.model.Token
import com.ducktapedapps.updoot.utils.Constants
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

//Adds token to endpoint api caches current user session info
@Singleton
class TokenInterceptor @Inject constructor() : Interceptor {
    var sessionToken: String? = null
    
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val requestBuilder = request.newBuilder()
        if (request.url.toString() != Constants.TRENDING_API_URL && request.header("Authorization") == null) { //Providing credentials
            Log.i(TAG, " intercept: no auth header present thus adding new ones")
            if (sessionToken != null) {
                Log.i(TAG, "intercept: using token $sessionToken")
                requestBuilder.addHeader("Authorization", "bearer $sessionToken")
            } else {
                Log.e(TAG, " intercept: session token is null ")
            }
        }
        return chain.proceed(requestBuilder.build())
    }

    companion object {
        private const val TAG = "TokenInterceptor"
    }
}