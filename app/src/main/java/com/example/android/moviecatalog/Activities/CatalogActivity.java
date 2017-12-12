package com.example.android.moviecatalog.Activities;


import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
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
import android.widget.Toast;

import com.example.android.moviecatalog.R;
import com.example.android.moviecatalog.Utils.JSONUtils;
import com.example.android.moviecatalog.Utils.Movie;
import com.example.android.moviecatalog.Utils.MovieAdapter;
import com.example.android.moviecatalog.Utils.FavoriteMovieCursorAdapter;
import com.example.android.moviecatalog.Utils.NetworkUtils;
import com.example.android.moviecatalog.data.MovieContract;

import java.net.URL;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CatalogActivity extends AppCompatActivity implements MovieAdapter.MovieAdapterOnClickHandler, FavoriteMovieCursorAdapter.MovieCursorAdapterOnClickHandler{

    private static final String TAG = CatalogActivity.class.getSimpleName();

    @BindView(R.id.rv_catalog)
    RecyclerView mRecyclerView;
    @BindView(R.id.progress_bar)
    ProgressBar mProgressBar;
    @BindView(R.id.tv_empty_state)
    TextView mEmptyState;


    //TODO: extract the dimens from every file
    //TODO: set the correct label for each activity


    public static MovieAdapter mMovieAdapter;
    public static FavoriteMovieCursorAdapter mFavoriteMovieCursorAdapter;

    public static final String EXTRA_MOVIE_PARCEL = "movieParcel";
    private static final String SEARCH_QUERY_URL_EXTRA = "query";
    private static final String DEFAULT_CATEGORY = "movie/popular";
    private static final String DEFAULT_SORT_ORDER = ".desc";
    private static final int FAVORITES_LOADER_ID = 1;
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

        // Get the user's sorting preferences
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String userChoice = sharedPref.getString(getString(R.string.key_sort_by), DEFAULT_CATEGORY);
        String sortOrder = sharedPref.getString(getString(R.string.key_sort_order), DEFAULT_SORT_ORDER);

        if(!userChoice.equals("favorites")) {
            // Create a new MovieAdapter and set it to the RecyclerView
            mMovieAdapter = new MovieAdapter(this);
            mRecyclerView.setAdapter(mMovieAdapter);
        } else {
            mFavoriteMovieCursorAdapter = new FavoriteMovieCursorAdapter(this);
            mRecyclerView.setAdapter(mFavoriteMovieCursorAdapter);
        }

        /**
         * Instantiate the LoaderCallBacks for the Movie[] return type
          */
        LoaderManager.LoaderCallbacks<Movie[]> movieCallback = new LoaderManager.LoaderCallbacks<Movie[]>() {
            @Override
            public Loader<Movie[]> onCreateLoader(int id, final Bundle bundle) {
                return new AsyncTaskLoader<Movie[]>(getApplicationContext()) {
                    Movie[] mData;

                    @Override
                    protected void onStartLoading(){
                        if(bundle == null){
                            return;
                        }

                        mProgressBar.setVisibility(View.VISIBLE);

                        if(mData != null){
                            deliverResult(mData);
                        } else {
                            forceLoad();
                        }
                    }

                    @Override
                    public Movie[] loadInBackground() {

                        String searchQueryUrlString = bundle.getString(SEARCH_QUERY_URL_EXTRA);
                        Log.v("IMPORTANT", searchQueryUrlString);
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
                    public void deliverResult(Movie[] data){
                        mData = data;
                        super.deliverResult(data);
                    }
                };
            }

            @Override
            public void onLoadFinished(Loader<Movie[]> loader, Movie[] movies) {
                mMovieAdapter.setMovieData(movies);
                mProgressBar.setVisibility(View.GONE);
                mEmptyState.setVisibility(View.GONE);
            }

            @Override
            public void onLoaderReset(Loader<Movie[]> loader) {
                mMovieAdapter.setMovieData(null);
            }
        };

        /**
         * Instantiate the LoaderCallBacks for the Cursor return type
         */
        LoaderManager.LoaderCallbacks<Cursor> favoritesCallback = new LoaderManager.LoaderCallbacks<Cursor>(){

            @Override
            public Loader<Cursor> onCreateLoader(int id, final Bundle bundle) {
                return new AsyncTaskLoader<Cursor>(getApplicationContext()) {
                    Cursor mFavoritesData = null;

                    @Override
                    protected void onStartLoading(){

                        if(mFavoritesData != null){
                            deliverResult(mFavoritesData);
                        } else {
                            forceLoad();
                        }
                    }

                    @Override
                    public Cursor loadInBackground() {

                        try{
                            return getContentResolver().query(
                                    MovieContract.FavoritesEntry.CONTENT_URI,
                                    null,
                                    null,
                                    null,
                                    null
                            );
                        } catch(Exception e){
                            Log.e(TAG, "Failed to load data from favorites");
                            e.printStackTrace();
                            return null;
                        }
                    }

                    @Override
                    public void deliverResult(Cursor data){
                        mFavoritesData = data;
                        super.deliverResult(data);
                    }
                };
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
                mFavoriteMovieCursorAdapter.swapCursor(cursor);
                mProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {
                mFavoriteMovieCursorAdapter.swapCursor(null);
            }
        };


        if (userChoice.equals("favorites")) {
            // Initialize the favorites loader here
            getLoaderManager().initLoader(FAVORITES_LOADER_ID, null, favoritesCallback);

            //TODO: if there are no favorites, show the user a message with instructions on how to add them to favorites

        } else {
            if (isConnected) {
                // If we have internet connectivity, add the user choice in the URL
                // and make the network request
                URL url = NetworkUtils.buildUrl(userChoice, sortOrder);

                Bundle queryBundle = new Bundle();
                queryBundle.putString(SEARCH_QUERY_URL_EXTRA, url.toString());

                getLoaderManager().initLoader(MOVIE_LOADER_ID, queryBundle, movieCallback);

            } else {
                // If we do not have connectivity, set the text of the empty state text view
                // to inform the user of the problem
                mEmptyState.setText(getString(R.string.no_internet_connection_message));
                mEmptyState.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.GONE);

            }
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
            case R.id.settings_delete_all_favorites:
                //TODO: add the confirmation dialog
                getContentResolver().delete(MovieContract.FavoritesEntry.CONTENT_URI, null, null);
                Toast.makeText(getApplicationContext(), "Favorites list has been cleared.", Toast.LENGTH_SHORT).show();

            case R.id.settings_watchlist:
                // Start the WatchlistActivity
                Intent intent = new Intent(CatalogActivity.this, WatchlistActivity.class);
                if(intent.resolveActivity(getPackageManager()) != null){
                    startActivity(intent);
                }
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void onCursorItemClick(Cursor cursor) {
        // Create a new movie object
        Movie movie = new Movie();

        // Store the required data
        String movieTitle = cursor.getString(cursor.getColumnIndex(MovieContract.FavoritesEntry.COLUMN_MOVIE_TITLE));
        String movieReleaseDate = cursor.getString(cursor.getColumnIndex(MovieContract.FavoritesEntry.COLUMN_MOVIE_RELEASE_DATE));
        String moviePlot = cursor.getString(cursor.getColumnIndex(MovieContract.FavoritesEntry.COLUMN_MOVIE_PLOT));
        String moviePoster = cursor.getString(cursor.getColumnIndex(MovieContract.FavoritesEntry.COLUMN_MOVIE_POSTER));
        double movieRating = cursor.getDouble(cursor.getColumnIndex(MovieContract.FavoritesEntry.COLUMN_MOVIE_VOTE_AVERAGE));
        int movieId = cursor.getInt(cursor.getColumnIndex(MovieContract.FavoritesEntry.COLUMN_MOVIE_ID));

        // Add the data from above to the new movie object
        movie.setMovieTitle(movieTitle);
        movie.setMovieReleaseDate(movieReleaseDate);
        movie.setMoviePlot(moviePlot);
        movie.setMoviePoster("file://" + moviePoster);
        movie.setMovieRating(movieRating);
        movie.setMovieId(movieId);

        // Create the intent to start the DetailsActivity
        Intent intent = new Intent(CatalogActivity.this, DetailsActivity.class);
        intent.putExtra(EXTRA_MOVIE_PARCEL, movie);
        if(intent.resolveActivity(getPackageManager()) != null){
            startActivity(intent);
        }

    }
}
