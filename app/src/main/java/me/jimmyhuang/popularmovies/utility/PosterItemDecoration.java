package me.jimmyhuang.popularmovies.utility;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

// https://stackoverflow.com/questions/28531996/android-recyclerview-gridlayoutmanager-column-spacing?utm_medium=organic&utm_source=google_rich_qa&utm_campaign=google_rich_qa
public class PosterItemDecoration extends RecyclerView.ItemDecoration {
    private final int space;

    public PosterItemDecoration(int space) {
        this.space = space;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        outRect.set(space, space, space, space);
    }
}
