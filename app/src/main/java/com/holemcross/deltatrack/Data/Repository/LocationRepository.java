package com.holemcross.deltatrack.data.repository;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.holemcross.deltatrack.data.Location;
import com.holemcross.deltatrack.data.database.DeltaTrackContract;
import com.holemcross.deltatrack.data.database.DeltaTrackDbHelper;

/**
 * Created by amortega on 9/1/2016.
 */
public class LocationRepository extends DeltaTrackRepository {
    public LocationRepository(DeltaTrackDbHelper helper) {
        super(helper);
    }

    public LocationRepository(SQLiteDatabase db) {
        super(db);
    }

    public long insertLocation(Location location){
        SQLiteDatabase db = getDb();

        // Insert Station Data First
        ContentValues newLocation = new ContentValues();
        newLocation.put(DeltaTrackContract.DeltaTrackLocation.COLUMN_NAME_LATITUDE, location.latitude);
        newLocation.put(DeltaTrackContract.DeltaTrackLocation.COLUMN_NAME_LONGITUDE, location.longitude);
        long newLocationId = db.insert(DeltaTrackContract.DeltaTrackLocation.TABLE_NAME, null, newLocation);

        return newLocationId;
    }
}
