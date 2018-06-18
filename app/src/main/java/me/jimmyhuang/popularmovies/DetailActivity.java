package me.jimmyhuang.popularmovies;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import me.jimmyhuang.popularmovies.data.FavoritesContract;
import me.jimmyhuang.popularmovies.model.Movie;
import me.jimmyhuang.popularmovies.model.Review;
import me.jimmyhuang.popularmovies.model.Trailer;
import me.jimmyhuang.popularmovies.utility.MainAdapter;
import me.jimmyhuang.popularmovies.utility.NetworkUtil;
import me.jimmyhuang.popularmovies.utility.ReviewAdapter;
import me.jimmyhuang.popularmovies.utility.TrailerAdapter;

import static me.jimmyhuang.popularmovies.utility.JsonUtil.parseReviewJson;
import static me.jimmyhuang.popularmovies.utility.JsonUtil.parseTrailerJson;
import static me.jimmyhuang.popularmovies.utility.NetworkUtil.buildPosterUrl;

public class DetailActivity extends AppCompatActivity {
    private Context mContext = this;

    private Movie mMovie;
    private List<Review> mReviews = new ArrayList<>();
    private List<Trailer> mTrailers = new ArrayList<>();

    private NestedScrollView mDetails;
    private ImageView mPosterImage;
    private TextView mTitleText;
    private TextView mReleaseText;
    private TextView mRatingText;
    private TextView mOverviewText;
    private Button mFavoritesButton;
    private RecyclerView mTrailerRecycler;
    private RecyclerView mReviewRecycler;

    private TrailerAdapter mTrailerAdapter;
    private ReviewAdapter mReviewAdapter;

    private Cursor mCursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        mDetails = findViewById(R.id.movie_detail_nsv);

        Intent intent = getIntent();

        if (intent.hasExtra(MainAdapter.MOVIE_INTENT_EXTRA)) {
            mMovie = intent.getParcelableExtra(MainAdapter.MOVIE_INTENT_EXTRA);

            mPosterImage = findViewById(R.id.detail_poster_iv);
            mTitleText = findViewById(R.id.detail_title_tv);
            mReleaseText = findViewById(R.id.detail_release_tv);
            mRatingText = findViewById(R.id.detail_rating_tv);
            mOverviewText = findViewById(R.id.detail_overview_tv);
            mFavoritesButton = findViewById(R.id.favorites_button);

            String posterUrl = mMovie.getPosterPath();
            if (!posterUrl.isEmpty()) {
                Picasso.with(mContext).load(buildPosterUrl(posterUrl).toString()).fit().into(mPosterImage);
            }

            mTitleText.setText(mMovie.getTitle());
            mReleaseText.setText(mMovie.getReleaseDate());
            mRatingText.setText(String.valueOf(mMovie.getVoteAverage()));
            mOverviewText.setText(mMovie.getOverview());

            mTrailerRecycler = findViewById(R.id.trailer_rv);
            mReviewRecycler = findViewById(R.id.review_rv);

            final LinearLayoutManager trailerLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,false);
            mTrailerRecycler.setLayoutManager(trailerLayoutManager);

            final LinearLayoutManager reviewLayoutManager = new LinearLayoutManager(this);
            mReviewRecycler.setLayoutManager(reviewLayoutManager);

            mTrailerAdapter = new TrailerAdapter(mTrailers);

            mTrailerRecycler.setAdapter(mTrailerAdapter);

            mReviewAdapter = new ReviewAdapter(mReviews);

            mReviewRecycler.setAdapter(mReviewAdapter);

            if (hasNetwork()) {
                URL trailerUrl = NetworkUtil.buildTrailerUrl(mMovie.getId());
                new MovieTrailerTask().execute(trailerUrl);

                URL reviewUrl = NetworkUtil.buildReviewUrl(mMovie.getId());
                new MovieReviewTask().execute(reviewUrl);
            }

            updateCursor(mMovie);

            mFavoritesButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mCursor.getCount() > 0) {
                        int deleted = v.getContext()
                                .getContentResolver()
                                .delete(FavoritesContract.FavoritesEntry.CONTENT_FAVORITE_URI,
                                        "id=?",
                                        new String[]{String.valueOf(mMovie.getId())}
                                );
                        if (deleted > 0) {
                            Toast.makeText(mContext, mContext.getResources().getString(R.string.favorite_deleted), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Uri uri = v.getContext()
                                .getContentResolver()
                                .insert(FavoritesContract.FavoritesEntry.CONTENT_FAVORITE_URI,
                                        favoriteContentValues(mMovie)
                                );
                        if (uri != null) {
                            Toast.makeText(mContext, mContext.getResources().getString(R.string.favorite_added), Toast.LENGTH_LONG).show();
                        }
                    }
                    updateCursor(mMovie);
                }
            });
        } else {
            Toast.makeText(this, getResources().getString(R.string.movie_detail_error), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void updateCursor(Movie movie) {
        if (mCursor != null) mCursor.close();
        mCursor = mContext.getContentResolver().query(
                FavoritesContract.FavoritesEntry.CONTENT_FAVORITES_URI,
                null,
                "id=?",
                new String[]{String.valueOf(movie.getId())},
                null
        );
        if (mCursor != null) {
            if (mCursor.getCount() > 0) {
                mFavoritesButton.setText(mContext.getResources().getString(R.string.favorite_delete));
            } else {
                mFavoritesButton.setText(mContext.getResources().getString(R.string.favorite_add));
            }
        };
    }

    private ContentValues favoriteContentValues(Movie movie) {
        ContentValues cv = new ContentValues();
        cv.put(FavoritesContract.FavoritesEntry.COLUMN_ID, movie.getId());
        cv.put(FavoritesContract.FavoritesEntry.COLUMN_TITLE, movie.getTitle());
        cv.put(FavoritesContract.FavoritesEntry.COLUMN_OVERVIEW, movie.getOverview());
        cv.put(FavoritesContract.FavoritesEntry.COLUMN_RELEASE_DATE, movie.getReleaseDate());
        cv.put(FavoritesContract.FavoritesEntry.COLUMN_VOTE_AVG, movie.getVoteAverage());
        cv.put(FavoritesContract.FavoritesEntry.COLUMN_POSTER_PATH, movie.getPosterPath());
        return cv;
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
                    mTrailerAdapter.notifyDataSetChanged();
                }
            }
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
                    mReviewAdapter.notifyDataSetChanged();
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
}
