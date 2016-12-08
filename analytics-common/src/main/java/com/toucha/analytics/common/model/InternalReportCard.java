package com.toucha.analytics.common.model;

import java.io.Serializable;
import java.util.List;

public class InternalReportCard implements Serializable {

    private static final long serialVersionUID = 7420088207792033258L;

    private String t;
    
    private List<InternalReportDataGroup> c;

    public InternalReportCard() {
    }
    
    public InternalReportCard(String t, List<InternalReportDataGroup> c) {
        super();
        this.t = t;
        this.c = c;
    }



    public String getT() {
        return t;
    }

    public void setT(String t) {
        this.t = t;
    }

    public List<InternalReportDataGroup> getC() {
        return c;
    }

    public void setC(List<InternalReportDataGroup> c) {
        this.c = c;
    }

    @Override
    public String toString() {
        return "InternalReportConstitute [t=" + t + ", c=" + c + "]";
    }
}
