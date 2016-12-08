package com.toucha.analytics.common.model;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.toucha.analytics.common.common.GeneralConstants;
import com.toucha.analytics.common.util.AppEvents;

public class BaseRequestAdmin extends BaseRequest {

    /**
     * 
     */
    private static final long serialVersionUID = 7293191482551563074L;

    /**
     * 管理员接口
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

            // 验证CompanyId不为-1 admin的company必为-1
            if (requestHeader.getCompanyId() != GeneralConstants.NEGATIVE_ONE) {
                errors.add(AppEvents.ScanReportServiceRequests.NotAdminErr.toErrorInfo());
            }

            // 验证clientId不为空 admin的clientId必为Zhabei
            if (StringUtils.isBlank(requestHeader.getClientId()) || (!requestHeader.getClientId().equals("Zhabei"))) {
                errors.add(AppEvents.ScanReportServiceRequests.NotAdminErr.toErrorInfo());
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
