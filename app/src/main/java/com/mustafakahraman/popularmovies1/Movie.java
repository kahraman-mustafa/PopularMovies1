package com.mustafakahraman.popularmovies1;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by kahraman on 17.04.2018.
 */

public class Movie implements Parcelable {

    private long _id;
    private String title;
    private String date;
    private String posterUrl;
    private double voteAvg;
    private String plotSynopsis;
    private boolean isFavorite;

    public Movie(long _id, String title, String date, String posterUrl, double voteAvg, String plotSynopsis, boolean isFavorite) {
        this._id = _id;
        this.title = title;
        this.date = date;
        this.posterUrl = posterUrl;
        this.voteAvg = voteAvg;
        this.plotSynopsis = plotSynopsis;
        this.isFavorite = isFavorite;
    }

    public Movie() {
        this._id = -1;
        this.title = "Title Not Available";
        this.date = "Date Not Available";
        this.posterUrl = "";
        this.voteAvg = 0;
        this.plotSynopsis = "Story Not Available";
        this.isFavorite = false;
    }

    // This is where you write the values you want to save to the `Parcel`.
    // The `Parcel` class has methods defined to help you save all of your values.
    // Note that there are only methods defined for simple values, lists, and other Parcelable objects.
    // You may need to make several classes Parcelable to send the data you want.
    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeLong(_id);
        out.writeDouble(voteAvg);
        out.writeInt((int) (isFavorite ? 1 : 0));
        out.writeString(title);
        out.writeString(date);
        out.writeString(posterUrl);
        out.writeString(plotSynopsis);
        // out.writeParcelable(mInfo, flags);
    }

    // Using the `in` variable, we can retrieve the values that
    // we originally wrote into the `Parcel`.  This constructor is usually
    // private so that only the `CREATOR` field can access.
    private Movie(Parcel in) {
        _id = in.readLong();
        voteAvg = in.readDouble();
        isFavorite = in.readInt() != 0;
        title = in.readString();
        date = in.readString();
        posterUrl = in.readString();
        plotSynopsis = in.readString();
        //mInfo = in.readParcelable(MySubParcelable.class.getClassLoader());
    }

    // In the vast majority of cases you can simply return 0 for this.
    // There are cases where you need to use the constant `CONTENTS_FILE_DESCRIPTOR`
    // But this is out of scope of this tutorial
    @Override
    public int describeContents() {
        return 0;
    }

    // After implementing the `Parcelable` interface, we need to create the
    // `Parcelable.Creator<MyParcelable> CREATOR` constant for our class;
    // Notice how it has our class specified as its type.
    public static final Parcelable.Creator<Movie> CREATOR
            = new Parcelable.Creator<Movie>() {

        // This simply calls our new constructor (typically private) and
        // passes along the unmarshalled `Parcel`, and then returns the new object!
        @Override
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        // We just need to copy this and change the type to match our class.
        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

    public long get_id() {
        return _id;
    }

    public void set_id(long _id) {
        this._id = _id;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public void toggleFavorite() {
        isFavorite = !isFavorite;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    public double getVoteAvg() {
        return voteAvg;
    }

    public void setVoteAvg(double voteAvg) {
        this.voteAvg = voteAvg;
    }

    public String getPlotSynopsis() {
        return plotSynopsis;
    }

    public void setPlotSynopsis(String plotSynopsis) {
        this.plotSynopsis = plotSynopsis;
    }

    @Override
    public String toString() {
        return "Movie{" +
                "_id=" + _id +
                ", title='" + title + '\'' +
                ", date='" + date + '\'' +
                ", posterUrl='" + posterUrl + '\'' +
                ", voteAvg=" + voteAvg +
                ", plotSynopsis='" + plotSynopsis + '\'' +
                ", isFavorite=" + isFavorite +
                '}';
    }
}
