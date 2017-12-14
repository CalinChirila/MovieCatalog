package com.example.android.moviecatalog.Activities;

import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Intent;
import android.content.Loader;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;

import com.example.android.moviecatalog.R;
import com.example.android.moviecatalog.Utils.JSONUtils;
import com.example.android.moviecatalog.Utils.NetworkUtils;
import com.example.android.moviecatalog.Utils.TrailerAdapter;

import java.net.URL;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MovieTrailersActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<String[]>, TrailerAdapter.TrailerAdapterOnClickHandler {
    @BindView(R.id.rv_movie_trailers)
    RecyclerView trailersList;

    private String jsonQuery;
    private static final int TRAILER_LOADER_ID = 4;

    TrailerAdapter mTrailerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_trailers);
        ButterKnife.bind(this);

        mTrailerAdapter = new TrailerAdapter(this);

        jsonQuery = getIntent().getExtras().getString(DetailsActivity.YOUTUBE_SEARCH_EXTRA); // https://api.themoviedb.org/3/movie/<movieId>/videos?q=<API_KEY>
        Bundle bundle = new Bundle();
        bundle.putString(DetailsActivity.YOUTUBE_SEARCH_EXTRA, jsonQuery);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        trailersList.setLayoutManager(layoutManager);
        trailersList.setHasFixedSize(true);

        trailersList.setAdapter(mTrailerAdapter);

        getLoaderManager().initLoader(TRAILER_LOADER_ID, bundle, this);
    }

    /**
     *  The loader needs to create an array of String,
     *  each string containing the youtube video extension for the trailer
     *  Ex: ProEFvXvW6c
     */
    @Override
    public Loader<String[]> onCreateLoader(int i, final Bundle bundle) {
        return new AsyncTaskLoader<String[]>(getApplicationContext()) {
            String trailerJSON[] = null;

            @Override
            protected void onStartLoading() {
                if (bundle == null) {
                    return;
                }

                if (trailerJSON != null) {
                    deliverResult(trailerJSON);
                } else {
                    forceLoad();
                }
            }

            @Override
            public String[] loadInBackground() {
                String youtubeSearchQuery = bundle.getString(DetailsActivity.YOUTUBE_SEARCH_EXTRA);  // https://api.themoviedb.org/3/movie/<movieId>/videos?q=<API_KEY>

                if (youtubeSearchQuery == null || TextUtils.isEmpty(youtubeSearchQuery)) {
                    return null;
                }
                String[] trailerIds = null;
                try {
                    URL trailerResponseUrl = new URL(youtubeSearchQuery);
                    String trailerJsonResponse = NetworkUtils.makeHttpRequest(trailerResponseUrl);
                    // Parse the json result returning the array of strings
                    trailerIds = JSONUtils.parseJsonForVideoIds(trailerJsonResponse);

                } catch (Exception e) {
                    e.printStackTrace();
                }
                return trailerIds;
            }

        };
    }

    @Override
    public void onLoadFinished(Loader<String[]> loader, String[] result) {
        mTrailerAdapter.setTrailerData(result);
    }

    @Override
    public void onLoaderReset(Loader<String[]> loader) {

    }

    @Override
    public void onTrailerClick(String youtubeString) {
        // Launch youtube intent
        Log.v("IMPORTANT", "Trailer clicked");
        Intent youtubeIntent = new Intent(Intent.ACTION_VIEW);
        String youtubeTrailer = JSONUtils.BASE_YOUTUBE_URL_STRING + youtubeString;
        Uri youtubeUri = Uri.parse(youtubeTrailer);
        youtubeIntent.setData(youtubeUri);
        if(youtubeIntent.resolveActivity(getPackageManager()) != null){
            startActivity(youtubeIntent);
        }
    }
}
