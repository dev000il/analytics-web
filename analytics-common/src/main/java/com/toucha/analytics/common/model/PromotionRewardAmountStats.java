/**
 * 
 */
package com.toucha.analytics.common.model;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.annotation.JSONField;

public class PromotionRewardAmountStats<T> {
    private List<PromotionRewardAmountStatsBase<T>> hourScan = new ArrayList<PromotionRewardAmountStatsBase<T>>();
    
    @JSONField(name="ids")
    private List<List<String>> ids;

    public List<PromotionRewardAmountStatsBase<T>> getHourScan() {
        return hourScan;
    }

    public void setHourScan(List<PromotionRewardAmountStatsBase<T>> hourScan) {
        this.hourScan = hourScan;
    }
    
    public List<List<String>> getIds() {
        return ids;
    }

    public void setIds(List<List<String>> ids) {
        this.ids = ids;
    }
}
