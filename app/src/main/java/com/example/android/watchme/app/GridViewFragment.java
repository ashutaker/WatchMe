package com.example.android.watchme.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
    public String mMovieStr;
    public final String LOG_TAG = GridViewFragment.class.getSimpleName();

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
    public void onStart() {
        super.onStart();
        fetchMovies();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.gridview_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            fetchMovies();
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

        fetchMovies();

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                //Toast.makeText(getActivity(), "" + position, Toast.LENGTH_SHORT).show();


                Intent intent = new Intent(getActivity(), DetailActivity.class)
                        .putExtra(Intent.EXTRA_TEXT, getSelectedMovie(position));

                startActivity(intent);
            }
        });
        return rootView;
    }

    public void fetchMovies() {

        SharedPreferences prefCategory = PreferenceManager.getDefaultSharedPreferences(getContext());
        String category = prefCategory.getString(getString(R.string.pref_sort_key), getString(R.string.pref_sort_default));

        FetchMovieTask fetchMovie = new FetchMovieTask();
        fetchMovie.execute(category);
    }

    //Extract the Movie ID of the item selected, to be passed to DetailActivity
    public String getSelectedMovie(int position){

        String movieID;
        try {
            JSONObject movieJson = new JSONObject(mMovieStr);

            JSONObject movieDetailStr = movieJson.getJSONArray("results").getJSONObject(position);

            movieID = movieDetailStr.getString("id");
            //String[] split = movieID.split(" ");
            //Log.v(LOG_TAG + " Movie ID ", movieDetailStr.toString());
            return movieID;

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public class FetchMovieTask extends AsyncTask<String, Void, String> {

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
        protected String doInBackground(String... params) {

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

                //Log.v(LOG_TAG, url.toString());

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

                //Log.v(LOG_TAG, mMovieJsonStr);


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


            return movieJsonStr;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {

                mMovieStr = result;
                Uri[] posterUrls;
                try {
                    JSONObject movieJson = new JSONObject(result);
                    JSONArray movieArray = movieJson.getJSONArray("results");
                    posterUrls = getPostersFromPicasso(result);

                    mGridViewData.clear();
                    mGridViewData = new ArrayList(posterUrls.length);

                    for (Uri moviePoster : posterUrls) {
                        mGridViewData.add(moviePoster.toString());
                        //Log.v(" Urls", mGridViewData.toString());
                    }

                    mGridViewAdapter.setData(mGridViewData);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }
    }
}

