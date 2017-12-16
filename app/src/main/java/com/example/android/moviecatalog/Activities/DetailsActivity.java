package com.example.android.moviecatalog.Activities;

import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.moviecatalog.R;
import com.example.android.moviecatalog.Utils.Movie;
import com.example.android.moviecatalog.Utils.NetworkUtils;
import com.example.android.moviecatalog.data.MovieContract;
import com.example.android.moviecatalog.data.MovieContract.FavoritesEntry;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailsActivity extends AppCompatActivity {

    public static final String YOUTUBE_SEARCH_EXTRA = "youtube";
    public static final String MOVIE_ID_EXTRA = "movieID";

    @BindView(R.id.tv_details_movie_title)
    TextView mDetailsMovieTitle;
    @BindView(R.id.iv_details_movie_poster)
    ImageView mDetailsMoviePoster;
    @BindView(R.id.tv_details_movie_plot)
    TextView mDetailsMoviePlot;
    @BindView(R.id.tv_details_movie_release_date)
    TextView mDetailsMovieReleaseDate;
    @BindView(R.id.tv_details_movie_vote_average)
    TextView mDetailsMovieVoteAverage;
    @BindView(R.id.button_add_to_favorites)
    ImageButton mAddToFavorites;
    @BindView(R.id.button_add_to_watch_list)
    Button mAddToWatchlist;
    @BindView(R.id.button_watch_trailer)
    Button mWatchTrailerButton;
    @BindView(R.id.button_reviews)
    Button mReviewsButton;

    private boolean isFavorite;
    private Uri mUri;
    private Movie movieParcel;

    private int mMovieId;
    private String mMovieTitle;
    private String mMoviePlot;
    private String mMovieReleaseDate;
    private double mMovieVoteAverage;
    private String mMoviePoster;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        ButterKnife.bind(this);

        final Intent intent = getIntent();

        /**
         * If the intent has a parcelable object named @movieParcel:
         * Create a new instance of the Movie class, unwrapping the parcelable we sent via Intent
         * Use the custom class getter methods to set the values accordingly
         */
        if (intent.hasExtra("movieParcel")) {

            movieParcel = intent.getParcelableExtra(CatalogActivity.EXTRA_MOVIE_PARCEL);

            // Set the movie poster
            mMoviePoster = movieParcel.getMoviePoster();

            Picasso.with(getApplicationContext())
                    .load(mMoviePoster)
                    .placeholder(R.drawable.posternotfound)
                    .error(R.drawable.posternotfound)
                    .into(mDetailsMoviePoster);

            // Set the movie title
            mMovieTitle = movieParcel.getMovieTitle();
            mDetailsMovieTitle.setText(mMovieTitle);

            // Set the movie plot
            mMoviePlot = movieParcel.getMoviePlot();
            mDetailsMoviePlot.setText(mMoviePlot);

            // Set the movie release date
            mMovieReleaseDate = movieParcel.getMovieReleaseDate();
            mDetailsMovieReleaseDate.setText(mMovieReleaseDate);

            // Set the movie average score
            mMovieVoteAverage = movieParcel.getMovieRating();
            mDetailsMovieVoteAverage.setText(String.valueOf(mMovieVoteAverage));

            // Set the movie ID
            mMovieId = movieParcel.getMovieId();

        }
        // Initialize the value of the boolean "isFavorite" here
        isFavorite = checkIfFavorite(mMovieTitle);
        if (isFavorite) {
            mAddToFavorites.setImageResource(android.R.drawable.btn_star_big_on);
        } else {
            mAddToFavorites.setImageResource(android.R.drawable.btn_star_big_off);
        }

        /**
         * Set what happens when the add to favorites button is clicked
         */
        mAddToFavorites.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {

                // If the movie isn't in the favorites list, add it.
                // Replace the boolean with a check to see if this movie is already in the favorites list or not.
                // Change the button icon and show a toast message.

                if (!isFavorite) {
                    // Add the movie to favorites
                    ContentValues values = new ContentValues();
                    values.put(FavoritesEntry.COLUMN_MOVIE_TITLE, mMovieTitle);
                    values.put(FavoritesEntry.COLUMN_MOVIE_VOTE_AVERAGE, mMovieVoteAverage);
                    values.put(FavoritesEntry.COLUMN_MOVIE_PLOT, mMoviePlot);
                    values.put(FavoritesEntry.COLUMN_MOVIE_RELEASE_DATE, mMovieReleaseDate);
                    values.put(FavoritesEntry.COLUMN_MOVIE_ID, mMovieId);

                    // Create the bitmap
                    Bitmap posterBitmap;
                    InputStream inputStream = null;

                    try {
                        URL moviePosterUrl = new URL(mMoviePoster);
                        inputStream = moviePosterUrl.openStream();
                        posterBitmap = BitmapFactory.decodeStream(inputStream);
                        mMoviePoster = saveImageToInternalStorage(posterBitmap);

                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            if (inputStream != null) {
                                inputStream.close();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    values.put(FavoritesEntry.COLUMN_MOVIE_POSTER, mMoviePoster);

                    mUri = getContentResolver().insert(FavoritesEntry.CONTENT_URI, values);

                    mAddToFavorites.setImageResource(android.R.drawable.btn_star_big_on);
                    Toast.makeText(getApplicationContext(), getString(R.string.movie_added_to_favorites), Toast.LENGTH_SHORT).show();
                    isFavorite = true;
                } else {

                    getContentResolver().delete(mUri, null, null);
                    mAddToFavorites.setImageResource(android.R.drawable.btn_star_big_off);
                    Toast.makeText(getApplicationContext(), getString(R.string.movie_removed_from_favorites), Toast.LENGTH_SHORT).show();
                    isFavorite = false;

                }
            }
        });

        /**
         * Set what happens when the Add to Watchlist button is clicked
         */
        mAddToWatchlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!isOnWatchlist(mMovieTitle)) {
                    ContentValues values = new ContentValues();
                    values.put(MovieContract.WatchlistEntry.COLUMN_MOVIE_TITLE, mMovieTitle);
                    values.put(MovieContract.WatchlistEntry.COLUMN_MOVIE_POSTER, mMoviePoster);

                    getContentResolver().insert(MovieContract.WatchlistEntry.CONTENT_URI, values);
                    Toast.makeText(getApplicationContext(), "Movie added to watchlist.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Movie is already in the watchlist", Toast.LENGTH_SHORT).show();
                }
            }
        });

        /**
         * Set what happens when the Watch Trailer Button is clicked
         */
        mWatchTrailerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Launch the MovieTrailersActivity
                Intent launchTrailersActivityIntent = new Intent(DetailsActivity.this,
                        MovieTrailersActivity.class);

                Movie selectedMovie = intent.getParcelableExtra(CatalogActivity.EXTRA_MOVIE_PARCEL);
                int movieId = selectedMovie.getMovieId();                                             // Ex: 347882
                String videosJson = NetworkUtils.buildVideoJsonString(getApplicationContext(), movieId);                       // https://api.themoviedb.org/3/movie/<movieId>/videos?q=<API_KEY>

                // Pass in the web address for the JSON that contains trailer information
                launchTrailersActivityIntent.putExtra(YOUTUBE_SEARCH_EXTRA, videosJson);

                if(launchTrailersActivityIntent.resolveActivity(getPackageManager()) != null){
                    startActivity(launchTrailersActivityIntent);
                }
            }
        });

        /**
         * Set what happens when the reviews button is clicked
         */
        mReviewsButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                // Set the movie id into a bundle and set it with the intent
                Intent reviewsIntent = new Intent(DetailsActivity.this, ReviewActivity.class);
                reviewsIntent.putExtra(MOVIE_ID_EXTRA, mMovieId);
                if(reviewsIntent.resolveActivity(getPackageManager()) != null){
                    startActivity(reviewsIntent);
                }
            }
        });
    }

    /**
     * Save the provided bitmap image to the local storage and return the path
     *
     * @param bitmap = the image to ba saved
     * @return = the file path
     */
    private String saveImageToInternalStorage(Bitmap bitmap) {
        ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());

        File directory = contextWrapper.getDir("imageDir", Context.MODE_PRIVATE);

        // Create a unique name for the image
        Long timestampLong = System.currentTimeMillis();
        String imageName = "FAV" + timestampLong + ".jpg";

        File imagePath = new File(directory, imageName);

        FileOutputStream outputStream = null;

        try {
            outputStream = new FileOutputStream(imagePath);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return imagePath.getAbsolutePath();
    }

    /**
     * Helper method that checks if a movie is a favorite or not.
     */
    private boolean checkIfFavorite(String movieTitle) {
        boolean isFavorite = false;
        Context context = getApplicationContext();

        String[] projection = {FavoritesEntry.COLUMN_MOVIE_TITLE, FavoritesEntry._ID};
        String selection = FavoritesEntry.COLUMN_MOVIE_TITLE + "=?";
        String[] selectionArgs = new String[]{movieTitle};

        Cursor cursor = context.getContentResolver().query(FavoritesEntry.CONTENT_URI, projection, selection, selectionArgs, null);
        String title;

        // Check the favorites database and see if you can find matching movie titles
        // If there's a match, the movie is favorite.
        if (cursor != null) {
            while (cursor.moveToNext()) {
                title = cursor.getString(cursor.getColumnIndex(projection[0]));
                if (title.equals(movieTitle)) {
                    isFavorite = true;
                    // Get the movie id and save it in mUri
                    long mId = cursor.getLong(cursor.getColumnIndex(FavoritesEntry._ID));
                    mUri = Uri.parse(FavoritesEntry.CONTENT_URI + "/" + String.valueOf(mId));
                    break;
                }
            }
            cursor.close();
        }

        return isFavorite;
    }

    @Override
    public void onSaveInstanceState(Bundle bundle){
        super.onSaveInstanceState(bundle);
        bundle.putParcelable(CatalogActivity.EXTRA_MOVIE_PARCEL, movieParcel);
    }

    @Override
    public void onRestoreInstanceState(Bundle bundle){
        super.onRestoreInstanceState(bundle);
        movieParcel = bundle.getParcelable(CatalogActivity.EXTRA_MOVIE_PARCEL);
    }

    /**
     * Helper method the check if the provided movie is in the watchlist
     */
    public boolean isOnWatchlist(String movieTitle){
        boolean isOnWatchlist = false;
        String[] projection = {MovieContract.WatchlistEntry.COLUMN_MOVIE_TITLE};
        Cursor cursor = getContentResolver().query(MovieContract.WatchlistEntry.CONTENT_URI, projection, null, null, null);
        while(cursor.moveToNext()) {
            String watchlistTitle = cursor.getString(cursor.getColumnIndex(MovieContract.WatchlistEntry.COLUMN_MOVIE_TITLE));
            if(watchlistTitle.equals(movieTitle)){
                isOnWatchlist = true;
            }
        }

        cursor.close();
        return isOnWatchlist;
    }
}
