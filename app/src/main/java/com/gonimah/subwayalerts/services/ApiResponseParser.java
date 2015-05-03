package com.gonimah.subwayalerts.services;

import com.gonimah.subwayalerts.models.TravelInformation;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ApiResponseParser {
    private static final String TRAIN_TIME_KEY = "train_time";
    private static final String SUBWAY_STATION_NAME_KEY = "subway_station_name";
    private static final String TRAIN_NAME_KEY = "train_name";
    private static final String SUBWAY_STATION_COMMUTE_TIME_MINS_KEY = "subway_station_commute_time_mins";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormat.forPattern("YYYY-MM-dd-HH-mm-ss");

    public List<TravelInformation> parse(JSONArray jsonArray) throws JSONException {
        List<TravelInformation> result = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            result.add(parseTravelInformation(jsonObject));
        }
        return result;
    }

    private TravelInformation parseTravelInformation(JSONObject jsonObject) throws JSONException {
        String trainTimeString = jsonObject.getString(TRAIN_TIME_KEY);
        DateTime trainTime = DATE_FORMAT.parseDateTime(trainTimeString);
        String subwayStationName = jsonObject.getString(SUBWAY_STATION_NAME_KEY);
        String trainName = jsonObject.getString(TRAIN_NAME_KEY);
        int subwayStationCommuteTimeMins = jsonObject.getInt(SUBWAY_STATION_COMMUTE_TIME_MINS_KEY);
        return new TravelInformation(trainTime, subwayStationName, trainName, subwayStationCommuteTimeMins);
    }
}
