package com.toucha.analytics.common.model;

import com.alibaba.fastjson.annotation.JSONField;

public class EnterLotteryDailyStatsBase {
	
	@JSONField(name="y")
	private String yesterdayMeasure;
	@JSONField(name="t")
	private String todayMeasure;
	
	public String getYesterdayMeasure() {
		return yesterdayMeasure;
	}
	public void setYesterdayMeasure(String yesterdayMeasure) {
		this.yesterdayMeasure = yesterdayMeasure;
	}
	public String getTodayMeasure() {
		return todayMeasure;
	}
	public void setTodayMeasure(String todayMeasure) {
		this.todayMeasure = todayMeasure;
	}
}
