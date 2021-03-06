package com.example.android.moviecatalog.Utils;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.example.android.moviecatalog.R;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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
    private static final String MOVIE_PATH = "movie";
    private static final String VIDEOS_PATH = "videos";
    private static final String REVIEWS_PATH = "reviews";

    private static final String API_KEY_PARAM = "api_key";
    private static final String SORT_ORDER_PARAM_KEY = "sort_by";
    private static final String INCLUDE_VIDEO_KEY = "include_video";
    private static final boolean INCLUDE_VIDEO = true;

    public static String buildVideoJsonString(Context context, int movieId){
        Uri.Builder uriBuilder = Uri.parse(BASE_URL_STRING).buildUpon()
                .appendPath(MOVIE_PATH)
                .appendPath(String.valueOf(movieId))
                .appendPath(VIDEOS_PATH)
                .appendQueryParameter(API_KEY_PARAM, context.getString(R.string.MOVIE_DB_API_KEY));

        return uriBuilder.build().toString();
    }

    public static String buildReviewJsonString(Context context, int movieId){
        Uri.Builder uriBuilder = Uri.parse(BASE_URL_STRING).buildUpon()
                .appendPath(MOVIE_PATH)
                .appendPath(String.valueOf(movieId))
                .appendPath(REVIEWS_PATH)
                .appendQueryParameter(API_KEY_PARAM, context.getString(R.string.MOVIE_DB_API_KEY));

        return uriBuilder.build().toString();
    }

    public static URL buildUrl(String urlString){
        if(urlString == null || TextUtils.isEmpty(urlString)) return null;
        URL url;
        try{
            url = new URL(urlString);
        } catch(MalformedURLException e){
            e.printStackTrace();
            return null;
        }
        return url;
    }

    /**
     * Build the url based on the user preferences
     * @return the constructed url
     */
    public static URL buildUrl(Context context, String userChoice, String sortOrder){

        Uri.Builder uriBuilder = Uri.parse(BASE_URL_STRING).buildUpon()
                .appendQueryParameter(API_KEY_PARAM, context.getString(R.string.MOVIE_DB_API_KEY));

        // If the user sorting choice isn't most popular or top rated, append the discover movie path
        // This will allow for sorting order
        if(!(userChoice.equals("movie/popular") || userChoice.equals("movie/top_rated"))){
            uriBuilder.appendPath("discover/movie");
            uriBuilder.appendQueryParameter(SORT_ORDER_PARAM_KEY, userChoice + sortOrder);

            // If the user want the movies returned in reverse chronological order, only show the movies
            // that have the current year as a release date.
            if((userChoice + sortOrder).equals("release_date.desc")){
                String currentYear = new SimpleDateFormat("yyyy").format(Calendar.getInstance().getTime());
                uriBuilder.appendQueryParameter(userChoice + ".lte", currentYear);
            }
        } else {

            switch(userChoice){
                case "movie/popular":
                    // Sort by popularity
                    uriBuilder.appendQueryParameter(SORT_ORDER_PARAM_KEY, "popularity" + sortOrder);
                    break;
                case "movie/top_rated":
                    // Sort by average rating
                    uriBuilder.appendQueryParameter(SORT_ORDER_PARAM_KEY, "vote_average" + sortOrder);
                    break;
            }

            uriBuilder.appendPath(userChoice);
        }

        // Always include video in the JSON result
        uriBuilder.appendQueryParameter(INCLUDE_VIDEO_KEY, String.valueOf(INCLUDE_VIDEO));

        Uri uri = uriBuilder.build();
        URL url = null;
        try{
            // The compiler would always replace "/" with "%2F", making the URL invalid
            // I use .replace to fix this, but seems like a cheap fix to me.
            // I have tried different unicode characters but to no avail.
            url = new URL (uri.toString().replace("%2F", "/"));
        } catch (MalformedURLException e){
            Log.e(LOG_TAG, "Invalid url");
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
            urlConnection.setConnectTimeout(5000);
            urlConnection.setReadTimeout(10000);
            InputStream inputStream = urlConnection.getInputStream();

            Scanner scanner = new Scanner(inputStream);
            scanner.useDelimiter("\\A");

            StringBuilder stringBuilder = new StringBuilder();
            while (scanner.hasNext()) {
                stringBuilder.append(scanner.next());
            }

            return stringBuilder.toString();

        }
        finally{
            urlConnection.disconnect();
        }
    }
}
