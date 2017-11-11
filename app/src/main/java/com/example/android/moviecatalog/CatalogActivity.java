package com.example.android.moviecatalog;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.moviecatalog.Utils.JSONUtils;
import com.example.android.moviecatalog.Utils.Movie;
import com.example.android.moviecatalog.Utils.MovieAdapter;
import com.example.android.moviecatalog.Utils.NetworkUtils;

import java.net.URL;

public class CatalogActivity extends AppCompatActivity implements MovieAdapter.MovieAdapterOnClickHandler {

    private static MovieAdapter mMovieAdapter;

    private static final String DEFAULT = "movie/popular";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Find the views
        // We could use the ButterKnife library, but we aren't working with that many views.
        RecyclerView mRecyclerView = findViewById(R.id.rv_catalog);
        ProgressBar mProgressBar = findViewById(R.id.progress_bar);
        TextView mEmptyState = findViewById(R.id.tv_empty_state);

        // Create a boolean that checks if we have internet connectivity
        ConnectivityManager cm = (ConnectivityManager) getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork;
        boolean isConnected = false;
        if (cm != null) {
            activeNetwork = cm.getActiveNetworkInfo();
            isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        }

        // Get a grid layout manager and set it to the RecyclerView
        GridLayoutManager mLayoutManager = new GridLayoutManager(this, 2);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // Set the RecyclerView's fixed size so that the size of the items doesn't change
        mRecyclerView.setHasFixedSize(true);

        // Create a new MovieAdapter and set it to the RecyclerView
        mMovieAdapter = new MovieAdapter(this);
        mRecyclerView.setAdapter(mMovieAdapter);

        // Get the user's sorting order preference
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String userChoice = sharedPref.getString(getString(R.string.key_sort_by), DEFAULT);

        if (isConnected) {
            // If we have internet connectivity, add the user choice in the URL
            // and make the network request
            URL url = NetworkUtils.buildUrl(userChoice);
            MovieAsyncTask task = new MovieAsyncTask();
            task.execute(url);

            // Hide the progress bar when loading is finished
            mProgressBar.setVisibility(View.GONE);
        } else {
            // If we do not have connectivity, set the text of the empty state text view
            // to inform the user of the problem
            mEmptyState.setText(getString(R.string.no_internet_connection_message));
            mEmptyState.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.GONE);

        }
    }

    @Override
    public void onClick(Movie movie) {
        // When an item in the RecyclerView is clicked, start the DetailsActivity
        // and pass the information of the clicked item to the activity
        Intent intent = new Intent(CatalogActivity.this, DetailsActivity.class);
        intent.putExtra("movieParcel", movie);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    public static class MovieAsyncTask extends AsyncTask<URL, Void, Movie[]> {  // Will use Loaders in the next stage

        @Override
        protected Movie[] doInBackground(URL... urls) {
            String jsonResponse;
            Movie[] movieData = null;

            try {
                jsonResponse = NetworkUtils.makeHttpRequest(urls[0]);
                movieData = JSONUtils.extractFeaturesFromJson(jsonResponse);

            } catch (Exception e) {
                e.printStackTrace();
            }

            return movieData;
        }

        @Override
        protected void onPostExecute(Movie[] movieData) {
            // After the loading is finished, display the results in their respective views
            mMovieAdapter.setMovieData(movieData);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {

        switch (menuItem.getItemId()) {
            case R.id.settings_menu:
                // Start the SettingsActivity
                Intent menuIntent = new Intent(CatalogActivity.this, SettingsActivity.class);
                if (menuIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(menuIntent);
                }
                break;
        }
        return super.onOptionsItemSelected(menuItem);
    }
}
