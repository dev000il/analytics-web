package com.toucha.analytics.model.request;

import java.util.List;

import com.alibaba.fastjson.annotation.JSONField;

public class PointRewardReportRequest extends TimeRangeReportRequest {

    private static final long serialVersionUID = 7623073296981009564L;

    @JSONField(name = "lids")
    private List<Integer> lotteryIds;

    @JSONField(name = "rids")
    private List<Integer> rewards;

    public List<Integer> getLotteryIds() {
        return lotteryIds;
    }

    public void setLotteryIds(List<Integer> lotteryIds) {
        this.lotteryIds = lotteryIds;
    }

    public List<Integer> getRewards() {
        return rewards;
    }

    public void setRewards(List<Integer> rewards) {
        this.rewards = rewards;
    }

}
