package com.procore.prdiffs.network;

import com.procore.prdiffs.model.PullRequest;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface ApiInterface {

    @GET("googlesamples/android-sunflower/pulls")
    Call<List<PullRequest>> getPullRequests(@Query("state") String state,
                                            @Query("sort") String created,
                                            @Query("direction") String direction);

    @GET
    Call<ResponseBody> getDiff(@Url String url);
}
