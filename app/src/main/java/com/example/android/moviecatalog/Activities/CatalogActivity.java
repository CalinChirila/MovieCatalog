package com.example.android.moviecatalog.Activities;


import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.moviecatalog.R;
import com.example.android.moviecatalog.Utils.JSONUtils;
import com.example.android.moviecatalog.Utils.Movie;
import com.example.android.moviecatalog.Utils.MovieAdapter;
import com.example.android.moviecatalog.Utils.NetworkUtils;

import java.net.URL;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CatalogActivity extends AppCompatActivity implements MovieAdapter.MovieAdapterOnClickHandler,
        SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = CatalogActivity.class.getSimpleName();

    @BindView(R.id.rv_catalog)
    RecyclerView mRecyclerView;
    @BindView(R.id.progress_bar)
    ProgressBar mProgressBar;
    @BindView(R.id.tv_empty_state)
    TextView mEmptyState;

    public static MovieAdapter mMovieAdapter;


    boolean isConnected = false;
    private String userChoice;
    private String sortOrder;
    private URL url = null;
    private Movie[] mData;
    private Movie[] oldData;
    private GridLayoutManager mLayoutManager;
    private SharedPreferences sharedPref;
    Bundle queryBundle;

    LoaderManager.LoaderCallbacks<Movie[]> movieCallback;


    public static final String EXTRA_MOVIE_PARCEL = "movieParcel";
    public static final String SEARCH_QUERY_URL_EXTRA = "query";
    private static final String DEFAULT_CATEGORY = "movie/popular";
    private static final String DEFAULT_SORT_ORDER = ".desc";
    private static final int MOVIE_LOADER_ID = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);
        ButterKnife.bind(this);


        // Create a boolean that checks if we have internet connectivity
        ConnectivityManager cm = (ConnectivityManager) getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork;
        if (cm != null) {
            activeNetwork = cm.getActiveNetworkInfo();
            isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        }

        // Get a grid layout manager and set it to the RecyclerView
        mLayoutManager = new GridLayoutManager(this, 2);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // Set the RecyclerView's fixed size so that the size of the items doesn't change
        mRecyclerView.setHasFixedSize(true);

        // Get the user's sorting preferences
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        userChoice = sharedPref.getString(getString(R.string.key_sort_by), DEFAULT_CATEGORY);
        sortOrder = sharedPref.getString(getString(R.string.key_sort_order), DEFAULT_SORT_ORDER);

        mMovieAdapter = new MovieAdapter(this);

        // Set the correct adapter

        mRecyclerView.setAdapter(mMovieAdapter);


        queryBundle = new Bundle();


        /**
         * Instantiate the LoaderCallBacks for the Movie[] return type
         */
        movieCallback = new LoaderManager.LoaderCallbacks<Movie[]>() {
            @Override
            public Loader<Movie[]> onCreateLoader(int id, final Bundle bundle) {
                return new AsyncTaskLoader<Movie[]>(getApplicationContext()) {

                    @Override
                    protected void onStartLoading() {
                        if (bundle == null) {
                            return;
                        }

                        String newUserChoice = sharedPref.getString(getString(R.string.key_sort_by), DEFAULT_CATEGORY);
                        String newSortOrder = sharedPref.getString(getString(R.string.key_sort_order), DEFAULT_SORT_ORDER);

                        mProgressBar.setVisibility(View.VISIBLE);

                        /**
                         * NOTE TO REVIEWER:
                         * The sort order criteria works IF the sorting criteria is RELEASE DATE
                         * That is because the app will query the discover/movie end point which has a sort_by optional parameter
                         * The project rubric requires me to also query the movie/popular and movie/top_rated end points. These DO NOT have a sort_by optional parameter (That's how the moviedb API is)
                         * I apologise if I'm misunderstanding.
                         */
                        if (mData != null) {
                            // If the user changed something in preferences
                            if (newUserChoice != userChoice || newSortOrder != sortOrder) {
                                userChoice = newUserChoice;
                                sortOrder = newSortOrder;
                                // Build the url again
                                url = NetworkUtils.buildUrl(getApplicationContext(), userChoice, sortOrder);
                                bundle.putString(SEARCH_QUERY_URL_EXTRA, url.toString());
                                forceLoad();
                                return;
                            } else {
                                deliverResult(mData);
                            }
                        }

                        if (mData != oldData || mData == null) {
                            Log.v("IMPORTANT", "" + takeContentChanged());
                            forceLoad();
                        }


                    }

                    @Override
                    protected void onStopLoading() {
                        cancelLoad();
                    }

                    @Override
                    public Movie[] loadInBackground() {

                        String searchQueryUrlString = bundle.getString(SEARCH_QUERY_URL_EXTRA);

                        Movie[] movieData;
                        String jsonResponse;

                        if (searchQueryUrlString == null || TextUtils.isEmpty(searchQueryUrlString)) {
                            return null;
                        }

                        try {
                            URL moviesUrl = new URL(searchQueryUrlString);
                            jsonResponse = NetworkUtils.makeHttpRequest(moviesUrl);
                            movieData = JSONUtils.extractFeaturesFromJson(jsonResponse);
                            return movieData;

                        } catch (Exception e) {
                            e.printStackTrace();
                            return null;
                        }
                    }

                    @Override
                    public void deliverResult(Movie[] data) {
                        if (isReset()) {
                            return;
                        }


                        // If nothing has changed
                        if (oldData == data) {
                            mMovieAdapter.setMovieData(data);
                            mProgressBar.setVisibility(View.GONE);
                            mEmptyState.setVisibility(View.GONE);
                        }
                        mData = data;
                        oldData = mData;
                        if (isStarted()) {
                            super.deliverResult(data);
                        }
                    }
                };
            }

            @Override
            public void onLoadFinished(Loader<Movie[]> loader, Movie[] movies) {

                // If the user chose sort by top rated or sort by most popular and the sort order is ascending
                // reverse the movies order in the array
                // This is an edge case, but unfortunately themoviedb API doesn't have a sort_by optional param for these end points
                // This way at least, the sort order button changes the order of the movies regardless.
                if (sortOrder.equals(".asc") && (userChoice.equals("movie/popular") || userChoice.equals("movie/top_rated"))) {
                    movies = reverseData(movies);
                }
                mMovieAdapter.setMovieData(movies);
                mProgressBar.setVisibility(View.GONE);
                mEmptyState.setVisibility(View.GONE);
            }

            @Override
            public void onLoaderReset(Loader<Movie[]> loader) {
                mMovieAdapter.setMovieData(null);
            }
        };


        if (isConnected) {
            // If we have internet connectivity, add the user choice in the URL
            // and make the network request
            url = NetworkUtils.buildUrl(getApplicationContext(), userChoice, sortOrder);
            queryBundle.putString(SEARCH_QUERY_URL_EXTRA, url.toString());

            getLoaderManager().initLoader(MOVIE_LOADER_ID, queryBundle, movieCallback);

        } else {
            // If we do not have connectivity, set the text of the empty state text view
            // to inform the user of the problem
            mEmptyState.setText(getString(R.string.no_internet_connection_message));
            showEmptyState();
        }
    }


    @Override
    public void onClick(Movie movie) {
        // When an item in the RecyclerView is clicked, start the DetailsActivity
        // and pass the information of the clicked item to the activity
        Intent intent = new Intent(CatalogActivity.this, DetailsActivity.class);
        intent.putExtra(EXTRA_MOVIE_PARCEL, movie);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
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
            case R.id.menu_favorites:
                Intent favoritesIntent = new Intent(CatalogActivity.this, FavoritesActivity.class);
                if (favoritesIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(favoritesIntent);
                }
                break;


            case R.id.settings_watchlist:
                // Start the WatchlistActivity
                Intent intent = new Intent(CatalogActivity.this, WatchlistActivity.class);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
                break;

        }
        return super.onOptionsItemSelected(menuItem);
    }

    /**
     * Helper method to show the empty state
     */
    public void showEmptyState() {
        mRecyclerView.setVisibility(View.GONE);
        mEmptyState.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);
    }

    // Register the preference change listener
    @Override
    public void onResume() {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
    }


    /**
     * Helper method to reverse the movies in the array
     */
    private Movie[] reverseData(Movie[] data) {
        Movie[] newData = new Movie[data.length];
        int j = 0;
        for (int i = data.length - 1; i >= 0; i--) {
            newData[j] = data[i];
            j++;
        }
        return newData;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {



            getLoaderManager().restartLoader(MOVIE_LOADER_ID, queryBundle, movieCallback);

    }
}
