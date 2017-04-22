package com.tdevs.popularmovies.popularmovies;


import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;


public interface RetrofitInterface {
    String movieDBUrl = "popular";

    @GET(movieDBUrl)
    Call<Movies> listRepos(@Query("api_key") String apiKey);
}
