package com.toucha.analytics.common.model;

import java.util.Date;

public class TimeBasedReportStatUnit<T> {
	private Date hour;
	private T measure;
	private String desc;
	
	public TimeBasedReportStatUnit(Date hour, T measure, String desc) {
		super();
		this.hour = hour;
		this.measure = measure;
		this.desc = desc;
	}
	
	public TimeBasedReportStatUnit(Date hour, T measure) {
		super();
		this.hour = hour;
		this.measure = measure;
	}

	public Date getHour() {
		return hour;
	}

	public void setHour(Date hour) {
		this.hour = hour;
	}

	public T getMeasure() {
		return measure;
	}

	public void setMeasure(T measure) {
		this.measure = measure;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}
	
}
