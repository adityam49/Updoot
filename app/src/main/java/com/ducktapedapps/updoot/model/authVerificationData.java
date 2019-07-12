package com.ducktapedapps.updoot.model;

public class authVerificationData {
    private String authUrl;
    private String state;

    public authVerificationData(String authUrl, String state) {
        this.authUrl = authUrl;
        this.state = state;
    }

    public String getAuthUrl() {
        return authUrl;
    }

    public void setAuthUrl(String authUrl) {
        this.authUrl = authUrl;
    }

    public String getState() {
        return state;
    }

}
