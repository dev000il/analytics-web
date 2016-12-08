package com.toucha.analytics.model.response;

import java.util.List;

import com.alibaba.fastjson.annotation.JSONField;
import com.toucha.analytics.common.dao.DataSet;
import com.toucha.analytics.common.model.ErrorInfo;

public class PromoActivityReportResponse {

    @JSONField(name="report")
    private DataSet report;
    
    @JSONField(name="ers")
    private List<ErrorInfo> ers;
    
    public void setReport(DataSet report) {
        this.report = report;
    }
    
    public void setErrors(List<ErrorInfo> ers) {
        this.ers = ers;
    }
}
