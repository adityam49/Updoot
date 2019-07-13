package com.ducktapedapps.updoot.utils;

public class constants {
    public static final String TOKEN_SHARED_PREFS_KEY = "tokenSharedPrefKey";

    public static final String USERLESS_TOKEN_EXPIRY_KEY = "userLessTokenExpiryKey";
    public static final String USERLESS_TOKEN_KEY = "userLessTokenKey";

    public static final String USER_TOKEN_KEY = "userTokenKey";
    public static final String USER_TOKEN_REFRESH_KEY = "userTokenRefreshKey";
    public static final String USER_TOKEN_EXPIRY_KEY = "userTokenExpiryKey";

    public static final String LOGIN_STATE = "login_state";
    public static final String DEVICE_ID_KEY = "deviceIdKey";
    public static final String client_id = "jW0kYyiXq1hJyQ";
    public static final String redirect_uri = "https://github.com/am-2x49/Updoot";
    public static final String API_BASE_URL = "https://oauth.reddit.com/";
    public static final String TOKEN_ACCESS_URL = "https://www.reddit.com/api/v1/access_token";
    public static final String scopes = "identity,edit,flair,history,modconfig,modflair,modlog,modposts,modwiki,mysubreddits,privatemessages,read,report,save,submit,subscribe,vote,wikiedit,wikiread";
    public static final String userLess_grantType = "https://oauth.reddit.com/grants/installed_client";
    public static final String user_grantType = "authorization_code";
    public static final String user_refresh_grantType = "refresh_token";

    //states
    public static final String SUCCESS_STATE = "success_state";
    public static final String LOADING_STATE = "loading_state";

    //login states
    public static final String LOGGED_OUT_STATE = "logged_out_state";
    public static final String LOGGED_IN_STATE = "logged_in_state";

    //submissions sorts
    public static final String HOT = "hot";
    public static final String TOP = "top";
    public static final String NEW = "new";
    public static final String RISING = "rising";
    public static final String BEST = "best";
    public static final String CONTROVERSIAL = "controversial";

    public static final int ACCOUNT_LOGIN_REQUEST_CODE = 99;


}
