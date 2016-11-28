package hina.tile;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;

import hina.popularmovies.R;

/**
 * custom array adapter for movies grid
 */
public class MovieTileAdapter extends ArrayAdapter<MovieTile> {

    private final static String BASE_IMAGE_URL = "http://image.tmdb.org/t/p/w185";
    public MovieTileAdapter(Activity context, List<MovieTile> movieTileList) {
        super(context,0,movieTileList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MovieTile movieTile = getItem(position);

        if(convertView == null) {
          convertView = LayoutInflater.from(getContext()).inflate(R.layout.movie_tile, parent, false);
        }

        ImageView movieImageView = (ImageView) convertView.findViewById(R.id.tile_image);
        Picasso.with(getContext()).load(BASE_IMAGE_URL + movieTile.getMovieImageURL()).into(movieImageView);
        return movieImageView;
    }
}
