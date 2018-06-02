package me.jimmyhuang.popularmovies.utility;

import android.net.Uri;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

import me.jimmyhuang.popularmovies.BuildConfig;
import me.jimmyhuang.popularmovies.R;

public class NetworkUtil {
    private final static String BASE_URL = "https://api.themoviedb.org/3";
    private final static String MOVIE_PATH = "movie";
    private final static String POPULAR_PATH = "popular";
    private final static String RATED_PATH = "top_rated";
    private final static String VIDEO_PATH = "videos";
    private final static String REVIEW_PATH = "reviews";

    private final static String PARAM_API_KEY = "api_key";

    private final static String PARAM_PAGE = "page";

    private final static String POSTER_BASE_URL = "http://image.tmdb.org/t/p";

    private final static String POSTER_SIZE = "w185";

    public static URL buildTrailerUrl(int id) {
        Uri uri = Uri.parse(BASE_URL).buildUpon()
                .appendEncodedPath(MOVIE_PATH)
                .appendEncodedPath(String.valueOf(id))
                .appendEncodedPath(VIDEO_PATH)
                .appendQueryParameter(PARAM_API_KEY, BuildConfig.MovieDbApiKey)
                .build();

        URL url = null;
        try {
            url = new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    public static URL buildReviewUrl(int id) {
        Uri uri = Uri.parse(BASE_URL).buildUpon()
                .appendEncodedPath(MOVIE_PATH)
                .appendEncodedPath(String.valueOf(id))
                .appendEncodedPath(REVIEW_PATH)
                .appendQueryParameter(PARAM_API_KEY, BuildConfig.MovieDbApiKey)
                .build();

        URL url = null;
        try {
            url = new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    public static URL buildPosterUrl(String path) {
        Uri.Builder uriBuilder = Uri.parse(POSTER_BASE_URL).buildUpon()
                .appendEncodedPath(POSTER_SIZE)
                .appendEncodedPath(path);

        Uri builtUri = uriBuilder.build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    public static URL buildDiscoverUrl(int sortId, int page) {
        String sortPath;
        switch (sortId) {
            case R.string.rated: sortPath = RATED_PATH; break;
            case R.string.popular:
            default: sortPath = POPULAR_PATH; break;
        }

        Uri.Builder uriBuilder = Uri.parse(BASE_URL).buildUpon()
                .appendEncodedPath(MOVIE_PATH);

        uriBuilder.appendEncodedPath(sortPath);

        if (page != 0) {
            uriBuilder.appendQueryParameter(PARAM_PAGE, String.valueOf(page));
        }

        Uri builtUri = uriBuilder
            .appendQueryParameter(PARAM_API_KEY, BuildConfig.MovieDbApiKey)
            .build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    public static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }
}
