package com.holemcross.deltatrack.data.database;

import android.provider.BaseColumns;

/**
 * Created by amortega on 8/31/2016.
 */
public final class DeltaTrackContract {
    private DeltaTrackContract(){}

    public static class DeltaTrackStation implements BaseColumns {
        public static final String TABLE_NAME = "station";
        public static final String COLUMN_NAME_STATION_NAME = "station_name";
        public static final String COLUMN_NAME_MAP_ID = "map_id";
        // Stops
    }

    public static class DeltaTrackStop implements BaseColumns {
        public static final String TABLE_NAME = "stop";
        public static final String COLUMN_NAME_STATION_ID = "station_id";
        public static final String COLUMN_NAME_STATION_NAME = "station_name";
        public static final String COLUMN_NAME_STOP_NAME = "stop_name";
        public static final String COLUMN_NAME_DESCRIPTIVE_NAME = "descriptive_name";
        public static final String COLUMN_NAME_MAP_ID = "map_id";
        public static final String COLUMN_NAME_STOP_ID = "stop_id";
        public static final String COLUMN_NAME_IS_HANDICAP_ACCESSIBLE = "is_handicap_accessible";
        public static final String COLUMN_NAME_LOCATION_ID = "location_id";
        // Routes
    }

    public static class DeltaTrackLocation implements BaseColumns {
        public static final String TABLE_NAME = "location";
        public static final String COLUMN_NAME_LATITUDE = "latitude";
        public static final String COLUMN_NAME_LONGITUDE = "longitude";
    }

    public static class DeltaTrackStopRoute implements BaseColumns {
        public static final String TABLE_NAME = "stoproute";
        public static final String COLUMN_NAME_STOP_ID = "stop_id";
        public static final String COLUMN_NAME_ROUTE_ID = "route_id";
    }

    public static class DeltaTrackRoute implements BaseColumns {
        public static final String TABLE_NAME = "routes";
        public static final String COLUMN_NAME_ROUTE_NAME = "route_name";
        public static final String COLUMN_NAME_ROUTE_ABBREVIATION = "route_abbreviation";
    }
}
