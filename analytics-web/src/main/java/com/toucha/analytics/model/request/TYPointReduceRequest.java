package com.toucha.analytics.model.request;

import java.util.List;

import com.alibaba.fastjson.annotation.JSONField;

public class TYPointReduceRequest extends TimeRangeReportRequest {

    private static final long serialVersionUID = -7290537703428533370L;

    @JSONField(name = "lids")
    private List<Integer> lotteryIds;

    public List<Integer> getLotteryIds() {
        return lotteryIds;
    }

    public void setLotteryIds(List<Integer> lotteryIds) {
        this.lotteryIds = lotteryIds;
    }
}
