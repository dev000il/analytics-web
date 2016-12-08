package com.toucha.platform.common.enums;

public enum DateTimeUnit {

	YEAR("YYYY"),MONTH("MM"),DAY("dd"),   HOUR("hh"), MINUTIE("mm"), SECOND("ss");
	String unit;

	DateTimeUnit(String unit) {
		this.unit = unit;
	}

	public String getValue() {
		return this.unit;
	}
}
