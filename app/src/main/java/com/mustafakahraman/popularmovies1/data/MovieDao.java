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
import com.mustafakahraman.popularmovies1.model.Review;
import com.mustafakahraman.popularmovies1.model.Video;

import java.util.List;

@Dao
public interface MovieDao {

    @Query("SELECT _id FROM movie WHERE is_favorite = 1")
    LiveData<List<Long>> getFavoriteMovieIds();

    @Query("SELECT * FROM movie WHERE is_favorite = 1 ORDER BY vote_avg")
    LiveData<List<Movie>> loadFavoriteMovies();

    @Query("SELECT review.* FROM movie, review WHERE movie._id = review.movie_id AND movie.is_favorite = 1")
    LiveData<List<Review>> loadReviewsOfFavoriteMovies();

    @Query("SELECT video.* FROM movie, video WHERE movie._id = video.movie_id AND movie.is_favorite = 1")
    LiveData<List<Video>> loadVideosOfFavoriteMovies();

    @Insert
    void insertMovie(Movie movie);

    /*Marks a method in a Dao annotated class as an insert method.
    The implementation of the method will insert its parameters into the database.
    All of the parameters of the Insert method must either be classes annotated with Entity or collections/array of it.
    Example:
    @Dao
    public interface MyDao {
        @Insert(onConflict = OnConflictStrategy.REPLACE)
        public void insertUsers(User... users);
        @Insert
        public void insertBoth(User user1, User user2);
        @Insert
        public void insertWithFriends(User user, List<User> friends);
    }*/

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertReviews(List<Review> reviews);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertVideos(List<Video> videos);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateMovie(Movie movie);

    /*Marks a method in a Dao annotated class as a delete method.
    The implementation of the method will delete its parameters from the database.
    All of the parameters of the Delete method must either be classes annotated with Entity or collections/array of it.
    Example:
    @Dao
    public interface MyDao {
        @Delete
        public void deleteUsers(User... users);
        @Delete
        public void deleteAll(User user1, User user2);
        @Delete
        public void deleteWithFriends(User user, List<User> friends);
    }*/

    @Delete
    void deleteMovie(Movie movie);

    @Delete
    void deleteReviews(List<Review> reviews);

    @Delete
    void deleteVideos(List<Video> videos);
}
