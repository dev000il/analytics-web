package com.toucha.analytics.common.model;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.toucha.analytics.common.util.AppEvents;

/**
 * 
 */
public class BaseRequestUser extends BaseRequest {

    /**
     * 
     */
    private static final long serialVersionUID = 5359835621015635425L;

    /**
     * 用户接口
     */
    @Override
    public void validateRequestCore(List<ErrorInfo> errors) {
        super.validateRequestCore(errors);
        
        PlatformRequestHeader requestHeader = this.getRequestHeader();
        if (requestHeader != null) {

            // 验证userId是否存在
            if (StringUtils.isBlank(requestHeader.getUserId())) {
                errors.add(AppEvents.UserIdBlankErr.toErrorInfo());
            }

            // 验证CompanyId不为空
            if (requestHeader.getCompanyId() < 1) {
                errors.add(AppEvents.ScanReportServiceRequests.MissingCompanyId.toErrorInfo());
            }

            // 验证clientId不为空
            if (StringUtils.isBlank(requestHeader.getClientId())) {
                errors.add(AppEvents.ScanReportServiceRequests.ClientIdBlankErr.toErrorInfo());
            }

            // 验证requestId不为空
            if (StringUtils.isBlank(requestHeader.getRequestId())) {
                errors.add(AppEvents.RequestIdBlankErr.toErrorInfo());
            }

            // 验证userIp不为空
            if (StringUtils.isBlank(requestHeader.getUserIp())) {
                errors.add(AppEvents.UserIpBlankErr.toErrorInfo());
            }
        } else {
            errors.add(AppEvents.PlatformRequestHeaderErr.toErrorInfo());
        }
    }
}
