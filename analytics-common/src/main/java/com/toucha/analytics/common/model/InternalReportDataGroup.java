package com.toucha.analytics.common.model;

import java.io.Serializable;
import java.util.List;

public class InternalReportDataGroup implements Serializable {

    private static final long serialVersionUID = -4471651824071902131L;

    private String t;
    
    private List<InternalReportData> d;

    public InternalReportDataGroup() {
    }

    public String getT() {
        return t;
    }

    public void setT(String t) {
        this.t = t;
    }

    public List<InternalReportData> getD() {
        return d;
    }

    public void setD(List<InternalReportData> d) {
        this.d = d;
    }

    @Override
    public String toString() {
        return "InternalReportDataGroup [t=" + t + ", d=" + d + "]";
    }
}
