package com.holemcross.deltatrack.data;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by amortega on 8/25/2016.
 */
public class Station {
    public long stationId;
    public String stationName;
    public int mapId;
    public ArrayList<Stop> stops;

    public Station(){

    }

    public Station(ArrayList<Stop> newStops){
        if(newStops.size() >= 1){
            stops = newStops;
            stationName = stops.get(0).stationName;
            mapId = stops.get(0).mapId;
        }
    }

    public List<Integer> getStopIds(){

        ArrayList<Integer> resultList = new ArrayList<Integer>();
        for (Stop stop: stops
             ) {
            resultList.add(stop.stopId);
        }
        return resultList;
    }

    public List<CtaRoutes> getRoutes(){
        ArrayList<CtaRoutes> resultList = new ArrayList<CtaRoutes>();
        for (Stop stop: stops
                ) {
            resultList.addAll(stop.routes);
        }
        // Makes list distinct
        Set<CtaRoutes> set = new HashSet<CtaRoutes>(resultList);

        return new ArrayList<CtaRoutes>(set);
    }

    public Location getLocation(){
        return stops.get(0).location;
    }
}
