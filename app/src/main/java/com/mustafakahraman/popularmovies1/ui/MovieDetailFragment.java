package com.mustafakahraman.popularmovies1.ui;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mustafakahraman.popularmovies1.R;
import com.mustafakahraman.popularmovies1.data.DateConverter;
import com.mustafakahraman.popularmovies1.helper.NetworkUtils;
import com.mustafakahraman.popularmovies1.model.Movie;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import timber.log.Timber;

public class MovieDetailFragment extends Fragment {

    Movie mMovie;

    @BindView(R.id.img_poster)
    ImageView imgPoster;
    @BindView(R.id.img_favorite)
    ImageView imgFavorite;
    @BindView(R.id.rb_voteavg) RatingBar rbVoteavg;
    @BindView(R.id.tv_title) TextView tvTitle;
    @BindView(R.id.tv_date) TextView tvDate;
    @BindView(R.id.tv_plotsynopsis) TextView tvPlotsynopsis;
    private Unbinder unbinder;

    MovieDetailViewModel movieDetailViewModel;
    MovieDetailViewModel viewModelToPassWhenTwoPane;

    private boolean mIsTwoPane = false;

    public MovieDetailFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.movie_detail_fragment, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        Timber.d("onCreateView() - Started");

        try {
            mIsTwoPane = !(getActivity().findViewById(R.id.llay_twopane_fragment) == null);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        Timber.d("onCreateView() - Finished");
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        Timber.d("onActivityCreated() - Started");
        super.onActivityCreated(savedInstanceState);

        movieDetailViewModel = ViewModelProviders.of(this).get(MovieDetailViewModel.class);
        setupMovieDetailViewModelObserver();

        if(movieDetailViewModel.getMovie().getValue() != null) {
            Timber.d("movieDetailViewModel value found");
            mMovie = movieDetailViewModel.getMovie().getValue();
            populateUIWithMovieData();
        } else {
        /*
        if it is two pane, it means two fragment in MovieCatalog activity
         and clicked movie info is passed via activity contexted MovieDetailViewModel (viewModelToPassWhenTwoPane)
         so it will be transferred to fragment contexted MovieDetailViewModel instance (movieDetailViewModel)
         it is because when device is rotated it is possible to be not two pane and then
         activity contexted MovieViewModel will be reconstructed and stored movie info disappear
         because of the fact that owner activity of the fragment is no longer Catalog activity but Detail activity

         All in all, fragment contexted view model however always has the same movie info regardless of the owner activity
         it is important to ensure that when a movie clicked in catalog activity, data will be transferred to "movieDetailViewModel",
         either by an intent which is the case when not two pane so detail fragment will be hosted by MovieDetail activity
         or by activity contexted "viewModelToPassWhenTwoPane" which is the case when two pane so detail fragment will be hosted by MovieCatalog activity

         When two pane movie is transferred like:
              viewModelToPassWhenTwoPane -> movieDetailViewModel
         When not two pane movie is transferred like:
              intent -> movieDetailViewModel

         in all conditions movie info is stored in movieDetailViewModel
         */
            if (mIsTwoPane) {
                Timber.d("viewModelToPassWhenTwoPane info");
                viewModelToPassWhenTwoPane = ViewModelProviders.of(getActivity()).get(MovieDetailViewModel.class);
                movieDetailViewModel.getMovie().postValue(viewModelToPassWhenTwoPane.getMovie().getValue());
            } else {
                try {
                    Timber.d("Intent info");
                    setMovieDataByIntent(getActivity().getIntent());
                } catch (NullPointerException e) {
                    closeOnError();
                    e.printStackTrace();
                }
            }
        }
        Timber.d("onActivityCreated() - Finished");
    }

    @Override
    public void onResume() {
        super.onResume();
        Timber.d("onResume() started");
    }

    @Override
    public void onDestroyView() {
        Timber.d("onDestroyView() - Started");
        super.onDestroyView();
        unbinder.unbind();
        Timber.d("onDestroyView() - Finished");
    }

    private void setMovieDataByIntent(Intent intent){
        if(intent.hasExtra(getString(R.string.INTENT_KEY_MOVIE))){
            Movie movie = (Movie) intent.getParcelableExtra(getString(R.string.INTENT_KEY_MOVIE));
            movieDetailViewModel.getMovie().postValue(movie);
        }  else {
            closeOnError();
        }
    }
    private void closeOnError() {
        try {
            getActivity().finish();
            Toast.makeText(getActivity(), R.string.detail_error_message, Toast.LENGTH_SHORT).show();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private void setupMovieDetailViewModelObserver() {
        movieDetailViewModel.getMovie().observe(this, new Observer<Movie>() {
            @Override
            public void onChanged(@Nullable Movie movie) {
                Timber.d("movieDetailViewModel observer change");
                mMovie = movie;
                populateUIWithMovieData();
            }
        });
    }

    @OnClick(R.id.img_favorite)
    public void onClickFavorite() {
        if(mMovie.isFavorite()) {
            imgFavorite.setImageResource(R.drawable.star_disabled);
        } else {
            imgFavorite.setImageResource(R.drawable.star_enabled);
        }
        movieDetailViewModel.toggleFavorite();
    }

    private void populateUIWithMovieData(){
        //rbVoteAvg.setNumStars(10);
        //rbVoteAvg.setMax(10);
        //rbVoteAvg.setStepSize(0.1f);
        Timber.d("populateUIWithMovieData started");
        Picasso.with(getActivity())
                .load(NetworkUtils.buildPosterUrl(
                        getActivity(),
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
        Timber.d("Average rating %s :", (float) mMovie.getVoteAvg());

        if(mMovie.isFavorite()) {
            imgFavorite.setImageResource(R.drawable.star_enabled);
        } else {
            imgFavorite.setImageResource(R.drawable.star_disabled);
        }
        Timber.d("populateUIWithMovieData finished");
    }
}
