package com.toucha.analytics.model.request;

import java.util.Date;
import java.util.List;

import com.alibaba.fastjson.annotation.JSONField;
import com.toucha.analytics.common.model.BaseRequestNormal;
import com.toucha.analytics.common.model.ErrorInfo;
import com.toucha.analytics.common.util.AppEvents;

public class TimeRangeReportRequest extends BaseRequestNormal {

    private static final long serialVersionUID = 3862092974079791462L;

    @JSONField(name = "sd")
    private Date startDate;

    @JSONField(name = "ed")
    private Date endDate;

    @JSONField(name = "dids")
    List<Long> distributorIds;

    @JSONField(name = "oids")
    List<String> orderIds;

    @JSONField(name = "pris")
    List<Integer> promotionIds;
    
    protected TimeRangeReportRequest() {
        
    }
    
    protected TimeRangeReportRequest(Date startDate, Date endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }
    
    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public List<Long> getDistributorIds() {
        return distributorIds;
    }

    public void setDistributorIds(List<Long> distributorIds) {
        this.distributorIds = distributorIds;
    }

    public List<String> getOrderIds() {
        return orderIds;
    }

    public void setOrderIds(List<String> orderIds) {
        this.orderIds = orderIds;
    }

    public List<Integer> getPromotionIds() {
        return promotionIds;
    }

    public void setPromotionIds(List<Integer> promotionIds) {
        this.promotionIds = promotionIds;
    }

    @Override
    public void validateRequestCore(List<ErrorInfo> errors) {
        super.validateRequestCore(errors);

        if (this.getStartDate() == null) {
            errors.add(createErrorWithUserAndCompany(AppEvents.ScanReportServiceRequests.MissingStartDate));
        }

        if (this.getEndDate() == null) {
            errors.add(createErrorWithUserAndCompany(AppEvents.ScanReportServiceRequests.MissingEndDate));
        }

        if (this.getStartDate() != null && this.getEndDate() != null && this.getStartDate().compareTo(this.getEndDate()) > 0) {
            errors.add(createErrorWithUserAndCompany(AppEvents.ScanReportServiceRequests.ErrorDateRange));
        }

    }
}
