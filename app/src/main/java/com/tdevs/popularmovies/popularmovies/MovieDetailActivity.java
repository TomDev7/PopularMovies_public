package com.tdevs.popularmovies.popularmovies;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import me.zhanghai.android.materialratingbar.MaterialRatingBar;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;


public class MovieDetailActivity extends AppCompatActivity {

    private ImageView imageViewPoster;
    private TextView textViewDescription;
    private TextView textViewTitle;
    private TextView textViewDate;
    private FloatingActionButton fabFavourite;
    DatabaseWrapper databaseWrapper;
    private OneMovie movie;
    private MaterialRatingBar materialRatingBar;
    ListView trailersListView;



    public OneMovie getMovie() {
        return movie;
    }

    public void setMovieFavourite(boolean favourite) {
        movie.setFavourite(favourite);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        imageViewPoster = (ImageView)findViewById(R.id.imageView_poster);
        textViewDate = (TextView)findViewById(R.id.textView_date);
        textViewTitle = (TextView)findViewById(R.id.textView_title);
        textViewDescription = (TextView)findViewById(R.id.textView_description);
        materialRatingBar = (MaterialRatingBar)findViewById(R.id.ratingBar_averageVote);
        fabFavourite = (FloatingActionButton)findViewById(R.id.fab);
        databaseWrapper = new DatabaseWrapper(getApplicationContext());
        trailersListView = (ListView)findViewById(R.id.trailers_list_view);

        Intent invokingIntent = getIntent();

        int selectedMovieId = 0;
        if (invokingIntent.hasExtra("selected_movie_id"))
        {
            selectedMovieId = invokingIntent.getIntExtra("selected_movie_id", 1);  //it is checked if  invokingIntent hasExtra ov this particular name, so theoretically there is no threat of making any use of the default number '1', which would cause a misidentification of the movie
            databaseWrapper.open();
            movie = databaseWrapper.getMovie(selectedMovieId);
            databaseWrapper.close();
        }
        else
        {
            finish();
        }

        databaseWrapper.open();
        OneMovie movie =  databaseWrapper.getMovie(selectedMovieId);
        databaseWrapper.close();

            textViewTitle.setText(movie.getTitle());
            textViewDescription.setText(movie.getOverview());
            Picasso.with(getApplicationContext()).load("http://image.tmdb.org/t/p/" + "w500" + movie.getPosterPath()).into(imageViewPoster);
            textViewDate.setText(movie.getReleaseDate());
            //ratingBarVoteAverage.setNumStars(10);
            //ratingBarVoteAverage.setRating(movie.getVoteAverage().floatValue());
        materialRatingBar.setNumStars(10);
        materialRatingBar.setRating(movie.getVoteAverage().floatValue());

        fabFavourite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (getMovie().getFavourite())
                {
                    setMovieFavourite(false);
                    fabFavourite.setImageResource(R.drawable.ic_favorite_border_black_24dp);
                }
                else
                {
                    setMovieFavourite(true);
                    fabFavourite.setImageResource(R.drawable.ic_favorite_black_18dp);
                }

                databaseWrapper.open();
                databaseWrapper.setMovieFavourite(getMovie());
                databaseWrapper.close();
            }
        });

        if (movie.getFavourite())
        {
            fabFavourite.setImageResource(R.drawable.ic_favorite_black_18dp);
        }
        else
        {
            fabFavourite.setImageResource(R.drawable.ic_favorite_border_black_24dp);
        }


        System.out.println("Beginning videos download");
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(getString(R.string.movie_db_url))
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RetrofitInterface retrofitInterface = retrofit.create(RetrofitInterface.class);
        Call<VideosResponse> result = retrofitInterface.getVideos(movie.getId().toString(), getString(R.string.movie_db_apikey));

        System.out.println("Call: " + result.toString());

        result.enqueue(new Callback<VideosResponse>() {
            @Override
            public void onResponse(Call<VideosResponse> call, Response<VideosResponse> response) {

                System.out.println("response id: " + response.body().getId());
                System.out.println("response result 1: " + response.body().getResults().get(0).getName());

                final TrailersListAdapter trailersAdapter = new TrailersListAdapter(response.body().getResults(), getApplicationContext());
                trailersListView.setAdapter(trailersAdapter);

                trailersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                        String videoUri = "https://www.youtube.com/watch?v=" + trailersAdapter.getTrailerKey(i);
                        Intent trailerIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(videoUri));
                        startActivity(trailerIntent);
                    }
                });
            }

            @Override
            public void onFailure(Call<VideosResponse> call, Throwable t) {
                System.out.println("onFailure videos download");
            }
        });
    }

}
