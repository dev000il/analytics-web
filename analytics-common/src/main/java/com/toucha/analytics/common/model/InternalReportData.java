package com.toucha.analytics.common.model;

import java.io.Serializable;
import java.math.BigDecimal;


public class InternalReportData implements Serializable {

    private static final long serialVersionUID = -1875892797476337655L;

    private String t;
    
    private BigDecimal c;

    public InternalReportData() {
    }

    public InternalReportData(String t, BigDecimal c) {
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

    public BigDecimal getC() {
        return c;
    }

    public void setC(BigDecimal c) {
        this.c = c;
    }

    @Override
    public String toString() {
        return "InternalReportData [t=" + t + ", c=" + c + "]";
    }
}
