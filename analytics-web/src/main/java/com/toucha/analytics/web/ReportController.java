package com.toucha.analytics.web;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.toucha.analytics.common.azure.AzureOperation;
import com.toucha.analytics.common.common.GeneralConstants;
import com.toucha.analytics.common.dao.DataSet;
import com.toucha.analytics.common.dao.DataSetWithKey;
import com.toucha.analytics.common.environment.SystemInitialization;
import com.toucha.analytics.common.exceptions.ServiceException;
import com.toucha.analytics.common.model.BaseRequestNormal;
import com.toucha.analytics.common.model.DailyStatsBase;
import com.toucha.analytics.common.model.EnterLotteryDailyStats;
import com.toucha.analytics.common.model.ErrorInfo;
import com.toucha.analytics.common.model.InternalReportGroup;
import com.toucha.analytics.common.model.Mgmevent;
import com.toucha.analytics.common.model.PromotionRewardAmountStats;
import com.toucha.analytics.common.model.ScanStats;
import com.toucha.analytics.common.model.TimeBasedReportStats;
import com.toucha.analytics.common.model.TimeBasedReportStatsBase;
import com.toucha.analytics.common.util.AppEvents;
import com.toucha.analytics.model.request.ActRawRequest;
import com.toucha.analytics.model.request.GenerateReportCsvRequest;
import com.toucha.analytics.model.request.InternalReportsRequest;
import com.toucha.analytics.model.request.MgmeventRequest;
import com.toucha.analytics.model.request.PointsSumReportRequest;
import com.toucha.analytics.model.request.PromoActivityReportRequest;
import com.toucha.analytics.model.request.PromotionFilterRequest;
import com.toucha.analytics.model.request.PromotionRewardReportRequest;
import com.toucha.analytics.model.request.PromotionRewardRequest;
import com.toucha.analytics.model.request.ScanReportRequest;
import com.toucha.analytics.model.request.TYPointReduceRequest;
import com.toucha.analytics.model.request.TimeRangeReportRequest;
import com.toucha.analytics.model.request.TrendStatsRequest;
import com.toucha.analytics.model.response.GeoScanResponse;
import com.toucha.analytics.model.response.GeoStateScanReponse;
import com.toucha.analytics.model.response.ServiceReportResponse;
import com.toucha.analytics.service.DistributorReportService;
import com.toucha.analytics.service.GeoReportService;
import com.toucha.analytics.service.InternalReportsService;
import com.toucha.analytics.service.MemberReportService;
import com.toucha.analytics.service.ScanReportService;
import com.toucha.analytics.service.TransactionReportService;
import com.toucha.analytics.service.TrendStatsService;
import com.toucha.analytics.thread.RewardActivityCsvDownloadWorker;
import com.toucha.analytics.utils.ControllerUtil;
import com.toucha.platform.common.enums.DateTimeUnit;

@Controller
@RequestMapping("report")
public class ReportController {

    @Resource(name = "scanReportService")
    private ScanReportService reportService;

    @Resource(name = "distributorReportService")
    private DistributorReportService distriibutorService;

    @Resource(name = "memberReportService")
    private MemberReportService memberReportService;

    @Autowired
    private InternalReportsService internalReportsService;

    @Autowired
    private TransactionReportService tranReportService;

    @Autowired
    private GeoReportService geoReportService;

    @Autowired
    private TrendStatsService trendStatsService;

    /**
     * This controller accepts date range scan request and returns scan
     * statistics in day, week, month
     * 
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("dateRangeScan")
    public @ResponseBody ServiceReportResponse<TimeBasedReportStats<Integer>> getDateRangeScanReport(HttpServletRequest request,
            HttpServletResponse response) {
        ServiceReportResponse<TimeBasedReportStats<Integer>> scanReportResponse = new ServiceReportResponse<TimeBasedReportStats<Integer>>();

        try {
            JSONObject json = ControllerUtil.buffer(request);
            ScanReportRequest scanReportRequest = JSON.parseObject(json.toJSONString(), ScanReportRequest.class);
            System.out.println(JSON.toJSONString(scanReportRequest));
            if (scanReportRequest != null) {
                List<ErrorInfo> errors = scanReportRequest.validateRequest();
                if (errors.isEmpty()) {
                    TimeBasedReportStats<Integer> stats = new TimeBasedReportStats<Integer>();
                    stats.setHourScan(reportService.getScanStatistics(scanReportRequest.getRequestHeader().getCompanyId(),
                            scanReportRequest.getProductIds(), scanReportRequest.getStartDate(), scanReportRequest.getEndDate()));
                    scanReportResponse.setReport(stats);
                } else {
                    response.setStatus(GeneralConstants.RESPONSE_STATUS_400);
                    scanReportResponse.addError(AppEvents.JsonKeyMissingErr);
                }
            } else {
                response.setStatus(GeneralConstants.RESPONSE_STATUS_400);
                scanReportResponse.addError(AppEvents.JsonKeyMissingErr);
            }
        } catch (ServiceException ex) {
            response.setStatus(GeneralConstants.RESPONSE_STATUS_500);
            scanReportResponse.addError(new ErrorInfo(ex.getErrorCode(), ex.getErrorMsg()));
        } catch (Exception ex) {
            AppEvents.LogException(ex, AppEvents.ScanReportService.GeneralScanReportException);
            response.setStatus(GeneralConstants.RESPONSE_STATUS_500);
            scanReportResponse.addError(AppEvents.ServerExceptionErr);
        }

        return scanReportResponse;
    }

    @RequestMapping("geoScan")
    public @ResponseBody ServiceReportResponse<GeoScanResponse> getGeoScanReport(HttpServletRequest request,
            HttpServletResponse response) {
        ServiceReportResponse<GeoScanResponse> scanReportResponse = new ServiceReportResponse<GeoScanResponse>();

        try {
            JSONObject json = ControllerUtil.buffer(request);
            ScanReportRequest scanReportRequest = JSON.parseObject(json.toJSONString(), ScanReportRequest.class);

            if (scanReportRequest != null) {
                List<ErrorInfo> errors = scanReportRequest.validateRequest();
                if (errors.isEmpty()) {
                    GeoScanResponse result = reportService.getGeoReport(scanReportRequest.getRequestHeader().getCompanyId(),
                            scanReportRequest.getProductIds(), scanReportRequest.getStartDate(), scanReportRequest.getEndDate());
                    scanReportResponse.setReport(result);
                } else {
                    response.setStatus(GeneralConstants.RESPONSE_STATUS_400);
                    scanReportResponse.addError(AppEvents.JsonKeyMissingErr);
                }
            } else {
                response.setStatus(GeneralConstants.RESPONSE_STATUS_400);
                scanReportResponse.addError(AppEvents.JsonKeyMissingErr);
            }
        } catch (ServiceException ex) {
            response.setStatus(GeneralConstants.RESPONSE_STATUS_500);
            scanReportResponse.addError(new ErrorInfo(ex.getErrorCode(), ex.getErrorMsg()));
        } catch (Exception ex) {
            AppEvents.LogException(ex, AppEvents.ScanReportService.GeneralScanReportException);
            response.setStatus(GeneralConstants.RESPONSE_STATUS_500);
            scanReportResponse.addError(AppEvents.ServerExceptionErr);
        }

        return scanReportResponse;
    }

    @RequestMapping("geoScan2")
    public @ResponseBody ServiceReportResponse<GeoStateScanReponse> getGeoScanReport2(HttpServletRequest request,
            HttpServletResponse response) {
        ServiceReportResponse<GeoStateScanReponse> scanReportResponse = new ServiceReportResponse<>();

        try {
            JSONObject json = ControllerUtil.buffer(request);
            ScanReportRequest scanReportRequest = JSON.parseObject(json.toJSONString(), ScanReportRequest.class);

            if (scanReportRequest != null) {
                List<ErrorInfo> errors = scanReportRequest.validateRequest();
                if (errors.isEmpty()) {
                    GeoStateScanReponse result = reportService.getGeoReportNew(
                            scanReportRequest.getRequestHeader().getCompanyId(), scanReportRequest.getProductIds(),
                            scanReportRequest.getStartDate(), scanReportRequest.getEndDate());
                    scanReportResponse.setReport(result);
                } else {
                    response.setStatus(GeneralConstants.RESPONSE_STATUS_400);
                    scanReportResponse.addError(AppEvents.JsonKeyMissingErr);
                }
            } else {
                response.setStatus(GeneralConstants.RESPONSE_STATUS_400);
                scanReportResponse.addError(AppEvents.JsonKeyMissingErr);
            }
        } catch (ServiceException ex) {
            response.setStatus(GeneralConstants.RESPONSE_STATUS_500);
            scanReportResponse.addError(new ErrorInfo(ex.getErrorCode(), ex.getErrorMsg()));
        } catch (Exception ex) {
            AppEvents.LogException(ex, AppEvents.ScanReportService.GeneralScanReportException);
            response.setStatus(GeneralConstants.RESPONSE_STATUS_500);
            scanReportResponse.addError(AppEvents.ServerExceptionErr);
        }

        return scanReportResponse;
    }

    @RequestMapping("activities/raw")
    public @ResponseBody ServiceReportResponse<DataSet> getRawActivities(HttpServletRequest request,
            HttpServletResponse response) {

        ServiceReportResponse<DataSet> serviceResponse = new ServiceReportResponse<DataSet>();

        try {
            JSONObject json = ControllerUtil.buffer(request);
            PromoActivityReportRequest serviceRequest = JSON.parseObject(json.toJSONString(), PromoActivityReportRequest.class);

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
            DataSet data = reportService.getRawActivities(serviceRequest);
            serviceResponse.setReport(data);
        } catch (ServiceException ex) {
            response.setStatus(GeneralConstants.RESPONSE_STATUS_500);
            serviceResponse.addError(new ErrorInfo(ex.getErrorCode(), ex.getErrorMsg()));
        } catch (Exception ex) {
            response.setStatus(GeneralConstants.RESPONSE_STATUS_500);
            serviceResponse.addError(AppEvents.ServerExceptionErr);

            AppEvents.LogException(ex, AppEvents.ScanReportService.GeneralScanReportException);
        }

        return serviceResponse;
    }

    @RequestMapping("act/raw")
    public @ResponseBody ServiceReportResponse<DataSetWithKey> getRawAct(HttpServletRequest request,
            HttpServletResponse response) {
        ServiceReportResponse<DataSetWithKey> serviceResponse = new ServiceReportResponse<DataSetWithKey>();
        try {
            JSONObject json = ControllerUtil.buffer(request);
            ActRawRequest serviceRequest = JSON.parseObject(json.toJSONString(), ActRawRequest.class);
            serviceRequest.convertStartDate();
            System.out.println(serviceRequest);
            List<ErrorInfo> errors = serviceRequest.validateRequest();
            if (!errors.isEmpty()) {
                response.setStatus(GeneralConstants.RESPONSE_STATUS_400);
                serviceResponse.addError(errors);
                return serviceResponse;
            }
            DataSetWithKey data = reportService.getRawAct(serviceRequest);
            serviceResponse.setReport(data);
        } catch (ServiceException ex) {
            response.setStatus(GeneralConstants.RESPONSE_STATUS_500);
            serviceResponse.addError(new ErrorInfo(ex.getErrorCode(), ex.getErrorMsg()));
        } catch (Exception ex) {
            response.setStatus(GeneralConstants.RESPONSE_STATUS_500);
            serviceResponse.addError(AppEvents.ServerExceptionErr);

            AppEvents.LogException(ex, AppEvents.ScanReportService.GeneralScanReportException);
        }
        return serviceResponse;
    }

    @RequestMapping("activities/csv")
    public @ResponseBody ServiceReportResponse<String> getCsvActivities(HttpServletRequest request,
            HttpServletResponse response) {

        // url for the csv
        ServiceReportResponse<String> serviceResponse = new ServiceReportResponse<String>();

        try {
            JSONObject json = ControllerUtil.buffer(request);
            GenerateReportCsvRequest serviceRequest = JSON.parseObject(json.toJSONString(), GenerateReportCsvRequest.class);

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

            RewardActivityCsvDownloadWorker worker = new RewardActivityCsvDownloadWorker(zipBlobName, pngBlobName, serviceRequest,
                    reportService);
            SystemInitialization.getReportCsvDownloadService().execute(worker);

        } catch (Exception ex) {
            response.setStatus(GeneralConstants.RESPONSE_STATUS_500);
            serviceResponse.addError(AppEvents.ServerExceptionErr);

            AppEvents.LogException(ex, AppEvents.ScanReportService.GeneralScanReportException);
        }

        return serviceResponse;
    }

    @RequestMapping("distributor/dayscan")
    public @ResponseBody ServiceReportResponse<ScanStats<?>> getDistributorDayScan(HttpServletRequest request,
            HttpServletResponse response) {

        ServiceReportResponse<ScanStats<?>> scanReportResponse = new ServiceReportResponse<ScanStats<?>>();

        // try {
        // JSONObject json = ControllerUtil.buffer(request);
        // DistributorScanRequest distributorDayScanRequest = JSON
        // .parseObject(json.toJSONString(), DistributorScanRequest.class);
        //
        // if (distributorDayScanRequest != null) {
        //
        // List<ErrorInfo> errors = distributorDayScanRequest.validateRequest();
        // if (errors.isEmpty()) {
        // ScanStats<?> result =
        // distriibutorService.getStatsReport(distributorDayScanRequest.getRequestHeader()
        // .getCompanyId(), distributorDayScanRequest.getProductIds(),
        // distributorDayScanRequest
        // .getDistributorIds(), distributorDayScanRequest.getStartDate(),
        // distributorDayScanRequest
        // .getEndDate());
        // scanReportResponse.setReport(result);
        // } else {
        // response.setStatus(GeneralConstants.RESPONSE_STATUS_400);
        // scanReportResponse.addError(AppEvents.JsonKeyMissingErr);
        // }
        // } else {
        // response.setStatus(GeneralConstants.RESPONSE_STATUS_400);
        // scanReportResponse.addError(AppEvents.JsonKeyMissingErr);
        // }
        // } catch (ServiceException ex) {
        // response.setStatus(GeneralConstants.RESPONSE_STATUS_500);
        // scanReportResponse.addError(new ErrorInfo(ex.getErrorCode(),
        // ex.getErrorMsg()));
        // } catch (Exception ex) {
        // AppEvents.LogException(ex,
        // AppEvents.ScanReportService.GeneralScanReportException);
        // response.setStatus(GeneralConstants.RESPONSE_STATUS_500);
        // scanReportResponse.addError(AppEvents.ServerExceptionErr);
        // }

        return scanReportResponse;
    }

    @RequestMapping("distributor/geoscan")
    public @ResponseBody ServiceReportResponse<GeoScanResponse> getDistributorGeoScanReport(HttpServletRequest request,
            HttpServletResponse response) {
        ServiceReportResponse<GeoScanResponse> scanReportResponse = new ServiceReportResponse<GeoScanResponse>();
        // try {
        // JSONObject json = ControllerUtil.buffer(request);
        // DistributorScanRequest distributorMapScanRequest = JSON
        // .parseObject(json.toJSONString(), DistributorScanRequest.class);
        // if (distributorMapScanRequest != null) {
        //
        // List<ErrorInfo> errors = distributorMapScanRequest.validateRequest();
        // if (errors.isEmpty()) {
        // GeoScanResponse result =
        // distriibutorService.getGeoReport(distributorMapScanRequest.getRequestHeader()
        // .getCompanyId(), distributorMapScanRequest.getProductIds(),
        // distributorMapScanRequest
        // .getDistributorIds(), distributorMapScanRequest.getStartDate(),
        // distributorMapScanRequest
        // .getEndDate());
        // scanReportResponse.setReport(result);
        // } else {
        // response.setStatus(GeneralConstants.RESPONSE_STATUS_400);
        // scanReportResponse.addError(AppEvents.JsonKeyMissingErr);
        // }
        // } else {
        // response.setStatus(GeneralConstants.RESPONSE_STATUS_400);
        // scanReportResponse.addError(AppEvents.JsonKeyMissingErr);
        // }
        // } catch (ServiceException ex) {
        // response.setStatus(GeneralConstants.RESPONSE_STATUS_500);
        // scanReportResponse.addError(new ErrorInfo(ex.getErrorCode(),
        // ex.getErrorMsg()));
        // } catch (Exception ex) {
        // AppEvents.LogException(ex,
        // AppEvents.ScanReportService.GeneralScanReportException);
        // response.setStatus(GeneralConstants.RESPONSE_STATUS_500);
        // scanReportResponse.addError(AppEvents.ServerExceptionErr);
        // }
        return scanReportResponse;
    }

    @RequestMapping("promotion/claimrewards")
    public @ResponseBody ServiceReportResponse<PromotionRewardAmountStats<BigDecimal>> getPromotionClaimRewards(
            HttpServletRequest request, HttpServletResponse response) {
        ServiceReportResponse<PromotionRewardAmountStats<BigDecimal>> scanReportResponse = new ServiceReportResponse<PromotionRewardAmountStats<BigDecimal>>();
        try {
            JSONObject json = ControllerUtil.buffer(request);
            PromotionRewardReportRequest promoRewardRequest = JSON.parseObject(json.toJSONString(),
                    PromotionRewardReportRequest.class);
            if (promoRewardRequest != null) {

                List<ErrorInfo> errors = promoRewardRequest.validateRequest();
                if (errors.isEmpty()) {
                    PromotionRewardAmountStats<BigDecimal> result = reportService
                            .getPromotionRewardAmountStats(promoRewardRequest);
                    scanReportResponse.setReport(result);
                } else {
                    response.setStatus(GeneralConstants.RESPONSE_STATUS_400);
                    scanReportResponse.addError(AppEvents.JsonKeyMissingErr);
                }
            } else {
                response.setStatus(GeneralConstants.RESPONSE_STATUS_400);
                scanReportResponse.addError(AppEvents.JsonKeyMissingErr);
            }
        } catch (ServiceException ex) {
            response.setStatus(GeneralConstants.RESPONSE_STATUS_500);
            scanReportResponse.addError(new ErrorInfo(ex.getErrorCode(), ex.getErrorMsg()));
        } catch (Exception ex) {
            AppEvents.LogException(ex, AppEvents.ScanReportService.GeneralScanReportException);
            response.setStatus(GeneralConstants.RESPONSE_STATUS_500);
            scanReportResponse.addError(AppEvents.ServerExceptionErr);
        }
        return scanReportResponse;
    }

    @RequestMapping("promotion/successclaims")
    public @ResponseBody ServiceReportResponse<TimeBasedReportStats<Integer>> getSuccessClaims(HttpServletRequest request,
            HttpServletResponse response) {
        ServiceReportResponse<TimeBasedReportStats<Integer>> scanReportResponse = new ServiceReportResponse<>();
        try {
            JSONObject json = ControllerUtil.buffer(request);
            PromotionRewardReportRequest promoRewardRequest = JSON.parseObject(json.toJSONString(),
                    PromotionRewardReportRequest.class);
            if (promoRewardRequest != null) {

                List<ErrorInfo> errors = promoRewardRequest.validateRequest();
                if (errors.isEmpty()) {
                    TimeBasedReportStats<Integer> result = reportService.getSuccessClaims(promoRewardRequest);
                    scanReportResponse.setReport(result);
                } else {
                    // response.setStatus(GeneralConstants.RESPONSE_STATUS_400);
                    // scanReportResponse.addError(AppEvents.JsonKeyMissingErr);
                    System.out.println("either prmotion id or product id is empty");
                    scanReportResponse.setReport(new TimeBasedReportStats<Integer>());
                }
            } else {
                response.setStatus(GeneralConstants.RESPONSE_STATUS_400);
                scanReportResponse.addError(AppEvents.JsonKeyMissingErr);
            }
        } catch (ServiceException ex) {
            response.setStatus(GeneralConstants.RESPONSE_STATUS_500);
            scanReportResponse.addError(new ErrorInfo(ex.getErrorCode(), ex.getErrorMsg()));
        } catch (Exception ex) {
            AppEvents.LogException(ex, AppEvents.ScanReportService.GeneralSuccessClaimedRewardError);
            response.setStatus(GeneralConstants.RESPONSE_STATUS_500);
            scanReportResponse.addError(AppEvents.ServerExceptionErr);
        }
        return scanReportResponse;
    }

    @RequestMapping("promotion/claimpoints")
    public @ResponseBody ServiceReportResponse<PromotionRewardAmountStats<Integer>> getPromotionClaimPoints(
            HttpServletRequest request, HttpServletResponse response) {
        ServiceReportResponse<PromotionRewardAmountStats<Integer>> scanReportResponse = new ServiceReportResponse<PromotionRewardAmountStats<Integer>>();
        try {
            JSONObject json = ControllerUtil.buffer(request);
            PromotionRewardRequest promoRewardRequest = JSON.parseObject(json.toJSONString(), PromotionRewardRequest.class);
            if (promoRewardRequest != null) {
                List<ErrorInfo> errors = promoRewardRequest.validateRequest();
                if (errors.isEmpty()) {
                    PromotionRewardAmountStats<Integer> result = reportService.getPromotionRewardPointStats(promoRewardRequest);
                    scanReportResponse.setReport(result);
                } else {
                    // response.setStatus(GeneralConstants.RESPONSE_STATUS_400);
                    // scanReportResponse.addError(AppEvents.JsonKeyMissingErr);
                    System.out.println("product id is empty");
                    scanReportResponse.setReport(new PromotionRewardAmountStats<Integer>());
                }
            } else {
                response.setStatus(GeneralConstants.RESPONSE_STATUS_400);
                scanReportResponse.addError(AppEvents.JsonKeyMissingErr);
            }
        } catch (ServiceException ex) {
            response.setStatus(GeneralConstants.RESPONSE_STATUS_500);
            scanReportResponse.addError(new ErrorInfo(ex.getErrorCode(), ex.getErrorMsg()));
        } catch (Exception ex) {
            AppEvents.LogException(ex, AppEvents.ScanReportService.GeneralScanReportException);
            response.setStatus(GeneralConstants.RESPONSE_STATUS_500);
            scanReportResponse.addError(AppEvents.ServerExceptionErr);
        }
        return scanReportResponse;
    }

    @RequestMapping("enterlottery")
    public @ResponseBody ServiceReportResponse<Map<String, TimeBasedReportStats<Integer>>> getEnterLotteryStats(
            HttpServletRequest request, HttpServletResponse response) {
        ServiceReportResponse<Map<String, TimeBasedReportStats<Integer>>> scanReportResponse = new ServiceReportResponse<>();
        try {
            JSONObject json = ControllerUtil.buffer(request);
            PromotionRewardReportRequest promoRewardRequest = JSON.parseObject(json.toJSONString(),
                    PromotionRewardReportRequest.class);
            if (promoRewardRequest != null) {

                List<ErrorInfo> errors = promoRewardRequest.validateRequest();
                if (errors.isEmpty()) {
                    TimeBasedReportStats<Integer> result1 = reportService.getEnterLotteryStatistics(promoRewardRequest, 0);
                    TimeBasedReportStats<Integer> result2 = reportService.getEnterLotteryStatistics(promoRewardRequest, 1);
                    Map<String, TimeBasedReportStats<Integer>> result = new HashMap<String, TimeBasedReportStats<Integer>>();
                    result.put("external", result1);
                    result.put("internal", result2);
                    scanReportResponse.setReport(result);
                } else {
                    // response.setStatus(GeneralConstants.RESPONSE_STATUS_400);
                    // scanReportResponse.addError(AppEvents.JsonKeyMissingErr);
                    System.out.println("either prmotion id or product id is empty");
                    scanReportResponse.setReport(new HashMap<String, TimeBasedReportStats<Integer>>());
                }
            } else {
                response.setStatus(GeneralConstants.RESPONSE_STATUS_400);
                scanReportResponse.addError(AppEvents.JsonKeyMissingErr);
            }
        } catch (ServiceException ex) {
            response.setStatus(GeneralConstants.RESPONSE_STATUS_500);
            scanReportResponse.addError(new ErrorInfo(ex.getErrorCode(), ex.getErrorMsg()));
        } catch (Exception ex) {
            AppEvents.LogException(ex, AppEvents.ScanReportService.GeneralEnterLotteryReportError);
            response.setStatus(GeneralConstants.RESPONSE_STATUS_500);
            scanReportResponse.addError(AppEvents.ServerExceptionErr);
        }
        return scanReportResponse;
    }

    @RequestMapping("enterlottery/usertype")
    public @ResponseBody ServiceReportResponse<TimeBasedReportStats<Integer>> getUserEnterLotteryStats(HttpServletRequest request,
            HttpServletResponse response) {
        ServiceReportResponse<TimeBasedReportStats<Integer>> scanReportResponse = new ServiceReportResponse<>();
        try {
            JSONObject json = ControllerUtil.buffer(request);
            PromotionRewardReportRequest promoRewardRequest = JSON.parseObject(json.toJSONString(),
                    PromotionRewardReportRequest.class);
            if (promoRewardRequest != null) {

                List<ErrorInfo> errors = promoRewardRequest.validateRequest();
                if (errors.isEmpty()) {
                    TimeBasedReportStats<Integer> result = memberReportService
                            .getEffectiveScanStatisticsUserType(promoRewardRequest);
                    scanReportResponse.setReport(result);
                } else {
                    // response.setStatus(GeneralConstants.RESPONSE_STATUS_400);
                    // scanReportResponse.addError(AppEvents.JsonKeyMissingErr);
                    System.out.println("either prmotion id or product id is empty");
                    scanReportResponse.setReport(new TimeBasedReportStats<Integer>());
                }
            } else {
                response.setStatus(GeneralConstants.RESPONSE_STATUS_400);
                scanReportResponse.addError(AppEvents.JsonKeyMissingErr);
            }
        } catch (ServiceException ex) {
            response.setStatus(GeneralConstants.RESPONSE_STATUS_500);
            scanReportResponse.addError(new ErrorInfo(ex.getErrorCode(), ex.getErrorMsg()));
        } catch (Exception ex) {
            AppEvents.LogException(ex, AppEvents.ScanReportService.GeneralEnterLotteryUserTypeError);
            response.setStatus(GeneralConstants.RESPONSE_STATUS_500);
            scanReportResponse.addError(AppEvents.ServerExceptionErr);
        }
        return scanReportResponse;
    }

    @RequestMapping("enterlottery/uniqueuser")
    public @ResponseBody ServiceReportResponse<Map<String, TimeBasedReportStats<Integer>>> getUniqueUserEnterLotteryStats(
            HttpServletRequest request, HttpServletResponse response) {
        ServiceReportResponse<Map<String, TimeBasedReportStats<Integer>>> scanReportResponse = new ServiceReportResponse<Map<String, TimeBasedReportStats<Integer>>>();
        try {
            JSONObject json = ControllerUtil.buffer(request);
            PromotionRewardReportRequest promoRewardRequest = JSON.parseObject(json.toJSONString(),
                    PromotionRewardReportRequest.class);
            if (promoRewardRequest != null) {

                List<ErrorInfo> errors = promoRewardRequest.validateRequest();
                if (errors.isEmpty()) {
                    TimeBasedReportStats<Integer> result1 = memberReportService
                            .getEffectiveScanStatisticsUniqueUser(promoRewardRequest, 0);
                    TimeBasedReportStats<Integer> result2 = memberReportService
                            .getEffectiveScanStatisticsUniqueUser(promoRewardRequest, 1);
                    Map<String, TimeBasedReportStats<Integer>> result = new HashMap<String, TimeBasedReportStats<Integer>>();
                    result.put("external", result1);
                    result.put("internal", result2);
                    scanReportResponse.setReport(result);
                } else {
                    // response.setStatus(GeneralConstants.RESPONSE_STATUS_400);
                    // scanReportResponse.addError(AppEvents.JsonKeyMissingErr);
                    System.out.println("either prmotion id or product id is empty");
                    scanReportResponse.setReport(new HashMap<String, TimeBasedReportStats<Integer>>());
                }
            } else {
                response.setStatus(GeneralConstants.RESPONSE_STATUS_400);
                scanReportResponse.addError(AppEvents.JsonKeyMissingErr);
            }
        } catch (ServiceException ex) {
            response.setStatus(GeneralConstants.RESPONSE_STATUS_500);
            scanReportResponse.addError(new ErrorInfo(ex.getErrorCode(), ex.getErrorMsg()));
        } catch (Exception ex) {
            AppEvents.LogException(ex, AppEvents.ScanReportService.GeneralEnterLotteryUniqueUserError);
            response.setStatus(GeneralConstants.RESPONSE_STATUS_500);
            scanReportResponse.addError(AppEvents.ServerExceptionErr);
        }
        return scanReportResponse;
    }

    @RequestMapping("enterlottery/todayyesterday")
    public @ResponseBody ServiceReportResponse<EnterLotteryDailyStats> getTodayYesterdayEnterLotteryMeasures(
            HttpServletRequest request, HttpServletResponse response) {
        ServiceReportResponse<EnterLotteryDailyStats> scanReportResponse = new ServiceReportResponse<EnterLotteryDailyStats>();
        try {
            JSONObject json = ControllerUtil.buffer(request);
            BaseRequestNormal req = JSON.parseObject(json.toJSONString(), BaseRequestNormal.class);
            if (req != null) {

                List<ErrorInfo> errors = req.validateRequest();
                if (errors.isEmpty()) {
                    EnterLotteryDailyStats result = reportService.getTodayYesterdayEnterLotteryStats(req);
                    scanReportResponse.setReport(result);
                } else {
                    response.setStatus(GeneralConstants.RESPONSE_STATUS_400);
                    scanReportResponse.addError(AppEvents.JsonKeyMissingErr);
                }
            } else {
                response.setStatus(GeneralConstants.RESPONSE_STATUS_400);
                scanReportResponse.addError(AppEvents.JsonKeyMissingErr);
            }
        } catch (ServiceException ex) {
            response.setStatus(GeneralConstants.RESPONSE_STATUS_500);
            scanReportResponse.addError(new ErrorInfo(ex.getErrorCode(), ex.getErrorMsg()));
        } catch (Exception ex) {
            AppEvents.LogException(ex, AppEvents.ScanReportService.GeneralEnterLotteryTodayYesterdayError);
            response.setStatus(GeneralConstants.RESPONSE_STATUS_500);
            scanReportResponse.addError(AppEvents.ServerExceptionErr);
        }
        return scanReportResponse;
    }

    /**
     * This interface serves the member report Returns new member count, in each
     * hour
     */
    @RequestMapping("member")
    public @ResponseBody ServiceReportResponse<TimeBasedReportStats<Integer>> getMemberStatistics(HttpServletRequest request,
            HttpServletResponse response) {
        ServiceReportResponse<TimeBasedReportStats<Integer>> memberStatisticsResponse = new ServiceReportResponse<TimeBasedReportStats<Integer>>();

        try {
            JSONObject json = ControllerUtil.buffer(request);
            TimeRangeReportRequest memberReportRequest = JSON.parseObject(json.toJSONString(), TimeRangeReportRequest.class);
            System.out.println(JSON.toJSONString(memberReportRequest));

            if (memberReportRequest != null) {
                List<ErrorInfo> errors = memberReportRequest.validateRequest();
                if (errors.isEmpty()) {
                    TimeBasedReportStats<Integer> stats = new TimeBasedReportStats<Integer>();
                    stats.setHourScan(
                            memberReportService.getMemberStatistics(memberReportRequest.getRequestHeader().getCompanyId(),
                                    memberReportRequest.getStartDate(), memberReportRequest.getEndDate()));
                    memberStatisticsResponse.setReport(stats);
                } else {
                    response.setStatus(GeneralConstants.RESPONSE_STATUS_400);
                    memberStatisticsResponse.addError(AppEvents.JsonKeyMissingErr);
                }
            }
        } catch (ServiceException ex) {
            response.setStatus(GeneralConstants.RESPONSE_STATUS_500);
            memberStatisticsResponse.addError(new ErrorInfo(ex.getErrorCode(), ex.getErrorMsg()));
        } catch (Exception ex) {
            AppEvents.LogException(ex, AppEvents.ScanReportService.GeneralExceptionInMemberStat);
            response.setStatus(GeneralConstants.RESPONSE_STATUS_500);
            memberStatisticsResponse.addError(AppEvents.ServerExceptionErr);
        }

        return memberStatisticsResponse;
    }

    @RequestMapping("/userMgmevents")
    public @ResponseBody ServiceReportResponse<List<Mgmevent>> getUserMgmevents(HttpServletRequest request,
            HttpServletResponse response) {
        ServiceReportResponse<List<Mgmevent>> scanReportResponse = new ServiceReportResponse<List<Mgmevent>>();
        try {
            JSONObject json = ControllerUtil.buffer(request);
            MgmeventRequest mgmeventRequest = JSON.parseObject(json.toJSONString(), MgmeventRequest.class);
            if (mgmeventRequest != null) {
                List<ErrorInfo> errors = mgmeventRequest.validateRequest();
                if (errors.isEmpty()) {
                    List<Mgmevent> result = reportService.getUserMgmevents(mgmeventRequest.getC(), mgmeventRequest.getU(),
                            mgmeventRequest.getStartDate(), mgmeventRequest.getEndDate());
                    scanReportResponse.setReport(result);
                } else {
                    response.setStatus(GeneralConstants.RESPONSE_STATUS_400);
                    scanReportResponse.addError(AppEvents.JsonKeyMissingErr);
                }
            } else {
                response.setStatus(GeneralConstants.RESPONSE_STATUS_400);
                scanReportResponse.addError(AppEvents.JsonKeyMissingErr);
            }
        } catch (Exception ex) {
            AppEvents.LogException(ex, AppEvents.ScanReportService.GeneralScanReportException);
            response.setStatus(GeneralConstants.RESPONSE_STATUS_500);
            scanReportResponse.addError(AppEvents.ServerExceptionErr);
        }
        return scanReportResponse;
    }

    @RequestMapping(value = "/internalReports", method = RequestMethod.POST)
    @ResponseBody
    public ServiceReportResponse<InternalReportGroup> getInernalReports(HttpServletRequest request,
            HttpServletResponse response) {
        ServiceReportResponse<InternalReportGroup> internalReportResponse = new ServiceReportResponse<InternalReportGroup>();
        try {
            JSONObject json = ControllerUtil.buffer(request);
            InternalReportsRequest internalRequest = JSON.parseObject(json.toJSONString(), InternalReportsRequest.class);
            if (internalRequest != null && internalRequest.validateRequest().isEmpty()) {
                InternalReportGroup result = internalReportsService.getAllReports(internalRequest.getCid(),
                        internalRequest.getCardName());
                internalReportResponse.setReport(result);
                response.setStatus(GeneralConstants.RESPONSE_STATUS_200);
            } else {
                response.setStatus(GeneralConstants.RESPONSE_STATUS_400);
                internalReportResponse.addError(AppEvents.JsonKeyMissingErr);
            }
        } catch (Exception e) {
            AppEvents.LogException(e, AppEvents.InternalReportService.GeneralInternalReportException);
            response.setStatus(GeneralConstants.RESPONSE_STATUS_500);
            internalReportResponse.addError(AppEvents.ServerExceptionErr);
        }
        return internalReportResponse;
    }

    @RequestMapping(value = "points/todayYesterdayPointsSum", method = RequestMethod.POST)
    @ResponseBody
    public ServiceReportResponse<EnterLotteryDailyStats> getTodayYesterdayPointsSum(HttpServletRequest request,
            HttpServletResponse response) {
        ServiceReportResponse<EnterLotteryDailyStats> pointsSumResp = new ServiceReportResponse<EnterLotteryDailyStats>();
        try {
            JSONObject json = ControllerUtil.buffer(request);
            PointsSumReportRequest pointsReq = JSON.parseObject(json.toJSONString(), PointsSumReportRequest.class);
            if (pointsReq != null && pointsReq.validateRequest().isEmpty()) {
                EnterLotteryDailyStats pointsSumReport = tranReportService.getTodayYesterdayGeneratePoints(pointsReq);
                pointsSumResp.setReport(pointsSumReport);
                response.setStatus(GeneralConstants.RESPONSE_STATUS_200);
            } else {
                response.setStatus(GeneralConstants.RESPONSE_STATUS_400);
                pointsSumResp.addError(AppEvents.JsonKeyMissingErr);
            }
        } catch (Exception e) {
            response.setStatus(GeneralConstants.RESPONSE_STATUS_500);
            pointsSumResp.addError(AppEvents.ServerExceptionErr);
        }
        return pointsSumResp;
    }

    @RequestMapping(value = "transaction/tyPointReduce", method = RequestMethod.POST)
    @ResponseBody
    public ServiceReportResponse<ScanStats<?>> getTYPointReduceStatistics(HttpServletRequest request,
            HttpServletResponse response) {
        ServiceReportResponse<ScanStats<?>> tyPointReduceReportResp = new ServiceReportResponse<ScanStats<?>>();
        try {
            JSONObject json = ControllerUtil.buffer(request);
            TYPointReduceRequest tyPointReduceReq = JSON.parseObject(json.toJSONString(), TYPointReduceRequest.class);
            if (tyPointReduceReq != null && tyPointReduceReq.validateRequest().isEmpty()) {
                ScanStats<TimeBasedReportStatsBase<Integer>> pointReduceReport = tranReportService.getTyPointReduceStatistics(
                        tyPointReduceReq.getRequestHeader().getCompanyId(), tyPointReduceReq.getLotteryIds(),
                        tyPointReduceReq.getStartDate(), tyPointReduceReq.getEndDate());
                tyPointReduceReportResp.setReport(pointReduceReport);
                response.setStatus(GeneralConstants.RESPONSE_STATUS_200);
            } else {
                response.setStatus(GeneralConstants.RESPONSE_STATUS_400);
                tyPointReduceReportResp.addError(AppEvents.JsonKeyMissingErr);
            }
        } catch (Exception e) {
            response.setStatus(GeneralConstants.RESPONSE_STATUS_500);
            tyPointReduceReportResp.addError(AppEvents.ServerExceptionErr);
        }
        return tyPointReduceReportResp;
    }

    @RequestMapping(value = "transaction/mtPointReduce", method = RequestMethod.POST)
    @ResponseBody
    public ServiceReportResponse<ScanStats<?>> getMTPointReduceStatistics(HttpServletRequest request,
            HttpServletResponse response) {
        ServiceReportResponse<ScanStats<?>> mtPointReduceReportResp = new ServiceReportResponse<ScanStats<?>>();
        try {
            JSONObject json = ControllerUtil.buffer(request);
            TimeRangeReportRequest timeRangeReportRequest = JSON.parseObject(json.toJSONString(), TimeRangeReportRequest.class);
            if (timeRangeReportRequest != null && timeRangeReportRequest.validateRequest().isEmpty()) {
                ScanStats<TimeBasedReportStatsBase<Integer>> pointReduceReport = tranReportService.getMTPointReduceStatistics(
                        timeRangeReportRequest.getRequestHeader().getCompanyId(), timeRangeReportRequest.getStartDate(),
                        timeRangeReportRequest.getEndDate());
                mtPointReduceReportResp.setReport(pointReduceReport);
                response.setStatus(GeneralConstants.RESPONSE_STATUS_200);
            } else {
                response.setStatus(GeneralConstants.RESPONSE_STATUS_400);
                mtPointReduceReportResp.addError(AppEvents.JsonKeyMissingErr);
            }
        } catch (Exception e) {
            response.setStatus(GeneralConstants.RESPONSE_STATUS_500);
            mtPointReduceReportResp.addError(AppEvents.ServerExceptionErr);
        }
        return mtPointReduceReportResp;
    }

    @RequestMapping(value = "activities/tyPointGenerate", method = RequestMethod.POST)
    @ResponseBody
    public ServiceReportResponse<ScanStats<?>> getTYPointGenerateStatistics(HttpServletRequest request,
            HttpServletResponse response) {
        ServiceReportResponse<ScanStats<?>> tyPointGenerateReportResp = new ServiceReportResponse<ScanStats<?>>();
        try {
            JSONObject json = ControllerUtil.buffer(request);
            TimeRangeReportRequest tyPointGenerateReq = JSON.parseObject(json.toJSONString(), TYPointReduceRequest.class);
            if (tyPointGenerateReq != null && tyPointGenerateReq.validateRequest().isEmpty()) {
                ScanStats<TimeBasedReportStatsBase<Integer>> pointGenerateReport = tranReportService.getTyPointGenerateStatistics(
                        tyPointGenerateReq.getRequestHeader().getCompanyId(), tyPointGenerateReq.getStartDate(),
                        tyPointGenerateReq.getEndDate());
                tyPointGenerateReportResp.setReport(pointGenerateReport);
                response.setStatus(GeneralConstants.RESPONSE_STATUS_200);
            } else {
                response.setStatus(GeneralConstants.RESPONSE_STATUS_400);
                tyPointGenerateReportResp.addError(AppEvents.JsonKeyMissingErr);
            }
        } catch (Exception e) {
            response.setStatus(GeneralConstants.RESPONSE_STATUS_500);
            tyPointGenerateReportResp.addError(AppEvents.ServerExceptionErr);
        }
        return tyPointGenerateReportResp;
    }

    @RequestMapping(value = "geo/dailystats", method = RequestMethod.POST)
    @ResponseBody
    public ServiceReportResponse<List<DailyStatsBase>> getDailyGeoCityStatistics(HttpServletRequest request,
            HttpServletResponse response) {
        ServiceReportResponse<List<DailyStatsBase>> reportResp = new ServiceReportResponse<>();

        JSONObject json = ControllerUtil.buffer(request);
        BaseRequestNormal req = JSON.parseObject(json.toJSONString(), BaseRequestNormal.class);
        if (req != null && req.validateRequest().isEmpty()) {
            try {
                List<DailyStatsBase> dailyStatses = geoReportService.getDailyGeoCityStats(req.getRequestHeader().getCompanyId());
                response.setStatus(GeneralConstants.RESPONSE_STATUS_200);
                reportResp.setReport(dailyStatses);
            } catch (ServiceException e) {
                response.setStatus(GeneralConstants.RESPONSE_STATUS_500);
                reportResp.addError(AppEvents.ServerExceptionErr);
            }
        } else {
            response.setStatus(GeneralConstants.RESPONSE_STATUS_400);
            reportResp.addError(AppEvents.JsonKeyMissingErr);
        }

        return reportResp;
    }

    @RequestMapping("geo/enterlottery")
    public @ResponseBody ServiceReportResponse<GeoStateScanReponse> getEnterLotteryGeoCityStatistics(HttpServletRequest request,
            HttpServletResponse response) {
        ServiceReportResponse<GeoStateScanReponse> reportResp = new ServiceReportResponse<>();

        try {
            JSONObject json = ControllerUtil.buffer(request);
            PromotionFilterRequest promFilterReq = JSON.parseObject(json.toJSONString(), PromotionFilterRequest.class);

            if (promFilterReq != null && promFilterReq.validateRequest().isEmpty()) {
                GeoStateScanReponse result = geoReportService.getEnterLotteryGeoCityStatics(
                        promFilterReq.getRequestHeader().getCompanyId(), promFilterReq.getPromotionIds(),
                        promFilterReq.getStartDate(), promFilterReq.getEndDate());
                reportResp.setReport(result);
            } else {
                response.setStatus(GeneralConstants.RESPONSE_STATUS_400);
                reportResp.addError(AppEvents.JsonKeyMissingErr);
            }

        } catch (ServiceException ex) {
            response.setStatus(GeneralConstants.RESPONSE_STATUS_500);
            reportResp.addError(new ErrorInfo(ex.getErrorCode(), ex.getErrorMsg()));
        } catch (Exception ex) {
            AppEvents.LogException(ex, AppEvents.ScanReportService.GeneralScanReportException);
            response.setStatus(GeneralConstants.RESPONSE_STATUS_500);
            reportResp.addError(AppEvents.ServerExceptionErr);
        }

        return reportResp;
    }

    @RequestMapping("geo/newuser")
    public @ResponseBody ServiceReportResponse<GeoStateScanReponse> getNewUserGeoCityStatistics(HttpServletRequest request,
            HttpServletResponse response) {
        ServiceReportResponse<GeoStateScanReponse> reportResp = new ServiceReportResponse<>();

        try {
            JSONObject json = ControllerUtil.buffer(request);
            PromotionFilterRequest promFilterReq = JSON.parseObject(json.toJSONString(), PromotionFilterRequest.class);

            if (promFilterReq != null && promFilterReq.validateRequest().isEmpty()) {
                GeoStateScanReponse result = geoReportService.getNewUserGeoCityStatics(
                        promFilterReq.getRequestHeader().getCompanyId(), promFilterReq.getPromotionIds(),
                        promFilterReq.getStartDate(), promFilterReq.getEndDate());
                reportResp.setReport(result);
            } else {
                response.setStatus(GeneralConstants.RESPONSE_STATUS_400);
                reportResp.addError(AppEvents.JsonKeyMissingErr);
            }

        } catch (ServiceException ex) {
            response.setStatus(GeneralConstants.RESPONSE_STATUS_500);
            reportResp.addError(new ErrorInfo(ex.getErrorCode(), ex.getErrorMsg()));
        } catch (Exception ex) {
            AppEvents.LogException(ex, AppEvents.ScanReportService.GeneralScanReportException);
            response.setStatus(GeneralConstants.RESPONSE_STATUS_500);
            reportResp.addError(AppEvents.ServerExceptionErr);
        }

        return reportResp;
    }

    @RequestMapping("trend/scanTags")
    public @ResponseBody ServiceReportResponse<TimeBasedReportStats<Integer>> getScanTagsTrendStats(HttpServletRequest request,
            HttpServletResponse response) {
        ServiceReportResponse<TimeBasedReportStats<Integer>> reportResp = new ServiceReportResponse<TimeBasedReportStats<Integer>>();

        try {
            JSONObject json = ControllerUtil.buffer(request);
            TrendStatsRequest reportReq = JSON.parseObject(json.toJSONString(), TrendStatsRequest.class);
            if (reportReq != null) {
                List<ErrorInfo> errors = reportReq.validateRequest();
                if (errors.isEmpty()) {
                    TimeBasedReportStats<Integer> stats = new TimeBasedReportStats<Integer>();
                    String range = reportReq.getRange();
                    if (range.equalsIgnoreCase(DateTimeUnit.YEAR.name())) {
                        stats.setHourScan(trendStatsService.getYearsScanTagsStats(reportReq.getRequestHeader().getCompanyId()));
                    } else if (range.equalsIgnoreCase(DateTimeUnit.MONTH.name())) {
                        stats.setHourScan(trendStatsService.getMonthsScanTagsStats(reportReq.getRequestHeader().getCompanyId()));
                    } else if (range.equalsIgnoreCase(DateTimeUnit.DAY.name())) {
                        stats.setHourScan(trendStatsService.getWeeksScanTagsStats(reportReq.getRequestHeader().getCompanyId()));
                    }

                    reportResp.setReport(stats);
                } else {
                    response.setStatus(GeneralConstants.RESPONSE_STATUS_400);
                    reportResp.addError(AppEvents.JsonKeyMissingErr);
                }
            } else {
                response.setStatus(GeneralConstants.RESPONSE_STATUS_400);
                reportResp.addError(AppEvents.JsonKeyMissingErr);
            }
        } catch (ServiceException ex) {
            response.setStatus(GeneralConstants.RESPONSE_STATUS_500);
            reportResp.addError(new ErrorInfo(ex.getErrorCode(), ex.getErrorMsg()));
        } catch (Exception ex) {
            AppEvents.LogException(ex, AppEvents.ScanReportService.GeneralScanReportException);
            response.setStatus(GeneralConstants.RESPONSE_STATUS_500);
            reportResp.addError(AppEvents.ServerExceptionErr);
        }

        return reportResp;
    }

    @RequestMapping("trend/enterLottery")
    public @ResponseBody ServiceReportResponse<TimeBasedReportStats<Integer>> getEnterLotteryTrendStats(
            HttpServletRequest request, HttpServletResponse response) {
        ServiceReportResponse<TimeBasedReportStats<Integer>> reportResp = new ServiceReportResponse<TimeBasedReportStats<Integer>>();

        try {
            JSONObject json = ControllerUtil.buffer(request);
            TrendStatsRequest reportReq = JSON.parseObject(json.toJSONString(), TrendStatsRequest.class);
            System.out.println(JSON.toJSONString(reportReq));
            if (reportReq != null) {
                List<ErrorInfo> errors = reportReq.validateRequest();
                if (errors.isEmpty()) {
                    TimeBasedReportStats<Integer> stats = new TimeBasedReportStats<Integer>();
                    String range = reportReq.getRange();
                    System.out.println("the value of range is:" + range + "; companyid value is:"
                            + reportReq.getRequestHeader().getCompanyId());
                    if (range.equalsIgnoreCase(DateTimeUnit.YEAR.name())) {
                        stats.setHourScan(
                                trendStatsService.getYearsEnterLotteryStats(reportReq.getRequestHeader().getCompanyId()));
                    } else if (range.equalsIgnoreCase(DateTimeUnit.MONTH.name())) {
                        stats.setHourScan(
                                trendStatsService.getMonthsEnterLotteryStats(reportReq.getRequestHeader().getCompanyId()));
                    } else if (range.equalsIgnoreCase(DateTimeUnit.DAY.name())) {
                        stats.setHourScan(
                                trendStatsService.getWeeksEnterLotteryStats(reportReq.getRequestHeader().getCompanyId()));
                    }

                    reportResp.setReport(stats);
                } else {
                    response.setStatus(GeneralConstants.RESPONSE_STATUS_400);
                    reportResp.addError(AppEvents.JsonKeyMissingErr);
                }
            } else {
                response.setStatus(GeneralConstants.RESPONSE_STATUS_400);
                reportResp.addError(AppEvents.JsonKeyMissingErr);
            }
        } catch (ServiceException ex) {
            response.setStatus(GeneralConstants.RESPONSE_STATUS_500);
            reportResp.addError(new ErrorInfo(ex.getErrorCode(), ex.getErrorMsg()));
        } catch (Exception ex) {
            AppEvents.LogException(ex, AppEvents.ScanReportService.GeneralScanReportException);
            response.setStatus(GeneralConstants.RESPONSE_STATUS_500);
            reportResp.addError(AppEvents.ServerExceptionErr);
        }

        return reportResp;
    }

    @RequestMapping("trend/newUser")
    public @ResponseBody ServiceReportResponse<TimeBasedReportStats<Integer>> getNewUserTrendStats(HttpServletRequest request,
            HttpServletResponse response) {
        ServiceReportResponse<TimeBasedReportStats<Integer>> reportResp = new ServiceReportResponse<TimeBasedReportStats<Integer>>();

        try {
            JSONObject json = ControllerUtil.buffer(request);
            TrendStatsRequest reportReq = JSON.parseObject(json.toJSONString(), TrendStatsRequest.class);
            System.out.println(JSON.toJSONString(reportReq));
            if (reportReq != null) {
                List<ErrorInfo> errors = reportReq.validateRequest();
                if (errors.isEmpty()) {
                    TimeBasedReportStats<Integer> stats = new TimeBasedReportStats<Integer>();
                    String range = reportReq.getRange();
                    System.out.println("the value of range is:" + range + "; companyid value is:"
                            + reportReq.getRequestHeader().getCompanyId());
                    if (range.equalsIgnoreCase(DateTimeUnit.YEAR.name())) {
                        stats.setHourScan(trendStatsService.getYearsNewUserStats(reportReq.getRequestHeader().getCompanyId()));
                    } else if (range.equalsIgnoreCase(DateTimeUnit.MONTH.name())) {
                        stats.setHourScan(trendStatsService.getMonthsNewUserStats(reportReq.getRequestHeader().getCompanyId()));
                    } else if (range.equalsIgnoreCase(DateTimeUnit.DAY.name())) {
                        stats.setHourScan(trendStatsService.getWeeksNewUserStats(reportReq.getRequestHeader().getCompanyId()));
                    }

                    reportResp.setReport(stats);
                } else {
                    response.setStatus(GeneralConstants.RESPONSE_STATUS_400);
                    reportResp.addError(AppEvents.JsonKeyMissingErr);
                }
            } else {
                response.setStatus(GeneralConstants.RESPONSE_STATUS_400);
                reportResp.addError(AppEvents.JsonKeyMissingErr);
            }
        } catch (ServiceException ex) {
            response.setStatus(GeneralConstants.RESPONSE_STATUS_500);
            reportResp.addError(new ErrorInfo(ex.getErrorCode(), ex.getErrorMsg()));
        } catch (Exception ex) {
            AppEvents.LogException(ex, AppEvents.ScanReportService.GeneralScanReportException);
            response.setStatus(GeneralConstants.RESPONSE_STATUS_500);
            reportResp.addError(AppEvents.ServerExceptionErr);
        }

        return reportResp;
    }

}
