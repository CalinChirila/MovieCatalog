package com.example.android.moviecatalog.Activities;

import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

import com.example.android.moviecatalog.R;
import com.example.android.moviecatalog.Utils.JSONUtils;
import com.example.android.moviecatalog.Utils.NetworkUtils;
import com.example.android.moviecatalog.Utils.Review;
import com.example.android.moviecatalog.Utils.ReviewAdapter;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ReviewActivity extends AppCompatActivity{
    @BindView(R.id.list_reviews)
    ListView reviewsList;

    private ReviewAdapter mReviewAdapter;
    private static final int REVIEW_LOADER_ID = 4;
    private int mMovieId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);
        ButterKnife.bind(this);

        mReviewAdapter = new ReviewAdapter(this, new ArrayList<Review>());

        Intent intent = getIntent();
        mMovieId = intent.getExtras().getInt(DetailsActivity.MOVIE_ID_EXTRA);

        getLoaderManager().initLoader(REVIEW_LOADER_ID, null, reviewCallback);



    }

    LoaderManager.LoaderCallbacks<List<Review>> reviewCallback = new LoaderManager.LoaderCallbacks<List<Review>>(){
        @Override
        public Loader<List<Review>> onCreateLoader(int id, Bundle bundle) {
            return new AsyncTaskLoader<List<Review>>(getApplicationContext()) {
                @Override
                protected void onStartLoading(){
                    forceLoad();
                }

                @Override
                public List<Review> loadInBackground() {
                    List<Review> reviewList;
                    String reviewUrlString = NetworkUtils.buildReviewJsonString(mMovieId);
                    URL reviewUrl = NetworkUtils.buildUrl(reviewUrlString);
                    String reviewJsonResponse;
                    try{
                        reviewJsonResponse = NetworkUtils.makeHttpRequest(reviewUrl);
                        reviewList = JSONUtils.parseJsonForReviews(reviewJsonResponse);

                        return reviewList;
                    } catch(IOException e){
                        e.printStackTrace();
                        return null;
                    }
                }

            };
        }

        @Override
        public void onLoadFinished(Loader<List<Review>> loader, List<Review> reviews) {
            // Use the adapter to arrange the reviews in the list(provided)
            mReviewAdapter.clear();
            if(reviews != null && !reviews.isEmpty()){
                mReviewAdapter.addAll(reviews);
            }
            reviewsList.setAdapter(mReviewAdapter);
        }

        @Override
        public void onLoaderReset(Loader<List<Review>> loader) {
            mReviewAdapter.clear();
        }
    };

}
