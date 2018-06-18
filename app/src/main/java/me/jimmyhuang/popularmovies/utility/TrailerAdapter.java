package me.jimmyhuang.popularmovies.utility;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.List;

import me.jimmyhuang.popularmovies.R;
import me.jimmyhuang.popularmovies.model.Trailer;

import static me.jimmyhuang.popularmovies.utility.NetworkUtil.buildYoutubeImageUrl;

public class TrailerAdapter extends RecyclerView.Adapter<TrailerAdapter.TrailerViewHolder> {

    private Context mContext;

    private RecyclerView mRecyclerView;

    private List<Trailer> mTrailers;

    private final View.OnClickListener mTrailerOnClickListener = new TrailerClickListener();

    public TrailerAdapter(List<Trailer> trailers) {
        mTrailers = trailers;
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView rv) {
        super.onAttachedToRecyclerView(rv);

        mRecyclerView = rv;
    }

    @NonNull
    @Override
    public TrailerAdapter.TrailerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(mContext);

        View view = inflater.inflate(R.layout.adapter_trailer, parent, false);

        return new TrailerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TrailerAdapter.TrailerViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return mTrailers.size();
    }

    public class TrailerViewHolder extends RecyclerView.ViewHolder {

        private final TextView mVideoText;
        private final ImageView mVideoImage;

        private TrailerViewHolder(View itemView) {
            super(itemView);

            mVideoText = itemView.findViewById(R.id.trailer_tv);
            mVideoImage = itemView.findViewById(R.id.trailer_iv);

            itemView.setOnClickListener(mTrailerOnClickListener);
        }

        private void bind(int position) {
            Trailer trailer = mTrailers.get(position);
            if (trailer != null) {
                mVideoText.setText(trailer.getName());
                Picasso.with(mContext)
                        .load(buildYoutubeImageUrl(trailer.getKey()).toString())
                        .fit().into(mVideoImage);
            }
        }
    }

    // https://developer.android.com/training/basics/intents/sending
    private void videoIntent(String id) {
        Intent videoIntent = new Intent(Intent.ACTION_VIEW, NetworkUtil.buildYoutubeVideoUri(id));

        PackageManager packageManager = mContext.getPackageManager();
        List<ResolveInfo> activities = packageManager.queryIntentActivities(videoIntent,
                PackageManager.MATCH_DEFAULT_ONLY);
        if (activities.size() > 0) {
            mContext.startActivity(videoIntent);
        } else {
            Toast.makeText(mContext, mContext.getResources().getString(R.string.video_error), Toast.LENGTH_LONG).show();
        }
    }

    private class TrailerClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            int position = mRecyclerView.getChildLayoutPosition(v);
            Object obj = mTrailers.get(position);
            if (obj instanceof Trailer) {
                Trailer trailer = (Trailer) obj;
                videoIntent(trailer.getKey());
            }
        }
    }
}
