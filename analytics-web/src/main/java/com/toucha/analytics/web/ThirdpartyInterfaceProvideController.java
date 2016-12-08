package com.toucha.analytics.web;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.Lists;
import com.sun.jersey.core.util.Base64;
import com.toucha.analytics.common.common.ApplicationConfig;
import com.toucha.analytics.common.common.GeneralConstants;
import com.toucha.analytics.common.exceptions.ServiceException;
import com.toucha.analytics.common.model.ErrorInfo;
import com.toucha.analytics.common.util.AppEvents;
import com.toucha.analytics.model.response.ProductCountResponse;
import com.toucha.analytics.model.response.ServiceReportResponse;
import com.toucha.analytics.shop.service.ShopMemberReportService;
import com.toucha.analytics.shop.service.ShopScanReportService;

@Controller
@RequestMapping("/thirdparty")
public class ThirdpartyInterfaceProvideController {

    @Autowired
    private ShopScanReportService shopScanReportService;

    @Autowired
    private ShopMemberReportService shopMemberReportService;

    @RequestMapping(value = "/shopScanReport/scanCount")
    public @ResponseBody ServiceReportResponse<List<ProductCountResponse>> getScanCount(Integer cid, String productIds,
            HttpServletRequest request, HttpServletResponse response) {
        long startTime = System.currentTimeMillis();

        ServiceReportResponse<List<ProductCountResponse>> productCountResp = new ServiceReportResponse<List<ProductCountResponse>>();
        String auth = request.getHeader("Authorization");
        if (!parameterValidate(cid, productIds, auth, productCountResp, response)) {
            return productCountResp;
        }

        System.out.println("cid is:" + cid + "; productIds is:" + productIds);

        List<ProductCountResponse> productCountList = Lists.newArrayList();
        try {
            productCountList = shopScanReportService.getScanCount(cid, productIds);
        } catch (ServiceException e) {
            response.setStatus(GeneralConstants.RESPONSE_STATUS_500);
            productCountResp.addError(AppEvents.ServerExceptionErr);
            return productCountResp;
        }

        productCountResp.setReport(productCountList);
        response.setStatus(GeneralConstants.RESPONSE_STATUS_200);
        System.out.println("ScanCount total spend time is : " + (System.currentTimeMillis() - startTime) + " ms");

        return productCountResp;
    }

    @RequestMapping(value = "/shopMemberReport/lotteryCount")
    public @ResponseBody ServiceReportResponse<List<ProductCountResponse>> getLotteryCount(Integer cid, String productIds,
            HttpServletRequest request, HttpServletResponse response) {
        long startTime = System.currentTimeMillis();
        ServiceReportResponse<List<ProductCountResponse>> productCountResp = new ServiceReportResponse<List<ProductCountResponse>>();

        String auth = request.getHeader("Authorization");
        if (!parameterValidate(cid, productIds, auth, productCountResp, response)) {
            return productCountResp;
        }

        System.out.println("cid is:" + cid + "; productIds is:" + productIds);

        List<ProductCountResponse> productCountList = Lists.newArrayList();
        try {
            productCountList = shopMemberReportService.getLotteryCount(cid, productIds);
        } catch (ServiceException e) {
            response.setStatus(GeneralConstants.RESPONSE_STATUS_500);
            productCountResp.addError(AppEvents.ServerExceptionErr);
            return productCountResp;
        }

        productCountResp.setReport(productCountList);
        response.setStatus(GeneralConstants.RESPONSE_STATUS_200);

        System.out.println("LotteryCount total spend time is : " + (System.currentTimeMillis() - startTime) + " ms");

        return productCountResp;
    }

    private boolean parameterValidate(Integer cid, String productIds, String auth,
            ServiceReportResponse<List<ProductCountResponse>> productCountResp, HttpServletResponse response) {
        if (cid == null) {
            response.setStatus(GeneralConstants.RESPONSE_STATUS_500);
            productCountResp.addError(new ErrorInfo("The must parameter cid is null,it can not be null."));
            return false;
        }

        //if productids not null,and starts with , or end with , or besides number or comma,have other symbol
        if (StringUtils.isNotBlank(productIds)
                && (productIds.startsWith(",") || productIds.endsWith(",") || !productIds.matches("^[\\d,]*$"))) {
            response.setStatus(GeneralConstants.RESPONSE_STATUS_500);
            productCountResp.addError(new ErrorInfo("The parameter of productIds style is wrong,please check it."));
            return false;
        }

        if (!authValidate(auth)) {
            response.setStatus(GeneralConstants.RESPONSE_STATUS_401);
            productCountResp.addError(new ErrorInfo("Auth fail.Please check the username or password."));
            return false;
        }

        return true;
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
        if (!ApplicationConfig.CENTBONUSERNAME.equals(userName) || !ApplicationConfig.CENTBONPASSWORD.equals(password)) {
            //auth fail
            return false;
        }

        //auth success
        return true;
    }

}
