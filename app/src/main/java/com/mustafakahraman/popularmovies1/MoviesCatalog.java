package com.mustafakahraman.popularmovies1;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mustafakahraman.popularmovies1.data.Movie;
import com.mustafakahraman.popularmovies1.data.NetworkUtils;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

public class MoviesCatalog extends AppCompatActivity implements CatalogAdapter.ItemClickListener{

    private final String LOG = "CatalogActivity";

    private CatalogAdapter mCatalogAdapter;

    private RecyclerView mCatalogRecyclerView;
    private ProgressBar mLoadingBar;
    private TextView mTvError;

    private ArrayList<Movie> movieCatalogList = new ArrayList<Movie>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movies_catalog);

        mLoadingBar = (ProgressBar) findViewById(R.id.pb_loading_bar);
        mTvError = (TextView) findViewById(R.id.tv_error_message);
        mCatalogRecyclerView = (RecyclerView) findViewById(R.id.rv_movie_catalog);

        showMovies(NetworkUtils.ORDER_BY_POPULARITY);
    }

    private void showMovies(String orderBy) {
        String queryUrl =
                NetworkUtils.buildQueryUrl(orderBy).toString();

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

    private void displayLoading() {
        mCatalogRecyclerView.setVisibility(View.INVISIBLE);
        mTvError.setVisibility(View.INVISIBLE);
        mLoadingBar.setVisibility(View.VISIBLE);
    }

    private void displayCatalog() {
        mCatalogRecyclerView.setVisibility(View.VISIBLE);
        mTvError.setVisibility(View.INVISIBLE);
        mLoadingBar.setVisibility(View.INVISIBLE);
    }

    private void displayError() {
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
        int menuItemIdSelected = item.getItemId();
        switch (menuItemIdSelected) {
            case R.id.menu_item_popular:
                Log.d(LOG, "Action Selected: Show Popular");
                showMovies(NetworkUtils.ORDER_BY_POPULARITY);
                return true;
            case R.id.menu_item_toprated:
                Log.d(LOG, "Action Selected: Show Top Rated");
                showMovies(NetworkUtils.ORDER_BY_RATING);
                return true;
            default:
                return true;
        }
    }

    // TODO (3): Add content provider concept to handle data
    // TODO (4): Add asynctask loader concept
}
