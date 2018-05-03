package com.mustafakahraman.popularmovies1;

import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.Intent;
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

import com.mustafakahraman.popularmovies1.helper.ConnectionLiveData;
import com.mustafakahraman.popularmovies1.helper.ConnectionModel;
import com.mustafakahraman.popularmovies1.helper.ItemOffsetDecoration;
import com.mustafakahraman.popularmovies1.helper.NetworkUtils;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

public class MoviesCatalog extends AppCompatActivity implements CatalogAdapter.ItemClickListener{

    private final String LOG = "CatalogActivity";

    private CatalogAdapter mCatalogAdapter;

    private RecyclerView mCatalogRecyclerView;
    private ProgressBar mLoadingBar;
    private TextView mTvError;

    public static String moviesOrderType = NetworkUtils.ORDER_BY_POPULARITY;

    private ArrayList<Movie> movieCatalogList = new ArrayList<Movie>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movies_catalog);

        mLoadingBar = (ProgressBar) findViewById(R.id.pb_loading_bar);
        mTvError = (TextView) findViewById(R.id.tv_error_message);
        mCatalogRecyclerView = (RecyclerView) findViewById(R.id.rv_movie_catalog);
        ItemOffsetDecoration itemDecoration = new ItemOffsetDecoration(MoviesCatalog.this, R.dimen.item_offset);
        mCatalogRecyclerView.addItemDecoration(itemDecoration);

        ConnectionLiveData connectionLiveData = new ConnectionLiveData(getApplicationContext());
        connectionLiveData.observe(this, new Observer<ConnectionModel>() {
            @Override
            public void onChanged(@Nullable ConnectionModel connection) {
                if (connection.getIsConnected()) {
                    switch (connection.getType()) {
                        case ConnectionModel.WifiData:
                            if(mTvError.getVisibility() == View.VISIBLE) {
                                showMoviesIfInternetAvailable();
                            }
                            break;
                        case ConnectionModel.MobileData:
                            if(mTvError.getVisibility() == View.VISIBLE) {
                                showMoviesIfInternetAvailable();
                            }
                            break;
                    }
                } else {
                    // Internet disconnected, Do something
                }
            }
        });

        showMoviesIfInternetAvailable();
    }

    private void showMoviesIfInternetAvailable() {
        NetworkUtils.InternetCheckTask internetCheckTask = new NetworkUtils.InternetCheckTask();
        internetCheckTask.execute(this);
    }

    public void showMovies() {
        String queryUrl = NetworkUtils.buildQueryUrl(moviesOrderType).toString();
        Log.d(LOG, queryUrl);

        new FetchMoviesTask().execute(queryUrl);
    }


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

            if (movieCatalogList != null) {
                displayCatalog();
            } else {
                displayError();
            }

            int numColumns = 3;
            GridLayoutManager layoutManager = new GridLayoutManager(MoviesCatalog.this, numColumns);

            mCatalogAdapter = new CatalogAdapter(MoviesCatalog.this, movies);
            mCatalogAdapter.setClickListener(MoviesCatalog.this);

            mCatalogRecyclerView.setLayoutManager(layoutManager);
            mCatalogRecyclerView.setAdapter(mCatalogAdapter);
        }
    }

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

    public void displayLoading() {
        mCatalogRecyclerView.setVisibility(View.INVISIBLE);
        mTvError.setVisibility(View.INVISIBLE);
        mLoadingBar.setVisibility(View.VISIBLE);
    }

    public void displayCatalog() {
        mCatalogRecyclerView.setVisibility(View.VISIBLE);
        mTvError.setVisibility(View.INVISIBLE);
        mLoadingBar.setVisibility(View.INVISIBLE);
    }

    public void displayError() {
        mCatalogRecyclerView.setVisibility(View.INVISIBLE);
        mTvError.setVisibility(View.VISIBLE);
        mLoadingBar.setVisibility(View.INVISIBLE);
    }

    private void startDetailActivity(Movie movie) {
        Context contextStartFrom = MoviesCatalog.this;
        Class classToOpen = MovieDetail.class;
        Intent intentOpenDetail = new Intent(contextStartFrom, classToOpen);

        intentOpenDetail.putExtra(getString(R.string.INTENT_KEY_ID), movie.get_id());
        intentOpenDetail.putExtra(getString(R.string.INTENT_KEY_TITLE), movie.getTitle());
        intentOpenDetail.putExtra(getString(R.string.INTENT_KEY_DATE), movie.getDate());
        intentOpenDetail.putExtra(getString(R.string.INTENT_KEY_POSTERURL), movie.getPosterUrl());
        intentOpenDetail.putExtra(getString(R.string.INTENT_KEY_PLOTSYNOPSIS), movie.getPlotSynopsis());
        intentOpenDetail.putExtra(getString(R.string.INTENT_KEY_VOTEAVG), movie.getVoteAvg());

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
