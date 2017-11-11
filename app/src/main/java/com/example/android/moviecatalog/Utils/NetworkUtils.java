package com.example.android.moviecatalog.Utils;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

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

    //TODO: insert your api key here
    private static final String API_KEY_VALUE = "[YOUR_API_KEY]";

    private static final String API_KEY_PARAM = "api_key";

    /**
     * Build the url based on the user preferences
     * @return the constructed url
     */
    public static URL buildUrl(String userChoice){

        Uri uri = Uri.parse(BASE_URL_STRING).buildUpon()
                .appendPath(userChoice)
                .appendQueryParameter(API_KEY_PARAM, API_KEY_VALUE).build();

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
