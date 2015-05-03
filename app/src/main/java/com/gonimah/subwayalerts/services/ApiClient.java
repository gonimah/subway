package com.gonimah.subwayalerts.services;

import android.util.Log;

import com.gonimah.subwayalerts.models.TravelInformation;
import com.google.android.gms.maps.model.LatLng;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class ApiClient {
    private static final String TAG = ApiClient.class.getSimpleName();
    private static final String API_URL = "http://www.mocky.io/v2/554610d35c5bf08411ef8822?";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormat.forPattern("YYYY-MM-dd-HH-mm-ss");

    private final ApiResponseParser mApiResponseParser;

    public ApiClient() {
        mApiResponseParser = new ApiResponseParser();
    }

    public List<TravelInformation> getData(LatLng origin, LatLng destination, DateTime arrivalTime) throws Exception {
        StringBuilder urlString = new StringBuilder(API_URL);
        urlString.append("origin_lat=").append(origin.latitude);
        urlString.append("&origin_long=").append(origin.longitude);
        urlString.append("&dest_lat=").append(destination.latitude);
        urlString.append("&dest_long=").append(destination.longitude);
        urlString.append("&arrival_time=").append(arrivalTime.toString(DATE_FORMAT));

        URL url = new URL(urlString.toString());
        Log.d(TAG, "Sending 'GET' request to URL : " + urlString.toString());
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

        List<TravelInformation> response = null;
        try {
            InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
            String responseString = readStream(inputStream);
            JSONArray json = new JSONArray(responseString.toString());
            response = mApiResponseParser.parse(json);
        } finally {
            urlConnection.disconnect();
            return response;
        }
    }

    private String readStream(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String inputLine;
        StringBuffer response = new StringBuffer();
        while ((inputLine = reader.readLine()) != null) {
            response.append(inputLine);
        }
        reader.close();
        return response.toString();
    }
}
