package com.holemcross.deltatrack;

/**
 * Created by amortega on 8/25/2016.
 */

import com.holemcross.deltatrack.data.Station;
import com.holemcross.deltatrack.data.TrainArrival;
import com.holemcross.deltatrack.exceptions.CtaServiceException;
import com.holemcross.deltatrack.services.CtaService;

import org.json.JSONException;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class CtaServiceUnitTests {

    @Test
    public void CtaService_GetArrivals_Valid(){

        CtaService service = new CtaService();
        ArrayList<TrainArrival> results = null;
        int mapId = 40730;
        String apiKey = "INSERT_KEY_HERE"; // Requires Valid Key to Function
        try {
            results = service.GetTrainArrivalsForMapId(mapId, apiKey);
        }catch(CtaServiceException ex){
            assertTrue(false);
        }catch(Exception ex){
            assertTrue(false);
        }
        System.out.print(results);

        assertTrue("Has content", !results.isEmpty());
    }

    @Test
    public void CtaService_GetStations_Valid(){

        CtaService service = new CtaService();
        ArrayList<Station> results = null;
        try {
            results = service.GetStations();
        }catch(CtaServiceException ex){
            assertTrue(false);
        }catch(Exception ex){
            assertTrue(false);
        }
        System.out.print(results);

        assertTrue("Has content", !results.isEmpty());
    }
}
