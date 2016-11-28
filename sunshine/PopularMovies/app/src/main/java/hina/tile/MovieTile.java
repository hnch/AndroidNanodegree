package hina.tile;

/**
 * each tile represents one movie tile in the grid
 */
public class MovieTile {
    private String movieId;
    private String movieImageURL;

    public MovieTile(String movieId, String movieImageURL) {
        this.movieId = movieId;
        this.movieImageURL = movieImageURL;
    }
    public String getMovieId() {
        return this.movieId;
    }

    public String getMovieImageURL() {
        return this.movieImageURL;
    }
}
