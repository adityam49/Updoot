package com.ducktapedapps.updoot.utils

import com.ducktapedapps.updoot.BuildConfig

object Constants {
    const val SHARED_PREFS = "shared_prefs"
    const val LAST_TRENDING_UPDATED_KEY = "last_trending_updated_key"
    const val TOKEN_SHARED_PREFS_KEY = "tokenSharedPrefKey"
    const val USER_TOKEN_REFRESH_KEY = "userTokenRefreshKey"

    const val LOGIN_STATE = "login_state"
    const val DEVICE_ID_KEY = "deviceIdKey"
    const val client_id = BuildConfig.CLIENT_ID
    const val redirect_uri = BuildConfig.REDIRECT_URI
    const val API_BASE_URL = "https://oauth.reddit.com/"
    const val BASE_URL = "https://www.reddit.com"

    //reddit api has no oauth support for trending subreddits api so using a hardcoded endPoint
    const val TRENDING_API_URL = "https://www.reddit.com/api/trending_subreddits/.json"
    const val TOKEN_ACCESS_URL = "https://www.reddit.com/api/v1/access_token"
    const val scopes = "identity,edit,flair,history,modconfig,modflair,modlog,modposts,modwiki,mysubreddits,privatemessages,read,report,save,submit,subscribe,vote,wikiedit,wikiread"
    const val userLess_grantType = "https://oauth.reddit.com/grants/installed_client"
    const val user_grantType = "authorization_code"
    const val user_refresh_grantType = "refresh_token"

    //submissions sorts
    const val HOT = "hot"
    const val TOP = "top"
    const val NEW = "new"
    const val RISING = "rising"
    const val BEST = "best"
    const val CONTROVERSIAL = "controversial"

    //submissions sort time
    const val NOW = "hour"
    const val TODAY = "day"
    const val THIS_WEEK = "week"
    const val THIS_MONTH = "month"
    const val THIS_YEAR = "year"
    const val ALL_TIME = "all"

    const val FRONTPAGE = ""

    const val ACCOUNT_LOGIN_REQUEST_CODE = 99

    const val ACCOUNT_TYPE = "updoot_account"
    const val ANON_USER = "Anonymous"

    //DB names
    const val SUBREDDIT_PREFS_DB = "subreddit_prefs_db"
    const val SUBREDDIT_DB = "subreddit_db"
}

