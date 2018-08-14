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
import android.widget.ProgressBar;
import android.widget.TableRow;
import android.widget.TextView;

import com.mustafakahraman.popularmovies1.R;
import com.mustafakahraman.popularmovies1.helper.AppExecutors;
import com.mustafakahraman.popularmovies1.helper.Connection;
import com.mustafakahraman.popularmovies1.helper.ConnectionViewModel;
import com.mustafakahraman.popularmovies1.helper.ItemOffsetDecoration;
import com.mustafakahraman.popularmovies1.helper.NetworkUtils;
import com.mustafakahraman.popularmovies1.helper.SortingOrderModel;
import com.mustafakahraman.popularmovies1.model.Movie;
import com.mustafakahraman.popularmovies1.model.Review;
import com.mustafakahraman.popularmovies1.model.Video;

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
    private Handler reviewVideoHandler = new Handler();

    private boolean mIsTwoPane = false;
    private boolean mIsFetchingProgress;

    private final int FETCH_CODE_MOVIE = 11;
    private final int FETCH_CODE_REVIEW = 12;
    private final int FETCH_CODE_VIDEO = 13;

    private boolean FLAG_REVIEW_FOUND_INMODEL;
    private boolean FLAG_VIDEO_FOUND_INMMODEL;

    // Mandatory empty constructor
    public MoviesCatalogFragment() {

    }

    // When a movie is clicked, this initialize opening detail activiy screen using selected movie info
    @Override
    public void onItemClick(int position) {
        final Movie movie = mCatalogAdapter.getItemAtPosition(position);
        ArrayList<Review> tempReviews = addReviewsForIntent(movie.get_id());
        ArrayList<Video> tempVideos = addVideosForIntent(movie.get_id());

        // if temp arrays has values that means values are come from directly view model
        // if not that means data will be fetched from internet, so wait for 1800 ms for finishing fetching progress
        if(FLAG_REVIEW_FOUND_INMODEL && FLAG_VIDEO_FOUND_INMMODEL){
            setupSending(movie, tempReviews, tempVideos);
        } else {
            displayLoading();
            reviewVideoHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    setupSending(movie, addReviewsForIntent(movie.get_id()), addVideosForIntent(movie.get_id()));
                }
            }, 2000);
        }
    }

    private void setupSending(Movie movie, ArrayList<Review> reviews, ArrayList<Video> videos) {
        if(mIsTwoPane) {
            movieDetailViewModel.getMovie().postValue(movie);
            movieDetailViewModel.getReviews().postValue(reviews);
            movieDetailViewModel.getVideos().postValue(videos);
        } else {
            Timber.d("Size of reviews : %s - setupSending()", reviews.size() );
            Timber.d("Size of videos : %s - setupSending()", videos.size());
            startDetailActivity(movie, reviews, videos);
        }
    }

    // Helper function to start detail activity
    private void startDetailActivity(Movie movie, ArrayList<Review> reviews, ArrayList<Video> videos) {
        Context contextStartFrom = getActivity();
        Class classToOpen = MovieDetail.class;

        Intent intentOpenDetail = new Intent(contextStartFrom, classToOpen);
        intentOpenDetail.putExtra(getString(R.string.INTENT_KEY_MOVIE), movie); // using the (String key, Parcelable value) overload!
        intentOpenDetail.putParcelableArrayListExtra(getString(R.string.INTENT_KEY_REVIEWS), reviews);
        intentOpenDetail.putParcelableArrayListExtra(getString(R.string.INTENT_KEY_VIDEOS), videos);

        startActivity(intentOpenDetail);
    }

    private ArrayList<Review> addReviewsForIntent(final long movieId) {
        ArrayList<Review> reviews = new ArrayList<Review>();

        try {
            // First check if selected movie is favorite or not, if so get reviews from favorite view model data
            if (moviesViewModel.getFavoriteMovieIds().getValue().contains(movieId)) {
                FLAG_REVIEW_FOUND_INMODEL = true;
                Timber.d("Selected movie found in fav movie ids; reviews are gotten from favReviewModel");
                if(moviesViewModel.getFavoriteMovieReviews().getValue() != null && moviesViewModel.getFavoriteMovieReviews().getValue().size() > 0) {
                    for (Review review : moviesViewModel.getFavoriteMovieReviews().getValue()) {
                        if (review.getMovieId() == movieId) {
                            reviews.add(review);
                        }
                    }
                }
                return reviews;
                // Secondly, check if selected movie is saved to review view model data before, if so get reviews from review view model data
            } else if(moviesViewModel.getReviewMovieIds().getValue().contains(movieId)) {
                FLAG_REVIEW_FOUND_INMODEL = true;
                Timber.d("Selected movie found in review movie ids; reviews are gotten from reviewModel");
                if(moviesViewModel.getReviews().getValue() != null && moviesViewModel.getReviews().getValue().size() > 0) {
                    for(Review review: moviesViewModel.getReviews().getValue()) {
                        if(review.getMovieId() == movieId) {
                            reviews.add(review);
                        }
                    }
                }
                return reviews;
                // If above options does not work, get reviews from internet. Then save them to temporary review view model data of MoviesViewModel
            } else {
                FLAG_REVIEW_FOUND_INMODEL = false;
                Timber.d("Selected movie not found in view models; reviews are fetching from net");
                fetchIfInternetAvailable(FETCH_CODE_REVIEW, movieId);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
            FLAG_REVIEW_FOUND_INMODEL = false;
            Timber.d("Error catched, selected movie review will be fetched from net");
            fetchIfInternetAvailable(FETCH_CODE_REVIEW, movieId);
        }

        return reviews;
    }

    private ArrayList<Video> addVideosForIntent(final long movieId) {
        ArrayList<Video> videos = new ArrayList<>();

        try {
            // First check if selected movie is favorite or not, if so get reviews from favorite view model data
            if (moviesViewModel.getFavoriteMovieIds().getValue().contains(movieId)) {
                FLAG_VIDEO_FOUND_INMMODEL = true;
                Timber.d("Selected movie found in fav movie ids; videos are gotten from favVideoModel");
                if (moviesViewModel.getFavoriteMovieVideos().getValue() != null && moviesViewModel.getFavoriteMovieVideos().getValue().size() > 0) {
                    for (Video video : moviesViewModel.getFavoriteMovieVideos().getValue()) {
                        if (video.getMovieId() == movieId) {
                            videos.add(video);
                        }
                    }
                }
                return videos;
                // Secondly, check if selected movie is saved to review view model data before, if so get reviews from review view model data
            } else if(moviesViewModel.getVideoMovieIds().getValue().contains(movieId)) {
                FLAG_VIDEO_FOUND_INMMODEL = true;
                Timber.d("Selected movie found in video movie ids; reviews are gotten from videoModel");
                if(moviesViewModel.getVideos().getValue() != null && moviesViewModel.getVideos().getValue().size() > 0) {
                    for (Video video : moviesViewModel.getVideos().getValue()) {
                        if (video.getMovieId() == movieId) {
                            videos.add(video);
                        }
                    }
                }
                return videos;
                // If above options does not work, get reviews from internet. Then save them to temporary review view model data of MoviesViewModel
            } else {
                FLAG_VIDEO_FOUND_INMMODEL = false;
                Timber.d("Selected movie not found in view models; videos are fetching from net");
                fetchIfInternetAvailable(FETCH_CODE_VIDEO, movieId);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
            FLAG_VIDEO_FOUND_INMMODEL = false;
            fetchIfInternetAvailable(FETCH_CODE_VIDEO, movieId);
        }

        return videos;
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
            /*if (getActivity().findViewById(R.id.llay_twopane_fragment) == null) {
                mIsTwoPane = false;
            } else {
                mIsTwoPane = true;
            }*/
            mIsTwoPane = false;
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

        moviesViewModel = ViewModelProviders.of(this).get(MoviesViewModel.class);

        mIsFetchingProgress = moviesViewModel.getIsFetchingProgress().getValue();

        if(mIsTwoPane) {
            movieDetailViewModel = ViewModelProviders.of(getActivity()).get(MovieDetailViewModel.class);
        }
        conViewModel = ViewModelProviders.of(this).get(ConnectionViewModel.class);
        sortingOrderModel = ViewModelProviders.of(this).get(SortingOrderModel.class);

        moviesOrderType = sortingOrderModel.getSortOder().getValue();

        // initially display error because if no viewmodel data is available it is required to error be displayed
        // in order to start fetching movies from  internet
        if(hasMovieInfoInViewModel()){
            displayLoading();
            Timber.d("Loading displaying started : onCreateView()");
        }

        setupAdapters();

        setupSortingOrderModel();
        setupMoviesViewModel();
        setupConnectionViewModel();

        setHasOptionsMenu(true);

        Timber.d("Called showMoviesFromMemoryOrInternet from : onActivityCreated()");
        showMoviesFromMemoryOrInternet();
    }

    @Override
    public void onStart() {
        super.onStart();
        if(hasMovieInfoInViewModel()) {
            displayCatalog();
        }
        FLAG_REVIEW_FOUND_INMODEL = false;
        FLAG_VIDEO_FOUND_INMMODEL = false;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    // Movies data is either downloaded or restored, then show them in UI
    private void setupAdapters() {
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
                                fetchMoviesIfViewModelEmpty();
                                break;
                            case Connection.MobileData:
                                Timber.d("ConnectionObserver 3GConnection Available");
                                fetchMoviesIfViewModelEmpty();
                                break;
                        }
                    } else {
                        NetworkUtils.isInternetAvailable = false;
                        Timber.d("ConnectionObserver Not connected");
                        // Internet disconnected, Do something
                        if (pbLoadingBar.getVisibility() == View.VISIBLE) {
                            displayError(R.string.error_disconnected);
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

        moviesViewModel.getIsErrorOccured().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean isOccured) {
                if(isOccured) {
                    displayError(moviesViewModel.getErrorMessage().getValue());
                }
                Timber.d("isErrorOccured changed: %s - moviesViewModel-onChanged()", isOccured);
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
                            displayError(R.string.error_none_popular);
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
                            displayError(R.string.error_none_favorite);
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
                            displayError(R.string.error_none_toprated);
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
        if(hasMovieInfoInViewModel()) {
            switch (moviesOrderType) {
                case NetworkUtils.ORDER_BY_FAVORITE:
                    mCatalogAdapter.setMovieList(moviesViewModel.getFavoriteMovies().getValue());
                    break;
                case NetworkUtils.ORDER_BY_POPULARITY:
                    mCatalogAdapter.setMovieList(moviesViewModel.getPopularMovies().getValue());
                    break;
                case NetworkUtils.ORDER_BY_TOPRATED:
                    mCatalogAdapter.setMovieList(moviesViewModel.getTopRatedMovies().getValue());
                    break;
            }
            displayCatalog();
        } else {
            if (!moviesOrderType.equals(NetworkUtils.ORDER_BY_FAVORITE)) {
                Timber.d("Called fetchIfInternetAvailable from : showMoviesFromMemoryOrInternet(), %s", moviesOrderType);
                fetchIfInternetAvailable(FETCH_CODE_MOVIE, -1);
            } else {
                Timber.d( "No favorite movies found to display");
                displayError(R.string.error_none_favorite);
            }
        }
    }

    private boolean hasMovieInfoInViewModel() {
        try {
            if (moviesOrderType.equals(NetworkUtils.ORDER_BY_POPULARITY) && moviesViewModel.getPopularMovies().getValue() != null && moviesViewModel.getPopularMovies().getValue().size() > 0
                    || moviesOrderType.equals(NetworkUtils.ORDER_BY_TOPRATED) && moviesViewModel.getTopRatedMovies().getValue() != null && moviesViewModel.getTopRatedMovies().getValue().size() > 0
                    || moviesOrderType.equals(NetworkUtils.ORDER_BY_FAVORITE) && moviesViewModel.getFavoriteMovies().getValue() != null && moviesViewModel.getFavoriteMovies().getValue().size() > 0) {
                Timber.d("hasMovieInfoInViewModel() - TRUE");
                return true;
            } else {
                Timber.d("hasMovieInfoInViewModel() - FALSE");
                return false;
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
            Timber.d("hasMovieInfoInViewModel() - FALSE");
            return false;
        }
    }

    private void fetchMoviesIfViewModelEmpty(){
        conChangeHandler.postDelayed(new Runnable() {
            public void run() {
                if(!hasMovieInfoInViewModel()) {
                    fetchIfInternetAvailable(FETCH_CODE_MOVIE, -1);
                }
            }
        }, 2100);   //2 seconds
    }


    public void fetchIfInternetAvailable(final int fetchCode, final long movieId) {

        if(!mIsFetchingProgress || fetchCode != FETCH_CODE_MOVIE) {

            Timber.d("moviesViewModel.getIsFetchingProgress().postValue(true) : fetchIfInternetAvailable()");
            if(fetchCode==FETCH_CODE_MOVIE) moviesViewModel.getIsFetchingProgress().postValue(true);

            displayLoading();
            Timber.d("Loading bar displaying started : in fetchIfInternetAvailable()");

            if (NetworkUtils.isInternetAvailable) {
                doFetchFromInternet(fetchCode, movieId);
            }

            if (!NetworkUtils.isInternetAvailable) {

                NetworkUtils.controlInternetAvailability();

                internetControlHandler.postDelayed(new Runnable() {
                    public void run() {
                        // just wait completion of internet test
                        Timber.d("Waited 1750 ms to check internet availability : in fetchIfInternetAvailable()");
                        if (NetworkUtils.isInternetAvailable) {
                            doFetchFromInternet(fetchCode, movieId);
                        } else {
                            Timber.d("Could not fetch movies because internet not available : in fetchIfInternetAvailable()");
                            Timber.d("moviesViewModel.getIsFetchingProgress().postValue(false) : fetchIfInternetAvailable()");
                            if(fetchCode==FETCH_CODE_MOVIE) moviesViewModel.getIsFetchingProgress().postValue(false);
                            displayError(R.string.error_internet_not_available);
                        }
                    }
                }, 1300);   //2 seconds
            }
        }
    }

    public void doFetchFromInternet(final int fetchCode, long movieId) {
        if(NetworkUtils.isInternetAvailable) {

            final String queryUrl;

            if(fetchCode == FETCH_CODE_MOVIE) {
                Timber.d("Movie fetching from internet started");
                queryUrl = NetworkUtils.buildMovieCatalogUrl(moviesOrderType).toString();
                Timber.d("Catalog url : %s", queryUrl);

                final ArrayList<Long> favIdList = (ArrayList<Long>) moviesViewModel.getFavoriteMovieIds().getValue();

                favDBListHandler.postDelayed(new Runnable() {
                    public void run() {
                        fetchUsingJson(fetchCode, queryUrl, -1, favIdList);
                    }
                }, 150);   //2 seconds
            } else if(fetchCode == FETCH_CODE_REVIEW) {
                queryUrl = NetworkUtils.buildMovieReviewUrl(movieId).toString();
                Timber.d("Review url : %s", queryUrl);
                fetchUsingJson(fetchCode, queryUrl, movieId, null);
            } else if(fetchCode == FETCH_CODE_VIDEO) {
                queryUrl = NetworkUtils.buildMovieVideoUrl(movieId).toString();
                Timber.d("Video url : %s", queryUrl);
                fetchUsingJson(fetchCode, queryUrl, movieId, null);
            }
        } else {
            Timber.d("moviesViewModel.getIsFetchingProgress().postValue(false) : doFetchFromInternet()");
            if(fetchCode==FETCH_CODE_MOVIE) moviesViewModel.getIsFetchingProgress().postValue(false);
            Timber.d( "Could not fetch movies because internet not available: in doFetchFromInternet()");
            displayError(R.string.error_internet_not_available);
        }
    }

    private void fetchUsingJson(final int fetchCode, final String queryUrl, final long movieId, final ArrayList<Long> favIdList) {

        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if(fetchCode == FETCH_CODE_MOVIE) {
                        // Here favId list is used to detect and set favorite attribute of movies while downloading
                        // and then show them as favorite in the movie catalog
                        List<Movie> movieList = NetworkUtils.extractMoviesFromJSON(NetworkUtils.getHttpJSONResponse(queryUrl),favIdList);
                        if (moviesOrderType.equals(NetworkUtils.ORDER_BY_POPULARITY)) {
                            moviesViewModel.getPopularMovies().postValue(movieList);
                        } else if (moviesOrderType.equals(NetworkUtils.ORDER_BY_TOPRATED)) {
                            moviesViewModel.getTopRatedMovies().postValue(movieList);
                        }
                    } else if(fetchCode == FETCH_CODE_REVIEW) {
                        List<Review> reviews = NetworkUtils.extractReviewsFromJSON(NetworkUtils.getHttpJSONResponse(queryUrl), movieId);
                        moviesViewModel.getReviews().postValue(reviews);
                        moviesViewModel.getReviewMovieIds().getValue().add(movieId);
                    } else if(fetchCode == FETCH_CODE_VIDEO) {
                        List<Video> videos = NetworkUtils.extractVideosFromJSON(NetworkUtils.getHttpJSONResponse(queryUrl), movieId);
                        moviesViewModel.getVideos().postValue(videos);
                        moviesViewModel.getVideoMovieIds().getValue().add(movieId);
                    }
                } catch (JSONException e) {
                    Timber.d( "Could not fetch movie from internet. Error message is shown");
                    moviesViewModel.getIsErrorOccured().postValue(true);
                    moviesViewModel.getErrorMessage().postValue(R.string.error_message);
                    e.printStackTrace();
                } catch (IOException e) {
                    Timber.d( "Could not fetch movie from internet. Error message is shown");
                    moviesViewModel.getIsErrorOccured().postValue(true);
                    moviesViewModel.getErrorMessage().postValue(R.string.error_message);
                    e.printStackTrace();
                } finally {
                    Timber.d("moviesViewModel.getIsFetchingProgress().postValue(false) : doFetchFromInternet()");
                    if(fetchCode==FETCH_CODE_MOVIE) moviesViewModel.getIsFetchingProgress().postValue(false);
                }
            }
        });
    }


    // Helper method to show loading bar
    public void displayLoading() {
        rvMovieCatalog.setVisibility(View.INVISIBLE);
        tvErrorMessage.setVisibility(View.INVISIBLE);
        pbLoadingBar.setVisibility(View.VISIBLE);
        moviesViewModel.getIsErrorOccured().postValue(false);
    }

    // Helper method to show movie catalog view
    public void displayCatalog() {
        rvMovieCatalog.setVisibility(View.VISIBLE);
        tvErrorMessage.setVisibility(View.INVISIBLE);
        pbLoadingBar.setVisibility(View.INVISIBLE);
        moviesViewModel.getIsErrorOccured().postValue(false);
    }

    // Helper method to show error message
    public void displayError(int errorStringResId) {
        rvMovieCatalog.setVisibility(View.INVISIBLE);
        tvErrorMessage.setVisibility(View.VISIBLE);
        pbLoadingBar.setVisibility(View.INVISIBLE);
        tvErrorMessage.setText(errorStringResId);
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
        reviewVideoHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onStop() {
        super.onStop();
        killAllHandlers();
        moviesViewModel.getIsFetchingProgress().postValue(false);
    }
}
