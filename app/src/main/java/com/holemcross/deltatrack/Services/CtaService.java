package com.holemcross.deltatrack.services;

import com.holemcross.deltatrack.data.Station;
import com.holemcross.deltatrack.data.Stop;
import com.holemcross.deltatrack.data.TrainArrival;
import com.holemcross.deltatrack.exceptions.CtaServiceException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.io.StringReader;

import helpers.Constants;
import io.mikael.urlbuilder.UrlBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import com.google.gson.*;


/**
 * Created by amortega on 8/25/2016.
 */
public class CtaService {
    private final String mCtaApiBaseUri;
    private final String mChicagoPortalBaseUri;

    public CtaService(){
        mCtaApiBaseUri = Constants.CTA_API_BASE_URI;
        mChicagoPortalBaseUri = Constants.CHICAGO_PORTAL_BASE_URI;
    }

    public CtaService(String ctaBaseUri, String chicagoPortalBaseUri){
        mCtaApiBaseUri = ctaBaseUri;
        mChicagoPortalBaseUri = chicagoPortalBaseUri;
    }

    public ArrayList<TrainArrival> GetTrainArrivalsForMapId(Integer mapId, String apiKey)
            throws CtaServiceException, SAXException, XPathExpressionException, ParserConfigurationException, ParseException, IOException {

        final String ARRIVALS_ENDPOINT_URI = "/1.0/ttarrivals.aspx";

        // Request Params
        final String MAPID_PARAM = "mapid";
        final String STOPID_PARAM = "stpid";
        final String MAX_PARAM = "max";
        final String ROUTE_PARAM = "rt";
        final String APIKEY_PARAM = "key";

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String arrivalsXml = null;

        try {
            // Create Url
            UrlBuilder builder = UrlBuilder.fromString(mCtaApiBaseUri + ARRIVALS_ENDPOINT_URI)
                    .addParameter(MAPID_PARAM, mapId.toString())
                    .addParameter(APIKEY_PARAM, apiKey);

            URL url = new URL(builder.toString());

            // Run API Call
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Get Buffer
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                throw new CtaServiceException("Did not receive expected data from CTA API");
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }

            // Store XML
            arrivalsXml = buffer.toString();
        }
        catch(IOException ex){
            throw new CtaServiceException("Failed to connect to API with message: " + ex.getMessage());
        }finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    System.out.print("Error closing stream");
                }
            }
        }

        if(arrivalsXml == null || arrivalsXml.length() <= 0){
            CtaServiceException exception = new CtaServiceException("CTA API Returned no data");
            System.out.print(exception.getMessage());
            throw exception;
        }

        ArrayList<TrainArrival> resultArray = new ArrayList<TrainArrival>();

        try{
            // Create Document Builder to Parse XML
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = factory.newDocumentBuilder();
            InputSource source = new InputSource( new StringReader( arrivalsXml));
            Document xmlDoc = documentBuilder.parse(source);
            XPath xPath = XPathFactory.newInstance().newXPath();

            final String errorCodeExpression = "/ctatt/errCd";
            final String etasExpression = "/ctatt/eta";

            Double errorCode = (Double) xPath.compile(errorCodeExpression).evaluate(xmlDoc, XPathConstants.NUMBER);

            if(errorCode != 0){
                CtaServiceException exception = new CtaServiceException("CTA API Returned Error Code: "+ errorCode);
                System.out.print(exception.getMessage());
                throw exception;
            }

            NodeList etas = (NodeList) xPath.compile(etasExpression).evaluate(xmlDoc, XPathConstants.NODESET);

            for (int i=0; i < etas.getLength();i++){
                Node arrivalNode = etas.item(i);
                resultArray.add(new TrainArrival((Element)arrivalNode));
            }
        }
        catch (IllegalArgumentException ex){
            System.out.print(ex.getMessage());
            throw ex;
        }catch (SAXException ex) {
            System.out.print(ex.getMessage());
            throw ex;
        }catch (XPathExpressionException ex) {
            System.out.print(ex.getMessage());
            throw ex;
        }catch (ParserConfigurationException ex){
            System.out.print(ex.getMessage());
            throw ex;
        }catch (IOException ex){
            System.out.print(ex.getMessage());
            throw ex;
        }

        System.out.print("Completed method with " + resultArray.size() +" arrivals.");
        return resultArray;
    }

    public ArrayList<Station> GetStations() throws CtaServiceException {
        final String ARRIVALS_ENDPOINT_URI = "/resource/8pix-ypme.json";

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String stationsJson = null;

        try {
            // Create Url
            UrlBuilder builder = UrlBuilder.fromString(mChicagoPortalBaseUri + ARRIVALS_ENDPOINT_URI);
            URL url = new URL(builder.toString());

            // Run API Call
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Get Buffer
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                throw new CtaServiceException("Did not receive expected data from CTA API");
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }

            stationsJson = buffer.toString();
        }
        catch(IOException ex){
            throw new CtaServiceException("Failed to connect to API with message: " + ex.getMessage());
        }finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    System.out.print("Error closing stream");
                }
            }
        }

        if(stationsJson == null || stationsJson.length() <= 0){
            CtaServiceException exception = new CtaServiceException("Chicago Portal API Returned no data");
            System.out.print(exception.getMessage());
            throw exception;
        }

        ArrayList<Stop> stopList = new ArrayList<Stop>();

        try{
            JsonParser parser = new JsonParser();
            JsonArray stations = parser.parse(stationsJson).getAsJsonArray();

            for (JsonElement station: stations
                 ) {
                stopList.add( new Stop(station.getAsJsonObject()));
            }
        }catch(JsonParseException ex){
            System.out.print(ex.getMessage());
        }

        // Get Unique Map Ids
        ArrayList<Integer> uniqueMapIds = new ArrayList<Integer>();
        for (Stop stop:stopList
                ) {
            if(!uniqueMapIds.contains(stop.mapId)){
                uniqueMapIds.add(stop.mapId);
            }
        }

        // Group Stops by Map Ids
        ArrayList<Station> resultList = new ArrayList<Station>();
        Station tempStation = null;
        ArrayList<Stop> tempStopList = null;

        for (Integer mapId: uniqueMapIds
                ) {
            tempStopList = new ArrayList<Stop>();
            for (Stop stop: stopList
                 ) {
                if(stop.mapId == mapId.intValue()){
                    // Add to temp list and pop
                    tempStopList.add(stop);
                    //stopList.remove(stop);
                }
            }
            resultList.add( new Station(tempStopList));
        }
        return resultList;
    }


}
