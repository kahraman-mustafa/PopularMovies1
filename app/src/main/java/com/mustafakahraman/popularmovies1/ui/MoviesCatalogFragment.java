package com.mustafakahraman.popularmovies1.ui;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mustafakahraman.popularmovies1.R;
import com.mustafakahraman.popularmovies1.helper.AppExecutors;
import com.mustafakahraman.popularmovies1.helper.Connection;
import com.mustafakahraman.popularmovies1.helper.ConnectionViewModel;
import com.mustafakahraman.popularmovies1.helper.ItemOffsetDecoration;
import com.mustafakahraman.popularmovies1.helper.NetworkUtils;
import com.mustafakahraman.popularmovies1.helper.SortingOrderModel;
import com.mustafakahraman.popularmovies1.model.Movie;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import timber.log.Timber;

public class MoviesCatalogFragment extends Fragment implements CatalogAdapter.ItemClickListener{

    private MoviesViewModel moviesViewModel;
    MovieDetailViewModel movieDetailViewModel;
    private SortingOrderModel sortingOrderModel;
    private ConnectionViewModel conViewModel;

    @BindView(R.id.rv_movie_catalog) RecyclerView rvMovieCatalog;
    @BindView(R.id.pb_loading_bar) ProgressBar pbLoadingBar;
    @BindView(R.id.tv_error_message) TextView tvErrorMessage;
    private Unbinder unbinder;

    private CatalogAdapter mCatalogAdapter;
    // Default sort order type
    private String moviesOrderType;

    private Handler sortChangeHandler = new Handler();
    private Handler conChangeHandler = new Handler();
    private Handler favDBListHandler = new Handler();
    private Handler internetControlHandler = new Handler();

    private boolean mIsTwoPane = false;
    private boolean mIsFetchingProgress;

    // Mandatory empty constructor
    public MoviesCatalogFragment() {

    }

    // When a movie is clicked, this initialize opening detail activiy screen using selected movie info
    @Override
    public void onItemClick(int position) {
        Movie movie = mCatalogAdapter.getItemAtPosition(position);
        if(mIsTwoPane) {
            movieDetailViewModel.getMovie().setValue(movie);
        } else {
            startDetailActivity(movie);
        }
    }

    // Helper function to start detail activity
    private void startDetailActivity(Movie movie) {
        Context contextStartFrom = getActivity();
        Class classToOpen = MovieDetail.class;

        Intent intentOpenDetail = new Intent(contextStartFrom, classToOpen);
        intentOpenDetail.putExtra(getString(R.string.INTENT_KEY_MOVIE), movie); // using the (String key, Parcelable value) overload!

        startActivity(intentOpenDetail);
    }

    // Override onAttach to make sure that the container activity has implemented the callback
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }


    // Inflates the GridView of all AndroidMe images
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.movie_catalog_fragment, container, false);

        try {
            if (getActivity().findViewById(R.id.llay_twopane_fragment) == null) {
                mIsTwoPane = false;
            } else {
                mIsTwoPane = true;
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        unbinder = ButterKnife.bind(this, rootView);

        // Return the root view
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // initially display error because if no viewmodel data is available it is required to error be displayed
        // in order to start fetching movies from  internet
        displayLoading();
        Timber.d("Loading displaying started : onCreateView()");

        moviesViewModel = ViewModelProviders.of(this).get(MoviesViewModel.class);

        if(moviesViewModel.getIsFetchingProgress().getValue() == null ||
                moviesViewModel.getIsFetchingProgress().getValue() != null &&
                        !moviesViewModel.getIsFetchingProgress().getValue().equals(true) &&
                        !moviesViewModel.getIsFetchingProgress().getValue().equals(false)) {
            moviesViewModel.getIsFetchingProgress().postValue(false);
            mIsFetchingProgress = false;
            Timber.d("mIsFetchingProgress assigned first time: %s - onCreated()", mIsFetchingProgress);
        } else {
            mIsFetchingProgress = moviesViewModel.getIsFetchingProgress().getValue();
            Timber.d("mIsFetchingProgress assigned: %s - onCreated()", mIsFetchingProgress);
        }

        if(mIsTwoPane) {
            movieDetailViewModel = ViewModelProviders.of(getActivity()).get(MovieDetailViewModel.class);
        }
        conViewModel = ViewModelProviders.of(this).get(ConnectionViewModel.class);
        sortingOrderModel = ViewModelProviders.of(this).get(SortingOrderModel.class);

        if(sortingOrderModel.getSortOder().getValue() == null ||
                sortingOrderModel.getSortOder().getValue() != null &&
                        !sortingOrderModel.getSortOder().getValue().equals(NetworkUtils.ORDER_BY_TOPRATED) &&
                        !sortingOrderModel.getSortOder().getValue().equals(NetworkUtils.ORDER_BY_POPULARITY) &&
                        !sortingOrderModel.getSortOder().getValue().equals(NetworkUtils.ORDER_BY_FAVORITE)) {
            sortingOrderModel.getSortOder().postValue(NetworkUtils.ORDER_BY_POPULARITY);
            moviesOrderType = NetworkUtils.ORDER_BY_POPULARITY;
            Timber.d("moviesOrderType assigned first time: %s - onCreated()", moviesOrderType);
        } else {
            moviesOrderType = sortingOrderModel.getSortOder().getValue();
            Timber.d("moviesOrderType assigned: %s - onCreated()", moviesOrderType);
        }

        setupMovieCatalogUI();

        setupSortingOrderModel();
        setupMoviesViewModel();
        setupConnectionViewModel();

        setHasOptionsMenu(true);

        Timber.d("Called showMoviesFromMemoryOrInternet from : onActivityCreated()");
        showMoviesFromMemoryOrInternet();

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    // Movies data is either downloaded or restored, then show them in UI
    private void setupMovieCatalogUI() {
        /*if (mIsTwoPane) {
            // Make sure our UI is in the correct state.
            try {
                FrameLayout container = (FrameLayout) getActivity().findViewById(R.id.fl_fr_movie_detail);
                container.getChildAt(0).setVisibility(View.INVISIBLE);
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }*/

        int numColumns = getResources().getInteger(R.integer.grid_columns);
        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), numColumns);
        rvMovieCatalog.setLayoutManager(layoutManager);

        int offsetInPixels = getResources().getDimensionPixelSize(R.dimen.item_offset);
        ItemOffsetDecoration itemDecoration = new ItemOffsetDecoration(offsetInPixels, numColumns, true);
        rvMovieCatalog.addItemDecoration(itemDecoration);

        mCatalogAdapter = new CatalogAdapter(getActivity(), moviesViewModel, new ArrayList<Movie>(), moviesOrderType);
        mCatalogAdapter.setClickListener(this);
        rvMovieCatalog.setAdapter(mCatalogAdapter);
    }

    private void setupSortingOrderModel() {
        Timber.d("moviesOrderType observer set : sortingOrderModel()");
        sortingOrderModel.getSortOder().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                moviesOrderType = s;
                sortChangeHandler.postDelayed(new Runnable() {
                    public void run() {
                        Timber.d("moviesOrderType changed: %s : sortingOrderModel-onChanged()", moviesOrderType);
                        Timber.d("Called showMoviesFromMemoryOrInternet from : sortingOrderModel-onChanged()");
                        showMoviesFromMemoryOrInternet();
                    }
                }, 50);   //2 seconds
            }
        });
    }

    private void setupConnectionViewModel() {

        // Internet connection/availability change oberserver initialization

        conViewModel.getConnectionLiveData().observe(this, new Observer<Connection>() {
            @Override
            public void onChanged(@Nullable Connection connection) {
                try {
                    if (connection.getIsConnected()) {
                        switch (connection.getType()) {
                            case Connection.WifiData:
                                Timber.d("ConnectionObserver WifiConnection Available");
                                fetchMoviesIfCatalogNotVisible();
                                break;
                            case Connection.MobileData:
                                Timber.d("ConnectionObserver 3GConnection Available");
                                fetchMoviesIfCatalogNotVisible();
                                break;
                        }
                    } else {
                        NetworkUtils.isInternetAvailable = false;
                        Timber.d("ConnectionObserver Not connected");
                        // Internet disconnected, Do something
                        if (pbLoadingBar.getVisibility() == View.VISIBLE) {
                            displayError();
                        }
                    }
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // This method initializes the live data objects which store movie catalog info
    private void setupMoviesViewModel() {

        moviesViewModel.getIsFetchingProgress().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean isProgress) {
                mIsFetchingProgress = isProgress;
                Timber.d("mIsFetchingProgress changed: %s - moviesViewModel-onChanged()", mIsFetchingProgress);
            }
        });

        moviesViewModel.getPopularMovies().observe(this, new Observer<List<Movie>>() {
            @Override
            public void onChanged(@Nullable List<Movie> movieList) {

                if(moviesOrderType.equals(NetworkUtils.ORDER_BY_POPULARITY)) {
                    if (movieList != null && movieList.size() > 0) {
                        displayCatalog();
                        mCatalogAdapter.setMovieList(movieList);
                    }
                    else {
                        if(pbLoadingBar.getVisibility() != View.VISIBLE) {
                            tvErrorMessage.setText(R.string.error_none_popular);
                            displayError();
                        }
                    }
                }
            }
        });

        moviesViewModel.getFavoriteMovies().observe(this, new Observer<List<Movie>>() {
            @Override
            public void onChanged(@Nullable List<Movie> movieList) {

                if(moviesOrderType.equals(NetworkUtils.ORDER_BY_FAVORITE)) {
                    if (movieList != null && movieList.size() > 0) {
                        displayCatalog();
                        mCatalogAdapter.setMovieList(movieList);
                    } else {
                        if(pbLoadingBar.getVisibility() != View.VISIBLE) {
                            tvErrorMessage.setText(R.string.error_none_favorite);
                            displayError();
                        }
                    }
                } else {
                    mCatalogAdapter.notifyDataSetChanged();
                }
            }
        });

        moviesViewModel.getTopRatedMovies().observe(this, new Observer<List<Movie>>() {
            @Override
            public void onChanged(@Nullable List<Movie> movieList) {

                if(moviesOrderType.equals(NetworkUtils.ORDER_BY_TOPRATED)) {
                    if (movieList != null && movieList.size() > 0) {
                        displayCatalog();
                        mCatalogAdapter.setMovieList(movieList);
                    }
                    else {
                        if(pbLoadingBar.getVisibility() != View.VISIBLE) {
                            tvErrorMessage.setText(R.string.error_none_toprated);
                            displayError();
                        }
                    }
                }
            }
        });

        moviesViewModel.getFavoriteMovieIds().observe(this, new Observer<List<Long>>() {
            @Override
            public void onChanged(@Nullable List<Long> longs) {
                //
            }
        });
    }

    public void showMoviesFromMemoryOrInternet() {

        switch (moviesOrderType){
            case NetworkUtils.ORDER_BY_FAVORITE:
                if(moviesViewModel.getFavoriteMovies().getValue() != null) {
                    if(moviesViewModel.getFavoriteMovies().getValue().size() > 0) {
                        mCatalogAdapter.setMovieList(moviesViewModel.getFavoriteMovies().getValue());
                        displayCatalog();
                    } else {
                        Timber.d( "No favorite movies found to display");
                        tvErrorMessage.setText(R.string.error_none_favorite);
                        displayError();
                    }
                } else {
                    Timber.d( "No favorite movies found to display");
                    tvErrorMessage.setText(R.string.error_none_favorite);
                    displayError();
                }
                break;
            case NetworkUtils.ORDER_BY_POPULARITY:
                if(moviesViewModel.getPopularMovies().getValue() != null) {
                    if(moviesViewModel.getPopularMovies().getValue().size() > 0) {
                        mCatalogAdapter.setMovieList(moviesViewModel.getPopularMovies().getValue());
                        displayCatalog();
                    } else {
                        Timber.d("Called fetchMoviesIfInternetAvailable from : showMoviesFromMemoryOrInternet(), ORDER_BY_POPULARITY");
                        fetchMoviesIfInternetAvailable();
                    }
                } else {
                    Timber.d("Called fetchMoviesIfInternetAvailable from : showMoviesFromMemoryOrInternet(), ORDER_BY_POPULARITY");
                    fetchMoviesIfInternetAvailable();
                }
                break;
            case NetworkUtils.ORDER_BY_TOPRATED:
                if(moviesViewModel.getTopRatedMovies().getValue() != null) {
                    if(moviesViewModel.getTopRatedMovies().getValue().size() > 0) {
                        mCatalogAdapter.setMovieList(moviesViewModel.getTopRatedMovies().getValue());
                        displayCatalog();
                    } else {
                        Timber.d("Called fetchMoviesIfInternetAvailable from : showMoviesFromMemoryOrInternet(), ORDER_BY_TOPRATED");
                        fetchMoviesIfInternetAvailable();
                    }
                } else {
                    Timber.d("Called fetchMoviesIfInternetAvailable from : showMoviesFromMemoryOrInternet(), ORDER_BY_TOPRATED");
                    fetchMoviesIfInternetAvailable();
                }
                break;
            default:
                break;
        }
    }

    private void fetchMoviesIfCatalogNotVisible(){
        conChangeHandler.postDelayed(new Runnable() {
            public void run() {
                if(tvErrorMessage.getVisibility() == View.VISIBLE || pbLoadingBar.getVisibility() == View.VISIBLE &
                        (moviesOrderType.equals(NetworkUtils.ORDER_BY_POPULARITY) ||
                                moviesOrderType.equals(NetworkUtils.ORDER_BY_TOPRATED))) {
                    Timber.d("Called fetchMoviesIfInternetAvailable from : fetchMoviesIfCatalogNotVisible(), Waited 3000 ms");
                    fetchMoviesIfInternetAvailable();
                }
            }
        }, 2100);   //2 seconds
    }

    // Provided that internet is available, download movie data
    // Called only when order type is selected as popularity or top rated
    public void fetchMoviesIfInternetAvailable() {

        if(!mIsFetchingProgress) {

            Timber.d("moviesViewModel.getIsFetchingProgress().postValue(true) : fetchMoviesIfInternetAvailable()");
            moviesViewModel.getIsFetchingProgress().postValue(true);

            displayLoading();
            Timber.d("Loading bar displaying started : in fetchMoviesIfInternetAvailable()");

            if (NetworkUtils.isInternetAvailable) {
                doFetchFromInternet();
            }

            if (!NetworkUtils.isInternetAvailable) {

                NetworkUtils.controlInternetAvailability();

                internetControlHandler.postDelayed(new Runnable() {
                    public void run() {
                        // just wait completion of internet test
                        Timber.d("Waited 1750 ms to check internet availability : in fetchMoviesIfInternetAvailable()");
                        if (NetworkUtils.isInternetAvailable) {
                            doFetchFromInternet();
                        } else {
                            Timber.d("Could not fetch movies because internet not available : in fetchMoviesIfInternetAvailable()");
                            tvErrorMessage.setText(R.string.error_message);
                            Timber.d("moviesViewModel.getIsFetchingProgress().postValue(false) : fetchMoviesIfInternetAvailable()");
                            moviesViewModel.getIsFetchingProgress().postValue(false);
                            displayError();
                        }
                    }
                }, 1750);   //2 seconds
            }
        }
    }

    public void doFetchFromInternet() {
        if(NetworkUtils.isInternetAvailable) {
            Timber.d( "Movie fetching from internet started");
            final String queryUrl = NetworkUtils.buildQueryUrl(moviesOrderType).toString();
            Timber.d("Movie url : %s", queryUrl);

            final ArrayList<Long> favIdList = (ArrayList<Long>) moviesViewModel.getFavoriteMovieIds().getValue();

            favDBListHandler.postDelayed(new Runnable() {
                public void run() {
                    // just for wait db operation
                }
            }, 250);   //2 seconds

            AppExecutors.getInstance().diskIO().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        List<Movie> movieList = NetworkUtils.extractMoviesFromJSON(NetworkUtils.getHttpJSONResponse(queryUrl),favIdList);
                        if (moviesOrderType.equals(NetworkUtils.ORDER_BY_POPULARITY)) {
                            moviesViewModel.getPopularMovies().postValue(movieList);
                        } else if (moviesOrderType.equals(NetworkUtils.ORDER_BY_TOPRATED)) {
                            moviesViewModel.getTopRatedMovies().postValue(movieList);
                        }
                    } catch (JSONException e) {
                        Timber.d( "Could not fetch movie from internet. Error message is shown");
                        tvErrorMessage.setText(R.string.error_message);
                        displayError();
                        e.printStackTrace();
                    } catch (IOException e) {
                        Timber.d( "Could not fetch movie from internet. Error message is shown");
                        tvErrorMessage.setText(R.string.error_message);
                        displayError();
                        e.printStackTrace();
                    } finally {
                        Timber.d("moviesViewModel.getIsFetchingProgress().postValue(false) : doFetchFromInternet()");
                        moviesViewModel.getIsFetchingProgress().postValue(false);
                    }
                }
            });
        } else {
            Timber.d("moviesViewModel.getIsFetchingProgress().postValue(false) : doFetchFromInternet()");
            moviesViewModel.getIsFetchingProgress().postValue(false);
            Timber.d( "Could not fetch movies because internet not available: in doFetchFromInternet()");
            tvErrorMessage.setText(R.string.error_message);
            displayError();
        }
    }


    // Helper method to show loading bar
    public void displayLoading() {
        rvMovieCatalog.setVisibility(View.INVISIBLE);
        tvErrorMessage.setVisibility(View.INVISIBLE);
        pbLoadingBar.setVisibility(View.VISIBLE);
    }

    // Helper method to show movie catalog view
    public void displayCatalog() {
        rvMovieCatalog.setVisibility(View.VISIBLE);
        tvErrorMessage.setVisibility(View.INVISIBLE);
        pbLoadingBar.setVisibility(View.INVISIBLE);
    }

    // Helper method to show error message
    public void displayError() {
        rvMovieCatalog.setVisibility(View.INVISIBLE);
        tvErrorMessage.setVisibility(View.VISIBLE);
        pbLoadingBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO (2): Move the control for the catalog order to settings
        int menuItemIdSelected = item.getItemId();
        MoviesCatalogFragment fragment = new MoviesCatalogFragment();
        switch (menuItemIdSelected) {
            case R.id.menu_item_popular:
                Timber.d( "Action Selected: Show Popular");
                sortingOrderModel.getSortOder().postValue(NetworkUtils.ORDER_BY_POPULARITY);
                return true;
            case R.id.menu_item_toprated:
                Timber.d( "Action Selected: Show Top Rated");
                sortingOrderModel.getSortOder().postValue(NetworkUtils.ORDER_BY_TOPRATED);
                return true;
            case R.id.menu_item_favorite:
                Timber.d( "Action Selected: Show Favorite");
                sortingOrderModel.getSortOder().postValue(NetworkUtils.ORDER_BY_FAVORITE);
                return true;
            default:
                return true;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (mIsTwoPane) {
            // Make sure our UI is in the correct state.
            try {
                getFragmentManager().beginTransaction()
                        .detach(getFragmentManager().findFragmentById(R.id.fr_movie_detail))
                        .commit();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

    private void killAllHandlers(){
        sortChangeHandler.removeCallbacksAndMessages(null);
        conChangeHandler.removeCallbacksAndMessages(null);
        favDBListHandler.removeCallbacksAndMessages(null);
        internetControlHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onStop() {
        super.onStop();
        killAllHandlers();
    }
}
