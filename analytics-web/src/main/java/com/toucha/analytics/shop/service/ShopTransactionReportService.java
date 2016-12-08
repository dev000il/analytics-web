package com.toucha.analytics.shop.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.toucha.analytics.common.exceptions.ServiceException;
import com.toucha.analytics.common.model.BaseRequestNormal;
import com.toucha.analytics.common.model.EnterLotteryDailyStats;
import com.toucha.analytics.common.model.EnterLotteryDailyStatsBase;
import com.toucha.analytics.common.model.ResponseReportStats;
import com.toucha.analytics.common.model.ScanStats;
import com.toucha.analytics.common.model.TimeBasedReportStatUnit;
import com.toucha.analytics.common.model.TimeBasedReportStatsBase;
import com.toucha.analytics.common.shop.dao.ShopPointReportDao;
import com.toucha.analytics.common.shop.dao.ShopTransactionReportDao;
import com.toucha.analytics.common.util.AppEvents;
import com.toucha.analytics.common.util.QueryResultHelper;
import com.toucha.analytics.utils.MdRewardSub;
import com.toucha.analytics.utils.ResponseReportUtil;
import com.toucha.platform.common.enums.TransactionType;

/**
 * This class design for all transaction event report services
 * 
 * @author senhui.li
 */
@Service("shopTranReportService")
public class ShopTransactionReportService {

    public static final int SCAN_POINT_REWARD = -2;
    public static final int SCAN_SHARING_POINT = -3;

    private ShopPointReportDao pointReportDao = new ShopPointReportDao();
    private ShopTransactionReportDao tranReportDao = new ShopTransactionReportDao();

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
    public ScanStats<TimeBasedReportStatsBase<Integer>> getTyPointGenerateStatistics(int companyId, List<Long> distributorIds,
            List<Integer> promotionIds, List<String> orderNumbers, Date startDate, Date endDate) throws ServiceException {
        ScanStats<TimeBasedReportStatsBase<Integer>> result = new ScanStats<>();

        TimeBasedReportStatsBase<Integer> scanStats = new TimeBasedReportStatsBase<>();
        int scanMeasures = 0;
        scanStats.setDesc("抽奖积分");
        TimeBasedReportStatsBase<Integer> basicStats = new TimeBasedReportStatsBase<>();
        int basicMeasures = 0;
        basicStats.setDesc("基础积分");
        TimeBasedReportStatsBase<Integer> totalStats = new TimeBasedReportStatsBase<>();
        Map<Date, Integer> total = new TreeMap<>();
        int totalMeasures = 0;
        totalStats.setDesc("总计");

        try {
            List<TimeBasedReportStatUnit<Integer>> tyPointGenerate = pointReportDao.getTYGeneratePointStats(companyId,
                    distributorIds, promotionIds, orderNumbers, startDate,
                    endDate);
            for (TimeBasedReportStatUnit<Integer> typg : tyPointGenerate) {
                if (typg.getDesc().equals(QueryResultHelper.BASIC_POINT_TYPE)) {
                    basicStats.getTimeseries().add(typg.getHour());
                    basicStats.getMeasures().add(typg.getMeasure());
                    basicMeasures += typg.getMeasure().intValue();
                } else {
                    scanStats.getTimeseries().add(typg.getHour());
                    scanStats.getMeasures().add(typg.getMeasure());
                    scanMeasures += typg.getMeasure().intValue();
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
            totalStats.setTotalMeasure(totalMeasures);

            result.getHourScan().add(scanStats);
            result.getHourScan().add(basicStats);
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
    public ScanStats<TimeBasedReportStatsBase<Integer>> getTyPointReduceStatistics(int companyId, List<Integer> promIds,
            Date startDate, Date endDate) throws ServiceException {
        ScanStats<TimeBasedReportStatsBase<Integer>> result = new ScanStats<TimeBasedReportStatsBase<Integer>>();

        Map<String, TimeBasedReportStatsBase<Integer>> stats = new HashMap<>();
        TimeBasedReportStatsBase<Integer> totalStats = new TimeBasedReportStatsBase<Integer>();
        try {
            List<TimeBasedReportStatUnit<Integer>> pointReduces = tranReportDao.getLotteryPointReduceStatistics(companyId,
                    promIds, startDate.getTime(), endDate.getTime());
            List<Date> dateDims = new ArrayList<Date>();
            List<Integer> totalMeasures = new ArrayList<Integer>();
            int totalMeasure = 0;
            totalStats.setDesc("总计");
            for (TimeBasedReportStatUnit<Integer> typr : pointReduces) {
                TimeBasedReportStatsBase<Integer> stat = stats.get(typr.getDesc());
                if (stat == null) {
                    stat = new TimeBasedReportStatsBase<>();
                    TransactionType tt = TransactionType.getType(Integer.parseInt(typr.getDesc()));
                    String desc = "未知";
                    switch (tt) {
                    case ExchangeMallReward:
                        desc = "商城兑换";
                        break;
                    case PointGame:
                        desc = "积分游戏";
                        break;
                    case PointLottery:
                        desc = "积分抽奖";
                        break;
                    case CrowdfundingActivity:
                        desc = "众筹活";
                        break;
                    case PresentedPoints:
                        desc = "积分赠送";
                        break;
                    default:
                        desc = "其它";
                        break;
                    }
                    stat.setDesc(desc);
                }

                stat.getMeasures().add(typr.getMeasure());
                stat.getTimeseries().add(typr.getHour());
                int total = stat.getTotalMeasure() == null ? 0 : stat.getTotalMeasure().intValue();
                stat.setTotalMeasure(total + typr.getMeasure());
                stats.put(typr.getDesc(), stat);

                dateDims.add(typr.getHour());
                totalMeasures.add(typr.getMeasure());
                totalMeasure += typr.getMeasure();
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
            String requestParam = "CompanyId: " + companyId + ", lotteryIds: " + promIds + ", StartDate: " + startDate
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

    public ResponseReportStats<Integer> getLotteryRewardStatistics(int companyId, List<Integer> lotteryIds,
            List<Integer> rewardIds, Date startDate, Date endDate) throws ServiceException {
        ResponseReportStats<Integer> result = new ResponseReportStats<>();

        try {
            List<TimeBasedReportStatUnit<Integer>> lotRewards = pointReportDao.getPointLotteryRewardStats(companyId, lotteryIds,
                    rewardIds, startDate, endDate);
            if (lotRewards == null || lotRewards.isEmpty()) {
                return result;
            }

            result = ResponseReportUtil.mergeAndFriendlyShow(lotRewards, MdRewardSub.rewardMapping);

        } catch (Exception ex) {
            String requestParam = "CompanyId: " + companyId + ", lotteryIds: " + lotteryIds + "rewardIds: " + rewardIds
                    + ", StartDate: " + startDate + ", EndDate: " + endDate;
            AppEvents.LogException(ex, AppEvents.ScanReportService.DateRangeScanReportServiceException, requestParam);

            throw new ServiceException(AppEvents.ServerExceptionErr);
        }

        return result;
    }

    public ResponseReportStats<Integer> getClaimRewardStatistics(int companyId, List<Integer> lotteryIds,
            List<Integer> rewardIds, Date startDate, Date endDate) throws ServiceException {
        ResponseReportStats<Integer> result = new ResponseReportStats<>();

        try {
            List<TimeBasedReportStatUnit<Integer>> lotRewards = pointReportDao.getClaimLotteryRewardStats(companyId, lotteryIds,
                    rewardIds, startDate, endDate);

            if (lotRewards == null || lotRewards.isEmpty()) {
                return result;
            }

            result = ResponseReportUtil.mergeAndFriendlyShow(lotRewards, MdRewardSub.rewardMapping);
        } catch (Exception ex) {
            String requestParam = "CompanyId: " + companyId + ", lotteryIds: " + lotteryIds + "rewardIds: " + rewardIds
                    + ", StartDate: " + startDate + ", EndDate: " + endDate;
            AppEvents.LogException(ex, AppEvents.ScanReportService.DateRangeScanReportServiceException, requestParam);

            throw new ServiceException(AppEvents.ServerExceptionErr);
        }

        return result;
    }
}
