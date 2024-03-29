package kiwi.jordancrawford.kiwiexplorer;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Map;

/**
 * Helpers to get cities from the JSON file or database.
 *
 * Created by Jordan on 29/09/16.
 */
public class Cities {
    private static ArrayList<City> cities = null;

    /**
     * Builds a city from a JSON object.
     *
     * @param jsonCity
     * @return
     * @throws JSONException
     */
    private static City buildCityFromJSON(JSONObject jsonCity) throws JSONException {
        City city = new City();
        city.setName(jsonCity.getString("name"));
        city.setLatitude(jsonCity.getDouble("lat"));
        city.setLongitude(jsonCity.getDouble("long"));
        city.setPictureResourceName(jsonCity.getString("picture"));
        return city;
    }

    /**
     * Sets up city data from an input stream of JSON.
     *
     * @param jsonInput
     * @return
     * @throws IOException
     * @throws JSONException
     */
    public static ArrayList<City> setupData(InputStream jsonInput) throws IOException, JSONException {
        ArrayList<City> cities = new ArrayList<>();
        // Read from the JSON file.
        Reader reader = new BufferedReader(new InputStreamReader(jsonInput));
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];

        int inputSize;
        try {
            // While there is something to read, read to the buffer.
            while((inputSize = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, inputSize);
            }
        } finally {
            jsonInput.close();
        }
        String inputString = writer.toString();

        // Parse the JSON and build cities from JSON.
        JSONArray jsonCities = new JSONArray(inputString);
        for (int cityIndex = 0; cityIndex < jsonCities.length(); cityIndex++) {
            JSONObject jsonCity = jsonCities.getJSONObject(cityIndex);
            cities.add(buildCityFromJSON(jsonCity));
        }

        return cities;
    }

    /**
     * Gets all cities. Either returns the last read cities or gets cities from the file if not read yet.
     * @param context
     * @return
     * @throws IOException
     * @throws JSONException
     */
    public static ArrayList<City> getCities(Context context) throws IOException, JSONException {
        if (cities == null) {
            cities = setupData(context.getApplicationContext().getResources().openRawResource(R.raw.cities));
        }
        return cities;
    }

    /**
     * Fills in CityData objects on the cities. CityData objects contain the city information from the database, such as the current location and if the city has been seen.
     *
     * @param context
     * @throws IOException
     * @throws JSONException
     */
    public static void fillInCityData(Context context) throws IOException, JSONException {
        ArrayList<City> cities = getCities(context);

        // Get a map of city names and CityData from the database.
        Map<String, CityData> allCityData = DatabaseHelper.getInstance(context).getAllCityData();

        for (City city : cities) {
            if (allCityData.containsKey(city.getName())) {
                city.setCityData(allCityData.get(city.getName()));
            } else {
                city.setCityData(null);
            }
        }
    }

}
