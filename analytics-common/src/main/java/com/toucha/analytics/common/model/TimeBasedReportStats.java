package com.toucha.analytics.common.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * General return value for time based report, one TimeBsedReportStatsBase will be one line in client chart (i.e. one hour)
 */
public class TimeBasedReportStats<T> implements Serializable {
    
    private static final long serialVersionUID = 4693303990708818523L;

    private List<TimeBasedReportStatsBase<T>> hourScan = new ArrayList<TimeBasedReportStatsBase<T>>();

    // hour is the minimal measure in the report
    public List<TimeBasedReportStatsBase<T>> getHourScan() {
        return hourScan;
    }

    public void setHourScan(List<TimeBasedReportStatsBase<T>> hourScan) {
        this.hourScan = hourScan;
    }
}
