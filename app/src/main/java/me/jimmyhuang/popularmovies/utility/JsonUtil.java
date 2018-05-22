package me.jimmyhuang.popularmovies.utility;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import me.jimmyhuang.popularmovies.model.Movie;

public class JsonUtil {

    public static List<Movie> parseDiscoverJson(String json) {
        try {
            List<Movie> movieArray = new ArrayList<>();

            JSONObject discoverObject = new JSONObject(json);

            JSONArray resultsObject = discoverObject.optJSONArray("results");

            for (int i = 0; i < resultsObject.length(); i++) {
                JSONObject movieObject = resultsObject.getJSONObject(i);

                int id = movieObject.optInt("id", 0);
                String title = movieObject.optString("title", "");

                Movie movie = new Movie(id, title);

                movie.setVoteAverage(movieObject.optInt("vote_average", 0));
                movie.setPosterPath(movieObject.optString("poster_path", ""));
                movie.setOverview(movieObject.optString("overview", ""));
                movie.setReleaseDate(movieObject.optString("release_date", ""));

                movieArray.add(movie);
            }

            return movieArray;
        } catch (Exception e){
            return null;
        }
    }

    public static int parseDiscoverTotalPagesJson(String json) {
        try {
            JSONObject discoverObject = new JSONObject(json);

            return discoverObject.optInt("total_pages", 0);
        } catch (Exception e){
            return 0;
        }
    }
}
