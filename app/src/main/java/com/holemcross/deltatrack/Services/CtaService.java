package com.holemcross.deltatrack.services;

import com.holemcross.deltatrack.data.TrainArrival;
import com.holemcross.deltatrack.exceptions.CtaServiceException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.io.StringReader;

import Helpers.Constants;
import io.mikael.urlbuilder.UrlBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;


/**
 * Created by amortega on 8/25/2016.
 */
public class CtaService {
    private final String mBaseUri;

    public CtaService(){
        mBaseUri = Constants.CTA_API_BASE_URI;
    }

    public CtaService(String baseUri){
        mBaseUri = baseUri;
    }

    public ArrayList<TrainArrival> GetTrainArrivalsForMapId(Integer mapId, String apiKey) throws CtaServiceException {

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
            UrlBuilder builder = UrlBuilder.fromString(mBaseUri + ARRIVALS_ENDPOINT_URI)
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
        }catch(Exception ex){
            System.out.print(ex.getMessage());
        }

        System.out.print("Completed method with " + resultArray.size() +" arrivals.");
        return resultArray;
    }


}
