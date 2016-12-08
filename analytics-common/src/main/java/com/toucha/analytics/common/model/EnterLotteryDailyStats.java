package com.toucha.analytics.common.model;

import java.util.ArrayList;
import java.util.List;

public class EnterLotteryDailyStats {
	private List<EnterLotteryDailyStatsBase> dailyMeasures = new ArrayList<EnterLotteryDailyStatsBase>();
    
    public List<EnterLotteryDailyStatsBase> getDailyMeasures() {
		return dailyMeasures;
	}

	public void setDailyMeasures(List<EnterLotteryDailyStatsBase> dailyMeasures) {
		this.dailyMeasures = dailyMeasures;
	}
}
