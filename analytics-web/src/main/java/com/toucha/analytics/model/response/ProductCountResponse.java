package com.toucha.analytics.model.response;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.annotation.JSONField;

public class ProductCountResponse implements Serializable {

    private static final long serialVersionUID = 5422290571572390520L;

    @JSONField(name = "product")
    private Integer product;

    @JSONField(name = "scanTimes")
    private Integer scanTimes;

    @JSONField(name = "lotteryTimes")
    private Integer lotteryTimes;

    @JSONField(name = "states")
    private List<StateCountResponse> states = new ArrayList<StateCountResponse>();

    public ProductCountResponse() {
    }

    public ProductCountResponse(Integer product, Integer scanTimes, Integer lotteryTimes) {
        this.product = product;
        this.scanTimes = scanTimes;
        this.lotteryTimes = lotteryTimes;
    }

    public Integer getProduct() {
        return product;
    }

    public void setProduct(Integer product) {
        this.product = product;
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

    public List<StateCountResponse> getStates() {
        return states;
    }

    public void setStates(List<StateCountResponse> states) {
        this.states = states;
    }

}
