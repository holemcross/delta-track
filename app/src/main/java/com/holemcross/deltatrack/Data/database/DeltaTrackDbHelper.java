package com.holemcross.deltatrack.data.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import helpers.SqlHelper;

/**
 * Created by amortega on 8/31/2016.
 */
public class DeltaTrackDbHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "DeltaTrack.db";

    public DeltaTrackDbHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String createTableRoute = SqlHelper.CREATE_TABLE + DeltaTrackContract.DeltaTrackRoute.TABLE_NAME +
                " (" + DeltaTrackContract.DeltaTrackRoute._ID + SqlHelper.PRIMARY_KEY +
                DeltaTrackContract.DeltaTrackRoute.COLUMN_NAME_ROUTE_NAME + SqlHelper.TEXT_TYPE + SqlHelper.COMMA_SEP +
                DeltaTrackContract.DeltaTrackRoute.COLUMN_NAME_ROUTE_ABBREVIATION + SqlHelper.TEXT_TYPE + " )";

        String createTableLocation = SqlHelper.CREATE_TABLE + DeltaTrackContract.DeltaTrackLocation.TABLE_NAME +
                " (" + DeltaTrackContract.DeltaTrackLocation._ID + SqlHelper.PRIMARY_KEY +
                DeltaTrackContract.DeltaTrackLocation.COLUMN_NAME_LATITUDE + SqlHelper.INT_TYPE + SqlHelper.COMMA_SEP +
                DeltaTrackContract.DeltaTrackLocation.COLUMN_NAME_LONGITUDE + SqlHelper.REAL_TYPE + " )";

        String createTableStopRoute = SqlHelper.CREATE_TABLE + DeltaTrackContract.DeltaTrackStopRoute.TABLE_NAME +
                " (" + DeltaTrackContract.DeltaTrackStopRoute._ID + SqlHelper.PRIMARY_KEY +
                DeltaTrackContract.DeltaTrackStopRoute.COLUMN_NAME_STOP_ID + SqlHelper.TEXT_TYPE + SqlHelper.COMMA_SEP +
                DeltaTrackContract.DeltaTrackStopRoute.COLUMN_NAME_ROUTE_ID + SqlHelper.TEXT_TYPE + SqlHelper.COMMA_SEP +
                SqlHelper.ForeignKeyHelper(DeltaTrackContract.DeltaTrackStopRoute.COLUMN_NAME_STOP_ID, DeltaTrackContract.DeltaTrackStop.TABLE_NAME) + SqlHelper.COMMA_SEP +
                SqlHelper.ForeignKeyHelper(DeltaTrackContract.DeltaTrackStopRoute.COLUMN_NAME_ROUTE_ID, DeltaTrackContract.DeltaTrackRoute.TABLE_NAME) + " )";

        String createTableStation = SqlHelper.CREATE_TABLE + DeltaTrackContract.DeltaTrackStation.TABLE_NAME +
                " (" + DeltaTrackContract.DeltaTrackStation._ID + SqlHelper.PRIMARY_KEY +
                DeltaTrackContract.DeltaTrackStation.COLUMN_NAME_STATION_NAME + SqlHelper.TEXT_TYPE + SqlHelper.COMMA_SEP +
                DeltaTrackContract.DeltaTrackStation.COLUMN_NAME_MAP_ID + SqlHelper.INT_TYPE + " )";

        String createTableStop = SqlHelper.CREATE_TABLE + DeltaTrackContract.DeltaTrackStop.TABLE_NAME +
                " (" + DeltaTrackContract.DeltaTrackStop._ID + SqlHelper.PRIMARY_KEY +
                DeltaTrackContract.DeltaTrackStop.COLUMN_NAME_STATION_ID + SqlHelper.INT_TYPE + SqlHelper.COMMA_SEP +
                DeltaTrackContract.DeltaTrackStop.COLUMN_NAME_STOP_NAME + SqlHelper.TEXT_TYPE + SqlHelper.COMMA_SEP +
                DeltaTrackContract.DeltaTrackStop.COLUMN_NAME_STATION_NAME + SqlHelper.TEXT_TYPE + SqlHelper.COMMA_SEP +
                DeltaTrackContract.DeltaTrackStop.COLUMN_NAME_MAP_ID + SqlHelper.INT_TYPE + SqlHelper.COMMA_SEP +
                DeltaTrackContract.DeltaTrackStop.COLUMN_NAME_STOP_ID + SqlHelper.INT_TYPE + SqlHelper.COMMA_SEP +
                DeltaTrackContract.DeltaTrackStop.COLUMN_NAME_DESCRIPTIVE_NAME + SqlHelper.TEXT_TYPE + SqlHelper.COMMA_SEP +
                DeltaTrackContract.DeltaTrackStop.COLUMN_NAME_LOCATION_ID + SqlHelper.INT_TYPE + SqlHelper.COMMA_SEP +
                DeltaTrackContract.DeltaTrackStop.COLUMN_NAME_IS_HANDICAP_ACCESSIBLE + SqlHelper.INT_TYPE + SqlHelper.COMMA_SEP +
                SqlHelper.ForeignKeyHelper(DeltaTrackContract.DeltaTrackStop.COLUMN_NAME_LOCATION_ID, DeltaTrackContract.DeltaTrackLocation.TABLE_NAME) + SqlHelper.COMMA_SEP +
                SqlHelper.ForeignKeyHelper(DeltaTrackContract.DeltaTrackStop.COLUMN_NAME_STATION_ID, DeltaTrackContract.DeltaTrackStation.TABLE_NAME) + " )";

        db.execSQL(createTableRoute);
        db.execSQL(createTableLocation);
        db.execSQL(createTableStation);
        db.execSQL(createTableStop);
        db.execSQL(createTableStopRoute);

        // Initialize
        initializeDefaultData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        dropDatabase(db);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //super.onDowngrade(db, oldVersion, newVersion);
        onUpgrade(db, oldVersion, newVersion);
    }

    public void dropDatabase(SQLiteDatabase db){
        String dropLocations = SqlHelper.DROP_TABLE + DeltaTrackContract.DeltaTrackLocation.TABLE_NAME;
        String dropRoutes = SqlHelper.DROP_TABLE + DeltaTrackContract.DeltaTrackRoute.TABLE_NAME;
        String dropStations = SqlHelper.DROP_TABLE + DeltaTrackContract.DeltaTrackStation.TABLE_NAME;
        String dropStops = SqlHelper.DROP_TABLE + DeltaTrackContract.DeltaTrackStop.TABLE_NAME;
        String dropStopRoutes = SqlHelper.DROP_TABLE + DeltaTrackContract.DeltaTrackStopRoute.TABLE_NAME;

        db.execSQL(dropLocations);
        db.execSQL(dropRoutes);
        db.execSQL(dropStations);
        db.execSQL(dropStops);
        db.execSQL(dropStopRoutes);
    }

    private void initializeDefaultData(SQLiteDatabase db){
        // Routes
        ContentValues routeBlue = new ContentValues();
        routeBlue.put(DeltaTrackContract.DeltaTrackRoute.COLUMN_NAME_ROUTE_NAME, "Blue");
        routeBlue.put(DeltaTrackContract.DeltaTrackRoute.COLUMN_NAME_ROUTE_ABBREVIATION, "blue");

        ContentValues routeBrown = new ContentValues();
        routeBrown.put(DeltaTrackContract.DeltaTrackRoute.COLUMN_NAME_ROUTE_NAME, "Brown");
        routeBrown.put(DeltaTrackContract.DeltaTrackRoute.COLUMN_NAME_ROUTE_ABBREVIATION, "brn");

        ContentValues routeGreen = new ContentValues();
        routeGreen.put(DeltaTrackContract.DeltaTrackRoute.COLUMN_NAME_ROUTE_NAME, "Green");
        routeGreen.put(DeltaTrackContract.DeltaTrackRoute.COLUMN_NAME_ROUTE_ABBREVIATION, "g");

        ContentValues routeOrange = new ContentValues();
        routeOrange.put(DeltaTrackContract.DeltaTrackRoute.COLUMN_NAME_ROUTE_NAME, "Orange");
        routeOrange.put(DeltaTrackContract.DeltaTrackRoute.COLUMN_NAME_ROUTE_ABBREVIATION, "o");

        ContentValues routePink = new ContentValues();
        routePink.put(DeltaTrackContract.DeltaTrackRoute.COLUMN_NAME_ROUTE_NAME, "Pink");
        routePink.put(DeltaTrackContract.DeltaTrackRoute.COLUMN_NAME_ROUTE_ABBREVIATION, "pnk");

        ContentValues routePurple = new ContentValues();
        routePurple.put(DeltaTrackContract.DeltaTrackRoute.COLUMN_NAME_ROUTE_NAME, "Purple");
        routePurple.put(DeltaTrackContract.DeltaTrackRoute.COLUMN_NAME_ROUTE_ABBREVIATION, "p");

        ContentValues routeRed = new ContentValues();
        routeRed.put(DeltaTrackContract.DeltaTrackRoute.COLUMN_NAME_ROUTE_NAME, "Red");
        routeRed.put(DeltaTrackContract.DeltaTrackRoute.COLUMN_NAME_ROUTE_ABBREVIATION, "red");

        ContentValues routeYellow = new ContentValues();
        routeYellow.put(DeltaTrackContract.DeltaTrackRoute.COLUMN_NAME_ROUTE_NAME, "Yellow");
        routeYellow.put(DeltaTrackContract.DeltaTrackRoute.COLUMN_NAME_ROUTE_ABBREVIATION, "y");

        db.insert(DeltaTrackContract.DeltaTrackRoute.TABLE_NAME, null, routeBlue);
        db.insert(DeltaTrackContract.DeltaTrackRoute.TABLE_NAME, null, routeBrown);
        db.insert(DeltaTrackContract.DeltaTrackRoute.TABLE_NAME, null, routeGreen);
        db.insert(DeltaTrackContract.DeltaTrackRoute.TABLE_NAME, null, routeOrange);
        db.insert(DeltaTrackContract.DeltaTrackRoute.TABLE_NAME, null, routePink);
        db.insert(DeltaTrackContract.DeltaTrackRoute.TABLE_NAME, null, routePurple);
        db.insert(DeltaTrackContract.DeltaTrackRoute.TABLE_NAME, null, routeRed);
        db.insert(DeltaTrackContract.DeltaTrackRoute.TABLE_NAME, null, routeYellow);
    }
}
