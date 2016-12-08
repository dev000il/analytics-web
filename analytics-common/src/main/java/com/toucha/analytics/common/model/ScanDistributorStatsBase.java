package com.toucha.analytics.common.model;

import com.alibaba.fastjson.annotation.JSONField;

public class ScanDistributorStatsBase extends StatsBase{

	@JSONField(name="d")
	private int distributorId;
	

	public int getDistributorId() {
		return distributorId;
	}

	public void setDistributorId(int distributorId) {
		this.distributorId = distributorId;
	}

	public ScanDistributorStatsBase() {
		this.distributorId =-1;
	}
	
}
