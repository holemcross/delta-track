package com.holemcross.deltatrack.data.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.holemcross.deltatrack.data.Station;
import com.holemcross.deltatrack.data.Stop;
import com.holemcross.deltatrack.data.database.DeltaTrackContract;
import com.holemcross.deltatrack.data.database.DeltaTrackDbHelper;

import java.util.ArrayList;

/**
 * Created by amortega on 8/31/2016.
 */
public class StationRepository extends DeltaTrackRepository{
    public StationRepository(DeltaTrackDbHelper helper){
        super(helper);
    }

    public void insertStations(ArrayList<Station> stations){
        for (Station station:stations
             ) {
            this.insertStation(station);
        }

    }

    public void insertStation(Station station){
        SQLiteDatabase db = getDb();

        // Insert Station Data First
        ContentValues newStation = new ContentValues();
        newStation.put(DeltaTrackContract.DeltaTrackStation.COLUMN_NAME_MAP_ID, station.mapId);
        newStation.put(DeltaTrackContract.DeltaTrackStation.COLUMN_NAME_STATION_NAME, station.stationName);
        long newStationId = db.insert(DeltaTrackContract.DeltaTrackStation.TABLE_NAME, null, newStation);


        // Create Stops
        StopRepository stopRepo = new StopRepository(db);

        for (Stop stop: station.stops
             ) {
            stopRepo.insertStopWithStationId(stop, newStationId);
        }
    }


    public ArrayList<Station> getAllStations(){
        SQLiteDatabase db = getDb(true);

        String[] projection = {
                DeltaTrackContract.DeltaTrackStation._ID,
                DeltaTrackContract.DeltaTrackStation.COLUMN_NAME_MAP_ID,
                DeltaTrackContract.DeltaTrackStation.COLUMN_NAME_STATION_NAME
        };

        Cursor c = db.query(
                DeltaTrackContract.DeltaTrackStation.TABLE_NAME,    // The table to query
                projection,                                         // The columns to return
                null,                                               // The columns for the WHERE clause
                null,                                               // The values for the WHERE clause
                null,                                               // don't group the rows
                null,                                               // don't filter by row groups
                null                                                // The sort order
        );
        ArrayList<Station> resultStations = new ArrayList<Station>();
        Station tempStation = null;
        if(c.getCount() > 0){
            c.moveToFirst();
            while(!c.isAfterLast()){
                tempStation = new Station();
                tempStation.stationId = c.getLong( c.getColumnIndex(DeltaTrackContract.DeltaTrackStation._ID));
                tempStation.mapId = c.getInt( c.getColumnIndex(DeltaTrackContract.DeltaTrackStation.COLUMN_NAME_MAP_ID));
                tempStation.stationName = c.getString( c.getColumnIndex(DeltaTrackContract.DeltaTrackStation.COLUMN_NAME_STATION_NAME));
                resultStations.add(tempStation);

                c.moveToNext();
            }
        }

        c.close();

        StopRepository stopRepo = new StopRepository(db);
        // Get Stops for stations
        for (Station station: resultStations
             ) {
            station.stops = stopRepo.getStopsByMapId(station.mapId);
        }
        return resultStations;
    }
}
