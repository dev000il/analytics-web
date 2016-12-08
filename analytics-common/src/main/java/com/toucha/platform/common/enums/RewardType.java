package com.toucha.platform.common.enums;

public enum RewardType {
	ClaimLater("-1"), PhoneBill("1"), AliPay("2"), UnionPay("4"), Point("6"), WeChat("7");
	
	String rewardType;
	
	RewardType(String rewardType) {
		this.rewardType = rewardType;
	}
	
	public String getValue() {
		return this.rewardType;
	}
}
