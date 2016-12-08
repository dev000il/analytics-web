package com.toucha.analytics.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sun.jersey.core.util.Base64;
import com.toucha.analytics.common.common.ApplicationConfig;
import com.toucha.analytics.common.common.GeneralConstants;
import com.toucha.analytics.common.model.ErrorInfo;
import com.toucha.analytics.common.util.AuthServerOAuth2Util;
import com.toucha.analytics.model.response.ServiceReportResponse;

@Controller
@RequestMapping("/accesstoken")
public class AccessTokenController {

    @RequestMapping(value = "/accessToken")
    public @ResponseBody ServiceReportResponse<String> getAccessToken(HttpServletRequest request, HttpServletResponse response) {
        String accessToken = null;
        ServiceReportResponse<String> accessTokenResp = new ServiceReportResponse<String>();

        String auth = request.getHeader("Authorization");
        if (!authValidate(auth)) {
            response.setStatus(GeneralConstants.RESPONSE_STATUS_401);
            accessTokenResp.addError(new ErrorInfo("Auth fail.Please check the username or password."));
            return accessTokenResp;
        }

        try {
            accessToken = AuthServerOAuth2Util.getAccessToken();
            accessToken = "Bearer " + accessToken;
        } catch (Exception e) {
            System.out.println("get access token error,error info is :" + e.getMessage());
        }

        accessTokenResp.setReport(accessToken);

        return accessTokenResp;
    }

    private boolean authValidate(String auth) {
        if (StringUtils.isBlank(auth) || auth.length() <= 6) {
            return false;
        }

        auth = auth.substring(6, auth.length());
        String decodedAuth = Base64.base64Decode(auth);
        if (StringUtils.isBlank(decodedAuth)) {
            return false;
        }

        String userName = decodedAuth.substring(0, decodedAuth.indexOf(":"));
        String password = decodedAuth.substring(decodedAuth.indexOf(":") + 1);
        if (!ApplicationConfig.ACCESS_TOKEN_USERNAME.equals(userName)
                || !ApplicationConfig.ACCESS_TOKEN_PASSWORD.equals(password)) {
            //auth fail
            return false;
        }

        //auth success
        return true;
    }
}
