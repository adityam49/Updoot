package com.ducktapedapps.updoot.api;

import com.ducktapedapps.updoot.model.thing;

import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface endPoint {
    @GET("top")
    Single<thing> getFrontPage(
            @Query("after") String after
    );


}
