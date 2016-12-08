package com.toucha.analytics.common.model;

import java.util.Date;

public class Mgmevent {
	String u;
	String a;
	Date ts;
	String msg;
	String oip;
	
	
	public String getU() {
		return u;
	}
	public void setU(String u) {
		this.u = u;
	}
	public String getA() {
		return a;
	}
	public void setA(String a) {
		this.a = a;
	}
	public Date getTs() {
		return ts;
	}
	public void setTs(Date ts) {
		this.ts = ts;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	
	public String getOip() {
		return oip;
	}
	public void setOip(String oip) {
		this.oip = oip;
	}
	public Mgmevent() {
	}
	public Mgmevent(String u, String a, Date ts, String msg, String oip) {

		this.u = u;
		this.a = a;
		this.ts = ts;
		this.msg = msg;
		this.oip = oip;
	}
	
	
	
	
}
