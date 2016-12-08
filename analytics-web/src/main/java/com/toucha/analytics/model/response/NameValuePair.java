package com.toucha.analytics.model.response;

public class NameValuePair {
	String name;
	int value;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getValue() {
		return value;
	}
	public void setValue(int value) {
		this.value = value;
	}
	public NameValuePair() {
		
	}
	
	public NameValuePair(String name, int value) {
		
		this.name = name;
		this.value = value;
	}

}
