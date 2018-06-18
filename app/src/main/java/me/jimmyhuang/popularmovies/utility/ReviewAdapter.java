package me.jimmyhuang.popularmovies.utility;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import me.jimmyhuang.popularmovies.R;
import me.jimmyhuang.popularmovies.model.Review;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private List<Review> mReviews;

    public ReviewAdapter(List<Review> reviews) {
        mReviews = reviews;
    }

    @NonNull
    @Override
    public ReviewAdapter.ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(R.layout.adapter_review, parent, false);

        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewAdapter.ReviewViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return mReviews.size();
    }

    public class ReviewViewHolder extends RecyclerView.ViewHolder {

        private final TextView mAuthorText;
        private final TextView mReviewText;

        private ReviewViewHolder(View itemView) {
            super(itemView);

            mAuthorText = itemView.findViewById(R.id.author_tv);
            mReviewText = itemView.findViewById(R.id.review_tv);
        }

        private void bind(int position) {
            Review review = mReviews.get(position);
            if (review != null) {
                String author = review.getAuthor();
                String reviewContent = review.getReview();
                mAuthorText.setText(author);
                mReviewText.setText(reviewContent);
            }
        }
    }
}
