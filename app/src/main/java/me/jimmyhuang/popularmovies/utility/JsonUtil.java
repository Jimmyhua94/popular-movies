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

            JSONArray resultsObject = discoverObject.getJSONArray("results");

            for (int i = 0; i < resultsObject.length(); i++) {
                JSONObject movieObject = resultsObject.getJSONObject(i);

                int id = movieObject.getInt("id");
                String title = movieObject.getString("title");

                Movie movie = new Movie(id, title);

                movie.setVoteCount(movieObject.getInt("vote_count"));
                movie.setVideo(movieObject.getBoolean("video"));
                movie.setVoteAverage(movieObject.getInt("vote_average"));
                movie.setPopularity(movieObject.getDouble("popularity"));
                movie.setPosterPath(movieObject.getString("poster_path"));
                movie.setOriginalLanguage(movieObject.getString("original_language"));
                movie.setOriginalTitle(movieObject.getString("original_title"));
                List<Integer> genreArray = new ArrayList<>();
                JSONArray genreJsonArray = movieObject.getJSONArray("genre_ids");
                for (int j = 0; j < genreJsonArray.length(); j++) {
                    genreArray.add(genreJsonArray.getInt(j));
                }
                movie.setGenreId(genreArray);
                movie.setBackdropPath(movieObject.getString("backdrop_path"));
                movie.setAdult(movieObject.getBoolean("adult"));
                movie.setOverview(movieObject.getString("overview"));
                movie.setReleaseDate(movieObject.getString("release_date"));

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

            return discoverObject.getInt("total_pages");
        } catch (Exception e){
            return 0;
        }
    }
}
