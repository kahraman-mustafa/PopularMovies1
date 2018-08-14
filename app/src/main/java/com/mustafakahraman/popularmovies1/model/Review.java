package com.mustafakahraman.popularmovies1.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.net.MalformedURLException;
import java.net.URL;

@Entity(tableName = "review")
public class Review implements Parcelable {

    @PrimaryKey @NonNull
    private String _id;
    @ColumnInfo(name = "movie_id")
    private long movieId;
    @ColumnInfo(name = "author")
    private String author;
    @ColumnInfo(name = "content")
    private String content;
    @ColumnInfo(name = "url")
    private String url;

    public Review(String _id, long movieId, String author, String content, String url) {
        this._id = _id;
        this.movieId = movieId;
        this.author = author;
        this.content = content;
        this.url = url;
    }

    @Ignore
    public Review() {
        this._id = "Not Available";
        this.movieId = -1;
        this.author = "Not Available";
        this.content = "Not Available";
        this.url = "Not Available";
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

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public URL getUrlUrl() {
        URL url = null;

        try {
            url = new URL(this.url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    @Override
    public String toString() {
        return "Review{" +
                "_id=" + _id +
                ", movieId=" + movieId +
                ", author='" + author + '\'' +
                ", content='" + content + '\'' +
                ", url='" + url + '\'' +
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
        out.writeString(author);
        out.writeString(content);
        out.writeString(url);
        // out.writeParcelable(mInfo, flags);
    }

    // Using the `in` variable, we can retrieve the values that
    // we originally wrote into the `Parcel`.  This constructor is usually
    // private so that only the `CREATOR` field can access.
    private Review(Parcel in) {
        _id = in.readString();
        movieId = in.readLong();
        author = in.readString();
        content = in.readString();
        url = in.readString();
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
    public static final Parcelable.Creator<Review> CREATOR
            = new Parcelable.Creator<Review>() {

        // This simply calls our new constructor (typically private) and
        // passes along the unmarshalled `Parcel`, and then returns the new object!
        @Override
        public Review createFromParcel(Parcel in) {
            return new Review(in);
        }

        // We just need to copy this and change the type to match our class.
        @Override
        public Review[] newArray(int size) {
            return new Review[size];
        }
    };
}
