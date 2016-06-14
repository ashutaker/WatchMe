package com.example.android.watchme.app;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
import java.util.Collections;

/**
 * A placeholder fragment containing a simple view.
 */


public class DetailActivityFragment extends Fragment implements TrailerViewAdapter.ClickListener {

    public final String LOG_TAG = DetailActivityFragment.class.getSimpleName();

    private TrailerViewAdapter mTrailerListAdapter;

    private ReviewAdapter mReviewListAdapter;

    private ProgressBar mDetailProgress;

    public DetailActivityFragment() {
    }

    String mMovieJsonStr;
    String mMovieId;
    View mRootView;
    String[] mTrailerKeys;
    ArrayList<String> mTrailerNames;
    ArrayList<String> mReviews;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mRootView = inflater.inflate(R.layout.fragment_detail, container, false);
        mDetailProgress = (ProgressBar) mRootView.findViewById(R.id.progress_details);

        mTrailerNames = new ArrayList<String>(Collections.<String>emptyList());
        mReviews = new ArrayList<String>(Collections.<String>emptyList());
        // The detail Activity called via intent.  Inspect the intent for movie data.
        Intent intent = getActivity().getIntent();

        if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {

            mMovieId = intent.getStringExtra(Intent.EXTRA_TEXT);
            Log.v(LOG_TAG, mMovieId);

            FetchSelectedMovieTask movie = new FetchSelectedMovieTask();
            movie.execute(mMovieId);

            FetchSelectedMovieTrailers movieTrailers = new FetchSelectedMovieTrailers();
            movieTrailers.execute(mMovieId);

            FetchSelectedMovieReview movieReviews = new FetchSelectedMovieReview();
            movieReviews.execute(mMovieId);

        }

        RecyclerView viewTrailerList = (RecyclerView) mRootView.findViewById(R.id.trailer_list);
        mTrailerListAdapter = new TrailerViewAdapter(getActivity(), mTrailerNames);
        viewTrailerList.setLayoutManager(new LinearLayoutManager(viewTrailerList.getContext()));
        viewTrailerList.setAdapter(mTrailerListAdapter);
        mTrailerListAdapter.setOnClickListener(this);

        RecyclerView viewReviewList = (RecyclerView) mRootView.findViewById(R.id.review_list);
        mReviewListAdapter = new ReviewAdapter(getActivity(), mReviews);
        viewReviewList.setLayoutManager(new LinearLayoutManager(viewReviewList.getContext()));
        viewReviewList.setAdapter(mReviewListAdapter);

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

    @Override
    public void itemClicked(View view, int position) {
        Uri uri = Uri.parse("http://www.youtube.com/watch").buildUpon()
                .appendQueryParameter("v", mTrailerKeys[position]).build();
        //Log.v(LOG_TAG, uri.toString());
        Intent intentTrailer = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intentTrailer);

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
                String[] trailerName = new String[trailerArray.length()];
                for (int i = 0; i < trailerArray.length(); i++) {
                    mTrailerKeys[i] = trailerArray.getJSONObject(i).getString("key");
                    trailerName[i] = trailerArray.getJSONObject(i).getString("name");
                }

                for (String name : trailerName) {
                    mTrailerNames.add(name);
                }
                //Log.v(LOG_TAG, String.valueOf(mTrailerNames));
                mTrailerListAdapter.swap(mTrailerNames);

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

    public class FetchSelectedMovieReview extends AsyncTask<String, Integer, String> {

        public void getReviews(String movieReviews) {

            try {
                JSONObject reviewJson = new JSONObject(movieReviews);
                JSONArray reviewsArray = reviewJson.getJSONArray("results");

                String[] reviewStr = new String[reviewsArray.length()];
                // mReviews = new ArrayList<String>(reviewsArray.length());

                for (int i = 0; i < reviewsArray.length(); i++) {
                    reviewStr[i] = reviewsArray.getJSONObject(i).getString("content");
                }
                for (String review : reviewStr) {
                    mReviews.add(review);
                    Log.v("Review Data : ", review);
                }
                mReviewListAdapter.swap(mReviews);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


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
                publishProgress(30);

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

            Log.v(LOG_TAG + " Reviews", reviewsJSONstr);
            publishProgress(100);
            return reviewsJSONstr;
        }

        @Override
        protected void onPostExecute(String resultReview) {
            if (resultReview != null) {
                getReviews(resultReview);
            } else {
                Log.v(LOG_TAG, " no data");
            }
        }

        protected void onProgressUpdate(Integer... values) {
            mDetailProgress.setProgress(values[0]);
            if(values[0]==100)
            {
                mDetailProgress.setVisibility(View.GONE);
            }

        }

    }
}
