package com.example.mainmachine.popularmoviep1;

import android.os.Parcel;
import android.os.Parcelable;

class Movie implements Parcelable{
    String name;
    String id;
    String posterUri;
    String overview;
    String voteAvg;
    String releaseDate;

    public Movie(Parcel in){
        this.name = in.readString();
        this.id = in.readString();
        this.posterUri = in.readString();
        this.overview = in.readString();
        this.voteAvg = in.readString();
        this.releaseDate = in.readString();
    }

    public Movie(){
    }

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
