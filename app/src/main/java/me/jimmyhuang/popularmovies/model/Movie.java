package me.jimmyhuang.popularmovies.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class Movie implements Parcelable {
    private int vote_count;
    private int id;
    private boolean video;
    private int vote_average;
    private String title;
    private double popularity;
    private String poster_path;
    private String original_language;
    private String original_title;
    private List<Integer> genre_ids = new ArrayList<>();
    private String backdrop_path;
    private boolean adult;
    private String overview;
    private String release_date;

    private Movie(Parcel in) {
        vote_count = in.readInt();
        id = in.readInt();
        video = in.readInt() == 1;
        vote_average = in.readInt();
        title = in.readString();
        popularity = in.readDouble();
        poster_path = in.readString();
        original_language = in.readString();
        original_title = in.readString();
        in.readList(genre_ids, null);
        backdrop_path = in.readString();
        adult = in.readInt() == 1;
        overview = in.readString();
        release_date = in.readString();
    }

    public Movie(int id, String title) {
        this.id = id;
        this.title = title;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(vote_count);
        parcel.writeInt(id);
        parcel.writeInt(video ? 1 : 0);
        parcel.writeInt(vote_average);
        parcel.writeString(title);
        parcel.writeDouble(popularity);
        parcel.writeString(poster_path);
        parcel.writeString(original_language);
        parcel.writeString(original_title);
        parcel.writeList(genre_ids);
        parcel.writeString(backdrop_path);
        parcel.writeInt(adult ? 1 : 0);
        parcel.writeString(overview);
        parcel.writeString(release_date);
    }

    public final static Parcelable.Creator<Movie> CREATOR = new Parcelable.Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel parcel) {
            return new Movie(parcel);
        }

        @Override
        public Movie[] newArray(int i) {
            return new Movie[i];
        }

    };

    public int getVoteCount() {
        return vote_count;
    }
    public void setVoteCount(int vote_count) {
        this.vote_count = vote_count;
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public boolean getVideo() {
        return video;
    }
    public void setVideo(boolean video) {
        this.video = video;
    }

    public int getVoteAverage() {
        return vote_average;
    }
    public void setVoteAverage(int vote_average) {
        this.vote_average = vote_average;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public double getPopularity() {
        return popularity;
    }
    public void setPopularity(double popularity) {
        this.popularity = popularity;
    }

    public String getPosterPath() {
        return poster_path;
    }
    public void setPosterPath(String poster_path) {
        this.poster_path = poster_path;
    }

    public String getOriginalLanguage() {
        return original_language;
    }
    public void setOriginalLanguage(String original_language) {
        this.original_language = original_language;
    }

    public String getOriginalTitle() {
        return original_title;
    }
    public void setOriginalTitle(String original_title) {
        this.original_title = original_title;
    }

    public List<Integer> getGenreId() {
        return genre_ids;
    }
    public void setGenreId(List<Integer> genre_ids) {
        this.genre_ids = genre_ids;
    }

    public String getBackdropPath() {
        return backdrop_path;
    }
    public void setBackdropPath(String backdrop_path) {
        this.backdrop_path = backdrop_path;
    }

    public boolean getAdult() {
        return adult;
    }
    public void setAdult(boolean adult) {
        this.adult = adult;
    }

    public String getOverview() {
        return overview;
    }
    public void setOverview(String overview) {
        this.overview = overview;
    }

    public String getReleaseDate() {
        return release_date;
    }
    public void setReleaseDate(String release_date) {
        this.release_date = release_date;
    }
}
