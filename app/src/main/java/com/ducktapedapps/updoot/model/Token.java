package com.ducktapedapps.updoot.model;

public class Token {
    private String access_token;
    private String refresh_token;
    private String token_type;
    private long expires_in;
    private long absolute_expiry;
    private String scope;

    public Token(String access_token,long absolute_expiry){
        this.access_token = access_token;
        this.absolute_expiry = absolute_expiry;
    }
    public void setAbsolute_expiry(long absolute_expiry) {
        this.absolute_expiry = absolute_expiry;
    }

    public String getAccess_token() {
        return access_token;
    }

    public long getAbsolute_expiry() {
        return absolute_expiry;
    }

    public long getExpires_in() {
        return expires_in;
    }

    @Override
    public String toString() {
        return "Token{" +
                "access_token='" + access_token + '\'' +
                ", refresh_token='" + refresh_token + '\'' +
                ", token_type='" + token_type + '\'' +
                ", expires_in=" + expires_in +
                ", absolute_expiry=" + absolute_expiry +
                ", scope='" + scope + '\'' +
                '}';
    }
}
