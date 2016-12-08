package com.toucha.analytics.common.model;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * Daily stats report basic
 *
 * @author senhui.li
 */
public class DailyStatsBase {

    @JSONField(name="y")
    private long yesterdayMeasure;
    @JSONField(name="t")
    private long todayMeasure;

    public DailyStatsBase() {
    }

    public DailyStatsBase(long yesterdayMeasure, long todayMeasure) {
        this.yesterdayMeasure = yesterdayMeasure;
        this.todayMeasure = todayMeasure;
    }

    public long getTodayMeasure() {
        return todayMeasure;
    }

    public void setTodayMeasure(long todayMeasure) {
        this.todayMeasure = todayMeasure;
    }

    public long getYesterdayMeasure() {
        return yesterdayMeasure;
    }

    public void setYesterdayMeasure(long yesterdayMeasure) {
        this.yesterdayMeasure = yesterdayMeasure;
    }
}
