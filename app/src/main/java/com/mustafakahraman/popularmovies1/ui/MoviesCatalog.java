package com.mustafakahraman.popularmovies1.ui;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mustafakahraman.popularmovies1.R;
import com.mustafakahraman.popularmovies1.data.AppDatabase;
import com.mustafakahraman.popularmovies1.helper.AppExecutors;
import com.mustafakahraman.popularmovies1.helper.Connection;
import com.mustafakahraman.popularmovies1.helper.ConnectionViewModel;
import com.mustafakahraman.popularmovies1.helper.ItemOffsetDecoration;
import com.mustafakahraman.popularmovies1.helper.NetworkUtils;
import com.mustafakahraman.popularmovies1.model.Movie;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MoviesCatalog extends AppCompatActivity implements CatalogAdapter.ItemClickListener {

    private final String LOG_TAG = "CatalogActivity";

    //ActivityMovieCatalogBinding mBinding;

    private CatalogAdapter mCatalogAdapter;

    // Default sort order type
    public static String moviesOrderType = NetworkUtils.ORDER_BY_POPULARITY;

    private AppDatabase mDb;
    private RecyclerView rvMovieCatalog;
    private TextView tvErrorMessage;
    private ProgressBar pbLoadingBar;

    CatalogViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_catalog);
        //mBinding = DataBindingUtil.setContentView(this, R.layout.activity_movie_catalog);

        initializeViews();
        mDb = AppDatabase.getInstance(this);

        viewModel = ViewModelProviders.of(this).get(CatalogViewModel.class);

        setupMovieCatalogUI();
        setupViewModel();

        // Internet connection/availability change oberserver initialization
        ConnectionViewModel conViewModel = ViewModelProviders.of(this).get(ConnectionViewModel.class);
        conViewModel.getConnectionLiveData().observe(this, new Observer<Connection>() {
            @Override
            public void onChanged(@Nullable Connection connection) {
                if (connection.getIsConnected()) {
                    if (connection.getIsInternetAvailable()) {
                        switch (connection.getType()) {
                            case Connection.WifiData:
                                Log.d(LOG_TAG, "Connection observer onCahnged()- WifiConnection Available");
                                fetchMoviesIfErrorVisible();
                                break;
                            case Connection.MobileData:
                                Log.d(LOG_TAG, "Connection observer - 3GConnection Available");
                                fetchMoviesIfErrorVisible();
                                break;
                        }
                    } else {

                        Log.d(LOG_TAG, "Connection observer - Connected but no internet");
                        // Internet disconnected, Do something
                        if(pbLoadingBar.getVisibility() == View.VISIBLE) {
                            displayError();
                        }
                    }
                } else {
                    Log.d(LOG_TAG, "Connection observer - No connection");
                    if(pbLoadingBar.getVisibility() == View.VISIBLE) {
                        displayError();
                    }
                }
            }
        });

        showMoviesFromMemoryOrInternet();

        // get movie catalog from saved instance state if saved before
        /*if(savedInstanceState != null) {
            movieCatalogList = savedInstanceState.getParcelableArrayList(getString(R.string.INTENT_KEY_MOVIE_CATALOG));
        }*/

    }
    
    private void initializeViews(){
        rvMovieCatalog = (RecyclerView) findViewById(R.id.rv_movie_catalog);
        pbLoadingBar = (ProgressBar) findViewById(R.id.pb_loading_bar);
        tvErrorMessage = (TextView) findViewById(R.id.tv_error_message);
    }

    // Movies data is either downloaded or restored, then show them in UI
    private void setupMovieCatalogUI() {
        int numColumns = getResources().getInteger(R.integer.grid_columns);
        GridLayoutManager layoutManager = new GridLayoutManager(MoviesCatalog.this, numColumns);
        rvMovieCatalog.setLayoutManager(layoutManager);

        int offsetInPixels = getResources().getDimensionPixelSize(R.dimen.item_offset);
        ItemOffsetDecoration itemDecoration = new ItemOffsetDecoration(offsetInPixels, numColumns, true);
        rvMovieCatalog.addItemDecoration(itemDecoration);

        mCatalogAdapter = new CatalogAdapter(MoviesCatalog.this, new ArrayList<Movie>());
        mCatalogAdapter.setClickListener(MoviesCatalog.this);
        rvMovieCatalog.setAdapter(mCatalogAdapter);

        Log.d(LOG_TAG, "setupMovieCatalogUI()");
    }

    // This method initializes the live data objects which store movie catalog info
    private void setupViewModel() {

        viewModel.getPopularMovies().observe(this, new Observer<List<Movie>>() {
            @Override
            public void onChanged(@Nullable List<Movie> movieList) {
                Log.d(LOG_TAG, "List<Movie> observer - onChanged() popular movie list");

                if(moviesOrderType.equals(NetworkUtils.ORDER_BY_POPULARITY)) {
                    if (movieList != null && movieList.size() > 0) {
                        displayCatalog();
                        mCatalogAdapter.setMovieList(movieList);
                        //mCatalogAdapter.notifyDataSetChanged();
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

        viewModel.getFavoriteMovies().observe(this, new Observer<List<Movie>>() {
            @Override
            public void onChanged(@Nullable List<Movie> movieList) {
                Log.d(LOG_TAG, "List<Movie> observer - onChanged() favorite movie list");
                if(moviesOrderType == NetworkUtils.ORDER_BY_FAVORITE) {
                    if (movieList != null && movieList.size() > 0) {
                        displayCatalog();
                        mCatalogAdapter.setMovieList(movieList);
                        //mCatalogAdapter.notifyDataSetChanged();
                    }
                    else {
                        if(pbLoadingBar.getVisibility() != View.VISIBLE) {
                            tvErrorMessage.setText(R.string.error_none_favorite);
                            displayError();
                        }
                    }
                }
            }
        });

        viewModel.getTopRatedMovies().observe(this, new Observer<List<Movie>>() {
            @Override
            public void onChanged(@Nullable List<Movie> movieList) {
                Log.d(LOG_TAG, "List<Movie> observer - onChanged() top rated movie list");

                if(moviesOrderType.equals(NetworkUtils.ORDER_BY_TOPRATED)) {
                    if (movieList != null && movieList.size() > 0) {
                        displayCatalog();
                        mCatalogAdapter.setMovieList(movieList);
                        //mCatalogAdapter.notifyDataSetChanged();
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
    }

    private void fetchMoviesIfErrorVisible(){
        if(tvErrorMessage.getVisibility() == View.VISIBLE &
                (moviesOrderType == NetworkUtils.ORDER_BY_POPULARITY ||
                moviesOrderType == NetworkUtils.ORDER_BY_TOPRATED)) {

            fetchMoviesFromInternet();
        }
    }

    private void showMoviesFromMemoryOrInternet() {

        switch (moviesOrderType){
            case NetworkUtils.ORDER_BY_FAVORITE:
                if(viewModel.getFavoriteMovies().getValue() != null) {
                    if(viewModel.getFavoriteMovies().getValue().size() > 0) {
                        mCatalogAdapter.setMovieList(viewModel.getFavoriteMovies().getValue());
                        //mCatalogAdapter.notifyDataSetChanged();
                        displayCatalog();
                    } else {
                        Log.d(LOG_TAG, "No favorite movies found to display");
                        tvErrorMessage.setText(R.string.error_none_favorite);
                        displayError();
                    }
                } else {
                    Log.d(LOG_TAG, "No favorite movies found to display");
                    tvErrorMessage.setText(R.string.error_none_favorite);
                    displayError();
                }
                break;
            case NetworkUtils.ORDER_BY_POPULARITY:
                if(viewModel.getPopularMovies().getValue() != null) {
                    if(viewModel.getPopularMovies().getValue().size() > 0) {
                        mCatalogAdapter.setMovieList(viewModel.getPopularMovies().getValue());
                        //mCatalogAdapter.notifyDataSetChanged();
                        displayCatalog();
                    } else {
                        Log.d(LOG_TAG, "No popular movies offline to display, fetch from internet");
                        fetchMoviesFromInternet();
                    }
                } else {
                    Log.d(LOG_TAG, "No popular movies offline to display, fetch from internet");
                    fetchMoviesFromInternet();
                }
                break;
            case NetworkUtils.ORDER_BY_TOPRATED:
                if(viewModel.getTopRatedMovies().getValue() != null) {
                    if(viewModel.getTopRatedMovies().getValue().size() > 0) {
                        mCatalogAdapter.setMovieList(viewModel.getTopRatedMovies().getValue());
                        //mCatalogAdapter.notifyDataSetChanged();
                        displayCatalog();
                    } else {
                        Log.d(LOG_TAG, "No top rated movies offline to display, fetch from internet");
                        fetchMoviesFromInternet();
                    }
                } else {
                    Log.d(LOG_TAG, "No top rated movies offline to display, fetch from internet");
                    fetchMoviesFromInternet();
                }
                break;
            default:
                break;
        }
    }

    // Provided that internet is available, download movie data
    // Called only when order type is selected as popularity or top rated
    public void fetchMoviesFromInternet() {
        if(NetworkUtils.getIsInternetAvailable()) {
            displayLoading();

            Log.d(LOG_TAG, "fetchMoviesFromInternet started");
            final String queryUrl = NetworkUtils.buildQueryUrl(moviesOrderType).toString();
            Log.d(LOG_TAG, queryUrl);

            AppExecutors.getInstance().diskIO().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        List<Movie> movieList = NetworkUtils.extractMoviesFromJSON(NetworkUtils.getHttpJSONResponse(queryUrl));
                        if (moviesOrderType == NetworkUtils.ORDER_BY_POPULARITY) {
                            viewModel.getPopularMovies().postValue(movieList);
                            Log.d(LOG_TAG, "LiveData of popular movie list is manipulated, viewmodel observer will be employed");

                        } else if (moviesOrderType == NetworkUtils.ORDER_BY_TOPRATED) {
                            viewModel.getTopRatedMovies().postValue(movieList);
                            Log.d(LOG_TAG, "LiveData of top rated movie list is manipulated, viewmodel observer will be employed");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        } else {
            Log.d(LOG_TAG, "Internet is not available");
            tvErrorMessage.setText(R.string.error_message);
            displayError();
        }
    }

    /*// invoked when the activity may be temporarily destroyed, save the instance state here
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(getString(R.string.INTENT_KEY_MOVIE_CATALOG), movieCatalogList);

        // call superclass to save any view hierarchy
        super.onSaveInstanceState(outState);
    }*/



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

    // When a movie is clicked, this initialize opening detail activiy screen using selected movie info
    @Override
    public void onItemClick(int position) {

        startDetailActivity(mCatalogAdapter.getItemAtPosition(position));
        //movieCatalogList.get(position).toggleFavorite();
        //mCatalogAdapter.notifyDataSetChanged();

        /*Toast.makeText(this,
                "You clicked movie " + mCatalogAdapter.getItem(position),
                Toast.LENGTH_SHORT).show();*/

        // COMPLETED (1): Add movie detail activity and layout and handle item click
    }

    // Helper function to start detail activity
    private void startDetailActivity(Movie movie) {
        Context contextStartFrom = MoviesCatalog.this;
        Class classToOpen = MovieDetail.class;

        Intent intentOpenDetail = new Intent(contextStartFrom, classToOpen);
        intentOpenDetail.putExtra(getString(R.string.INTENT_KEY_MOVIE), movie); // using the (String key, Parcelable value) overload!

        startActivity(intentOpenDetail);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO (2): Move the control for the catalog order to settings
        int menuItemIdSelected = item.getItemId();
        switch (menuItemIdSelected) {
            case R.id.menu_item_popular:
                Log.d(LOG_TAG, "Action Selected: Show Popular");
                moviesOrderType = NetworkUtils.ORDER_BY_POPULARITY;
                showMoviesFromMemoryOrInternet();
                return true;
            case R.id.menu_item_toprated:
                Log.d(LOG_TAG, "Action Selected: Show Top Rated");
                moviesOrderType = NetworkUtils.ORDER_BY_TOPRATED;
                showMoviesFromMemoryOrInternet();
                return true;
            case R.id.menu_item_favorite:
                Log.d(LOG_TAG, "Action Selected: Show Favorite");
                moviesOrderType = NetworkUtils.ORDER_BY_FAVORITE;
                showMoviesFromMemoryOrInternet();
                return true;
            default:
                return true;
        }
    }



}
