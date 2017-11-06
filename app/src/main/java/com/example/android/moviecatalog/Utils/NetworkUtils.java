package com.example.android.moviecatalog.Utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;


/**
 * Created by Astraeus on 11/5/2017.
 */

public class NetworkUtils {

    private static final String LOG_TAG = NetworkUtils.class.getSimpleName();

    /**
     * Build the Request Url. It must take the user preference into consideration
     */
    private static final String BASE_URL_STRING = "https://api.themoviedb.org/3/";

    private static final String BASE_POSTER_URL_STRING = "https://image.tmdb.org/t/p/w500";

    private static final String[] USER_PREFERENCE_QUERY =
            new String[]{ "movie/popular", "movie/top_rated" };

    private static final String QUERY_MOVIE_PARAM = "movie";

    private static final String API_KEY_VALUE = "dcff6e16c2f2a33f47ea1b8b3b668911";

    private static final String API_KEY_PARAM = "api_key";

    /**
     * Build the url based on the user preferences
     * @return the constructed url
     */
    // Aceasta metoda ar trebui sa accepte ca si parametru user preference
    public static URL buildUrl(){

        // Citeste user preference si in functie de asta atribuie valoarea String userChoice
        // Deocamdata o sa fie automat movie/popular

        String userChoice = USER_PREFERENCE_QUERY[0];
        Log.v("IMPORTANT", "User chose: " + userChoice);

        Uri uri = Uri.parse(BASE_URL_STRING).buildUpon()
                .appendPath(userChoice)
                .appendQueryParameter(API_KEY_PARAM, API_KEY_VALUE).build();


        URL url = null;
        try{
            url = new URL (uri.toString().replace("%2F", "/")); // This .replace thing really looks like a cheap-shot to me, but it was really frustrating.

            Log.v("IMPORTANT", "URL is: " + url);
        } catch (MalformedURLException e){
            Log.e(LOG_TAG, "Invalid url: " + url);
        }

        return url;
    }

    // Connect to the internet according to the given url and return the JSON response
    public static String makeHttpRequest (URL url) throws IOException {

        // If the provided url is null or empty, exit early
        if(url == null || TextUtils.isEmpty(url.toString())){
            return null;
        }

        HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();

        try{
            InputStream inputStream = urlConnection.getInputStream();

            Scanner scanner = new Scanner(inputStream);
            scanner.useDelimiter("\\A");

            StringBuilder stringBuilder = new StringBuilder();
            while (scanner.hasNext()) {
                stringBuilder.append(scanner.next());
            }

            Log.v("IMPORTANT", "Json string: " + stringBuilder.toString());
            return stringBuilder.toString();



        }
        finally{
            urlConnection.disconnect();
        }
    }

    /**
     * Parse the json response
     * You need to obtain the movie title, image, genre, rating
     */
    public static void extractFeaturesFromJson(String jsonString) throws JSONException{

        if(jsonString == null || TextUtils.isEmpty(jsonString)){
            return;
        }

        JSONObject rootJsonObject = new JSONObject(jsonString);
        JSONArray resultsArray = rootJsonObject.optJSONArray("results");

        if(resultsArray.length() != 0){

            // Every time this iterates, it has to populate 1 item in the recycler view
            ////////////////////////////////////////////////////////
            // Every movie must contain:
            // Poster
            // Title
            // Genre
            // Release date
            // Plot
            // Rating
            for (int i = 0; i < resultsArray.length(); i++){
                JSONObject movieObject = resultsArray.optJSONObject(i);

                String movieTitle = movieObject.optString("title");
                double movieRating = movieObject.optDouble("vote_average");
                String moviePlot = movieObject.optString("overview");
                String movieReleaseDate = movieObject.optString("release_date");

                //TODO: remains to be seen how to get the genres
                //TODO: figure out how to put objects into the recycler view


                /**
                 * This block of code is for retrieving the movie poster
                 */
                String moviePosterPath = movieObject.optString("poster_path");
                String moviePosterString = BASE_POSTER_URL_STRING + moviePosterPath;
                URL moviePosterURL = null;
                Bitmap moviePoster = null;
                try {
                    moviePosterURL = new URL(moviePosterString);
                    moviePoster = BitmapFactory.decodeStream(moviePosterURL.openStream());
                } catch (Exception e){
                    e.printStackTrace();
                }



            }
        }


    }

}
