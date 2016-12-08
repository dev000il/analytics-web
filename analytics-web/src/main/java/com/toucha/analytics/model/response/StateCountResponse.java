package com.toucha.analytics.model.response;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.annotation.JSONField;

public class StateCountResponse implements Serializable {

    private static final long serialVersionUID = 5967984791665465378L;

    @JSONField(name = "state")
    private String state;

    @JSONField(name = "scanTimes")
    private Integer scanTimes;

    @JSONField(name = "lotteryTimes")
    private Integer lotteryTimes;

    @JSONField(name = "citys")
    private List<CityCountResponse> citys = new ArrayList<CityCountResponse>();

    public StateCountResponse() {
    }

    public StateCountResponse(String state, Integer scanTimes, Integer lotteryTimes) {
        this.state = state;
        this.scanTimes = scanTimes;
        this.lotteryTimes = lotteryTimes;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Integer getScanTimes() {
        return scanTimes;
    }

    public void setScanTimes(Integer scanTimes) {
        this.scanTimes = scanTimes;
    }

    public Integer getLotteryTimes() {
        return lotteryTimes;
    }

    public void setLotteryTimes(Integer lotteryTimes) {
        this.lotteryTimes = lotteryTimes;
    }

    public List<CityCountResponse> getCitys() {
        return citys;
    }

    public void setCitys(List<CityCountResponse> citys) {
        this.citys = citys;
    }

}
