package com.ducktapedapps.updoot.utils;

public class constants {
    public static final String USERLESS_TOKEN_EXPIRY_KEY = "userLessTokenExpiryKey";
    public static final String USERLESS_TOKEN_KEY = "userLessTokenKey";
    public static final String TOKEN_SHARED_PREFS_KEY = "tokenSharedPrefKey";
    public static final String DEVICE_ID_KEY = "deviceIdKey";

    public static final String baseUrl = "https://oauth.reddit.com/";
    public static final String token_access_base_url = "https://www.reddit.com/api/v1/";
    public static final String client_id = "9M6Bbt1AAfnoSg";

    public static final String userLess_grantType = "https://oauth.reddit.com/grants/installed_client";

    public enum state {
        SUCCESS, ERROR, LOADING
    }
}
