package com.mustafakahraman.popularmovies1.data;

/**
 * Created by kahraman on 17.04.2018.
 */

public class Movie {

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
