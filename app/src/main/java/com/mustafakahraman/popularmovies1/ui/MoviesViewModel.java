package com.mustafakahraman.popularmovies1.ui;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import com.mustafakahraman.popularmovies1.R;
import com.mustafakahraman.popularmovies1.data.AppDatabase;
import com.mustafakahraman.popularmovies1.helper.AppExecutors;
import com.mustafakahraman.popularmovies1.model.Movie;
import com.mustafakahraman.popularmovies1.model.Review;
import com.mustafakahraman.popularmovies1.model.Video;

import java.util.ArrayList;
import java.util.List;

public class MoviesViewModel extends AndroidViewModel {

    AppDatabase mDb = AppDatabase.getInstance(this.getApplication());

    private MutableLiveData<List<Movie>> popularMovies = new MutableLiveData<>();
    private MutableLiveData<List<Movie>> topRatedMovies = new MutableLiveData<>();
    private LiveData<List<Movie>> favoriteMovies;
    private LiveData<List<Long>> favoriteMovieIds;
    private LiveData<List<Review>> favoriteMovieReviews;
    private LiveData<List<Video>> favoriteMovieVideos;
    private MutableLiveData<List<Review>> reviews = new MutableLiveData<>();
    private MutableLiveData<List<Video>> videos = new MutableLiveData<>();
    private MutableLiveData<List<Long>> reviewMovieIds = new MutableLiveData<>();
    private MutableLiveData<List<Long>> videoMovieIds = new MutableLiveData<>();
    private MutableLiveData<Boolean> isFetchingProgress = new MutableLiveData<>();
    private MutableLiveData<Integer> errorMessage = new MutableLiveData<>();
    private MutableLiveData<Boolean> isErrorOccured = new MutableLiveData<>();

    public MoviesViewModel(Application application) {
        super(application);

        popularMovies.setValue(new ArrayList<Movie>());
        topRatedMovies.setValue(new ArrayList<Movie>());
        reviews.setValue(new ArrayList<Review>());
        videos.setValue(new ArrayList<Video>());
        reviewMovieIds.setValue(new ArrayList<Long>());
        videoMovieIds.setValue(new ArrayList<Long>());
        isFetchingProgress.setValue(false);
        isErrorOccured.setValue(false);
        errorMessage.setValue(R.string.error_message);

        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    favoriteMovies = mDb.movieDao().loadFavoriteMovies();
                } catch(Exception e) {
                    e.printStackTrace();
                    favoriteMovies = new MutableLiveData<List<Movie>>();
                }
            }
        });

        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    favoriteMovieIds = mDb.movieDao().getFavoriteMovieIds();
                } catch(Exception e) {
                    e.printStackTrace();
                    favoriteMovieIds = new MutableLiveData<List<Long>>();
                }
            }
        });

        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    favoriteMovieReviews = mDb.movieDao().loadReviewsOfFavoriteMovies();
                } catch(Exception e) {
                    e.printStackTrace();
                    favoriteMovieReviews = new MutableLiveData<List<Review>>();
                }
            }
        });

        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    favoriteMovieVideos = mDb.movieDao().loadVideosOfFavoriteMovies();
                } catch(Exception e) {
                    e.printStackTrace();
                    favoriteMovieVideos = new MutableLiveData<List<Video>>();
                }
            }
        });
    }

    public MutableLiveData<List<Movie>> getPopularMovies() {
        return popularMovies;
    }

    public MutableLiveData<List<Movie>> getTopRatedMovies() {
        return topRatedMovies;
    }

    public LiveData<List<Movie>> getFavoriteMovies() {
        return favoriteMovies;
    }

    public LiveData<List<Long>> getFavoriteMovieIds() {
        return favoriteMovieIds;
    }

    public LiveData<List<Review>> getFavoriteMovieReviews() {
        return favoriteMovieReviews;
    }

    public LiveData<List<Video>> getFavoriteMovieVideos() {
        return favoriteMovieVideos;
    }

    public MutableLiveData<List<Review>> getReviews() {
        return reviews;
    }

    public MutableLiveData<List<Video>> getVideos() {
        return videos;
    }

    public MutableLiveData<List<Long>> getReviewMovieIds() {
        return reviewMovieIds;
    }

    public MutableLiveData<List<Long>> getVideoMovieIds() {
        return videoMovieIds;
    }

    public MutableLiveData<Boolean> getIsErrorOccured() {
        return isErrorOccured;
    }

    public MutableLiveData<Integer> getErrorMessage() {
        return errorMessage;
    }

    public MutableLiveData<Boolean> getIsFetchingProgress() {
        return isFetchingProgress;
    }
}
