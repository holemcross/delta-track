package com.holemcross.deltatrack.data;

import com.google.gson.JsonObject;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by amortega on 8/30/2016.
 */
public class Stop implements Serializable {
    public long id;
    public long stationId; // Foreign Key
    public String stationName;
    public String stopName;
    public String descriptiveName;
    public ArrayList<CtaRoutes> routes;
    public int mapId;
    public int stopId;
    public Location location;
    public Boolean isHandicapAccessible;

    public Stop(){

    }

    public Stop(JsonObject stopJson){
        stationName = stopJson.get("station_name").getAsString();
        stopName = stopJson.get("stop_name").getAsString();
        descriptiveName = stopJson.get("station_descriptive_name").getAsString();
        mapId = stopJson.get("map_id").getAsInt();
        stopId = stopJson.get("stop_id").getAsInt();
        isHandicapAccessible = stopJson.get("ada").getAsBoolean();

        // Location
        JsonObject locationJson = stopJson.get("location").getAsJsonObject();
        location = new Location(locationJson.get("latitude").getAsDouble(), locationJson.get("longitude").getAsDouble());

        // Routes
        Boolean green = stopJson.has("g") ? stopJson.get("g").getAsBoolean() : false;
        Boolean pink = stopJson.has("pnk") ? stopJson.get("pnk").getAsBoolean() : false;
        Boolean orange = stopJson.has("o") ? stopJson.get("o").getAsBoolean() : false;
        Boolean red = stopJson.has("red") ? stopJson.get("red").getAsBoolean(): false;
        Boolean purple = stopJson.has("p") ? stopJson.get("p").getAsBoolean() : false;
        Boolean blue = stopJson.has("blue") ? stopJson.get("blue").getAsBoolean() : false;
        Boolean brown = stopJson.has("brn") ? stopJson.get("brn").getAsBoolean() : false;
        Boolean yellow = stopJson.has("y") ? stopJson.get("y").getAsBoolean() : false;

        routes = new ArrayList<CtaRoutes>();
        if(green){
            routes.add(CtaRoutes.Green);
        }
        if(pink){
            routes.add(CtaRoutes.Pink);
        }
        if(orange){
            routes.add(CtaRoutes.Orange);
        }
        if(red){
            routes.add(CtaRoutes.Red);
        }
        if(purple){
            routes.add(CtaRoutes.Purple);
        }
        if(blue){
            routes.add(CtaRoutes.Blue);
        }if(brown){
            routes.add(CtaRoutes.Brown);
        }
        if(yellow){
            routes.add(CtaRoutes.Yellow);
        }

    }
}
