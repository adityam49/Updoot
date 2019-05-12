package com.ducktapedapps.updoot.api;

import com.ducktapedapps.updoot.model.Token;

import io.reactivex.Single;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface login {
    @FormUrlEncoded
    @POST("access_token")
    Single<Token> getUserLessToken(
            @Field("grant_type") String grant_type,
            @Field("device_id") String device_id
    );

}
