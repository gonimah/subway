package com.gonimah.subwayalerts.models;

import org.joda.time.DateTime;

public class TravelInformation {
    private DateTime mTrainTime;
    private String mSubwayStationName;
    private String mTrainName;
    private int mSubwayStationCommuteTimeInMin;

    public TravelInformation(DateTime trainTime, String subwayStationName, String trainName,
                             int subwayStationCommuteTimeInMin) {
        mTrainTime = trainTime;
        mSubwayStationName = subwayStationName;
        mTrainName = trainName;
        mSubwayStationCommuteTimeInMin = subwayStationCommuteTimeInMin;
    }

    public DateTime getTrainTime() {
        return mTrainTime;
    }

    public String getSubwayStationName() {
        return mSubwayStationName;
    }

    public String getTrainName() {
        return mTrainName;
    }

    public int getSubwayStationCommuteTimeInMin() {
        return mSubwayStationCommuteTimeInMin;
    }

    @Override
    public String toString() {
       return String.format("TravelInformation: TrainTime=%s, SubwayStationName=%s, TrainName=%s, SubwayStationCommuteTimeMins=%d",
                mTrainTime.toString(), mSubwayStationName, mTrainTime, mSubwayStationCommuteTimeInMin);
    }
}
