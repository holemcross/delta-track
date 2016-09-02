package com.holemcross.deltatrack.data.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.holemcross.deltatrack.data.Location;
import com.holemcross.deltatrack.data.Stop;
import com.holemcross.deltatrack.data.database.DeltaTrackContract;
import com.holemcross.deltatrack.data.database.DeltaTrackDbHelper;

import java.util.ArrayList;

import helpers.SqlHelper;

/**
 * Created by amortega on 8/31/2016.
 */
public class StopRepository extends DeltaTrackRepository {
    public StopRepository(DeltaTrackDbHelper helper){
        super(helper);
    }

    public StopRepository(SQLiteDatabase db) {
        super(db);
    }

    public long insertStopWithStationId(Stop stop, long stationId){
        SQLiteDatabase db = getDb();
        // Insert Location First
        LocationRepository locationRepo = new LocationRepository(db);
        long locationId = locationRepo.insertLocation(stop.location);

        // Insert Stop
        ContentValues newStop = new ContentValues();
        newStop.put(DeltaTrackContract.DeltaTrackStop.COLUMN_NAME_MAP_ID, stop.mapId);
        newStop.put(DeltaTrackContract.DeltaTrackStop.COLUMN_NAME_STOP_ID, stop.stopId);
        newStop.put(DeltaTrackContract.DeltaTrackStop.COLUMN_NAME_STATION_ID, stationId);
        newStop.put(DeltaTrackContract.DeltaTrackStop.COLUMN_NAME_STATION_NAME, stop.stationName);
        newStop.put(DeltaTrackContract.DeltaTrackStop.COLUMN_NAME_STOP_NAME, stop.stopName);
        newStop.put(DeltaTrackContract.DeltaTrackStop.COLUMN_NAME_DESCRIPTIVE_NAME, stop.descriptiveName);
        newStop.put(DeltaTrackContract.DeltaTrackStop.COLUMN_NAME_IS_HANDICAP_ACCESSIBLE, stop.isHandicapAccessible);
        newStop.put(DeltaTrackContract.DeltaTrackStop.COLUMN_NAME_LOCATION_ID, locationId);
        long newStopId = db.insert(DeltaTrackContract.DeltaTrackStop.TABLE_NAME, null, newStop);

        // Insert Routes
        RoutesRepository routesRepo = new RoutesRepository(db);
        routesRepo.insertStopRoutesWithRoutesAndStopId(stop.routes, newStopId);

        return newStopId;
    }

    public ArrayList<Stop> getStopsByMapId(int mapId){
        SQLiteDatabase db = getDb(true);
        String[] projection = {
                SqlHelper.TableDotProperty(DeltaTrackContract.DeltaTrackStop.TABLE_NAME, DeltaTrackContract.DeltaTrackStop._ID),
                SqlHelper.TableDotProperty(DeltaTrackContract.DeltaTrackStop.TABLE_NAME, DeltaTrackContract.DeltaTrackStop.COLUMN_NAME_MAP_ID),
                SqlHelper.TableDotProperty(DeltaTrackContract.DeltaTrackStop.TABLE_NAME, DeltaTrackContract.DeltaTrackStop.COLUMN_NAME_STATION_NAME),
                SqlHelper.TableDotProperty(DeltaTrackContract.DeltaTrackStop.TABLE_NAME, DeltaTrackContract.DeltaTrackStop.COLUMN_NAME_IS_HANDICAP_ACCESSIBLE),
                SqlHelper.TableDotProperty(DeltaTrackContract.DeltaTrackStop.TABLE_NAME, DeltaTrackContract.DeltaTrackStop.COLUMN_NAME_STATION_ID),
                SqlHelper.TableDotProperty(DeltaTrackContract.DeltaTrackStop.TABLE_NAME, DeltaTrackContract.DeltaTrackStop.COLUMN_NAME_STOP_NAME),
                SqlHelper.TableDotProperty(DeltaTrackContract.DeltaTrackStop.TABLE_NAME, DeltaTrackContract.DeltaTrackStop.COLUMN_NAME_STOP_ID),
                SqlHelper.TableDotProperty(DeltaTrackContract.DeltaTrackStop.TABLE_NAME, DeltaTrackContract.DeltaTrackStop.COLUMN_NAME_DESCRIPTIVE_NAME),
                SqlHelper.TableDotProperty(DeltaTrackContract.DeltaTrackLocation.TABLE_NAME, DeltaTrackContract.DeltaTrackLocation.COLUMN_NAME_LATITUDE),
                SqlHelper.TableDotProperty(DeltaTrackContract.DeltaTrackLocation.TABLE_NAME, DeltaTrackContract.DeltaTrackLocation.COLUMN_NAME_LONGITUDE)
        };

        String selection = DeltaTrackContract.DeltaTrackStop.COLUMN_NAME_MAP_ID + " = ?";
        String[] selectionArgs = { Integer.toString(mapId) };

        Cursor c = db.query(
                DeltaTrackContract.DeltaTrackStop.TABLE_NAME + " INNER JOIN " + DeltaTrackContract.DeltaTrackLocation.TABLE_NAME +
                        " ON " + SqlHelper.TableDotProperty(DeltaTrackContract.DeltaTrackStop.TABLE_NAME, DeltaTrackContract.DeltaTrackStop.COLUMN_NAME_LOCATION_ID) +
                        " = " + SqlHelper.TableDotProperty(DeltaTrackContract.DeltaTrackLocation.TABLE_NAME, DeltaTrackContract.DeltaTrackLocation._ID),    // The table to query
                projection,                                         // The columns to return
                selection,                                          // The columns for the WHERE clause
                selectionArgs,                                      // The values for the WHERE clause
                null,                                               // don't group the rows
                null,                                               // don't filter by row groups
                null                                                // The sort order
        );

        ArrayList<Stop> resultStops = new ArrayList<Stop>();
        Stop tempStop = null;
        if( c.getCount() > 0){
            c.moveToFirst();
            while(!c.isAfterLast()){
                tempStop = new Stop();
                tempStop.id = c.getLong( c.getColumnIndex(SqlHelper.TableDotProperty(DeltaTrackContract.DeltaTrackStop.TABLE_NAME, DeltaTrackContract.DeltaTrackStop._ID)));
                tempStop.mapId = c.getInt( c.getColumnIndex(SqlHelper.TableDotProperty(DeltaTrackContract.DeltaTrackStop.TABLE_NAME, DeltaTrackContract.DeltaTrackStop.COLUMN_NAME_MAP_ID)));
                tempStop.stopId = c.getInt( c.getColumnIndex(SqlHelper.TableDotProperty(DeltaTrackContract.DeltaTrackStop.TABLE_NAME, DeltaTrackContract.DeltaTrackStop.COLUMN_NAME_STOP_ID)));
                tempStop.stationName = c.getString( c.getColumnIndex(SqlHelper.TableDotProperty(DeltaTrackContract.DeltaTrackStop.TABLE_NAME, DeltaTrackContract.DeltaTrackStop.COLUMN_NAME_STATION_NAME)));
                tempStop.descriptiveName = c.getString( c.getColumnIndex(SqlHelper.TableDotProperty(DeltaTrackContract.DeltaTrackStop.TABLE_NAME, DeltaTrackContract.DeltaTrackStop.COLUMN_NAME_DESCRIPTIVE_NAME)));
                tempStop.stopName = c.getString( c.getColumnIndex(SqlHelper.TableDotProperty(DeltaTrackContract.DeltaTrackStop.TABLE_NAME, DeltaTrackContract.DeltaTrackStop.COLUMN_NAME_STOP_NAME)));
                tempStop.descriptiveName = c.getString( c.getColumnIndex(SqlHelper.TableDotProperty(DeltaTrackContract.DeltaTrackStop.TABLE_NAME, DeltaTrackContract.DeltaTrackStop.COLUMN_NAME_DESCRIPTIVE_NAME)));
                tempStop.isHandicapAccessible =  c.getInt( c.getColumnIndex(SqlHelper.TableDotProperty(DeltaTrackContract.DeltaTrackStop.TABLE_NAME, DeltaTrackContract.DeltaTrackStop.COLUMN_NAME_IS_HANDICAP_ACCESSIBLE))) != 0;
                tempStop.location = new Location(c.getDouble( c.getColumnIndex(SqlHelper.TableDotProperty(DeltaTrackContract.DeltaTrackLocation.TABLE_NAME, DeltaTrackContract.DeltaTrackLocation.COLUMN_NAME_LATITUDE))),
                        c.getDouble( c.getColumnIndex(SqlHelper.TableDotProperty(DeltaTrackContract.DeltaTrackLocation.TABLE_NAME, DeltaTrackContract.DeltaTrackLocation.COLUMN_NAME_LONGITUDE))));
                resultStops.add(tempStop);

                c.moveToNext();
            }
        }

        c.close();

        RoutesRepository routesRepo = new RoutesRepository(db);

        // Get Routes
        for (Stop stop: resultStops
             ) {
            stop.routes = routesRepo.getAllRoutesForStopId(stop.id);
        }

        return resultStops;
    }
}
