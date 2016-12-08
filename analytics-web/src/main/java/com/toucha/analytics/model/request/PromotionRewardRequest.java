package com.toucha.analytics.model.request;

import java.util.List;

import com.alibaba.fastjson.annotation.JSONField;
import com.toucha.analytics.common.model.ErrorInfo;
import com.toucha.analytics.common.util.AppEvents;

public class PromotionRewardRequest extends TimeRangeReportRequest{

    private static final long serialVersionUID = -2514105167337042610L;

    @JSONField(name = "pids")
    List<Integer> productIds;

    public List<Integer> getProductIds() {
        return productIds;
    }

    public void setProductIds(List<Integer> productIds) {
        this.productIds = productIds;
    }
	
	@Override
	public void validateRequestCore(List<ErrorInfo> errors) {
		super.validateRequestCore(errors);
		
		if (productIds == null || productIds.size() ==0 ) {
			errors.add(createErrorWithUserAndCompany(AppEvents.ScanReportServiceRequests.MissingProductId));
		}
	}
    
}
