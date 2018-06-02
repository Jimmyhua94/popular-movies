package me.jimmyhuang.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import me.jimmyhuang.popularmovies.model.Movie;
import me.jimmyhuang.popularmovies.model.Review;
import me.jimmyhuang.popularmovies.model.Trailer;
import me.jimmyhuang.popularmovies.utility.DetailAdapter;
import me.jimmyhuang.popularmovies.utility.NetworkUtil;
import me.jimmyhuang.popularmovies.utility.PosterAdapter;

import static me.jimmyhuang.popularmovies.utility.JsonUtil.parseReviewJson;
import static me.jimmyhuang.popularmovies.utility.JsonUtil.parseTrailerJson;

public class DetailActivity extends AppCompatActivity {

    private List<Object> mItems = new ArrayList<>();

    private RecyclerView mDetails;

    private DetailAdapter mDetailAdapter;

    private Movie mMovie;
    private List<Review> mReviews = new ArrayList<>();
    private List<Trailer> mTrailers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        mDetails = findViewById(R.id.detail_rv);

        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mDetails.setLayoutManager(layoutManager);

        Intent intent = getIntent();

        if (intent.hasExtra(PosterAdapter.MOVIE_INTENT_EXTRA)) {
            mMovie = intent.getParcelableExtra(PosterAdapter.MOVIE_INTENT_EXTRA);
            mItems.add(mMovie);

            if (hasNetwork()) {
                URL trailerUrl = NetworkUtil.buildTrailerUrl(mMovie.getId());
                new MovieTrailerTask().execute(trailerUrl);

                URL reviewUrl = NetworkUtil.buildReviewUrl(mMovie.getId());
                new MovieReviewTask().execute(reviewUrl);
            }

            mDetailAdapter = new DetailAdapter(mItems);

            mDetails.setAdapter(mDetailAdapter);
        } else {
            Toast.makeText(this, getResources().getString(R.string.movie_detail_error), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private class MovieReviewTask extends AsyncTask<URL, Void, String> {

        @Override
        protected String doInBackground(URL... urls) {
            URL movieApiUrl = urls[0];
            String results = null;
            try {
                results = NetworkUtil.getResponseFromHttpUrl(movieApiUrl);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return results;
        }

        @Override
        protected void onPostExecute(String s) {
            if (s != null && !s.equals("")) {
                List<Review> reviewList = parseReviewJson(s);
                if (reviewList != null) {
                    mReviews.clear();
                    mReviews.addAll(reviewList);
                    setupDetailsArray();
                    mDetailAdapter.notifyDataSetChanged();
                }
            }
        }

    }

    private class MovieTrailerTask extends AsyncTask<URL, Void, String> {

        @Override
        protected String doInBackground(URL... urls) {
            URL movieApiUrl = urls[0];
            String results = null;
            try {
                results = NetworkUtil.getResponseFromHttpUrl(movieApiUrl);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return results;
        }

        @Override
        protected void onPostExecute(String s) {
            if (s != null && !s.equals("")) {
                List<Trailer> trailerList = parseTrailerJson(s);
                if (trailerList != null) {
                    mTrailers.clear();
                    mTrailers.addAll(trailerList);
                    setupDetailsArray();
                    mDetailAdapter.notifyDataSetChanged();
                }
            }
        }

    }

    // https://developer.android.com/training/monitoring-device-state/connectivity-monitoring
    private boolean hasNetwork() {
        ConnectivityManager cm =
                (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork;
        Boolean hasConnection = false;
        if (cm != null) {
            activeNetwork = cm.getActiveNetworkInfo();
            hasConnection = activeNetwork != null &&
                    activeNetwork.isConnectedOrConnecting();
        }

        if (!hasConnection) {
            Toast.makeText(DetailActivity.this,getResources().getString(R.string.network_error), Toast.LENGTH_LONG).show();
        }

        return hasConnection;
    }

    private void setupDetailsArray() {
        mItems.clear();
        if (mMovie != null) {
            mItems.add(mMovie);
        }
        if (!(mTrailers.isEmpty())) {
            mItems.addAll(mTrailers);
        }
        if (!(mReviews.isEmpty())) {
            mItems.addAll(mReviews);
        }
    }
}
