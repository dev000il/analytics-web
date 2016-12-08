package com.toucha.analytics.model.request;

import java.util.List;

import com.alibaba.fastjson.annotation.JSONField;
import com.toucha.analytics.common.model.ErrorInfo;

public class PromotionRewardReportRequest extends TimeRangeReportRequest {
    private static final long serialVersionUID = 6603013288056385547L;

    @JSONField(name = "pids")
    List<Integer> productIds;
    
    @JSONField(name = "rids")
    List<Integer> rewardIds;

    String src;

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public List<Integer> getProductIds() {
        return productIds;
    }

    public void setRewardIds(List<Integer> rewardIds) {
        this.rewardIds = rewardIds;
    }
    
    public List<Integer> getRewardIds() {
        return rewardIds;
    }

    public void setProductIds(List<Integer> productIds) {
        this.productIds = productIds;
    }

    @Override
    public void validateRequestCore(List<ErrorInfo> errors) {
        super.validateRequestCore(errors);

        /*
         * if ( productIds == null ) {
         * errors.add(createErrorWithUserAndCompany(AppEvents
         * .ScanReportServiceRequests.MissingProductId)); }
         */
    }

}
