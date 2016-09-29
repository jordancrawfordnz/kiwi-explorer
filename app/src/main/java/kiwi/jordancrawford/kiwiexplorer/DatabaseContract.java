package kiwi.jordancrawford.kiwiexplorer;

import android.provider.BaseColumns;

/**
 * Contains types and column names for the database.
 *
 * Created by Jordan on 29/09/16.
 */
public class DatabaseContract {
    private static final String TEXT_TYPE = "TEXT";
    private static final String BOOLEAN_TYPE = "BOOLEAN";

    public static class CityDataEntry implements BaseColumns {
        public static final String TABLE_NAME = "city_data";

        public static final String CITY_NAME = "city_name";
        public static final String CITY_NAME_TYPE = TEXT_TYPE;
        public static final String CITY_SEEN = "city_seen";
        public static final String CITY_SEEN_TYPE = BOOLEAN_TYPE;
        public static final String IS_CURRENT_LOCATION = "current_location";
        public static final String IS_CURRENT_LOCATION_TYPE = BOOLEAN_TYPE;
    }
}
