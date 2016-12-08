package com.toucha.analytics.web;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.toucha.analytics.common.azure.AzureOperation;
import com.toucha.analytics.common.common.GeneralConstants;
import com.toucha.analytics.common.dao.DataSet;
import com.toucha.analytics.common.environment.SystemInitialization;
import com.toucha.analytics.common.model.ErrorInfo;
import com.toucha.analytics.common.util.AppEvents;
import com.toucha.analytics.model.request.ShoplogCsvRequest;
import com.toucha.analytics.model.request.ShoplogRequest;
import com.toucha.analytics.model.response.ServiceReportResponse;
import com.toucha.analytics.service.ShoplogService;
import com.toucha.analytics.thread.ShoplogCsvWorker;
import com.toucha.analytics.utils.ControllerUtil;

@Controller
@RequestMapping("shop")
public class ShopController {

    @Resource(name = "shoplogService")
    private ShoplogService shoplogService;

    @RequestMapping("log/raw")
    public @ResponseBody ServiceReportResponse<DataSet> getRawActivities(HttpServletRequest request, HttpServletResponse response) {

        ServiceReportResponse<DataSet> serviceResponse = new ServiceReportResponse<DataSet>();

        try {
            JSONObject json = ControllerUtil.buffer(request);
            ShoplogRequest serviceRequest = JSON.parseObject(json.toJSONString(), ShoplogRequest.class);

            // Invalid JSON
            if (serviceRequest == null) {
                response.setStatus(GeneralConstants.RESPONSE_STATUS_400);
                serviceResponse.addError(AppEvents.JsonKeyMissingErr);
                return serviceResponse;
            }

            // Invalid request
            List<ErrorInfo> errors = serviceRequest.validateRequest();
            if (!errors.isEmpty()) {
                response.setStatus(GeneralConstants.RESPONSE_STATUS_400);
                serviceResponse.addError(errors);
                return serviceResponse;
            }
            DataSet data = shoplogService.getRawActivities(serviceRequest);
            serviceResponse.setReport(data);
        } catch (Exception ex) {
            ex.printStackTrace();
            response.setStatus(GeneralConstants.RESPONSE_STATUS_500);
            serviceResponse.addError(AppEvents.ServerExceptionErr);

            AppEvents.LogException(ex, AppEvents.ScanReportService.GeneralScanReportException);
        }

        return serviceResponse;
    }

    @RequestMapping("log/csv")
    public @ResponseBody ServiceReportResponse<String> getCsvActivities(HttpServletRequest request, HttpServletResponse response) {

        // url for the csv
        ServiceReportResponse<String> serviceResponse = new ServiceReportResponse<String>();

        try {
            JSONObject json = ControllerUtil.buffer(request);
            ShoplogCsvRequest serviceRequest = JSON.parseObject(json.toJSONString(), ShoplogCsvRequest.class);

            // Invalid JSON
            if (serviceRequest == null) {
                response.setStatus(GeneralConstants.RESPONSE_STATUS_400);
                serviceResponse.addError(AppEvents.JsonKeyMissingErr);
                return serviceResponse;
            }

            // Invalid request
            List<ErrorInfo> errors = serviceRequest.validateRequest();
            if (!errors.isEmpty()) {
                response.setStatus(GeneralConstants.RESPONSE_STATUS_400);
                serviceResponse.addError(errors);
                return serviceResponse;
            }

            String requestId = serviceRequest.getRequestHeader().getRequestId();
            DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
            String zipBlobName = dateFormat.format(Calendar.getInstance().getTime()) + "_" + requestId + ".zip";
            String pngBlobName = requestId + ".png";

            String zipBlobUrl = AzureOperation.getCsvDownloadBlobUrl(zipBlobName, serviceRequest.getEnvironment());
            String pngBlobUrl = AzureOperation.getCsvDownloadBlobUrl(pngBlobName, serviceRequest.getEnvironment());
            serviceResponse.setReport(zipBlobUrl + "," + pngBlobUrl);

            ShoplogCsvWorker worker = new ShoplogCsvWorker(zipBlobName, pngBlobName, serviceRequest, shoplogService);
            SystemInitialization.getReportCsvDownloadService().execute(worker);

        } catch (Exception ex) {
            response.setStatus(GeneralConstants.RESPONSE_STATUS_500);
            serviceResponse.addError(AppEvents.ServerExceptionErr);

            AppEvents.LogException(ex, AppEvents.ScanReportService.GeneralScanReportException);
        }

        return serviceResponse;
    }

}
