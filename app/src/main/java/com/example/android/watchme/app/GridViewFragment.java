package com.example.android.watchme.app;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * A placeholder fragment containing a simple view.
 */
public class GridViewFragment extends Fragment {

    public GridViewAdapter mGridViewAdapter;
    public ArrayList mGridViewData;

    // Will contain the raw JSON response as a string.
    String movieJsonStr = null;

    public GridViewFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.gridview_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            FetchMovieTask fetchMovie = new FetchMovieTask();
            fetchMovie.execute("popular");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        String[] mTempData = {"http://i.imgur.com/DvpvklR.png", "http://i.imgur.com/DvpvklR.png"};

        mGridViewData = new ArrayList<String>(Arrays.asList(mTempData));

        mGridViewAdapter = new GridViewAdapter(getContext(), R.layout.grid_item_layout, mGridViewData);


        GridView gridView = (GridView) rootView.findViewById(R.id.gridview);
        gridView.setAdapter(mGridViewAdapter);

        FetchMovieTask fetchMovie = new FetchMovieTask();
        fetchMovie.execute("popular");

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                //Toast.makeText(getActivity(), "" + position, Toast.LENGTH_SHORT).show();


                Intent intent = new Intent(getActivity(), DetailActivity.class)
                        .putExtra(Intent.EXTRA_TEXT, movieJsonStr);
                startActivity(intent);
            }
        });
        return rootView;
    }



    public class FetchMovieTask extends AsyncTask<String, Void, Uri[]> {

        private final String LOG_TAG = FetchMovieTask.class.getSimpleName();






        // Extract the poster path from JSON and return the complete url of the poster image.
        public Uri[] getPostersFromPicasso(String movieJsonStr)
                throws JSONException {

            final String POSTER_BASE_URI = "http://image.tmdb.org/t/p";
            final String POSTER_SIZE = "w342";

            JSONObject movieJson = new JSONObject(movieJsonStr);
            JSONArray movieArray = movieJson.getJSONArray("results");

            String[] posterUrls = new String[movieArray.length()];
            Uri[] posterUri = new Uri[movieArray.length()];

            for (int i = 0; i < movieArray.length(); i++) {

                JSONObject movieData = movieArray.getJSONObject(i);
                posterUrls[i] = movieData.getString("poster_path");


                posterUri[i] = Uri.parse(POSTER_BASE_URI).buildUpon()
                        .appendPath(POSTER_SIZE)
                        .appendEncodedPath(posterUrls[i])
                        .build();

            }
            return posterUri;
        }

        @Override
        protected Uri[] doInBackground(String... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String movieJsonStr = null;

            try {

                final String MOVIEDB_BASE_URL = "http://api.themoviedb.org/3";
                final String REFERENCE_PATH = "movie";
                final String CATEGORY_PATH = params[0];
                final String APIKEY_PARAM = "api_key";

                Uri builtUri = Uri.parse(MOVIEDB_BASE_URL).buildUpon()
                        .appendPath(REFERENCE_PATH)
                        .appendPath(CATEGORY_PATH)
                        .appendQueryParameter(APIKEY_PARAM, BuildConfig.MOVIE_DB_API_KEY)
                        .build();

                URL url = new URL(builtUri.toString());

                Log.v(LOG_TAG, url.toString());

                // Create the request to API, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();

                if (inputStream == null) {
                    return null;
                }

                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                if ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    return null;
                }

                movieJsonStr = buffer.toString();

                Log.v(LOG_TAG, movieJsonStr);


            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in
                // attempting to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                return getPostersFromPicasso(movieJsonStr);

            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Uri[] result) {
            if (result != null) {
                //mGridViewData.clear();

                mGridViewData.clear();
                mGridViewData = new ArrayList(result.length);
                for (Uri moviePoster : result) {

                    mGridViewData.add(moviePoster.toString());

                    Log.v(" Data ", mGridViewData.toString());
                }

                mGridViewAdapter.setData(mGridViewData);

            }
        }
    }
}

