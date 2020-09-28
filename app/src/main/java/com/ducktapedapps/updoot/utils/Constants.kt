package com.ducktapedapps.updoot.utils

import com.ducktapedapps.updoot.BuildConfig

object Constants {
    const val USER_TOKEN_REFRESH_KEY = "userTokenRefreshKey"
    const val USER_ICON_KEY: String = "user_icon_key"

    const val CURRENT_ACCOUNT_NAME = "current_account_name"
    const val CACHED_ACCOUNT_COUNT = "cached_account_count"

    const val DEVICE_ID_KEY = "deviceIdKey"
    const val client_id = BuildConfig.CLIENT_ID
    const val redirect_uri = BuildConfig.REDIRECT_URI
    const val API_BASE_URL = "https://oauth.reddit.com/"
    const val BASE_URL = "https://www.reddit.com"

    //reddit api has no oauth support for trending subreddits api so using a hardcoded endPoint
    const val TRENDING_API_URL = "https://www.reddit.com/api/trending_subreddits/.json"
    const val TOKEN_ACCESS_URL = "https://www.reddit.com/api/v1/access_token"
    const val TOKEN_REVOKE_URL = "https://www.reddit.com/api/v1/revoke_token"
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

    const val UPDOOT_ACCOUNT = "updoot_account"
    const val ADD_ACCOUNT = "Add account"
    const val ANON_USER = "Anonymous"

    const val UPDOOT_DB = "updoot_db"

    //notification
    const val NOTIFICATION_CHANNEL_ID = "com.ducktapedapps.updoot.notification"

    const val DEBOUNCE_TIME_OUT = 400L
}

