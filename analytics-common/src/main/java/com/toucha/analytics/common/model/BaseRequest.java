package com.toucha.analytics.common.model;

import java.io.Serializable;
import java.util.List;

import com.alibaba.fastjson.annotation.JSONField;
import com.google.common.collect.Lists;
import com.toucha.analytics.common.util.AppEvents;

public class BaseRequest implements Serializable {
    private static final long serialVersionUID = -8316591079686096581L;
    @JSONField(name = "header")
    private PlatformRequestHeader requestHeader;

    public PlatformRequestHeader getRequestHeader() {
        return requestHeader;
    }

    public void setRequestHeader(PlatformRequestHeader requestHeader) {
        this.requestHeader = requestHeader;
    }

    public List<ErrorInfo> validateRequest() {
        List<ErrorInfo> errors = Lists.newArrayList();
        this.validateRequestCore(errors);
        return errors;
    }
    
    public void validateRequestCore(List<ErrorInfo> errors) {
        
    }
    
    protected ErrorInfo createErrorWithUserAndCompany(AppEvents ae) {
        return ae.toErrorInfo(getRequestHeader().getUserId(), getRequestHeader().getCompanyId());
    }
}
