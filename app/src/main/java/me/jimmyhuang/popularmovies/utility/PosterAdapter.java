package me.jimmyhuang.popularmovies.utility;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import me.jimmyhuang.popularmovies.DetailActivity;
import me.jimmyhuang.popularmovies.R;
import me.jimmyhuang.popularmovies.model.Movie;

import static me.jimmyhuang.popularmovies.utility.NetworkUtil.buildPosterUrl;


public class PosterAdapter extends RecyclerView.Adapter<PosterAdapter.PosterViewHolder> {
    private final View.OnClickListener mOnClickListener = new PosterClickListener();

    private RecyclerView mRecyclerView;

    private final List<Movie> mMovies;

    private Picasso picasso;

    public PosterAdapter(List<Movie> movies) {
        mMovies = movies;
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView rv) {
        super.onAttachedToRecyclerView(rv);

        mRecyclerView = rv;

        Context context = rv.getContext();

        Picasso.Builder builder = new Picasso.Builder(context);
        builder.listener(new Picasso.Listener() {
            @Override
            public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
                Log.e("URL", exception.toString());
            }
        });
        picasso = builder.build();
    }

    @Override @NonNull
    public PosterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(R.layout.poster, parent, false);

        view.setOnClickListener(mOnClickListener);

        return new PosterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PosterViewHolder holder, int position) {
        holder.bind(position);  //replace position with url
    }

    @Override
    public int getItemCount() {
        return mMovies.size();
    }

    public class PosterViewHolder extends RecyclerView.ViewHolder {

        private final TextView posterTextView;
        private final ImageView posterView;

        private PosterViewHolder(View itemView) {
            super(itemView);

            posterTextView = itemView.findViewById(R.id.poster_tv);
            posterView = itemView.findViewById(R.id.poster_iv);
        }

        private void bind(int position) {
            Movie movie = mMovies.get(position);
            if (movie != null) {
                String posterUrl = movie.getPosterPath();
                String posterTitle = movie.getTitle();
                posterTextView.setText(posterTitle);
                if (!posterUrl.isEmpty()) {
                    picasso.load(buildPosterUrl(posterUrl).toString()).fit().into(posterView);
                }
            }
        }
    }

    private class PosterClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            Context context = mRecyclerView.getContext();

            int position = mRecyclerView.getChildLayoutPosition(v);
            Movie movie = mMovies.get(position);

            Intent intent = new Intent(context, DetailActivity.class);
            intent.putExtra("MovieObject", movie);
            context.startActivity(intent);
        }
    }
}
