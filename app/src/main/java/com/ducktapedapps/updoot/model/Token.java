package com.ducktapedapps.updoot.model;

import androidx.annotation.NonNull;

public class Token {
    private String access_token;
    private String refresh_token;
    private long absolute_expiry;

    //for userless token
    public Token(String access_token, long absolute_expiry) {
        this.access_token = access_token;
        this.absolute_expiry = absolute_expiry;
    }

    //for user token
    public Token(String access_token, String refresh_token, long absolute_expiry) {
        this.access_token = access_token;
        this.refresh_token = refresh_token;
        this.absolute_expiry = absolute_expiry;
    }

    public String getRefresh_token() {
        return refresh_token;
    }

    public String getAccess_token() {
        return access_token;
    }

    public long getAbsolute_expiry() {
        return absolute_expiry;
    }


    @NonNull
    @Override
    public String toString() {
        return "Token{" +
                "access_token='" + access_token + '\'' +
                ", refresh_token='" + refresh_token + '\'' +
                ", absolute_expiry=" + absolute_expiry +
                '}';
    }
}
