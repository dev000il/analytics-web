/**
 * 
 */
package com.toucha.analytics.common.model;

import java.util.HashSet;
import java.util.Set;

public class PromotionHourlyData<T> {
    T hourlyMeasure;
    Set<String> userIds;
    
    public PromotionHourlyData() {
        userIds = new HashSet<String>();
    }
    
    public T getHourlyMeasure() {
        return hourlyMeasure;
    }

    public void setHourlyMeasure(T hourlyMeasure) {
        this.hourlyMeasure = hourlyMeasure;
    }

    public Set<String> getUserIds() {
        return userIds;
    }

    public void setUserIds(Set<String> userIds) {
        this.userIds = userIds;
    }
}
