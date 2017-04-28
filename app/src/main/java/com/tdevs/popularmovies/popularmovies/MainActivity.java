package com.tdevs.popularmovies.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements GreenAdapter.ListItemClickListener {

    private GreenAdapter mAdapter;
    private RecyclerView mNumberList;
    private SharedPreferences sharedPreferences;
    private DatabaseWrapper databaseWrapper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences("PopularMovies_preferences", MODE_PRIVATE);
        databaseWrapper = new DatabaseWrapper(getApplicationContext());
        mNumberList = (RecyclerView)findViewById(R.id.rv_numbers);

        int postersInARow = 2;
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
        {
            postersInARow = 4;
        }

        GridLayoutManager gridManager = new GridLayoutManager(this, postersInARow);
        mNumberList.setLayoutManager(gridManager);

        mAdapter = new GreenAdapter(4, getApplicationContext(), this);
        mNumberList.setAdapter(mAdapter);

        databaseWrapper.open();
        int numOfMoviesInDb = databaseWrapper.getNumOfMovies();
        databaseWrapper.close();

        if (numOfMoviesInDb != 0)
        {
            refreshList();
        }
        else
        {
            downloadData();
        }

        if (savedInstanceState != null) {
            int listPosition = savedInstanceState.getInt("listPosition", 0);
            mNumberList.scrollToPosition(listPosition);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        int firstVisibleItemPosition = ((GridLayoutManager) mNumberList.getLayoutManager()).findLastVisibleItemPosition();
        outState.putInt("listPosition", firstVisibleItemPosition);

        super.onSaveInstanceState(outState);
    }

    private void downloadData()
    {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        if(netInfo != null && netInfo.isConnectedOrConnecting())
        {
            Retrofit myRetrofit = new Retrofit.Builder().baseUrl(getString(R.string.movie_db_url)).addConverterFactory(GsonConverterFactory.create()).build();
            RetrofitInterface myService = myRetrofit.create(RetrofitInterface.class);

            Call<Movies> call = myService.listReposP(getString(R.string.movie_db_apikey));

            if (sharedPreferences.getInt("sortby", 1) == 2)
            {
                call = myService.listReposR(getString(R.string.movie_db_apikey));
            }
            //the code above makes sure something (movies sorted by popularity) is presented to the user even if something goes wrong
            //and there is no valid sort option chosen
            //if there are more sorting options, new if's will be added

            call.enqueue(new Callback<Movies>() {
                @Override
                public void onResponse(Call<Movies> call, Response<Movies> response) {

                    List<OneMovie> movies = response.body().getResults();

                    databaseWrapper.open();
                    databaseWrapper.removeAllMovies();

                    for (int i = 0; i < movies.size(); i++)
                    {
                        databaseWrapper.insertOneMovie(movies.get(i));
                    }

                    databaseWrapper.close();

                    //There was a solution applied to save only new movies that did not occur in the local database
                    //But as the project requires the data (different data) to be downloaded by each choice of sorting type
                    //The solution turned out to be an overkill
                    /*databaseWrapper.open();
                    for (int i = 0; i < movies.size(); i++)
                    {
                        OneMovie a = databaseWrapper.getMovie(movies.get(i).getId());

                        if (a == null)
                        {
                            databaseWrapper.insertOneMovie(movies.get(i));
                        }
                    }

                    databaseWrapper.close();*/

                    refreshList();
                }

                @Override
                public void onFailure(Call<Movies> call, Throwable t) {
                    //System.out.println("onFailure. " + t.toString());
                }
            });
        }
        else
        {
            Toast.makeText(this, R.string.no_internet_prompt, Toast.LENGTH_LONG).show();
        }
    }

    public void refreshList()
    {
        databaseWrapper.open();

        mAdapter.changeItemCount(databaseWrapper.getNumOfMovies());
        mAdapter.notifyDataSetChangedOverride();

        databaseWrapper.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menInflater = getMenuInflater();
        menInflater.inflate(R.menu.action_bar_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_refresh)
        {
            Toast.makeText(this, "Refreshing...", Toast.LENGTH_SHORT).show();

            downloadData();

            return true;
        }
        else if (item.getItemId() == R.id.action_sortby_popularity)
        {
            sharedPreferences.edit().putInt("sortby", 1).commit();  //1 = sort by popularity
            downloadData();
        }
        else if (item.getItemId() == R.id.action_sortby_rating)
        {
            sharedPreferences.edit().putInt("sortby", 2).commit();  //2 = sort by rating
            downloadData();
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onListItemClick(int selectedMovieId) {

        Intent detailActivity = new Intent(MainActivity.this, MovieDetailActivity.class);
        detailActivity.putExtra("selected_movie_id", selectedMovieId);

        startActivity(detailActivity);
    }
}

