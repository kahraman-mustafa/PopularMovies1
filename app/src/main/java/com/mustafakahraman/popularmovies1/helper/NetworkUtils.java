package com.mustafakahraman.popularmovies1.helper;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.mustafakahraman.popularmovies1.data.DateConverter;
import com.mustafakahraman.popularmovies1.model.Movie;
import com.mustafakahraman.popularmovies1.ui.MoviesCatalog;
import com.mustafakahraman.popularmovies1.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by kahraman on 18.04.2018.
 */

public class NetworkUtils {

    private static final String URL_SCHEME = "https";
    private static final String URL_AUTHORITY = "api.themoviedb.org";
    private static final String URL_PATH_1 = "3";
    private static final String URL_PATH_2 = "movie";
    public static final String ORDER_BY_POPULARITY = "popular";
    public static final String ORDER_BY_TOPRATED = "top_rated";
    public static final String ORDER_BY_FAVORITE = "favorite";
    private static final String KEY_APIKEY = "api_key";
    private static final String KEY_LANGUAGE = "language";
    private static final String KEY_PAGE = "page";
    private static final String VALUE_APIKEY = "YOUR_API_KEY_HERE";
    private static final String VALUE_LANGUAGE_EN = "en-US";
    public static boolean isInternetAvailable = false;

    public static JSONObject getHttpJSONResponse(String url) throws IOException, JSONException {

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();

        return new JSONObject(response.body().string());

    }

    public static ArrayList<Movie> extractMoviesFromJSON(JSONObject jsonCatalog, ArrayList<Long> favIdList) throws JSONException {

        ArrayList<Movie> movieList = new ArrayList<Movie>();

        JSONArray pageResults = jsonCatalog.getJSONArray("results");

        int pageSize = pageResults.length();

        for(int i = 0; i < pageSize; i++) {
            JSONObject jsonMovie = (JSONObject) pageResults.get(i);

            long movieID = jsonMovie.optLong("id", -1);
            String movieTitle = jsonMovie.optString("title", "Title Not Available");
            String movieReleaseDate = jsonMovie.optString("release_date", "Date Not Available");
            double movieVoteAvg = jsonMovie.optDouble("vote_average", 0);
            String moviePosterUrl = jsonMovie.optString("poster_path", "");
            String moviePlotSynopsis = jsonMovie.optString("overview", "Story Not Available");
            boolean isFavorite = favIdList.contains(movieID);

            Movie movieToInsert = new Movie(movieID, movieTitle, DateConverter.toDate(movieReleaseDate), moviePosterUrl,
                    movieVoteAvg, moviePlotSynopsis, isFavorite);

            movieList.add(movieToInsert);
        }

        return movieList;
    }

    public static URL buildQueryUrl(String orderBy) {

        Uri.Builder builder = new Uri.Builder();
        Uri queryUri = builder.scheme(URL_SCHEME)
                .authority(URL_AUTHORITY)
                .appendPath(URL_PATH_1)
                .appendPath(URL_PATH_2)
                .appendPath(orderBy)
                .appendQueryParameter(KEY_APIKEY, VALUE_APIKEY)
                .appendQueryParameter(KEY_LANGUAGE, VALUE_LANGUAGE_EN)
                .appendQueryParameter(KEY_PAGE, "1").build();

        URL queryUrl = null;

        try {
            queryUrl = new URL(queryUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return queryUrl;
    }

    public static String buildPosterUrl(Context context, String posterSize, String posterPath) {
        return context.getString(R.string.POSTER_BASE_URL) + posterSize + posterPath;
    }

    public static void controlInternetAvailability() {
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    int connectionTimeOutInMillisec = 1500;
                    Socket sock = new Socket();
                    sock.connect(new InetSocketAddress("8.8.8.8", 53), connectionTimeOutInMillisec);
                    sock.close();
                    isInternetAvailable = true;
                } catch (IOException e) {
                    isInternetAvailable = false;
                }
            }
        });
    }

}
