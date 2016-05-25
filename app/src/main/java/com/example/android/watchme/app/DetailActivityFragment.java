package com.example.android.watchme.app;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

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

/**
 * A placeholder fragment containing a simple view.
 */

//TODO http://api.themoviedb.org/3/movie/244786/reviews?api_key=b523e1577f1f9b16d3fab2a699fe8b85

//TODO http://api.themoviedb.org/3/movie/244786/videos?api_key=b523e1577f1f9b16d3fab2a699fe8b85


public class DetailActivityFragment extends Fragment {

    public final String LOG_TAG = DetailActivity.class.getSimpleName();

    private ArrayAdapter<String> mTrailerListAdapter;

    private ArrayAdapter<String> mReviewListAdapter;

    public DetailActivityFragment() {
    }

    String mMovieJsonStr;
    String mMovieId;
    View mRootView;
    String[] mTrailerKeys;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mRootView = inflater.inflate(R.layout.fragment_detail, container, false);
        mTrailerListAdapter = new ArrayAdapter<String>(
                getActivity(),
                R.layout.trailers_list_item_layout,
                R.id.trailer_link,
                new ArrayList<String>());

        // The detail Activity called via intent.  Inspect the intent for movie data.
        Intent intent = getActivity().getIntent();

        if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {

            mMovieId = intent.getStringExtra(Intent.EXTRA_TEXT);
            Log.v(LOG_TAG, mMovieId);
            FetchSelectedMovieTask movie = new FetchSelectedMovieTask();
            movie.execute(mMovieId);

            FetchSelectedMovieTrailers movieTrailers = new FetchSelectedMovieTrailers();
            movieTrailers.execute(mMovieId);

        }

        ListView viewTrailerList = (ListView) mRootView.findViewById(R.id.trailer_list);
        viewTrailerList.setAdapter(mTrailerListAdapter);

        viewTrailerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Uri uri = Uri.parse("http://www.youtube.com/watch").buildUpon()
                        .appendQueryParameter("v", mTrailerKeys[position]).build();

                Log.v(LOG_TAG, uri.toString());


                Intent intentTrailer = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intentTrailer);
            }
        });

        return mRootView;
    }


    // Extract specific info from the JSON received
    private View setMovieDataFromJson(String movieDetail, View view)
            throws JSONException {

        View rootView = view;

        JSONObject movieJson = new JSONObject(movieDetail);

        String posterPath = movieJson.getString("poster_path");
        String releaseDate = movieJson.getString("release_date");
        String originalTitle = movieJson.getString("original_title");
        String overview = movieJson.getString("overview");
        String runtime = movieJson.getString("runtime");
        ;
        //String id = movieJson.getString("id");
        String rating = movieJson.getString("vote_average");

        final String POSTER_BASE_URI = "http://image.tmdb.org/t/p";
        final String POSTER_SIZE = "w342";

        Uri posterUri = Uri.parse(POSTER_BASE_URI).buildUpon()
                .appendPath(POSTER_SIZE)
                .appendEncodedPath(posterPath)
                .build();

        ((TextView) rootView.findViewById(R.id.release_date)).setText(releaseDate);
        ((TextView) rootView.findViewById(R.id.movie_title)).setText(originalTitle);
        ((TextView) rootView.findViewById(R.id.rating)).setText(rating);
        ((TextView) rootView.findViewById(R.id.plot)).setText(overview);
        ((TextView) rootView.findViewById(R.id.plot)).setEllipsize(TextUtils.TruncateAt.END);
        ((TextView) rootView.findViewById(R.id.plot)).setMaxLines(5);
        ((TextView) rootView.findViewById(R.id.plot)).setLines(5);
        ((TextView) rootView.findViewById(R.id.runtime)).setText(runtime);

        Picasso.with(getContext())
                .load(posterUri)
                .into((ImageView) rootView.findViewById(R.id.poster));

        return rootView;
    }


    public class FetchSelectedMovieTask extends AsyncTask<String, Void, String> {

        private final String LOG_TAG = FetchSelectedMovieTask.class.getSimpleName();

        @Override
        protected String doInBackground(String... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String selectedMovieJsonStr = null;

            try {
                final String MOVIEDB_BASE_URL = "http://api.themoviedb.org/3";
                final String REFERENCE_PATH = "movie";
                final String MOVIE_ID = params[0];
                final String APIKEY_PARAM = "api_key";


                Uri builtUri = Uri.parse(MOVIEDB_BASE_URL).buildUpon()
                        .appendPath(REFERENCE_PATH)
                        .appendPath(MOVIE_ID)
                        .appendQueryParameter(APIKEY_PARAM, BuildConfig.MOVIE_DB_API_KEY)
                        .build();

                URL url = new URL(builtUri.toString());

                Log.v(LOG_TAG, builtUri.toString());

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
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }
                selectedMovieJsonStr = buffer.toString();

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in
                // attempting to parse it.
                return null;
            }

            return selectedMovieJsonStr;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                mMovieJsonStr = result;
                try {
                    mRootView = setMovieDataFromJson(mMovieJsonStr, mRootView);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Log.v(LOG_TAG, "No data received");
            }
        }
    }

    public class FetchSelectedMovieTrailers extends AsyncTask<String, Void, String> {

        public void getTrailersKeys(String movieTrailers) {

            try {
                JSONObject trailerJson = new JSONObject(movieTrailers);
                JSONArray trailerArray = trailerJson.getJSONArray("results");
                mTrailerKeys = new String[trailerArray.length()];
                for (int i = 0; i < trailerArray.length(); i++) {
                    mTrailerKeys[i] = trailerArray.getJSONObject(i).getString("key");
                }
                int count = 1;
                for (String trailerLink : mTrailerKeys) {

                    mTrailerListAdapter.add("Trailer " + String.valueOf(count));
                    count++;
                }


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(String... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String trailersJSONstr;

            // Will contain the raw JSON response as a string.
            String selectedMovieJsonStr = null;

            try {
                final String MOVIEDB_BASE_URL = "http://api.themoviedb.org/3";
                final String REFERENCE_PATH = "movie";
                final String MOVIE_ID = params[0];
                final String TRAILERS = "videos";
                final String APIKEY_PARAM = "api_key";


                Uri builtUri = Uri.parse(MOVIEDB_BASE_URL).buildUpon()
                        .appendPath(REFERENCE_PATH)
                        .appendPath(MOVIE_ID)
                        .appendPath(TRAILERS)
                        .appendQueryParameter(APIKEY_PARAM, BuildConfig.MOVIE_DB_API_KEY)
                        .build();

                URL url = new URL(builtUri.toString());

                Log.v(LOG_TAG, builtUri.toString());

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
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }
                trailersJSONstr = buffer.toString();

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in
                // attempting to parse it.
                return null;
            }

            Log.v(LOG_TAG + "trailers", trailersJSONstr);
            return trailersJSONstr;
        }

        @Override
        protected void onPostExecute(String result) {


            if (result != null) {
                getTrailersKeys(result);

            }

        }
    }

    public class FetchSelectedMovieReview extends  AsyncTask<String,Void ,String>{
        @Override
        protected String doInBackground(String... params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String reviewsJSONstr;


            // Will contain the raw JSON response as a string.
            String selectedMovieJsonStr = null;

            try {
                final String MOVIEDB_BASE_URL = "http://api.themoviedb.org/3";
                final String REFERENCE_PATH = "movie";
                final String MOVIE_ID = params[0];
                final String REVIEWS = "reviews";
                final String APIKEY_PARAM = "api_key";


                Uri builtUri = Uri.parse(MOVIEDB_BASE_URL).buildUpon()
                        .appendPath(REFERENCE_PATH)
                        .appendPath(MOVIE_ID)
                        .appendPath(REVIEWS)
                        .appendQueryParameter(APIKEY_PARAM, BuildConfig.MOVIE_DB_API_KEY)
                        .build();

                URL url = new URL(builtUri.toString());

                Log.v(LOG_TAG, builtUri.toString());

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
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }
                reviewsJSONstr = buffer.toString();

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in
                // attempting to parse it.
                return null;
            }

            Log.v(LOG_TAG + "trailers", reviewsJSONstr);
            return reviewsJSONstr;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }
}
