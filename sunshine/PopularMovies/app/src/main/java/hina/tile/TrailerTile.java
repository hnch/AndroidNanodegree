package hina.tile;

/**
 * each tile represents one trailer in the grid
 */

public class TrailerTile {
    private String trailerSite;
    private String trailerKey;

    public TrailerTile(String trailerSite, String trailerKey) {
        this.trailerSite = trailerSite;
        this.trailerKey = trailerKey;
    }

    public String getTrailerSite() {
        return this.trailerSite;
    }

    public String getTrailerKey() {
        return this.trailerKey;
    }

}
