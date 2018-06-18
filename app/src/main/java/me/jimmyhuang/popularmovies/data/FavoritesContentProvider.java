package me.jimmyhuang.popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import me.jimmyhuang.popularmovies.R;

public class FavoritesContentProvider extends ContentProvider {
    private FavoritesDbHelper mFavoritesDbHelper;

    public static final int FAVORITES = 100;
    public static final int FAVORITE = 101;

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    public static UriMatcher buildUriMatcher() {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        uriMatcher.addURI(FavoritesContract.AUTHORITY, FavoritesContract.PATH_FAVORITES, FAVORITES);
        uriMatcher.addURI(FavoritesContract.AUTHORITY, FavoritesContract.PATH_FAVORITE , FAVORITE);
        uriMatcher.addURI(FavoritesContract.AUTHORITY, FavoritesContract.PATH_FAVORITE + "/#", FAVORITE);

        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        mFavoritesDbHelper = new FavoritesDbHelper(context);
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        final SQLiteDatabase db = mFavoritesDbHelper.getReadableDatabase();
        int match = sUriMatcher.match(uri);
        Cursor retCursor;
        switch(match) {
            case FAVORITES:
                retCursor = db.query(FavoritesContract.FavoritesEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final SQLiteDatabase db = mFavoritesDbHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        Uri retUri;
        switch(match) {
            case FAVORITE:
                long id = db.insert(FavoritesContract.FavoritesEntry.TABLE_NAME, null, values);
                if (id > 0) {
                    retUri = ContentUris.withAppendedId(FavoritesContract.FavoritesEntry.CONTENT_FAVORITE_URI, id);
                } else {
                    throw new android.database.SQLException(getContext().getResources().getString(R.string.insert_error) + uri);
                }
                break;
            default:
                throw new UnsupportedOperationException(getContext().getResources().getString(R.string.unknown_uri) + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return retUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        final SQLiteDatabase db = mFavoritesDbHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        int retDeleted = 0;
        switch(match) {
            case FAVORITE:
                if (!selection.isEmpty() && selectionArgs.length != 0) {
                    retDeleted = db.delete(FavoritesContract.FavoritesEntry.TABLE_NAME,
                            selection,
                            selectionArgs);
                    if (retDeleted == 0) {
                        throw new android.database.SQLException(getContext().getResources().getString(R.string.insert_error) + uri);
                    }
                }
                break;
            default:
                throw new UnsupportedOperationException(getContext().getResources().getString(R.string.unknown_uri) + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return retDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}
