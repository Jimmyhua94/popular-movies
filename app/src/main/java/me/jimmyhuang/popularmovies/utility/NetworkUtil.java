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
    private final static String DISCOVER_PATH = "discover/movie";
//    final static String MOVIE_PATH = "movie";

    private final static String PARAM_SORT = "sort_by";
    private final static String sortMostPopular = "popularity.desc";
    private final static String sortTopRated = "vote_average.desc";

    private final static String PARAM_API_KEY = "api_key";

    private final static String PARAM_PAGE = "page";

    private final static String POSTER_BASE_URL = "http://image.tmdb.org/t/p";

    private final static String POSTER_SIZE = "w185";


    public static URL buildPosterUrl(String path) {
//        if (!path.isEmpty() && path.matches("^/")) {
//            path = path.replaceFirst("^/", "");
//        }
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
        String staticSort;
        switch (sortId) {
            case R.string.rated: staticSort = sortTopRated; break;
            case R.string.popular:
            default: staticSort = sortMostPopular; break;
        }

        Uri.Builder uriBuilder = Uri.parse(BASE_URL).buildUpon()
                .appendEncodedPath(DISCOVER_PATH);

        if (page != 0) {
            uriBuilder.appendQueryParameter(PARAM_PAGE, String.valueOf(page));
        }

        Uri builtUri = uriBuilder
            .appendQueryParameter(PARAM_SORT, staticSort)
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
