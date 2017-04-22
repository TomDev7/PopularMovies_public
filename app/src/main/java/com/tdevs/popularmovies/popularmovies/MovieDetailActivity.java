package com.tdevs.popularmovies.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;


public class MovieDetailActivity extends AppCompatActivity {

    private ImageView imageViewPoster;
    private TextView textViewDescription;
    private TextView textViewTitle;
    private TextView textViewDate;
    private RatingBar ratingBarVoteAverage;
    private FloatingActionButton fabFavourite;
    DatabaseWrapper databaseWrapper;
    private OneMovie movie;



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
        ratingBarVoteAverage = (RatingBar)findViewById(R.id.ratingBar_averageVote);
        fabFavourite = (FloatingActionButton)findViewById(R.id.fab);
        databaseWrapper = new DatabaseWrapper(getApplicationContext());

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
            ratingBarVoteAverage.setNumStars(10);
            ratingBarVoteAverage.setRating(movie.getVoteAverage().floatValue());

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
    }
}
