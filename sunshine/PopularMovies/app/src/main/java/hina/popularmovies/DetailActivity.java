package hina.popularmovies;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import hina.util.NetworkUtility;

/**
 * Activity to show movie details
 */
public class DetailActivity extends ActionBarActivity {

    private static final String BASE_RATING = "/10";
    private final static String BASE_IMAGE_URL = "http://image.tmdb.org/t/p/w185";
    private static final String TITLE = "title";
    private static final String VOTE_AVERAGE = "vote_average";
    private static final String RELEASE_DATE = "release_date";
    private static final String OVERVIEW = "overview";
    private static final String BACKDROP_PATH = "backdrop_path";
    private String MOVIE_DETAIL = "movie/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
    }

    @Override
    public void onStart() {
        super.onStart();
        //get movie details
        getMovieDetails();
    }

    /**
     * populate movie details from API response
     * @param movieDetailJson
     */
    public void updateMovieDetails(String movieDetailJson) {
        try {
            JSONObject movieDetails = new JSONObject(movieDetailJson);
            String title = movieDetails.getString(TITLE);
            String rating = movieDetails.getString(VOTE_AVERAGE);
            String release = movieDetails.getString(RELEASE_DATE);
            String synopsis = movieDetails.getString(OVERVIEW);
            String thumbnail = movieDetails.getString(BACKDROP_PATH);
            ((TextView) findViewById(R.id.detail_title)).setText(title);
            ((TextView) findViewById(R.id.detail_rating)).setText(rating + BASE_RATING);
            ((TextView) findViewById(R.id.detail_release)).setText(release);
            ((TextView) findViewById(R.id.detail_synopsis)).setText(synopsis);
            ImageView movieImageView = (ImageView) findViewById(R.id.detail_thumbnail);
            Picasso.with(getBaseContext()).load(BASE_IMAGE_URL + thumbnail).into(movieImageView);
        }
        catch (JSONException e) {
            Log.e("DetailActivity", "error converting JSON", e);
        }

    }

    private void getMovieDetails() {
        Intent intent = getIntent();
        if (null != intent && intent.hasExtra(Intent.EXTRA_TEXT)) {
            String movieId = intent.getStringExtra(Intent.EXTRA_TEXT);
            FetchDetailTask fetchDetailTask = new FetchDetailTask();
            fetchDetailTask.execute(movieId);
        }
    }

    /**
     * Task to get the movie details from MovieDB
     */
    public class FetchDetailTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            if (params.length == 0) {
                return null;
            }

            StringBuffer buffer = NetworkUtility.getDataFromMovieDb(MOVIE_DETAIL + params[0], new HashMap<>());
            return buffer.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            updateMovieDetails(result);
        }
    }
}