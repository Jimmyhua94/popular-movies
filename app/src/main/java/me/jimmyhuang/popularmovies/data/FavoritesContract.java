package me.jimmyhuang.popularmovies.data;

import android.net.Uri;
import android.provider.BaseColumns;

public final class FavoritesContract {
    public static final String AUTHORITY = "me.jimmyhuang.popularmovies";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    public static final String PATH_FAVORITES = "favorites";

    public static final String PATH_FAVORITE = "favorite";

    private FavoritesContract() {}

    public static class FavoritesEntry implements BaseColumns {
        public static final Uri CONTENT_FAVORITES_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_FAVORITES).build();

        public static final Uri CONTENT_FAVORITE_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_FAVORITE).build();

        public static final String TABLE_NAME = "favorites";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_OVERVIEW = "overview";
        public static final String COLUMN_RELEASE_DATE = "release_date";
        public static final String COLUMN_VOTE_AVG = "vote_avg";
        public static final String COLUMN_POSTER_PATH = "poster_path";
    }
}
