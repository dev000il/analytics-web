package com.toucha.analytics.model.request;

import java.util.List;

import com.alibaba.fastjson.annotation.JSONField;

public class PromotionFilterRequest extends TimeRangeReportRequest {

    private static final long serialVersionUID = -7569788226447913685L;

    @JSONField(name = "pris")
    private List<Integer> promotionIds;

    @Override
    public List<Integer> getPromotionIds() {
        return promotionIds;
    }

    @Override
    public void setPromotionIds(List<Integer> promotionIds) {
        this.promotionIds = promotionIds;
    }
}
