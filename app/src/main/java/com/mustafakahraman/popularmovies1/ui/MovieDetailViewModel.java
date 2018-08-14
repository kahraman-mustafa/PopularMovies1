package com.mustafakahraman.popularmovies1.ui;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;

import com.mustafakahraman.popularmovies1.data.AppDatabase;
import com.mustafakahraman.popularmovies1.helper.AppExecutors;
import com.mustafakahraman.popularmovies1.model.Movie;
import com.mustafakahraman.popularmovies1.model.Review;
import com.mustafakahraman.popularmovies1.model.Video;

import java.util.List;

public class MovieDetailViewModel extends AndroidViewModel {

    AppDatabase mDb = AppDatabase.getInstance(this.getApplication());

    private MutableLiveData<Movie> movie = new MutableLiveData<>();
    private MutableLiveData<List<Review>> reviews = new MutableLiveData<>();
    private MutableLiveData<List<Video>> videos = new MutableLiveData<>();

    public MovieDetailViewModel(@NonNull Application application) {
        super(application);
    }

    public MutableLiveData<Movie> getMovie() {
        return movie;
    }

    public MutableLiveData<List<Review>> getReviews() {
        return reviews;
    }

    public MutableLiveData<List<Video>> getVideos() {
        return videos;
    }

    public void toggleFavorite() {
        boolean curFav = movie.getValue().isFavorite();

        if(curFav) {
            AppExecutors.getInstance().diskIO().execute(new Runnable() {
                @Override
                public void run() {
                    mDb.movieDao().deleteMovie(movie.getValue());
                    mDb.movieDao().deleteReviews(reviews.getValue());
                    mDb.movieDao().deleteVideos(videos.getValue());
                }
            });
        } else {
            AppExecutors.getInstance().diskIO().execute(new Runnable() {
                @Override
                public void run() {
                    mDb.movieDao().insertMovie(movie.getValue());
                    mDb.movieDao().insertReviews(reviews.getValue());
                    mDb.movieDao().insertVideos(videos.getValue());
                }
            });
        }

        movie.getValue().setFavorite(!curFav);
    }
}
