package com.toucha.analytics.common.model;

public class ScanLotteryCount {

    private Integer product;

    private String state;

    private String city;

    private Integer totalCount;

    public ScanLotteryCount() {
    }

    public ScanLotteryCount(Integer product, String state, String city, Integer totalCount) {
        this.product = product;
        this.state = state;
        this.city = city;
        this.totalCount = totalCount;
    }

    public Integer getProduct() {
        return product;
    }

    public void setProduct(Integer product) {
        this.product = product;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }
}
