package com.holemcross.deltatrack.data;

import java.io.Serializable;

/**
 * Created by amortega on 8/30/2016.
 */
public class Location implements Serializable {

    public double latitude;
    public double longitude;

    public Location(){

    }

    public Location(double lat, double lng){
        latitude = lat;
        longitude = lng;
    }
}
