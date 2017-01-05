package hina.tile;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import hina.popularmovies.R;

/**
 * Created by jainhina on 05/01/17.
 */

public class ReviewTileAdapter extends ArrayAdapter<ReviewTile> {

    public ReviewTileAdapter(Activity context, List<ReviewTile>reviewTileList) {
        super(context, 0, reviewTileList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ReviewTile reviewTile = getItem(position);
        if(convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.review_tile, parent, false);
        }
        TextView review = (TextView) convertView.findViewById(R.id.review);
        review.setText(reviewTile.getReview());
        return review;
    }
}
