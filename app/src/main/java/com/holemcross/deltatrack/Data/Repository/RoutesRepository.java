package com.holemcross.deltatrack.data.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.holemcross.deltatrack.data.CtaRoutes;
import com.holemcross.deltatrack.data.Route;
import com.holemcross.deltatrack.data.database.DeltaTrackContract;
import com.holemcross.deltatrack.data.database.DeltaTrackDbHelper;

import java.util.ArrayList;

import helpers.SqlHelper;

/**
 * Created by amortega on 9/1/2016.
 */
public class RoutesRepository extends DeltaTrackRepository {

    public RoutesRepository(DeltaTrackDbHelper helper) {
        super(helper);
    }
    public RoutesRepository(SQLiteDatabase db) {
        super(db);
    }

    public void insertStopRoutesWithRoutesAndStopId(ArrayList<CtaRoutes> routes, long stopId){
        SQLiteDatabase db = getDb();

        ArrayList<Route> allRoutes = getAllRoutes();

        for (CtaRoutes route: routes
             ) {
            // Get Route Id
            long routeId = -1;
            for (Route rt:allRoutes
                 ) {
                if(rt.route == route){
                    routeId = rt.id;
                    break;
                }
            }
            ContentValues newStopRoute = new ContentValues();
            newStopRoute.put(DeltaTrackContract.DeltaTrackStopRoute.COLUMN_NAME_STOP_ID, stopId);
            newStopRoute.put(DeltaTrackContract.DeltaTrackStopRoute.COLUMN_NAME_ROUTE_ID, routeId);
            db.insert(DeltaTrackContract.DeltaTrackStopRoute.TABLE_NAME, null, newStopRoute);
        }
    }

    public ArrayList<Route> getAllRoutes(){
        SQLiteDatabase db = getDb(true);

        String[] projection = {
                DeltaTrackContract.DeltaTrackRoute._ID,
                DeltaTrackContract.DeltaTrackRoute.COLUMN_NAME_ROUTE_NAME,
                DeltaTrackContract.DeltaTrackRoute.COLUMN_NAME_ROUTE_ABBREVIATION
        };

        Cursor c = db.query(
                DeltaTrackContract.DeltaTrackRoute.TABLE_NAME,    // The table to query
                projection,                                         // The columns to return
                null,                                               // The columns for the WHERE clause
                null,                                               // The values for the WHERE clause
                null,                                               // don't group the rows
                null,                                               // don't filter by row groups
                null                                                // The sort order
        );

        ArrayList<Route> resultRoutes = new ArrayList<Route>();
        Route tempRoute = null;
        if(c.getCount() > 0){
            c.moveToFirst();
            while(!c.isAfterLast()){
                tempRoute = new Route();
                tempRoute.id = c.getLong( c.getColumnIndex(DeltaTrackContract.DeltaTrackRoute._ID));
                tempRoute.name = c.getString( c.getColumnIndex(DeltaTrackContract.DeltaTrackRoute.COLUMN_NAME_ROUTE_NAME));
                tempRoute.abbreviation = c.getString( c.getColumnIndex(DeltaTrackContract.DeltaTrackRoute.COLUMN_NAME_ROUTE_ABBREVIATION));
                tempRoute.route = getCtaRouteByName(tempRoute.name);
                resultRoutes.add(tempRoute);
                c.moveToNext();
            }
        }

        c.close();

        return resultRoutes;
    }

    public ArrayList<CtaRoutes> getAllRoutesForStopId(long stopId){
        SQLiteDatabase db = getDb(true);

        // Get All StopRoutes
        String[] projection = {
                SqlHelper.TableDotProperty(DeltaTrackContract.DeltaTrackRoute.TABLE_NAME, DeltaTrackContract.DeltaTrackRoute.COLUMN_NAME_ROUTE_NAME),
        };

        String selection = DeltaTrackContract.DeltaTrackStopRoute.COLUMN_NAME_STOP_ID+ " = ?";
        String[] selectionArgs = { Long.toString(stopId) };

        Cursor c = db.query(
                DeltaTrackContract.DeltaTrackRoute.TABLE_NAME + " INNER JOIN " + DeltaTrackContract.DeltaTrackStopRoute.TABLE_NAME + " ON " +
                        SqlHelper.TableDotProperty(DeltaTrackContract.DeltaTrackRoute.TABLE_NAME, DeltaTrackContract.DeltaTrackRoute._ID) + " = " +
                        SqlHelper.TableDotProperty(DeltaTrackContract.DeltaTrackStopRoute.TABLE_NAME, DeltaTrackContract.DeltaTrackStopRoute.COLUMN_NAME_ROUTE_ID),    // The table to query
                projection,                                         // The columns to return
                selection,                                               // The columns for the WHERE clause
                selectionArgs,                                               // The values for the WHERE clause
                null,                                               // don't group the rows
                null,                                               // don't filter by row groups
                null                                                // The sort order
        );

        ArrayList<CtaRoutes> resultRoutes = new ArrayList<CtaRoutes>();

        String tempRouteName = null;
        if(c.getCount() > 0){
            c.moveToFirst();
            while(!c.isAfterLast()){
                tempRouteName = c.getString( c.getColumnIndex(SqlHelper.TableDotProperty(DeltaTrackContract.DeltaTrackRoute.TABLE_NAME, DeltaTrackContract.DeltaTrackRoute.COLUMN_NAME_ROUTE_NAME)));
                resultRoutes.add(getCtaRouteByName(tempRouteName));
                c.moveToNext();
            }
        }
        c.close();

        return resultRoutes;
    }



    private CtaRoutes getCtaRouteByName(String routeName){
        switch (routeName){
            case "Blue":
                return CtaRoutes.Blue;
            case "Brown":
                return CtaRoutes.Brown;
            case "Green":
                return CtaRoutes.Green;
            case "Orange":
                return CtaRoutes.Orange;
            case "Pink":
                return CtaRoutes.Pink;
            case "Purple":
                return CtaRoutes.Purple;
            case "Red":
                return CtaRoutes.Red;
            case "Yellow":
                return CtaRoutes.Yellow;
            default:
                return null;
        }
    }
}
