package me.jimmyhuang.popularmovies.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Movie implements Parcelable {
    private int id;
    private int vote_average;
    private String title;
    private String poster_path;
    private String overview;
    private String release_date;

    private Movie(Parcel in) {
        id = in.readInt();
        vote_average = in.readInt();
        title = in.readString();
        poster_path = in.readString();
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
        parcel.writeInt(id);
        parcel.writeInt(vote_average);
        parcel.writeString(title);
        parcel.writeString(poster_path);
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

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getVoteAverage() {
        return vote_average;
    }
    public void setVoteAverage(int vote_average) {
        this.vote_average = vote_average;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) { this.title = title; }

    public String getPosterPath() {
        return poster_path;
    }
    public void setPosterPath(String poster_path) {
        this.poster_path = poster_path;
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
