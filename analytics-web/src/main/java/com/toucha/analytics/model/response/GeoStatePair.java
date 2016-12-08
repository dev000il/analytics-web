package com.toucha.analytics.model.response;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Senhui on 1/12/2016.
 */
public class GeoStatePair {

    String stateName;

    List<NameValuePair> citites = new ArrayList<>();

    public String getStateName() {
        return stateName;
    }

    public void setStateName(String stateName) {
        this.stateName = stateName;
    }

    public List<NameValuePair> getCitites() {
        return citites;
    }

    public void setCitites(List<NameValuePair> citites) {
        this.citites = citites;
    }

    public void appendCity(NameValuePair city) {
        this.citites.add(city);
    }
}
