package hina.popularmovies;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import hina.db.FavoriteMovieContract.FavoriteMovieEntry;
import hina.db.FavoriteMovieHelper;
import hina.tile.TrailerTile;
import hina.tile.TrailerTileAdapter;
import hina.util.NetworkUtility;

import static hina.popularmovies.MainActivityFragment.MOVIE_ID;
import static hina.popularmovies.MainActivityFragment.MOVIE_IMAGE;
import static hina.popularmovies.MainActivityFragment.RESULTS;

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
    public static String MOVIE_DETAIL = "movie/";
    private String TRAILER = "/videos";
    private String movieId;
    private String movieImage;
    private ArrayAdapter<TrailerTile> mVideosAdapter;
    private FavoriteMovieHelper mFavoriteHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        mFavoriteHelper = new FavoriteMovieHelper(this);
        mVideosAdapter = new TrailerTileAdapter(
                this,
                new ArrayList<>()
        );
        ListView listView = (ListView) findViewById(R.id.list_view_trailer);
        listView.setAdapter(mVideosAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                TrailerTile trailerTile = mVideosAdapter.getItem(i);
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + trailerTile.getTrailerKey()));
                startActivity(intent);
            }
        });

        Button favorite = (Button) findViewById(R.id.detail_fav);
        favorite.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View view) {
                ContentValues values = new ContentValues();
                values.put(FavoriteMovieEntry.MOVIE_ID, movieId);
                values.put(FavoriteMovieEntry.MOVIE_IMAGE, movieImage);
                SQLiteDatabase db = mFavoriteHelper.getWritableDatabase();
                db.insert(FavoriteMovieEntry.TABLE_NAME,null, values);
                db.close();
            }
        });

        Button review = (Button) findViewById(R.id.detail_review);
        review.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ReviewActivity.class).putExtra(MOVIE_ID, movieId);
                startActivity(intent);
            }
        });
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
        if (null != intent && intent.hasExtra(MOVIE_ID)) {
            movieId = intent.getStringExtra(MOVIE_ID);
            movieImage = intent.getStringExtra(MOVIE_IMAGE);
            FetchDetailTask fetchDetailTask = new FetchDetailTask();
            fetchDetailTask.execute(movieId);
            FetchVideosTask fetchVideosTask = new FetchVideosTask();
            fetchVideosTask.execute(movieId);
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

    /**
     * Task to get the movie details from MovieDB
     */
    public class FetchVideosTask extends AsyncTask<String, Void, List<TrailerTile>> {
        @Override
        protected List<TrailerTile> doInBackground(String... params) {
            if (params.length == 0) {
                return null;
            }

            StringBuffer buffer = NetworkUtility.getDataFromMovieDb(MOVIE_DETAIL + params[0] + TRAILER, new HashMap<>());
            return getTrailerDataFromJson(buffer.toString());
        }

        @Override
        protected void onPostExecute(List<TrailerTile> result) {
            if(null != result) {
                mVideosAdapter.clear();
                for(TrailerTile trailerTile : result) {
                    mVideosAdapter.add(trailerTile);
                }
            }
        }

        private List<TrailerTile> getTrailerDataFromJson(String jsonString) {
            List<TrailerTile> trailerTileList = new ArrayList<>();
            try {
                JSONObject resultObject = new JSONObject(jsonString);
                JSONArray resultArray = resultObject.getJSONArray(RESULTS);

                for (int i = 0; i < resultArray.length(); i++) {
                    JSONObject trailerObject = resultArray.getJSONObject(i);
                    String trailerKey = trailerObject.getString("key");
                    String site = trailerObject.getString("site");
                    TrailerTile trailer = new TrailerTile(site, trailerKey);
                    trailerTileList.add(trailer);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("MainActivityFragment","Error in fetching movie details", e);
            }
            return trailerTileList;
        }
    }
}