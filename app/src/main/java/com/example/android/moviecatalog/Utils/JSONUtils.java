package com.example.android.moviecatalog.Utils;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Astraeus on 11/8/2017.
 * This class is meant for methods that work with the json response
 */

public class JSONUtils {
    private static final String TAG = JSONUtils.class.getSimpleName();

    private static final String BASE_POSTER_URL_STRING = "https://image.tmdb.org/t/p/w185";
    public static final String BASE_YOUTUBE_URL_STRING = "http://www.youtube.com/watch?v=";

    public static String parseJsonForVideoId(String jsonVideos) throws JSONException {

        if(jsonVideos == null || TextUtils.isEmpty(jsonVideos)){
            return null;
        }
        String videoId;
        JSONObject jsonRoot = new JSONObject(jsonVideos);
        JSONArray resultsArray = jsonRoot.getJSONArray("results");
        // Get the first video
        JSONObject trailerObject = resultsArray.getJSONObject(0);

        videoId = trailerObject.optString("key");

        return videoId;
    }

    /**
     * Parse the json response
     * At each iteration, add the resulting Movie object to a Movie array and return that array
     */
    public static Movie[] extractFeaturesFromJson(String jsonString) throws JSONException {

        if (jsonString == null || TextUtils.isEmpty(jsonString)) {
            return null;
        }

        Movie[] movieData = null;

        JSONObject rootJsonObject = new JSONObject(jsonString);
        JSONArray resultsArray = rootJsonObject.optJSONArray("results");


        if (resultsArray.length() != 0) {

            movieData = new Movie[resultsArray.length()];

            // Every time this iterates, it has to populate 1 item in the recycler view
            ////////////////////////////////////////////////////////
            // Every movie must contain:
            // Poster
            // Title
            // Release date
            // Plot
            // Rating
            for (int i = 0; i < movieData.length; i++) {
                JSONObject movieObject = resultsArray.optJSONObject(i);


                String movieTitle = movieObject.optString("title");


                double movieRating = movieObject.optDouble("vote_average");


                String moviePlot = movieObject.optString("overview");

                String movieReleaseDate = movieObject.optString("release_date");

                int movieId = movieObject.optInt("id");


                /**
                 * This block of code is for retrieving the movie poster
                 * This might change when using Picasso
                 * Use Picasso when creating the imageView
                 * Picasso.with(context).load("http://i.imgur.com/DvpvklR.png").into(imageView);
                 */
                String moviePosterPath = movieObject.optString("poster_path");
                String moviePosterString = BASE_POSTER_URL_STRING + moviePosterPath;


                Movie movie = new Movie();

                movie.setMovieTitle(movieTitle);
                movie.setMoviePlot(moviePlot);
                movie.setMovieReleaseDate(movieReleaseDate);
                movie.setMovieRating(movieRating);
                movie.setMoviePoster(moviePosterString);
                movie.setMovieId(movieId);


                movieData[i] = movie;

            }
        }
        return movieData;
    }

    public static List<Review> parseJsonForReviews(String reviewsJson){
        if(reviewsJson == null || TextUtils.isEmpty(reviewsJson)){
            return null;
        }

        List<Review> reviewList = new ArrayList<>();
        try{
            JSONObject root = new JSONObject(reviewsJson);
            JSONArray resultsArray = root.getJSONArray("results");

            // Parse the entire results array
            for(int i = 0; i < resultsArray.length(); i++){
                JSONObject reviewObject = resultsArray.getJSONObject(i);

                String author = reviewObject.getString("author");
                String text = reviewObject.getString("content");

                reviewList.add(new Review(author, text));
            }
            return reviewList;
        } catch (JSONException e){
            Log.e(TAG, "Problem parsing reviews json");
            return null;
        }
    }
}
