package com.mustafakahraman.popularmovies1;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mustafakahraman.popularmovies1.data.Movie;
import com.mustafakahraman.popularmovies1.data.NetworkUtils;
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

    private void setMovieDataByIntent(Intent intent){
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
    }

    private void populateUIWithMovieData(){
        ButterKnife.bind(this);

        Picasso.with(this)
                .load(NetworkUtils.buildPosterUrl(
                        this,
                        getString(R.string.POSTER_SIZE_W185),
                        mMovie.getPosterUrl()))
                .into(imgPoster);

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
