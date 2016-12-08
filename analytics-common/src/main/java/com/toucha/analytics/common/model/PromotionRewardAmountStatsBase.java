/**
 * 
 */
package com.toucha.analytics.common.model;

import java.util.Date;
import java.util.List;

import com.alibaba.fastjson.annotation.JSONField;

public class PromotionRewardAmountStatsBase<T> {
    
    @JSONField(name="d")
    private String rewardType;
    
    @JSONField(name="tc")
    private T totalMeasure;
    
    @JSONField(name="ts")
    private List<Date> timeseries;
    
    @JSONField(name="counts")
    private List<T> measures;
    
    public PromotionRewardAmountStatsBase(String rewardType, T totalMeasure, List<Date> timeseries, List<T> measures) {
        super();
        this.rewardType = rewardType;
        this.totalMeasure = totalMeasure;
        this.timeseries = timeseries;
        this.measures = measures;
    }
    
    public PromotionRewardAmountStatsBase() {
        
    }

    public String getRewardType() {
        return rewardType;
    }

    public void setRewardType(String rewardType) {
        this.rewardType = rewardType;
    }

    public T getTotalMeasure() {
        return totalMeasure;
    }

    public void setTotalMeasure(T totalMeasure) {
        this.totalMeasure = totalMeasure;
    }

    public List<Date> getTimeseries() {
        return timeseries;
    }

    public void setTimeseries(List<Date> timeseries) {
        this.timeseries = timeseries;
    }

    public List<T> getMeasures() {
        return measures;
    }

    public void setMeasures(List<T> measures) {
        this.measures = measures;
    }
}
