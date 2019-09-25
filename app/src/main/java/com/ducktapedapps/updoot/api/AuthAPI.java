package com.ducktapedapps.updoot.api;

import com.ducktapedapps.updoot.model.Token;

import io.reactivex.Single;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Url;

public interface AuthAPI {
    @POST()
    @FormUrlEncoded
    Single<Token> getUserLessToken(
            @Url String auth_url,
            @Header("Authorization") String credentials,
            @Field("grant_type") String grant_type,
            @Field("device_id") String device_id
    );

    @FormUrlEncoded
    @POST
    Single<Token> getUserToken(
            @Url String auth_url,
            @Header("Authorization") String credentials,
            @Field("grant_type") String grant_type,
            @Field("code") String code,
            @Field("redirect_uri") String redirect_uri
    );

    @FormUrlEncoded
    @POST()
    Single<Token> getRefreshedToken(
            @Url String auth_url,
            @Header("Authorization") String credentials,
            @Field("grant_type") String grant_type,
            @Field("refresh_token") String refresh_token
    );
}
