package kiwi.jordancrawford.kiwiexplorer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.wifi.WifiEnterpriseConfig;

import java.util.HashMap;
import java.util.Map;

import static kiwi.jordancrawford.kiwiexplorer.DatabaseContract.CityDataEntry;

/**
 * Created by Jordan on 29/09/16.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String NO_ID_EXCEPTION_MESSAGE = "No ID on record. Is it saved?";
    private static final String ID_PROPERTIES = "INTEGER PRIMARY KEY AUTOINCREMENT";
    private static final String SQL_CREATE_TABLE_CITY_DATA =
            "CREATE TABLE " + CityDataEntry.TABLE_NAME + "("
                    + CityDataEntry._ID + " " + ID_PROPERTIES + ","
                    + CityDataEntry.CITY_NAME + " " + CityDataEntry.CITY_NAME_TYPE + ","
                    + CityDataEntry.CITY_SEEN + " " + CityDataEntry.CITY_SEEN_TYPE + ","
                    + CityDataEntry.IS_CURRENT_LOCATION + " " + CityDataEntry.IS_CURRENT_LOCATION_TYPE +  ")";
    private static final String SQL_DROP_TABLE_CITY_DATA =
            "DROP TABLE IF EXISTS " + CityDataEntry.TABLE_NAME;

    private String[] projection = {
            CityDataEntry._ID,
            CityDataEntry.CITY_NAME,
            CityDataEntry.CITY_SEEN,
            CityDataEntry.IS_CURRENT_LOCATION
    };

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "cities.db";

    public static DatabaseHelper instance = null;

    /*
        As per: http://stackoverflow.com/questions/8888530/is-it-ok-to-have-one-instance-of-sqliteopenhelper-shared-by-all-activities-in-an
        If this gets cleared it doesn't matter, a new helper instance can be created. This ensures all accesses will use the same SQLIteDatabase object.
     */
    public static DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            // Instantiate with the application context so the activity context isn't leaked.
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Gets the field required to add / update a CityData.
    private ContentValues getCityDataFields(CityData cityData) {
        ContentValues values = new ContentValues();
        values.put(CityDataEntry.CITY_NAME, cityData.getCityName());
        values.put(CityDataEntry.CITY_SEEN, cityData.isCitySeen());
        values.put(CityDataEntry.IS_CURRENT_LOCATION, cityData.isCurrentLocation());
        return values;
    }

    /**
     * Fills in a CityData object based on the current position of a cursor.
     * @param cursor The cursor to fill in from.
     * @return A filled in CityData object.
     */
    private CityData getCityDataFromCursor(Cursor cursor) {
        CityData cityData = new CityData();
        cityData.setId(cursor.getInt(0));
        cityData.setCityName(cursor.getString(1));
        cityData.setCitySeen(cursor.getInt(2) == 1);
        cityData.setCurrentLocation(cursor.getInt(3) == 1);
        return cityData;
    }

    /**
     * Searches the database for a city name. May not get a result.
     *
     * @param cityName The city name to look for results for.
     * @return Null or a CityData object.
     */
    private CityData getRawCityDataByCityName(String cityName) {
        Cursor queryResult = getReadableDatabase().query(
                CityDataEntry.TABLE_NAME,
                projection,
                CityDataEntry.CITY_NAME + " = ?",
                new String[]{ cityName },
                null,
                null,
                null
        );
        if (queryResult.getCount() > 0) {
            queryResult.moveToFirst();
            CityData cityData = getCityDataFromCursor(queryResult);
            return cityData;
        } else {
            return null;
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE_CITY_DATA);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // If the schema changes, just delete everything and re-create it.
        db.execSQL(SQL_DROP_TABLE_CITY_DATA);
        onCreate(db);
    }

    /**
     * Gets the CityData by the city name. This will always return a CityData object (this is the source of creation of these objects!).
     *
     * @param cityName
     * @return
     */
    public CityData getCityDataByCityName(String cityName) {
        CityData toReturn = getRawCityDataByCityName(cityName);
        if (toReturn == null) {
            // Make a CityData object.
            toReturn = new CityData();
            toReturn.setCityName(cityName);

            // Add the CityData to the database.
            long id = getWritableDatabase().insert(
                    CityDataEntry.TABLE_NAME,
                    null,
                    getCityDataFields(toReturn)
            );
            toReturn.setId(id);
        }

        return toReturn;
    }

    /**
     * Gets the current city. This returns the current city or null if there is none.
     *
     * @return
     */
    public CityData getCurrentCity() {
        Cursor queryResult = getReadableDatabase().query(
                CityDataEntry.TABLE_NAME,
                projection,
                CityDataEntry.IS_CURRENT_LOCATION + " = 1",
                null,
                null,
                null,
                null
        );
        if (queryResult.getCount() > 0) {
            queryResult.moveToFirst();
            CityData cityData = getCityDataFromCursor(queryResult);
            return cityData;
        } else {
            return null;
        }
    }

    /**
     * Gets all available CityData objects.
     *
     * @return
     */
    public Map<String, CityData> getAllCityData() {
        Cursor queryResult = getReadableDatabase().query(
                CityDataEntry.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                null
        );
        Map<String, CityData> toReturn = new HashMap<>();
        if (queryResult.moveToFirst()) {
            do {
                CityData cityData = getCityDataFromCursor(queryResult);
                toReturn.put(cityData.getCityName(), cityData);
            } while(queryResult.moveToNext());
        }
        queryResult.close();
        return toReturn;
    }

    /**
     * Updates CityData object. True if updated, false if an error occurred.
     *
     * @param toUpdate
     */
    public boolean updateCityData(CityData toUpdate) {
        // Update the city.
        if (toUpdate.getId() == -1) {
            throw new IllegalArgumentException(NO_ID_EXCEPTION_MESSAGE);
        }
        int updateResult = getWritableDatabase().update(
                CityDataEntry.TABLE_NAME,
                getCityDataFields(toUpdate),
                CityDataEntry._ID + " = ?",
                new String[] { String.valueOf(toUpdate.getId()) });
        return updateResult == 1;
    }
}
