package com.toucha.analytics.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.toucha.analytics.common.dao.PointReportDao;
import com.toucha.analytics.common.exceptions.ServiceException;
import com.toucha.analytics.common.model.BaseRequestNormal;
import com.toucha.analytics.common.model.EnterLotteryDailyStats;
import com.toucha.analytics.common.model.EnterLotteryDailyStatsBase;
import com.toucha.analytics.common.model.ScanStats;
import com.toucha.analytics.common.model.TimeBasedReportStatUnit;
import com.toucha.analytics.common.model.TimeBasedReportStatsBase;
import com.toucha.analytics.common.util.AppEvents;

/**
 * This class design for all transaction event report services
 * 
 * @author senhui.li
 */
@Service("tranReportService")
public class TransactionReportService {

    public static final int SCAN_POINT_REWARD = -2;
    public static final int SCAN_SHARING_POINT = -3;

    private PointReportDao pointReportDao = new PointReportDao();

    public EnterLotteryDailyStats getTodayYesterdayGeneratePoints(BaseRequestNormal req) throws ServiceException {
        EnterLotteryDailyStats result = new EnterLotteryDailyStats();
        Integer companyId = req.getRequestHeader().getCompanyId();
        if (companyId != null) {
            try {
                EnterLotteryDailyStatsBase tyPointsGenerate = pointReportDao.getPointsGenerateSum(companyId.intValue());
                EnterLotteryDailyStatsBase tyPointsReduce = pointReportDao.getTYPointsReduceSum(companyId.intValue());
                EnterLotteryDailyStatsBase thirdpartyPointsReduce = pointReportDao.getMTPointsReduceSum(companyId.intValue());
                result.getDailyMeasures().add(tyPointsGenerate);
                result.getDailyMeasures().add(tyPointsReduce);
                result.getDailyMeasures().add(thirdpartyPointsReduce);
            } catch (Exception e) {
                AppEvents.LogException(e, AppEvents.ScanReportService.EnterLotteryTodayYesterdayServiceError,
                        JSON.toJSONString(req));
                throw new ServiceException(AppEvents.ScanReportService.EnterLotteryTodayYesterdayServiceError);
            }
        }

        return result;
    }

    /**
     * Get TouYun Generate points statistic data
     */
    public ScanStats<TimeBasedReportStatsBase<Integer>> getTyPointGenerateStatistics(int companyId, Date startDate, Date endDate)
            throws ServiceException {
        ScanStats<TimeBasedReportStatsBase<Integer>> result = new ScanStats<TimeBasedReportStatsBase<Integer>>();

        TimeBasedReportStatsBase<Integer> scanStats = new TimeBasedReportStatsBase<Integer>();
        int scanMeasures = 0;
        scanStats.setDesc("抽奖积分");
        TimeBasedReportStatsBase<Integer> basicStats = new TimeBasedReportStatsBase<Integer>();
        int basicMeasures = 0;
        basicStats.setDesc("基础积分");
        TimeBasedReportStatsBase<Integer> sharingStats = new TimeBasedReportStatsBase<Integer>();
        int sharingMeasures = 0;
        sharingStats.setDesc("分享积分");
        TimeBasedReportStatsBase<Integer> totalStats = new TimeBasedReportStatsBase<Integer>();
        Map<Date, Integer> total = new TreeMap<Date, Integer>();
        int totalMeasures = 0;
        totalStats.setDesc("总计");

        try {
            List<TimeBasedReportStatUnit<Integer>> tyPointGenerate = pointReportDao.getTYGeneratePointStats(companyId, startDate,
                    endDate);
            for (TimeBasedReportStatUnit<Integer> typg : tyPointGenerate) {
                int pointtype = Integer.parseInt(typg.getDesc());
                if (pointtype == SCAN_POINT_REWARD) {
                    scanStats.getTimeseries().add(typg.getHour());
                    scanStats.getMeasures().add(typg.getMeasure());
                    scanMeasures += typg.getMeasure().intValue();
                } else if (pointtype == SCAN_SHARING_POINT) {
                    sharingStats.getTimeseries().add(typg.getHour());
                    sharingStats.getMeasures().add(typg.getMeasure());
                    sharingMeasures += typg.getMeasure().intValue();
                } else {
                    basicStats.getTimeseries().add(typg.getHour());
                    basicStats.getMeasures().add(typg.getMeasure());
                    basicMeasures += typg.getMeasure().intValue();
                }

                Date date = typg.getHour();
                if (total.containsKey(date)) {
                    total.put(date, total.get(date) + typg.getMeasure());
                } else {
                    total.put(date, typg.getMeasure());
                }

                totalMeasures += typg.getMeasure();
            }

            // sum total points data by date
            for (Date date : total.keySet()) {
                totalStats.getTimeseries().add(date);
                totalStats.getMeasures().add(total.get(date));
            }

            scanStats.setTotalMeasure(scanMeasures);
            basicStats.setTotalMeasure(basicMeasures);
            sharingStats.setTotalMeasure(sharingMeasures);
            totalStats.setTotalMeasure(totalMeasures);

            result.getHourScan().add(scanStats);
            result.getHourScan().add(basicStats);
            result.getHourScan().add(sharingStats);
            result.getHourScan().add(totalStats);

        } catch (SQLException e) {
            String requestParam = "CompanyId: " + companyId + ", StartDate: " + startDate + ", EndDate: " + endDate;
            AppEvents.LogException(e, AppEvents.ScanReportService.DateRangeScanReportServiceException, requestParam);

            throw new ServiceException(AppEvents.ServerExceptionErr);
        }

        return result;
    }

    /**
     * Get TuoYun point reduces statistic data
     */
    public ScanStats<TimeBasedReportStatsBase<Integer>> getTyPointReduceStatistics(int companyId, List<Integer> lotteryIds,
            Date startDate, Date endDate) throws ServiceException {
        ScanStats<TimeBasedReportStatsBase<Integer>> result = new ScanStats<TimeBasedReportStatsBase<Integer>>();

        Map<Integer, TimeBasedReportStatsBase<Integer>> stats = new HashMap<Integer, TimeBasedReportStatsBase<Integer>>();
        TimeBasedReportStatsBase<Integer> totalStats = new TimeBasedReportStatsBase<Integer>();
        try {
            List<TimeBasedReportStatUnit<Integer>> tyPointReduces = pointReportDao.getTYPointReduceStats(companyId, lotteryIds,
                    startDate, endDate);
            List<Date> dateDims = new ArrayList<Date>();
            List<Integer> totalMeasures = new ArrayList<Integer>();
            int totalMeasure = 0;
            totalStats.setDesc("总计");
            // loop all lottery statistic
            PR: for (TimeBasedReportStatUnit<Integer> typr : tyPointReduces) {
                for (Integer li : lotteryIds) {
                    TimeBasedReportStatsBase<Integer> stat = stats.get(li);
                    stat = stat == null ? new TimeBasedReportStatsBase<Integer>() : stat;
                    if (Integer.toString(li).equals(typr.getDesc())) {
                        stat.setDesc(typr.getDesc());
                        stat.getMeasures().add(typr.getMeasure());
                        stat.getTimeseries().add(typr.getHour());
                        int total = stat.getTotalMeasure() == null ? 0 : stat.getTotalMeasure().intValue();
                        stat.setTotalMeasure(total + typr.getMeasure());
                        stats.put(li, stat);

                        dateDims.add(typr.getHour());
                        totalMeasure += typr.getMeasure();
                        totalMeasures.add(typr.getMeasure());
                        // if current lottery id equals point reduce
                        // collection's lottery id
                        // continue to point reduce statistic loop
                        // PR means is point reduces
                        continue PR;
                    }
                }
            }

            if (!stats.isEmpty()) {
                for (TimeBasedReportStatsBase<Integer> stat : stats.values()) {
                    result.getHourScan().add(stat);
                }
            }

            totalStats.setTimeseries(dateDims);
            totalStats.setMeasures(totalMeasures);
            totalStats.setTotalMeasure(totalMeasure);
            result.getHourScan().add(totalStats);

        } catch (Exception ex) {
            String requestParam = "CompanyId: " + companyId + ", lotteryIds: " + lotteryIds + ", StartDate: " + startDate
                    + ", EndDate: " + endDate;
            AppEvents.LogException(ex, AppEvents.ScanReportService.DateRangeScanReportServiceException, requestParam);

            throw new ServiceException(AppEvents.ServerExceptionErr);
        }

        return result;
    }

    /**
     * Get MengTou point reduces statistic data
     */
    public ScanStats<TimeBasedReportStatsBase<Integer>> getMTPointReduceStatistics(int companyId,
            Date startDate, Date endDate) throws ServiceException {
        ScanStats<TimeBasedReportStatsBase<Integer>> result = new ScanStats<TimeBasedReportStatsBase<Integer>>();

        try {
            List<TimeBasedReportStatUnit<Integer>> mtPointReduces = pointReportDao.getMTPointReduceStats(companyId, startDate,
                    endDate);
            TimeBasedReportStatsBase<Integer> pointStat = new TimeBasedReportStatsBase<Integer>();
            pointStat.setDesc("积分消耗");
            int pointTotalMeasures = 0;
            TimeBasedReportStatsBase<Integer> callStat = new TimeBasedReportStatsBase<Integer>();
            callStat.setDesc("请求次数");
            int callTotalMeasures = 0;
            TimeBasedReportStatsBase<Integer> userStat = new TimeBasedReportStatsBase<Integer>();
            userStat.setDesc("用户数量");
            int userTotalMeasures = 0;

            for (TimeBasedReportStatUnit<Integer> stat : mtPointReduces) {
                if (stat.getDesc().equals("point")) {
                    pointStat.getTimeseries().add(stat.getHour());
                    pointStat.getMeasures().add(stat.getMeasure());
                    pointTotalMeasures += stat.getMeasure();
                } else if (stat.getDesc().equals("call")) {
                    callStat.getTimeseries().add(stat.getHour());
                    callStat.getMeasures().add(stat.getMeasure());
                    callTotalMeasures += stat.getMeasure();
                } else {
                    userStat.getTimeseries().add(stat.getHour());
                    userStat.getMeasures().add(stat.getMeasure());
                    userTotalMeasures += stat.getMeasure();
                }
            }
            
            pointStat.setTotalMeasure(pointTotalMeasures);
            callStat.setTotalMeasure(callTotalMeasures);
            userStat.setTotalMeasure(userTotalMeasures);

            result.getHourScan().add(pointStat);
            result.getHourScan().add(callStat);
            result.getHourScan().add(userStat);

        } catch (Exception ex) {
            String requestParam = "CompanyId: " + companyId + ", StartDate: " + startDate
                    + ", EndDate: " + endDate;
            AppEvents.LogException(ex, AppEvents.ScanReportService.DateRangeScanReportServiceException, requestParam);

            throw new ServiceException(AppEvents.ServerExceptionErr);
        }

        return result;
    }

}
