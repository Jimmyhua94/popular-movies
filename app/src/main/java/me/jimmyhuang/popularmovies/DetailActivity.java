package me.jimmyhuang.popularmovies;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import me.jimmyhuang.popularmovies.model.Movie;

import static me.jimmyhuang.popularmovies.utility.NetworkUtil.buildPosterUrl;

public class DetailActivity extends AppCompatActivity {
    private ImageView mPosterImage;
    private TextView mTitleText;
    private TextView mReleaseText;
    private TextView mRatingText;
    private TextView mOverviewText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.poster_detail);

        mPosterImage = findViewById(R.id.detail_poster_iv);
        mTitleText = findViewById(R.id.detail_title_tv);
        mReleaseText = findViewById(R.id.detail_release_tv);
        mRatingText = findViewById(R.id.detail_rating_tv);
        mOverviewText = findViewById(R.id.detail_overview_tv);

        Intent intent = getIntent();

        if (intent.hasExtra("MovieObject")) {
            Movie movie = intent.getParcelableExtra("MovieObject");

            if (movie != null) {
                String posterUrl = movie.getPosterPath();
                if (!posterUrl.isEmpty()) {

                    Picasso.Builder builder = new Picasso.Builder(this);
                    builder.listener(new Picasso.Listener() {
                        @Override
                        public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
                            Log.e("URL", exception.toString());
                        }
                    });
                    builder.build().with(this).load(buildPosterUrl(posterUrl).toString()).fit().into(mPosterImage);
                }

                mTitleText.setText(movie.getTitle());
                mReleaseText.setText(movie.getReleaseDate());
                mRatingText.setText(String.valueOf(movie.getVoteAverage()));
                mOverviewText.setText(movie.getOverview());
            }
        } else {
            Toast.makeText(this, getResources().getString(R.string.movie_detail_error), Toast.LENGTH_LONG).show();
            finish();
        }
    }
}
