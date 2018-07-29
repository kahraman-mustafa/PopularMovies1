package com.mustafakahraman.popularmovies1.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mustafakahraman.popularmovies1.R;
import com.mustafakahraman.popularmovies1.data.DateConverter;
import com.mustafakahraman.popularmovies1.helper.NetworkUtils;
import com.mustafakahraman.popularmovies1.model.Movie;
import com.squareup.picasso.Picasso;

public class MovieDetail extends AppCompatActivity {

    //ActivityMovieDetailBinding mBinding;

    Movie mMovie = new Movie();

    private ImageView imgPoster;
    private TextView tvTitle, tvDate, tvPlotsynopsis;
    private RatingBar rbVoteavg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);
        //mBinding = DataBindingUtil.setContentView(this, R.layout.activity_movie_detail);

        initializeViews();
        Intent intentOpenedMovieDetail = getIntent();
        if (intentOpenedMovieDetail == null) {
            closeOnError();
        }

        setMovieDataByIntent(intentOpenedMovieDetail);
        populateUIWithMovieData();
    }

    private void initializeViews() {
        imgPoster = (ImageView) findViewById(R.id.img_poster);
        tvTitle = (TextView) findViewById(R.id.tv_title);
        tvDate = (TextView) findViewById(R.id.tv_date);
        tvPlotsynopsis = (TextView) findViewById(R.id.tv_plotsynopsis);
        rbVoteavg = (RatingBar) findViewById(R.id.rb_voteavg);
    }

    // invoked when the activity may be temporarily destroyed, save the instance state here
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(getString(R.string.INTENT_KEY_MOVIE), mMovie);

        // call superclass to save any view hierarchy
        super.onSaveInstanceState(outState);
    }

    // This callback is called only when there is a saved instance that is previously saved by using
    // onSaveInstanceState(). We restore some state in onCreate(), while we can optionally restore
    // other state here, possibly usable after onStart() has completed.
    // The savedInstanceState Bundle is same as the one used in onCreate().
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        mMovie = savedInstanceState.getParcelable(getString(R.string.INTENT_KEY_MOVIE));
    }

    private void setMovieDataByIntent(Intent intent){

        if(intent.hasExtra(getString(R.string.INTENT_KEY_MOVIE))){
            mMovie = (Movie) getIntent().getParcelableExtra(getString(R.string.INTENT_KEY_MOVIE));
        }  else {
            closeOnError();
            return;
        }

    }

    private void populateUIWithMovieData(){
        //rbVoteAvg.setNumStars(10);
        //rbVoteAvg.setMax(10);
        //rbVoteAvg.setStepSize(0.1f);

        Picasso.with(this)
                .load(NetworkUtils.buildPosterUrl(
                        this,
                        getString(R.string.POSTER_SIZE_W342),
                        mMovie.getPosterUrl()))
                .placeholder(R.drawable.poster_placeholder)
                //.resize(R.dimens.poster_width, R.dimens.poster_height)
                .error(R.drawable.poster_placeholder)
                .into(imgPoster);
        // TODO (6): Resize poster for different screen densities

        tvTitle.setText(mMovie.getTitle());
        tvDate.setText(DateConverter.toStringDate(mMovie.getDate()));
        tvPlotsynopsis.setText(mMovie.getPlotSynopsis());
        rbVoteavg.setRating((float) mMovie.getVoteAvg());
    }

    private void closeOnError() {
        finish();
        Toast.makeText(this, R.string.detail_error_message, Toast.LENGTH_SHORT).show();
    }
}
