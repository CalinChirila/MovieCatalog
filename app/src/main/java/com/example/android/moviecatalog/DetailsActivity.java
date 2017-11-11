package com.example.android.moviecatalog;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.moviecatalog.Utils.Movie;
import com.squareup.picasso.Picasso;

public class DetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        TextView mDetailsMovieTitle = findViewById(R.id.tv_details_movie_title);
        ImageView mDetailsMoviePoster = findViewById(R.id.iv_details_movie_poster);
        TextView mDetailsMoviePlot = findViewById(R.id.tv_details_movie_plot);
        TextView mDetailsMovieReleaseDate = findViewById(R.id.tv_details_movie_release_date);
        TextView mDetailsMovieVoteAverage = findViewById(R.id.tv_details_movie_vote_average);

        Intent intent = getIntent();

        /**
         * If the intent has a parcelable object named @movieParcel:
         * Create a new instance of the Movie class, unwrapping the parcelable we sent via Intent
         * Use the custom class getter methods to set the values accordingly
         */
        if (intent.hasExtra("movieParcel")) {

            Movie movieParcel = intent.getParcelableExtra("movieParcel");

            // Set the movie poster
            Picasso.with(getApplicationContext())
                    .load(movieParcel.getMoviePoster())
                    .into(mDetailsMoviePoster);

            // Set the movie title
            String movieTitle = movieParcel.getMovieTitle();
            mDetailsMovieTitle.setText(movieTitle);

            // Set the movie plot
            String moviePlot = movieParcel.getMoviePlot();
            mDetailsMoviePlot.setText(moviePlot);

            // Set the movie release date
            String movieReleaseDate = movieParcel.getMovieReleaseDate();
            mDetailsMovieReleaseDate.setText(movieReleaseDate);

            // Set the movie average score
            double movieVoteAverage = movieParcel.getMovieRating();
            mDetailsMovieVoteAverage.setText(String.valueOf(movieVoteAverage));

        }
    }
}
