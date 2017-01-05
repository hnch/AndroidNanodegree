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
 * custom array adapter for trailers list
 */

public class TrailerTileAdapter extends ArrayAdapter<TrailerTile> {

    public TrailerTileAdapter(Activity context, List<TrailerTile> trailerTileList) {
        super(context,0,trailerTileList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TrailerTile trailerTile = getItem(position);

        if(convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.trailer_tile, parent, false);
        }
        TextView textView = (TextView) convertView.findViewById(R.id.trailer);
        textView.setText("Trailer " + String.valueOf(position));
        return textView;
    }
}
