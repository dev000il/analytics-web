package com.toucha.analytics.common.model;

import java.util.ArrayList;
import java.util.List;

public class ScanStats<T> {
	
	private List<T> hourScan = new ArrayList<T>();

	public List<T> getHourScan() {
		return hourScan;
	}

	public void setHourScan(List<T> hourScan) {
		this.hourScan = hourScan;
	}
	
}
