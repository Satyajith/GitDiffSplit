package com.procore.prdiffs.network;

import com.procore.prdiffs.model.PullRequest;

import java.util.List;

import io.reactivex.Single;
import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Url;

/* interface with all necessary api calls */
public interface ApiInterface {

    @GET("boto/boto3/pulls")
    Single<List<PullRequest>> getPullRequests(@Query("state") String state,
                                              @Query("sort") String created,
                                              @Query("direction") String direction);

    @GET
    Single<ResponseBody> getDiff(@Url String url);
}
