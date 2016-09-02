package com.holemcross.deltatrack.data;

import org.w3c.dom.Element;

import java.io.Serializable;
import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import helpers.Constants;

/**
 * Created by amortega on 8/25/2016.
 */
public class TrainArrival implements Serializable {

    public int mapId;
    public int stopId;
    public String stationName;
    public String stopDescription;
    public int runNumber;
    public int destinationStopId;
    public String destinationName;
    public CtaRoutes route;
    public int trainRouteDirection;
    public Date predictionTime;
    public Date arrivalTime;
    public boolean isApproaching;
    public boolean isNotLiveData;
    public boolean isDelayed;

    public TrainArrival(){

    }

    public TrainArrival(Element ctaEta) throws IllegalArgumentException {

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
        try{
            mapId = Integer.parseInt( ctaEta.getElementsByTagName("staId").item(0).getTextContent());
            stopId = Integer.parseInt( ctaEta.getElementsByTagName("stpId").item(0).getTextContent());
            runNumber = Integer.parseInt( ctaEta.getElementsByTagName("rn").item(0).getTextContent());
            trainRouteDirection = Integer.parseInt(ctaEta.getElementsByTagName("trDr").item(0).getTextContent());
            destinationStopId = Integer.parseInt( ctaEta.getElementsByTagName("destSt").item(0).getTextContent());
            stationName = ctaEta.getElementsByTagName("staNm").item(0).getTextContent();
            stopDescription = ctaEta.getElementsByTagName("stpDe").item(0).getTextContent();
            destinationName = ctaEta.getElementsByTagName("destNm").item(0).getTextContent();
            isApproaching = Boolean.parseBoolean( ctaEta.getElementsByTagName("isApp").item(0).getTextContent());
            isNotLiveData = Boolean.parseBoolean( ctaEta.getElementsByTagName("isSch").item(0).getTextContent());
            isDelayed = Boolean.parseBoolean( ctaEta.getElementsByTagName("isDly").item(0).getTextContent());
            predictionTime = dateFormat.parse(ctaEta.getElementsByTagName("prdt").item(0).getTextContent());
            arrivalTime = dateFormat.parse(ctaEta.getElementsByTagName("arrT").item(0).getTextContent());
            route = Constants.CTA_ROUTES_MAP.get(ctaEta.getElementsByTagName("rt").item(0).getTextContent());

        }catch(ParseException ex){
            IllegalArgumentException exception = new IllegalArgumentException("Failed to construct class with error: " + ex.getMessage());
            throw exception;
        }
    }

}
