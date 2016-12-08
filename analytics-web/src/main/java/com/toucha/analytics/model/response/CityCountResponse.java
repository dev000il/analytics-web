package com.toucha.analytics.model.response;

import java.io.Serializable;

import com.alibaba.fastjson.annotation.JSONField;

public class CityCountResponse implements Serializable {

    private static final long serialVersionUID = 7396729426245940521L;

    @JSONField(name = "city")
    private String city;

    @JSONField(name = "scanTimes")
    private Integer scanTimes;

    @JSONField(name = "lotteryTimes")
    private Integer lotteryTimes;

    public CityCountResponse() {
    }

    public CityCountResponse(String city, Integer scanTimes, Integer lotteryTimes) {
        this.city = city;
        this.scanTimes = scanTimes;
        this.lotteryTimes = lotteryTimes;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
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

}
