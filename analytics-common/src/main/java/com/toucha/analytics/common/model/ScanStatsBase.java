package com.toucha.analytics.common.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.alibaba.fastjson.annotation.JSONField;

public class ScanStatsBase {

	@JSONField(name="tc")
	 int totalCount;
	
	@JSONField(name="ts")
	 List<Date> timeseries;

	 List<Integer> counts;
	
	public int getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}

	public List<Integer> getCounts() {
		return counts;
	}

	public void setCounts(List<Integer> counts) {
		this.counts = counts;
	}

	public List<Date> getTimeseries() {
		return timeseries;
	}

	public void setTimeseries(List<Date> timeseries) {
		this.timeseries = timeseries;
	}
	
	public ScanStatsBase(){
		this.totalCount = 0;
		this.timeseries = new ArrayList<Date>();
		this.counts = new ArrayList<Integer>();
	}
}
