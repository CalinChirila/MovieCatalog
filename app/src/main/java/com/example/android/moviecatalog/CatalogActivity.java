package com.example.android.moviecatalog;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.example.android.moviecatalog.Utils.NetworkUtils;

import java.io.IOException;
import java.net.URL;

public class CatalogActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        URL url = NetworkUtils.buildUrl();

        MovieAsyncTask task = new MovieAsyncTask();
        task.execute(url);

    }

    public class MovieAsyncTask extends AsyncTask<URL, Void, String>{

        @Override
        protected String doInBackground(URL... urls) {
            String jsonResponse = null;

            try{
                jsonResponse = NetworkUtils.makeHttpRequest(urls[0]);

            } catch (IOException e){
                e.printStackTrace();
            }

            return jsonResponse;
        }

        @Override
        protected void onPostExecute(String jsonResponse){
            // After the loading is finished, display the results in their respective views
        }
    }
}
