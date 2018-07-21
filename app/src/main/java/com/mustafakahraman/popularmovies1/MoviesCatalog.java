package com.mustafakahraman.popularmovies1;

import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
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

import com.mustafakahraman.popularmovies1.databinding.ActivityMoviesCatalogBinding;
import com.mustafakahraman.popularmovies1.helper.ConnectionLiveData;
import com.mustafakahraman.popularmovies1.helper.ConnectionModel;
import com.mustafakahraman.popularmovies1.helper.ItemOffsetDecoration;
import com.mustafakahraman.popularmovies1.helper.NetworkUtils;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

public class MoviesCatalog extends AppCompatActivity implements CatalogAdapter.ItemClickListener{

    ActivityMoviesCatalogBinding mBinding;

    private final String LOG = "CatalogActivity";

    private CatalogAdapter mCatalogAdapter;

    public static String moviesOrderType = NetworkUtils.ORDER_BY_POPULARITY;

    private ArrayList<Movie> movieCatalogList = new ArrayList<Movie>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_movies_catalog);

        ItemOffsetDecoration itemDecoration = new ItemOffsetDecoration(MoviesCatalog.this, R.dimen.item_offset);
        mBinding.rvMovieCatalog.addItemDecoration(itemDecoration);

        // Internet connection/availability change oberserver initialization
        ConnectionLiveData connectionLiveData = new ConnectionLiveData(getApplicationContext());
        connectionLiveData.observe(this, new Observer<ConnectionModel>() {
            @Override
            public void onChanged(@Nullable ConnectionModel connection) {
                if (connection.getIsConnected()) {
                    switch (connection.getType()) {
                        case ConnectionModel.WifiData:
                            if(mBinding.tvErrorMessage.getVisibility() == View.VISIBLE) {
                                showMoviesIfInternetAvailable();
                            }
                            break;
                        case ConnectionModel.MobileData:
                            if(mBinding.tvErrorMessage.getVisibility() == View.VISIBLE) {
                                showMoviesIfInternetAvailable();
                            }
                            break;
                    }
                } else {
                    // Internet disconnected, Do something
                }
            }
        });

        // get movie catalog from saved instance state if saved before
        if(savedInstanceState != null) {
            movieCatalogList = savedInstanceState.getParcelableArrayList(getString(R.string.INTENT_KEY_MOVIE_CATALOG));
        }

        if(movieCatalogList != null && movieCatalogList.size() > 0) {
            populateUIWithMovieCatalog();
        } else {
            showMoviesIfInternetAvailable();
        }

    }

    // Movies data is either downloaded or restored, then show them in UI
    private void populateUIWithMovieCatalog() {
        if (movieCatalogList != null && movieCatalogList.size() > 0) {
            displayCatalog();
        } else {
            displayError();
        }

        int orientation = getResources().getConfiguration().orientation;
        // COMPLETED : Change number of columns according to layout portrait or landscape
        // TODO : Adjust number of columns according to screen/pixel denstiy
        int numColumns;
        if(orientation == Configuration.ORIENTATION_PORTRAIT) {
            numColumns = 3;
        } else {
            numColumns = 5;
        }

        GridLayoutManager layoutManager = new GridLayoutManager(MoviesCatalog.this, numColumns);

        mCatalogAdapter = new CatalogAdapter(MoviesCatalog.this, movieCatalogList);
        mCatalogAdapter.setClickListener(MoviesCatalog.this);

        mBinding.rvMovieCatalog.setLayoutManager(layoutManager);
        mBinding.rvMovieCatalog.setAdapter(mCatalogAdapter);
    }

    // Checks whether the internet is available or not
    // if available then call showMovies and download movies data
    // if not then shows an error message
    private void showMoviesIfInternetAvailable() {
        NetworkUtils.InternetCheckTask internetCheckTask = new NetworkUtils.InternetCheckTask();
        internetCheckTask.execute(this);
    }

    // Provided that internet is available, download movie data
    public void showMovies() {
        String queryUrl = NetworkUtils.buildQueryUrl(moviesOrderType).toString();
        Log.d(LOG, queryUrl);

        new FetchMoviesTask().execute(queryUrl);
    }

    // invoked when the activity may be temporarily destroyed, save the instance state here
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(getString(R.string.INTENT_KEY_MOVIE_CATALOG), movieCatalogList);

        // call superclass to save any view hierarchy
        super.onSaveInstanceState(outState);
    }

    // This callback is called only when there is a saved instance that is previously saved by using
    // onSaveInstanceState(). We restore some state in onCreate(), while we can optionally restore
    // other state here, possibly usable after onStart() has completed.
    // The savedInstanceState Bundle is same as the one used in onCreate().
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        // Do sth
    }

    // This is called when the internet is available
    // it is responsible to show loading bar when data is downloading
    // and of course to download movies data
    public class FetchMoviesTask extends AsyncTask<String, Void, ArrayList<Movie>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            displayLoading();
        }

        @Override
        protected ArrayList<Movie> doInBackground(String... queryUrl) {

            ArrayList<Movie> movies = new ArrayList<Movie>();

            try {
                movies = NetworkUtils.extractMoviesFromJSON(
                        NetworkUtils.getHttpJSONResponse(queryUrl[0]));
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return movies;
        }

        @Override
        protected void onPostExecute(ArrayList<Movie> movies) {
            super.onPostExecute(movies);

            movieCatalogList = movies;

            populateUIWithMovieCatalog();
        }
    }

    // When a movie is clicked, this initialize opening detail activiy screen using selected movie info
    @Override
    public void onItemClick(View view, int position) {

        startDetailActivity(movieCatalogList.get(position));
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

    // Helper method to show loading bar
    public void displayLoading() {
        mBinding.rvMovieCatalog.setVisibility(View.INVISIBLE);
        mBinding.tvErrorMessage.setVisibility(View.INVISIBLE);
        mBinding.pbLoadingBar.setVisibility(View.VISIBLE);
    }

    // Helper method to show movie catalog view
    public void displayCatalog() {
        mBinding.rvMovieCatalog.setVisibility(View.VISIBLE);
        mBinding.tvErrorMessage.setVisibility(View.INVISIBLE);
        mBinding.pbLoadingBar.setVisibility(View.INVISIBLE);
    }

    // Helper method to show error message
    public void displayError() {
        mBinding.rvMovieCatalog.setVisibility(View.INVISIBLE);
        mBinding.tvErrorMessage.setVisibility(View.VISIBLE);
        mBinding.pbLoadingBar.setVisibility(View.INVISIBLE);
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
                Log.d(LOG, "Action Selected: Show Popular");
                moviesOrderType = NetworkUtils.ORDER_BY_POPULARITY;
                showMoviesIfInternetAvailable();
                return true;
            case R.id.menu_item_toprated:
                Log.d(LOG, "Action Selected: Show Top Rated");
                moviesOrderType = NetworkUtils.ORDER_BY_TOPRATED;
                showMoviesIfInternetAvailable();
                return true;
            default:
                return true;
        }
    }

}
