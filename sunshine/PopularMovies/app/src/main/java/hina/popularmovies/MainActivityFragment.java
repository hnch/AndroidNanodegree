package hina.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hina.db.FavoriteMovieHelper;
import hina.tile.MovieTile;
import hina.tile.MovieTileAdapter;
import hina.util.NetworkUtility;

import static hina.db.FavoriteMovieContract.FavoriteMovieEntry.TABLE_NAME;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    private static final String TOP_RATED = "vote_average.desc";
    private static final String FAVORITE = "favorite";
    private static final String MOST_POPULAR = "popularity.desc";
    private static final String SORT_KEY = "SORT_KEY";
    public static final String RESULTS = "results";
    public static final String ID = "id";
    public static final String POSTER_PATH = "poster_path";
    private ArrayAdapter<MovieTile> mMoviesAdapter;
    private String sortKey = MOST_POPULAR;
    private static final String DISCOVER_MOVIE_POPULAR = "movie/popular";
    private static final String TOP_RATED_MOVIE = "movie/top_rated";
    public static final String MOVIE_ID = "id";
    public static final String MOVIE_IMAGE = "image";
    private FavoriteMovieHelper mFavoriteHelper;

    public MainActivityFragment() {
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_main, menu);
        mFavoriteHelper = new FavoriteMovieHelper(getActivity());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        FetchMoviesTask fetchMoviesTask = new FetchMoviesTask();
        //update the movie list based on user selection
        switch(id) {
            case R.id.action_rating:
                fetchMoviesTask.execute(TOP_RATED);
                sortKey = TOP_RATED;
                break;
            case R.id.action_favorite:
                fetchMoviesTask.execute(FAVORITE);
                break;
            default:
                fetchMoviesTask.execute(MOST_POPULAR);
                sortKey = MOST_POPULAR;
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //restore the order which was used to sort previously
        if (null != savedInstanceState) {
            sortKey = savedInstanceState.getString(SORT_KEY);
        }
        updateMovies(sortKey);
        setHasOptionsMenu(true);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        //save the setting which user chose to sort the movies
        savedInstanceState.putString(SORT_KEY, sortKey);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        mMoviesAdapter = new MovieTileAdapter(
                getActivity(),
                new ArrayList<>()
        );
        GridView gridView = (GridView) rootView.findViewById(R.id.gridview_movies);
        gridView.setAdapter(mMoviesAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                MovieTile movieTile = mMoviesAdapter.getItem(i);
                Intent intent = new Intent(getActivity(), DetailActivity.class).putExtra(MOVIE_ID, movieTile.getMovieId()).putExtra(MOVIE_IMAGE,movieTile.getMovieImageURL());
                startActivity(intent);
            }
        });

        return rootView;

    }

    //update based on the sort key selected
    private void updateMovies(String sortKey) {
        FetchMoviesTask fetchMoviesTask = new FetchMoviesTask();
        if(isOnline()) {
            fetchMoviesTask.execute(sortKey);
        }
    }

    /**
     * to avoid app crash when network is not available
     * @return
     */
    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    /**
     * task to get the list of movies from movieDB
     */
    public class FetchMoviesTask extends AsyncTask<String, Void, List<MovieTile>> {
        private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();


        @Override
        protected List<MovieTile> doInBackground(String... params) {

            if (params.length == 0) {
                return null;
            }

            String movieOperation = DISCOVER_MOVIE_POPULAR;
            Map<String, String> queryParams = new HashMap<>();
            if(params[0].equals(TOP_RATED)) {
                movieOperation = TOP_RATED_MOVIE;
            }
            else if(params[0].equals(FAVORITE)) {
                SQLiteDatabase db = mFavoriteHelper.getReadableDatabase();
                Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
                Log.e("db", cursor.toString());

                JSONArray resultArray = new JSONArray();
                JSONObject resultObject = new JSONObject();
                if (cursor != null && cursor.getCount() > 0) {
                    while (cursor.moveToNext()) {
                        try {
                            JSONObject object = new JSONObject();
                            object.put("id", cursor.getString(0));
                            object.put("poster_path", cursor.getString(1));
                            resultArray.put(object);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
                try {
                    resultObject.put("results", resultArray);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                db.close();
                return getMovieDataFromJson(resultObject.toString());
            }
            StringBuffer buffer = NetworkUtility.getDataFromMovieDb(movieOperation, queryParams);
            try {
                String moviesJsonStr = buffer.toString();
                return getMovieDataFromJson(moviesJsonStr);
            } catch (Exception e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<MovieTile> result) {
            if (null != result) {
                mMoviesAdapter.clear();
                for (MovieTile movieTile : result) {
                    mMoviesAdapter.add(movieTile);
                }
            }
        }

        private List<MovieTile> getMovieDataFromJson(String jsonString) {
            List<MovieTile> movieTileList = new ArrayList<>();
            try {
                JSONObject resultObject = new JSONObject(jsonString);
                JSONArray resultArray = resultObject.getJSONArray(RESULTS);

                for (int i = 0; i < resultArray.length(); i++) {
                    JSONObject movieObject = resultArray.getJSONObject(i);
                    String movieId = movieObject.getString(ID);
                    String imageUrl = movieObject.getString(POSTER_PATH);
                    MovieTile movieTile = new MovieTile(movieId, imageUrl);
                    movieTileList.add(movieTile);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("MainActivityFragment","Error in fetching movie details", e);
            }
            return movieTileList;
        }

    }
}

