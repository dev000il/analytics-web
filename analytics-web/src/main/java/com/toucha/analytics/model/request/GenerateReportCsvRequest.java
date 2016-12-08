package com.toucha.analytics.model.request;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.toucha.analytics.common.azure.AzureOperation;
import com.toucha.analytics.common.model.ErrorInfo;
import com.toucha.analytics.common.util.AppEvents;

public class GenerateReportCsvRequest extends PromoActivityReportRequest {

    private static final long serialVersionUID = 4876674818335737345L;

    @JSONField(name = "env")
    private String environment;
    
    public GenerateReportCsvRequest() {
        
    }

    public GenerateReportCsvRequest(String environment) {
        this.environment = environment;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    @Override
    public int getRequestedDAORecordsCount() {
        // When requesting a CSV, return "-1" to receive all.
        return this.getCountRequested() == null ? -1 : this.getCountRequested().intValue();
    }

    @Override
    public void validateRequestCore(List<ErrorInfo> errors) {
        super.validateRequestCore(errors);

        if (StringUtils.isBlank(this.environment)) {
            errors.add(AppEvents.ScanReportServiceRequests.MissingEnvironment.toErrorInfo());
        } else {
            Set<String> supportedEnvs = AzureOperation.getSupportedEnvironments();
            if (!supportedEnvs.contains(this.environment.toUpperCase())) {
                AppEvents.LogError(AppEvents.ScanReportServiceRequests.ErrorEnvironmentRequested,
                        this.environment, JSON.toJSONString(supportedEnvs));
                errors.add(AppEvents.ScanReportServiceRequests.ErrorEnvironment.toErrorInfo());
            }
        }
    }

}
