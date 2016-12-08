package com.toucha.analytics.model.request;

import java.util.List;

import com.alibaba.fastjson.annotation.JSONField;

public class MobileDataReportRequest extends TimeRangeReportRequest {

    private static final long serialVersionUID = 6937676773538152642L;

    @JSONField(name = "prn")
    private List<String> providerName;

    public List<String> getProviderName() {
        return providerName;
    }

    public void setProviderName(List<String> providerName) {
        this.providerName = providerName;
    }

}
