package hina.util;

import android.net.Uri;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

/**
 * Utility class for making network calls
 */
public class NetworkUtility {

    private static final String BASE_URL = "http://api.themoviedb.org/3/";
    private static final String API_KEY = "";
    private static final String PARAM_API_KEY = "api_key";


    public static StringBuffer getDataFromMovieDb(String movieOpn, Map<String,String> queryParams) {
        // network call for getting movies data
        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        try {
            Uri uri = Uri.parse(BASE_URL + movieOpn);
            uri = uri.buildUpon().appendQueryParameter(PARAM_API_KEY, API_KEY).build();

            for (String key : queryParams.keySet()) {
                uri = uri.buildUpon().appendQueryParameter(key, queryParams.get(key)).build();
            }

            URL url = new URL(uri.toString());

            // Create the request to OpenWeatherMap, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }
            return buffer;

        } catch (IOException e) {
            Log.e("NetworkUtility", "Error ", e);
            // If the code didn't successfully get the weather data, there's no point in attemping
            // to parse it.
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e("NetworkUtility", "Error closing stream", e);
                }
            }
        }

    }
}
