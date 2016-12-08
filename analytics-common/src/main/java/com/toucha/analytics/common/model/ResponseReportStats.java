package com.toucha.analytics.common.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * The standardized model for statistics response
 * 
 * @author senhui.li
 */
public class ResponseReportStats<T> implements Serializable {

    private static final long serialVersionUID = -6020650853830584511L;

    private List<TimeBasedReportStatsBase<T>> hourlyStats = new ArrayList<>();

    public List<TimeBasedReportStatsBase<T>> getHourlyStats() {
        return hourlyStats;
    }

    public void add(TimeBasedReportStatsBase<T> stat) {
        this.hourlyStats.add(stat);
    }
}
