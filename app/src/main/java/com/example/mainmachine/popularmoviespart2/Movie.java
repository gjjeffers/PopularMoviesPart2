package com.example.mainmachine.popularmoviespart2;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

class Movie implements Parcelable{
    String name;
    String id;
    String posterUri;
    String overview;
    String voteAvg;
    String releaseDate;
    String trailer[];
    String trailerId[];
    String reviewer[];
    String review[];

    public Movie(Parcel in){
        this.name = in.readString();
        this.id = in.readString();
        this.posterUri = in.readString();
        this.overview = in.readString();
        this.voteAvg = in.readString();
        this.releaseDate = in.readString();
        this.trailer = in.createStringArray();
        this.trailerId = in.createStringArray();
        this.reviewer = in.createStringArray();
        this.review = in.createStringArray();
    }

    public Movie(JSONObject movie){
        try {
            this.name = movie.getString("original_title");
            this.id = movie.getString("id");
            if (!movie.getString("poster_path").equals("null")) {
                this.posterUri = "http://image.tmdb.org/t/p/w185" + movie.getString("poster_path");
            }
            this.overview = movie.getString("overview");
            this.voteAvg = movie.getString("vote_average");
            this.releaseDate = movie.getString("release_date");
        }
        catch (JSONException e){
            Log.e("Parsing Error: ", e.getMessage());
        }
    }

    public Movie(){}

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(id);
        dest.writeString(posterUri);
        dest.writeString(overview);
        dest.writeString(voteAvg);
        dest.writeString(releaseDate);
        dest.writeStringArray(trailer);
        dest.writeStringArray(trailerId);
        dest.writeStringArray(reviewer);
        dest.writeStringArray(review);
    }

    public static final Parcelable.Creator<Movie> CREATOR = new Parcelable.Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };
}
