package com.toucha.analytics.model.request;

import java.util.List;

import com.alibaba.fastjson.annotation.JSONField;

public class ScanReportRequest extends TimeRangeReportRequest {

    private static final long serialVersionUID = 760868896121383678L;

    @JSONField(name = "pids")
	List<Integer> productIds;

	public List<Integer> getProductIds() {
		return productIds;
	}

	public void setProductIds(List<Integer> productIds) {
		this.productIds = productIds;
	}

}
