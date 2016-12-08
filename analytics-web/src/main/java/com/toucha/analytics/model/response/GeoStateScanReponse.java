package com.toucha.analytics.model.response;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;

/**
 * Created by Senhui on 1/12/2016.
 */
public class GeoStateScanReponse {

    int statsNums;
    int cityNums;

    @JSONField(name = "states")
    List<GeoStatePair> geoStatePairs;

    public int getStatsNums() {
        return statsNums;
    }

    public void setStatsNums(int statsNums) {
        this.statsNums = statsNums;
    }

    public int getCityNums() {
        return cityNums;
    }

    public void setCityNums(int cityNums) {
        this.cityNums = cityNums;
    }

    public List<GeoStatePair> getGeoStatePairs() {
        return geoStatePairs;
    }

    public void setGeoStatePairs(List<GeoStatePair> geoStatePairs) {
        this.geoStatePairs = geoStatePairs;
    }
}
