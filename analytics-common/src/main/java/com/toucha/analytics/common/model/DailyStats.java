package com.toucha.analytics.common.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Daily statics data
 *
 * @author senhui.li
 */
public class DailyStats {

    private List<DailyStatsBase> dailyMeasures = new ArrayList<>();

    public void setDailyMeasures(List<DailyStatsBase> dailyMeasures) {
        this.dailyMeasures = dailyMeasures;
    }

    public List<DailyStatsBase> getDailyMeasures() {
        return dailyMeasures;
    }
}
