package com.ducktapedapps.updoot.api;

import com.ducktapedapps.updoot.model.tempModel;

import io.reactivex.Observable;
import io.reactivex.Single;
import retrofit2.http.GET;

public interface endPoint {
    @GET("top")
    Single<tempModel> getFrontPage();
}
