package hina.popularmovies;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import hina.tile.ReviewTile;
import hina.tile.ReviewTileAdapter;
import hina.util.NetworkUtility;

import static hina.popularmovies.DetailActivity.MOVIE_DETAIL;
import static hina.popularmovies.MainActivityFragment.MOVIE_ID;
import static hina.popularmovies.MainActivityFragment.RESULTS;

/**
 * Created by jainhina on 05/01/17.
 */

public class ReviewActivity extends ActionBarActivity {
    private String movieId;
    private ReviewTileAdapter mReviewTileAdapter;
    private final static String REVIEWS = "/reviews";

    @Override
    public void onStart() {
        super.onStart();
        getReviews();
        mReviewTileAdapter = new ReviewTileAdapter(this, new ArrayList<>());
        ListView listView = (ListView) findViewById(R.id.list_reviews);
        listView.setAdapter(mReviewTileAdapter);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.review_main);

    }

    private void getReviews() {
        Intent intent = getIntent();
        if (null != intent && intent.hasExtra(MOVIE_ID)) {
            movieId = intent.getStringExtra(MOVIE_ID);
            FetchReviewsTask fetchReviewsTask = new FetchReviewsTask();
            fetchReviewsTask.execute(movieId);
        }
    }

    public class FetchReviewsTask extends AsyncTask<String, Void, List<ReviewTile>> {
        @Override
        protected List<ReviewTile> doInBackground(String... params) {
            if (params.length == 0) {
                return null;
            }

            StringBuffer buffer = NetworkUtility.getDataFromMovieDb(MOVIE_DETAIL + params[0] + REVIEWS, new HashMap<>());
            return getReviewDataFromJson(buffer.toString());
        }

        @Override
        protected void onPostExecute(List<ReviewTile> result) {
            if(null != result) {
                mReviewTileAdapter.clear();
                for(ReviewTile reviewTile : result) {
                    mReviewTileAdapter.add(reviewTile);
                }
            }
        }

        private List<ReviewTile> getReviewDataFromJson(String jsonString) {
            List<ReviewTile> reviewTileList = new ArrayList<>();
            try {
                JSONObject resultObject = new JSONObject(jsonString);
                JSONArray resultArray = resultObject.getJSONArray(RESULTS);

                for (int i = 0; i < resultArray.length(); i++) {
                    JSONObject trailerObject = resultArray.getJSONObject(i);
                    String reviewDetail = trailerObject.getString("content");
                    ReviewTile review = new ReviewTile(reviewDetail);
                    reviewTileList.add(review);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("MainActivityFragment","Error in fetching movie details", e);
            }
            return reviewTileList;
        }
    }

}
