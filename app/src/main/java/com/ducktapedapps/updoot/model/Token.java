package com.ducktapedapps.updoot.model;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Token implements Serializable {
    private static final String TAG = "Token";
    private String access_token;
    private String refresh_token;

    @SerializedName("expires_in")
    private long absolute_expiry;

    private String token_type;

    //for userless token
    public Token(String access_token, long absolute_expiry, String type) {
        new Token(access_token, null, absolute_expiry, token_type);
    }

    public Token() {
    }

    //for user token
    public Token(String access_token, String refresh_token, long absolute_expiry, String token_type) {
        this.access_token = access_token;
        this.refresh_token = refresh_token;
        this.absolute_expiry = absolute_expiry;
        this.token_type = token_type;
    }

    public String getToken_type() {
        return token_type;
    }

    public void setAbsolute_expiry() {
        this.absolute_expiry = this.absolute_expiry * 1000 + System.currentTimeMillis();
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
