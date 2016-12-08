package com.toucha.analytics.model.request;

public class MgmeventRequest extends TimeRangeReportRequest {
    
    private static final long serialVersionUID = -38118317660619949L;
    
    int c;
    String u;

    public int getC() {
        return c;
    }

    public void setC(int c) {
        this.c = c;
    }

    public String getU() {
        return u;
    }

    public void setU(String u) {
        this.u = u;
    }

}
