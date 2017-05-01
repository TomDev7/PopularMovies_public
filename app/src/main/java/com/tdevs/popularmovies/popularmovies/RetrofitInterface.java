package com.tdevs.popularmovies.popularmovies;


import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;


public interface RetrofitInterface {
    String movieDBUrlPopularity = "popular";
    String movieDBUrlRating = "top_rated";


    @GET(movieDBUrlPopularity)
    Call<Movies> listReposP(@Query("api_key") String apiKey);

    @GET(movieDBUrlRating)
    Call<Movies> listReposR(@Query("api_key") String apiKey);

    @GET("{movid}/videos")
    Call<VideosResponse> getVideos(@Path("movid") String movid, @Query("api_key") String apiKey);

    @GET("{movid}/reviews")
    Call<ReviewsResponse> getReviews(@Path("movid") String movid, @Query("api_key") String apiKey);
}
