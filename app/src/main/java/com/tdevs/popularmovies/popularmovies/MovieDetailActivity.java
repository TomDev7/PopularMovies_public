package com.tdevs.popularmovies.popularmovies;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import me.zhanghai.android.materialratingbar.MaterialRatingBar;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class MovieDetailActivity extends AppCompatActivity {

    private ImageView imageViewPoster;
    private TextView textViewDescription;
    private TextView textViewTitle;
    private TextView textViewDate;
    private FloatingActionButton fabFavourite;
    DatabaseWrapper databaseWrapper;
    private OneMovie movie;
    private MaterialRatingBar materialRatingBar;
    List<VideoResult> trailersList;
    List<ReviewResult> reviewsList;
    LinearLayout trailersListLinearLayout;
    LinearLayout reviewsLinearLayout;
    boolean isMovieFavorite;


    public OneMovie getMovie() {
        return movie;
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
        trailersListLinearLayout = (LinearLayout)findViewById(R.id.trailers_linear_layout);
        reviewsLinearLayout = (LinearLayout)findViewById(R.id.reviews_linear_layout);

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
            materialRatingBar.setNumStars(10);
            materialRatingBar.setRating(movie.getVoteAverage().floatValue());

        fabFavourite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (isMovieFavorite)
                {
                    isMovieFavorite = false;
                    fabFavourite.setImageResource(R.drawable.ic_favorite_border_black_24dp);

                    Uri uriToDelete = Contract.FavoriteEntry.CONTENT_URI.buildUpon().appendPath(getMovie().getId().toString()).build();
                    getContentResolver().delete(uriToDelete, null, null);
                }
                else
                {
                    isMovieFavorite = true;
                    fabFavourite.setImageResource(R.drawable.ic_favorite_black_18dp);

                    //add row to table with favorites
                    ContentValues cv = new ContentValues();
                    cv.put(Contract.FavoriteEntry.COLUMN_MOVID, getMovie().getId());
                    cv.put(Contract.FavoriteEntry.COLUMN_TITLE, getMovie().getTitle());

                    getContentResolver().insert(Contract.FavoriteEntry.CONTENT_URI, cv);
                }
            }
        });

        //setting appropriate FAB icon
        fabFavourite.setImageResource(R.drawable.ic_favorite_border_black_24dp);
        isMovieFavorite = false;

        String[] projection = {Contract.FavoriteEntry.COLUMN_MOVID};
        Cursor c = getContentResolver().query(Contract.FavoriteEntry.CONTENT_URI, projection, null, null, null);
        c.moveToFirst();

        while (!c.isAfterLast()) {

            if (c.getString(0).contentEquals(getMovie().getId().toString())) {

                fabFavourite.setImageResource(R.drawable.ic_favorite_black_18dp);
                isMovieFavorite = true;
                break;
            }
            c.moveToNext();
        }

        c.close();


        downlowadTrailers();
        downloadReviews();
    }

    private void downlowadTrailers()
    {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(getString(R.string.movie_db_url))
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RetrofitInterface retrofitInterface = retrofit.create(RetrofitInterface.class);
        Call<VideosResponse> result = retrofitInterface.getVideos(movie.getId().toString(), getString(R.string.movie_db_apikey));


        result.enqueue(new Callback<VideosResponse>() {
            @Override
            public void onResponse(Call<VideosResponse> call, Response<VideosResponse> response) {

                trailersList = response.body().getResults();

                LayoutInflater inflater = (LayoutInflater)getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                for (VideoResult trailer : trailersList) {
                    View row = inflater.inflate(R.layout.trailer_list_item, null);
                    TextView trailerName = (TextView) row.findViewById(R.id.trailer_name_text_view);
                    final TextView trailerKey = (TextView) row.findViewById(R.id.trailer_key_text_view);
                    trailerName.setText(trailer.getName());
                    trailerKey.setText(trailer.getKey());
                    trailersListLinearLayout.addView(row);

                    row.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String videoUri = "https://www.youtube.com/watch?v=" + trailerKey.getText();
                            Intent trailerIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(videoUri));
                            startActivity(trailerIntent);
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<VideosResponse> call, Throwable t) {
                System.out.println("onFailure videos download");
            }
        });
    }

    private void downloadReviews()
    {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(getString(R.string.movie_db_url))
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RetrofitInterface retrofitInterface = retrofit.create(RetrofitInterface.class);
        Call<ReviewsResponse> result = retrofitInterface.getReviews(movie.getId().toString(), getString(R.string.movie_db_apikey));

        result.enqueue(new Callback<ReviewsResponse>(){

            @Override
            public void onResponse(Call<ReviewsResponse> call, Response<ReviewsResponse> response) {

                reviewsList = response.body().getResults();
                LayoutInflater inflater = (LayoutInflater)getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                for (ReviewResult review : reviewsList) {
                    View row = inflater.inflate(R.layout.review_list_item, null);
                    TextView reviewText = (TextView) row.findViewById(R.id.review_text_text_view);
                    TextView reviewAuthor = (TextView) row.findViewById(R.id.review_author_text_view);
                    final TextView reviewUrl = (TextView) row.findViewById(R.id.review_url_text_view);
                    reviewText.setText(review.getContent());
                    reviewAuthor.setText("Review by " + review.getAuthor());
                    reviewUrl.setText(review.getUrl());
                    reviewsLinearLayout.addView(row);

                    row.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent reviewIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(reviewUrl.getText().toString()));
                            startActivity(reviewIntent);
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<ReviewsResponse> call, Throwable t) {
                System.out.println("onFailure downloadReviews");
            }
        });
    }

}
