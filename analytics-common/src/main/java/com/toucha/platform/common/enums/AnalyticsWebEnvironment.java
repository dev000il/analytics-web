package com.toucha.platform.common.enums;

public enum AnalyticsWebEnvironment {
	TEST("TEST"), STABLE("STABLE"), PROD("PROD"), DEV("DEV");
	
	private String environment;

	private AnalyticsWebEnvironment(String environment) {
		this.environment = environment;
	}

	public String getEnvironment() {
		return environment;
	}

	public void setEnvironment(String environment) {
		this.environment = environment;
	}
	
}
