package com.mustafakahraman.popularmovies1.ui;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.util.Log;

import com.mustafakahraman.popularmovies1.data.AppDatabase;
import com.mustafakahraman.popularmovies1.helper.AppExecutors;
import com.mustafakahraman.popularmovies1.model.Movie;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CatalogViewModel extends AndroidViewModel {

    // Constant for logging
    private static final String TAG = CatalogViewModel.class.getSimpleName();

    AppDatabase mDb = AppDatabase.getInstance(this.getApplication());

    private MutableLiveData<List<Movie>> popularMovies;
    private MutableLiveData<List<Movie>> topRatedMovies;
    private LiveData<List<Movie>> favoriteMovies;

    public CatalogViewModel(Application application) {
        super(application);
        popularMovies = new MutableLiveData<List<Movie>>();
        topRatedMovies = new MutableLiveData<List<Movie>>();
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

        Log.d(TAG, "CatalogViewModel - constructor loaded movies from database");

    }

    public MutableLiveData<List<Movie>> getPopularMovies() {
        Log.d(TAG, "CatalogViewModel - getPopularMovies()");
        return popularMovies;
    }

    public MutableLiveData<List<Movie>> getTopRatedMovies() {
        Log.d(TAG, "CatalogViewModel - getTopRatedMovies()");
        return topRatedMovies;
    }

    public LiveData<List<Movie>> getFavoriteMovies() {
        Log.d(TAG, "CatalogViewModel - getFavoriteMovies()");
        return favoriteMovies;
    }
}
