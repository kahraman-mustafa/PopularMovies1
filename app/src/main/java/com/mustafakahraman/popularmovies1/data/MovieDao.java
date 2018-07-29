package com.mustafakahraman.popularmovies1.data;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.mustafakahraman.popularmovies1.model.Movie;

import java.util.List;

@Dao
public interface MovieDao {

    @Query("SELECT * FROM movie WHERE is_favorite = 1 ORDER BY vote_avg")
    LiveData<List<Movie>> loadFavoriteMovies();

    @Query("SELECT COUNT(*) FROM movie WHERE is_favorite = 1")
    long getNumberOfSavedFavoriteMovies();

    /*@Query("SELECT * FROM movie WHERE is_popular = 1")
    LiveData<List<Movie>> loadPopularMovies();

    @Query("SELECT * FROM movie WHERE is_top_rated = 1")
    LiveData<List<Movie>> loadTopRatedMovies();

    @Query("SELECT COUNT(*) FROM movie WHERE is_popular = 1")
    long getNumberOfSavedPopularMovies();

    @Query("SELECT COUNT(*) FROM movie WHERE is_top_rated = 1")
    long getNumberOfSavedTopRatedMovies();*/

    @Insert
    void insertMovie(Movie movie);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateMovie(Movie movie);

    @Delete
    void deleteMovie(Movie movie);

    /*@Query("DELETE FROM movie WHERE is_favorite = 0")
    void deleteNonFavoriteMovies();*/

    @Query("SELECT * FROM movie WHERE _id = :id")
    LiveData<Movie> loadMovieById(int id);
}
