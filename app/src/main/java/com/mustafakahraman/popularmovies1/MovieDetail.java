package com.mustafakahraman.popularmovies1;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mustafakahraman.popularmovies1.helper.NetworkUtils;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MovieDetail extends AppCompatActivity {

    Movie mMovie = new Movie();

    @BindView(R.id.img_poster) ImageView imgPoster;
    @BindView (R.id.tv_title) TextView tvTitle;
    @BindView (R.id.tv_date) TextView tvDate;
    @BindView (R.id.rb_voteavg) RatingBar rbVoteAvg;
    @BindView (R.id.tv_plotsynopsis) TextView tvPlotSynopsis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        Intent intentOpenedMovieDetail = getIntent();
        if (intentOpenedMovieDetail == null) {
            closeOnError();
        }

        setMovieDataByIntent(intentOpenedMovieDetail);
        populateUIWithMovieData();
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

        /* Before learning Parcelable
        if(intent.hasExtra(getString(R.string.INTENT_KEY_ID))){
            mMovie.set_id(intent.getLongExtra(getString(R.string.INTENT_KEY_ID), -1));
            if (mMovie.get_id() == -1) {
                // INTENT_KEY_ID not found in intent
                closeOnError();
                return;
            }
        }
        if(intent.hasExtra(getString(R.string.INTENT_KEY_TITLE))){
            mMovie.setTitle(intent.getStringExtra(getString(R.string.INTENT_KEY_TITLE)));
        }
        if(intent.hasExtra(getString(R.string.INTENT_KEY_DATE))){
            mMovie.setDate(intent.getStringExtra(getString(R.string.INTENT_KEY_DATE)));
        }
        if(intent.hasExtra(getString(R.string.INTENT_KEY_POSTERURL))){
            mMovie.setPosterUrl(intent.getStringExtra(getString(R.string.INTENT_KEY_POSTERURL)));
        }
        if(intent.hasExtra(getString(R.string.INTENT_KEY_PLOTSYNOPSIS))){
            mMovie.setPlotSynopsis(intent.getStringExtra(getString(R.string.INTENT_KEY_PLOTSYNOPSIS)));
        }
        if(intent.hasExtra(getString(R.string.INTENT_KEY_VOTEAVG))){
            mMovie.setVoteAvg(intent.getDoubleExtra(getString(R.string.INTENT_KEY_VOTEAVG), 0));
        }
        */
    }

    private void populateUIWithMovieData(){
        ButterKnife.bind(this);

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
        tvDate.setText(mMovie.getDate());
        tvPlotSynopsis.setText(mMovie.getPlotSynopsis());
        rbVoteAvg.setRating((float) mMovie.getVoteAvg());
    }

    private void closeOnError() {
        finish();
        Toast.makeText(this, R.string.detail_error_message, Toast.LENGTH_SHORT).show();
    }
}
