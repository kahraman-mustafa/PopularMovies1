package com.mustafakahraman.popularmovies1.ui;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import com.mustafakahraman.popularmovies1.data.AppDatabase;
import com.mustafakahraman.popularmovies1.helper.AppExecutors;
import com.mustafakahraman.popularmovies1.model.Movie;

import java.util.List;

public class MoviesViewModel extends AndroidViewModel {

    AppDatabase mDb = AppDatabase.getInstance(this.getApplication());

    private MutableLiveData<List<Movie>> popularMovies = new MutableLiveData<>();
    private MutableLiveData<List<Movie>> topRatedMovies = new MutableLiveData<>();
    private LiveData<List<Movie>> favoriteMovies;
    private LiveData<List<Long>> favoriteMovieIds;
    private MutableLiveData<Boolean> isFetchingProgress = new MutableLiveData<>();

    public MoviesViewModel(Application application) {
        super(application);

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

    public MutableLiveData<Boolean> getIsFetchingProgress() {
        return isFetchingProgress;
    }
}
