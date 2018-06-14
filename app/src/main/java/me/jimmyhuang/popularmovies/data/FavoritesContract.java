package me.jimmyhuang.popularmovies.data;

import android.provider.BaseColumns;

public final class FavoritesContract {
    private FavoritesContract() {}

    public static class FavoritesEntry implements BaseColumns {
        public static final String TABLE_NAME = "favorites";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_OVERVIEW = "overview";
        public static final String COLUMN_RELEASE_DATE = "release_date";
        public static final String COLUMN_VOTE_AVG = "vote_avg";
        public static final String COLUMN_POSTER_PATH = "poster_path";
    }
}
