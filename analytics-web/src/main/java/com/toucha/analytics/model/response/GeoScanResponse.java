package com.toucha.analytics.model.response;

import java.util.List;

public class GeoScanResponse {
	List<NameValuePair> cityStats;
	List<NameValuePair> stateStats;
	int cityEmpty;
	int stateEmpty;
	
	public List<NameValuePair> getCityStats() {
		return cityStats;
	}
	public void setCityStats(List<NameValuePair> cityStats) {
		this.cityStats = cityStats;
	}
	public List<NameValuePair> getStateStats() {
		return stateStats;
	}
	public void setStateStats(List<NameValuePair> stateStats) {
		this.stateStats = stateStats;
	}
	public int getCityEmpty() {
		return cityEmpty;
	}
	public void setCityEmpty(int cityEmpty) {
		this.cityEmpty = cityEmpty;
	}
	public int getStateEmpty() {
		return stateEmpty;
	}
	public void setStateEmpty(int stateEmpty) {
		this.stateEmpty = stateEmpty;
	}

}
