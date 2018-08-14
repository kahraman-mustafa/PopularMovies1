package com.mustafakahraman.popularmovies1.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.net.MalformedURLException;
import java.net.URL;

@Entity(tableName = "video")
public class Video implements Parcelable {

    @PrimaryKey @NonNull
    private String _id;
    @ColumnInfo(name = "movie_id")
    private long movieId;
    // App can handle only YouTube urls currently
    @ColumnInfo(name = "youtube_suffix")
    private String youtubeSuffix;
    @ColumnInfo(name = "name")
    private String name;
    @ColumnInfo(name = "type")
    private String type;
    @Ignore
    private URL videoUrl;

    public Video(String _id, long movieId, String youtubeSuffix, String name, String type) {
        this._id = _id;
        this.movieId = movieId;
        this.youtubeSuffix = youtubeSuffix;
        this.name = name;
        this.type = type;
    }

    @Ignore
    public Video() {
        this._id = "Not Available";
        this.movieId = -1;
        this.youtubeSuffix = "Not Available";
        this.name = "Not Available";
        this.type = "Not Available";
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public long getMovieId() {
        return movieId;
    }

    public void setMovieId(long movieId) {
        this.movieId = movieId;
    }

    public String getYoutubeSuffix() {
        return youtubeSuffix;
    }

    public void setYoutubeSuffix(String youtubeSuffix) {
        this.youtubeSuffix = youtubeSuffix;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public URL getVideoUrl() {
        return buildVideoUrl();
    }

    private URL buildVideoUrl() {
        Uri.Builder builder = new Uri.Builder();
        Uri queryUri = builder.scheme("https")
                .authority("www.youtube.com")
                .appendPath("watch")
                .appendQueryParameter("v", youtubeSuffix).build();

        URL queryUrl = null;

        try {
            queryUrl = new URL(queryUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return queryUrl;
    }

    @Override
    public String toString() {
        return "Video{" +
                "_id=" + _id +
                ", movieId=" + movieId +
                ", youtubeSuffix='" + youtubeSuffix + '\'' +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", videoUrl=" + videoUrl +
                '}';
    }

    // This is where you write the values you want to save to the `Parcel`.
    // The `Parcel` class has methods defined to help you save all of your values.
    // Note that there are only methods defined for simple values, lists, and other Parcelable objects.
    // You may need to make several classes Parcelable to send the data you want.
    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(_id);
        out.writeLong(movieId);
        out.writeString(youtubeSuffix);
        out.writeString(name);
        out.writeString(type);
        // out.writeParcelable(mInfo, flags);
    }

    // Using the `in` variable, we can retrieve the values that
    // we originally wrote into the `Parcel`.  This constructor is usually
    // private so that only the `CREATOR` field can access.
    private Video(Parcel in) {
        _id = in.readString();
        movieId = in.readLong();
        youtubeSuffix = in.readString();
        name = in.readString();
        type = in.readString();
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
    public static final Parcelable.Creator<Video> CREATOR
            = new Parcelable.Creator<Video>() {

        // This simply calls our new constructor (typically private) and
        // passes along the unmarshalled `Parcel`, and then returns the new object!
        @Override
        public Video createFromParcel(Parcel in) {
            return new Video(in);
        }

        // We just need to copy this and change the type to match our class.
        @Override
        public Video[] newArray(int size) {
            return new Video[size];
        }
    };

}
