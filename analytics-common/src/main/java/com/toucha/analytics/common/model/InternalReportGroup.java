package com.toucha.analytics.common.model;

import java.io.Serializable;
import java.util.List;

public class InternalReportGroup implements Serializable {
    
    private static final long serialVersionUID = 2134881441062262837L;

    private String t;
    
    private String d;
    
    private List<InternalReportCard> g;

    public InternalReportGroup() {
    }

    public String getT() {
        return t;
    }

    public void setT(String t) {
        this.t = t;
    }

    public String getD() {
        return d;
    }

    public void setD(String d) {
        this.d = d;
    }

    public List<InternalReportCard> getG() {
        return g;
    }

    public void setG(List<InternalReportCard> g) {
        this.g = g;
    }

    @Override
    public String toString() {
        return "InternalReportGroup [t=" + t + ", d=" + d + ", g=" + g + "]";
    }
}
