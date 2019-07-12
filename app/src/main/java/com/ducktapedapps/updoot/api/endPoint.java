package com.ducktapedapps.updoot.api;

import com.ducktapedapps.updoot.model.thing;

import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface endPoint {

    @GET("/api/v1/me")
    Single<String> getUserIdentity();

    @GET("{sort}")
    Single<thing> getFrontPage(
            @Path("sort") String userId,
            @Query("after") String after
    );


}
