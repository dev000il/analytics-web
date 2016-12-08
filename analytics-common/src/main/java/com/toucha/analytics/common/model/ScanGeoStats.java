package com.toucha.analytics.common.model;

public class ScanGeoStats {
	
	private String city;
	private String state;
	private int count;

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}
	
	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public ScanGeoStats() {
    }

	public ScanGeoStats(String city, String state, int count) {
		this.city = city;
		this.state = state;
		this.count = count;
	}
}
