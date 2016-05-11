package com.example.android.watchme.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment {

    public DetailActivityFragment() {
    }

    String movieJsonStr;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        // The detail Activity called via intent.  Inspect the intent for movie data.
        Intent intent = getActivity().getIntent();

        if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
            movieJsonStr = intent.getStringExtra(Intent.EXTRA_TEXT);
        }

        return rootView;

    }

    //Extract the Movie ID of the item selected.
    public String getMovieDetail(int position)
            throws JSONException {

        String movieId;

        JSONObject movieJson = new JSONObject(movieJsonStr);
        JSONObject movieDetailStr = movieJson.getJSONArray("results").getJSONObject(position);
        return movieDetailStr.toString();
    }

    // Extract specific info from the JSON received
    private View setMovieDataFromJson(String movieDetail, View view)
            throws JSONException {

        View rootView = view;
        JSONObject movieJson = new JSONObject(movieDetail);

        String posterPath= movieJson.getString("poster_path");
        String releaseDate = movieJson.getString("release_date");
        String originalTitle = movieJson.getString("original_title");
        String overview = movieJson.getString("overview");;
        //String id = movieJson.getString("id");
        String rating= movieJson.getString("vote_average");

        final String POSTER_BASE_URI = "http://image.tmdb.org/t/p";
        final String POSTER_SIZE = "w342";

        Uri posterUri = Uri.parse(POSTER_BASE_URI).buildUpon()
                .appendPath(POSTER_SIZE)
                .appendEncodedPath(posterPath)
                .build();

        ((TextView) rootView.findViewById(R.id.release_date)).setText(releaseDate);
        ((TextView)rootView.findViewById(R.id.movie_title)).setText(originalTitle);
        ((TextView)rootView.findViewById(R.id.plot)).setText(overview);
        ((TextView)rootView.findViewById(R.id.rating)).setText(rating);

        Picasso.with(getContext())
                .load(posterUri)
                .into((ImageView) rootView.findViewById(R.id.poster));

        return rootView;
    }
}
