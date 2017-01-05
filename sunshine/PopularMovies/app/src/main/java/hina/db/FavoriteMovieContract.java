package hina.db;

import android.provider.BaseColumns;

/**
 * Created by jainhina on 04/01/17.
 */

public class FavoriteMovieContract {

    public static final class FavoriteMovieEntry implements BaseColumns {
        public static final String TABLE_NAME = "favorite";
        public static final String MOVIE_ID = "id";
        public static final String MOVIE_IMAGE = "image";
    }
}
