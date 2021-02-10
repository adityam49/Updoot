package com.ducktapedapps.updoot.data.remote

import com.ducktapedapps.updoot.data.remote.model.Token
import com.ducktapedapps.updoot.utils.Constants
import okhttp3.Credentials
import retrofit2.http.*

interface AuthAPI {
    @POST
    @FormUrlEncoded
    suspend fun getUserLessToken(
            @Url auth_url: String = Constants.TOKEN_ACCESS_URL,
            @Header("Authorization") credentials: String = Credentials.basic(Constants.client_id, ""),
            @Field("grant_type") grant_type: String = Constants.userLess_grantType,
            @Field("device_id") device_id: String
    ): Token

    @FormUrlEncoded
    @POST
    suspend fun getRefreshedToken(
            @Url auth_url: String = Constants.TOKEN_ACCESS_URL,
            @Header("Authorization") credentials: String = Credentials.basic(Constants.client_id, ""),
            @Field("grant_type") grant_type: String = Constants.user_refresh_grantType,
            @Field("refresh_token") refresh_token: String
    ): Token

    @FormUrlEncoded
    @POST
    suspend fun getUserToken(
            @Url auth_url: String = Constants.TOKEN_ACCESS_URL,
            @Header("Authorization") credentials: String = Credentials.basic(Constants.client_id, ""),
            @Field("grant_type") grant_type: String = Constants.user_grantType,
            @Field("code") code: String,
            @Field("redirect_uri") redirect_uri: String = Constants.redirect_uri
    ): Token

    @FormUrlEncoded
    @POST
    suspend fun logout(
            @Url auth_url: String = Constants.TOKEN_REVOKE_URL,
            @Header("Authorization") credentials: String = Credentials.basic(Constants.client_id, ""),
            @Field("token") refresh_token: String,
            @Field("token_type") token_type: String = "bearer"
    ) : retrofit2.Response<Unit>
}