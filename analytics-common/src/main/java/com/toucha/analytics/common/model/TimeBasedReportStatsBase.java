package com.toucha.analytics.common.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * 
 * This is a general time based report statistics, d identifies the line legend in the report
 * @param <T>
 */
public class TimeBasedReportStatsBase<T> implements Serializable {

    private static final long serialVersionUID = -6924099786941828984L;

    @JSONField(name = "d")
	private String desc;

	@JSONField(name = "tc")
	private T totalMeasure;

	@JSONField(name = "ts")
	private List<Date> timeseries = new ArrayList<>();

	@JSONField(name = "counts")
	private List<T> measures = new ArrayList<T>();

	public TimeBasedReportStatsBase(String desc, T totalMeasure,
			List<Date> timeseries, List<T> measures) {
		super();
		this.desc = desc;
		this.totalMeasure = totalMeasure;
		this.timeseries = timeseries;
		this.measures = measures;
	}

	public TimeBasedReportStatsBase() {
        this.timeseries = new ArrayList<Date>();
        this.measures = new ArrayList<T>();
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public T getTotalMeasure() {
		return totalMeasure;
	}

	public void setTotalMeasure(T totalMeasure) {
		this.totalMeasure = totalMeasure;
	}

	public List<Date> getTimeseries() {
		return timeseries;
	}

	public void setTimeseries(List<Date> timeseries) {
		this.timeseries = timeseries;
	}

	public List<T> getMeasures() {
		return measures;
	}

	public void setMeasures(List<T> measures) {
		this.measures = measures;
	}

    @Override
    public String toString() {
        return "TimeBasedReportStatsBase [desc=" + desc + ", totalMeasure=" + totalMeasure + ", timeseries=" + timeseries
                + ", measures=" + measures + "]";
    }
}
