package com.toucha.analytics.shop.service;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.toucha.analytics.common.azure.AzureOperation;
import com.toucha.analytics.common.exceptions.ServiceException;
import com.toucha.analytics.common.model.BaseRequestNormal;
import com.toucha.analytics.common.model.EnterLotteryDailyStats;
import com.toucha.analytics.common.model.EnterLotteryDailyStatsBase;
import com.toucha.analytics.common.model.Mgmevent;
import com.toucha.analytics.common.model.PromotionRewardAmountStats;
import com.toucha.analytics.common.model.PromotionRewardAmountStatsBase;
import com.toucha.analytics.common.model.ResponseReportStats;
import com.toucha.analytics.common.model.ScanGeoStats;
import com.toucha.analytics.common.model.ScanLotteryCount;
import com.toucha.analytics.common.model.TimeBasedReportStatUnit;
import com.toucha.analytics.common.model.TimeBasedReportStats;
import com.toucha.analytics.common.model.TimeBasedReportStatsBase;
import com.toucha.analytics.common.shop.dao.ShopDataSet;
import com.toucha.analytics.common.shop.dao.ShopPromotionDao;
import com.toucha.analytics.common.shop.dao.ShopScanReportDao;
import com.toucha.analytics.common.shop.dao.ShopScanlogDao;
import com.toucha.analytics.common.util.AppEvents;
import com.toucha.analytics.common.util.ZipUtil;
import com.toucha.analytics.model.request.GenerateReportCsvRequest;
import com.toucha.analytics.model.request.PromoActivityReportRequest;
import com.toucha.analytics.model.request.PromotionRewardReportRequest;
import com.toucha.analytics.model.request.PromotionRewardRequest;
import com.toucha.analytics.model.response.CityCountResponse;
import com.toucha.analytics.model.response.GeoScanResponse;
import com.toucha.analytics.model.response.GeoStateScanReponse;
import com.toucha.analytics.model.response.NameValuePair;
import com.toucha.analytics.model.response.ProductCountResponse;
import com.toucha.analytics.model.response.StateCountResponse;
import com.toucha.analytics.service.GeoReportService;
import com.toucha.analytics.utils.MdProductSub;
import com.toucha.analytics.utils.MdRewardSub;
import com.toucha.analytics.utils.ResponseReportUtil;
import com.toucha.analytics.utils.reporting.ShopFriendlyLabelDisplay;
import com.toucha.platform.common.enums.RequestRewardsEnum;
import com.toucha.platform.common.enums.RewardType;
import com.toucha.platform.common.enums.UserScanType;

@Service("shopScanReportService")
public class ShopScanReportService {

    private ShopScanReportDao scanReportDao = new ShopScanReportDao();

    private ShopScanlogDao scanlogDao = new ShopScanlogDao();

    private ShopPromotionDao promotionDao = new ShopPromotionDao();

    @Autowired
    private GeoReportService geoReportService;

    public List<Mgmevent> getUserMgmevents(int c, String u, Date startTime, Date endTime) {
        return scanlogDao.findUserMgmevents(c, u, startTime, endTime);
    }

    public List<TimeBasedReportStatsBase<Integer>> getScanStatistics(int companyId, List<Long> distributorIds,
            List<Integer> promotionIds, List<String> orderNumbers, Date startDate, Date endDate) throws ServiceException {
        List<TimeBasedReportStatsBase<Integer>> result = new ArrayList<>();

        try {
            List<TimeBasedReportStatUnit<Integer>> scanStatUnits = scanReportDao.getDateRangeStatistics(companyId, distributorIds,
                    promotionIds, orderNumbers, startDate, endDate);
            TimeBasedReportStatsBase<Integer> scanStats = new TimeBasedReportStatsBase<>();
            List<Date> hourSerious = new ArrayList<>();
            List<Integer> measures = new ArrayList<>();
            int totalMeasure = 0;

            TimeBasedReportStatsBase<Integer> tagStats = new TimeBasedReportStatsBase<>();
            List<Date> tagHourSerious = new ArrayList<>();
            List<Integer> tagmeasures = new ArrayList<>();
            int totalTagMeasure = 0;

            for (TimeBasedReportStatUnit<Integer> unit : scanStatUnits) {
                if (unit.getDesc().equals("scancnt")) {
                    hourSerious.add(unit.getHour());
                    measures.add(unit.getMeasure());
                    totalMeasure += unit.getMeasure();
                } else {
                    tagmeasures.add(unit.getMeasure());
                    tagHourSerious.add(unit.getHour());
                    totalTagMeasure += unit.getMeasure();
                }
            }
            scanStats.setTimeseries(hourSerious);
            scanStats.setMeasures(measures);
            scanStats.setTotalMeasure(totalMeasure);
            scanStats.setDesc("扫码量");

            tagStats.setTimeseries(hourSerious);
            tagStats.setMeasures(tagmeasures);
            tagStats.setTotalMeasure(totalTagMeasure);
            tagStats.setDesc("被扫标签个数");

            result.add(scanStats);
            result.add(tagStats);

        } catch (Exception ex) {
            String requestParam = "CompanyId: " + companyId + ", distributorIds: " + distributorIds + ", promotionIds: "
                    + promotionIds + ", orderNumbers: " + orderNumbers + ", StartDate: " + startDate + ", EndDate: " + endDate;
            AppEvents.LogException(ex, AppEvents.ScanReportService.DateRangeScanReportServiceException, requestParam);

            throw new ServiceException(AppEvents.ServerExceptionErr);
        }

        return result;
    }

    public GeoScanResponse getGeoReport(int companyId, List<Integer> pids, Date startDate, Date endDate) throws ServiceException {

        GeoScanResponse result = new GeoScanResponse();

        try {
            Map<String, Integer> cityStats = new HashMap<String, Integer>();
            Map<String, Integer> stateStats = new HashMap<String, Integer>();
            List<ScanGeoStats> scanGeoStats = scanReportDao.getScanGeoStatistics(companyId, pids, startDate, endDate);
            for (ScanGeoStats s : scanGeoStats) {
                String city = s.getCity();// .replace("市", "");
                String state = s.getState();// .replace("省", "");

                stateStats.put(state, stateStats.containsKey(state) ? s.getCount() + stateStats.get(state) : s.getCount());
                cityStats.put(city, cityStats.containsKey(city) ? s.getCount() + cityStats.get(city) : s.getCount());

            }

            List<NameValuePair> cityList = new ArrayList<NameValuePair>();
            List<NameValuePair> stateList = new ArrayList<NameValuePair>();

            for (String key : cityStats.keySet()) {
                if (key.equals("")) {
                    result.setCityEmpty(cityStats.get(key));
                } else {
                    cityList.add(new NameValuePair(key, cityStats.get(key)));
                }
            }

            for (String key : stateStats.keySet()) {
                if (key.equals("")) {
                    result.setStateEmpty(stateStats.get(key));
                } else {
                    stateList.add(new NameValuePair(key, stateStats.get(key)));
                }
            }
            result.setCityStats(cityList);
            result.setStateStats(stateList);

        } catch (Exception ex) {
            String requestParam = "CompanyId: " + companyId + ", ProductIds: " + pids + ", StartDate: " + startDate
                    + ", EndDate: " + endDate;
            AppEvents.LogException(ex, AppEvents.ScanReportService.GeoScanReportServiceException, requestParam);

            throw new ServiceException(AppEvents.ServerExceptionErr);
        }

        return result;
    }

    public GeoStateScanReponse getGeoReportNew(int companyId, List<Integer> pids, Date startDate, Date endDate)
            throws ServiceException {
        GeoStateScanReponse result = new GeoStateScanReponse();
        try {
            List<ScanGeoStats> scanGeoStats = scanReportDao.getScanGeoStatistics(companyId, pids, startDate, endDate);
            result = geoReportService.getGeoCityStatics(scanGeoStats);
        } catch (SQLException e) {
            throw new ServiceException(e);
        }

        return result;
    }

    @SuppressWarnings("unused")
    private List<TimeBasedReportStatsBase<Integer>> getEffectiveScanStatistics(int companyId, List<Integer> productIds,
            Date start, Date end) throws ServiceException {

        // there will be three lines: old, new, total
        List<TimeBasedReportStatsBase<Integer>> result = new ArrayList<TimeBasedReportStatsBase<Integer>>();
        TimeBasedReportStatsBase<Integer> oldMember = new TimeBasedReportStatsBase<Integer>();
        TimeBasedReportStatsBase<Integer> newMember = new TimeBasedReportStatsBase<Integer>();
        TimeBasedReportStatsBase<Integer> totalMember = new TimeBasedReportStatsBase<Integer>();

        try {
            List<TimeBasedReportStatUnit<Integer>> memberStats = scanReportDao.getEffectiveScanStats(companyId, productIds,
                    new ArrayList<Integer>(), start, end);

            Map<Date, int[]> tmpStat = new TreeMap<Date, int[]>();
            for (TimeBasedReportStatUnit<Integer> unit : memberStats) {
                Date hour = unit.getHour();
                if (!tmpStat.containsKey(hour)) {
                    int[] hourStat = new int[2];
                    hourStat[0] = hourStat[1] = 0;
                    if (unit.getDesc().equals("1")) {
                        hourStat[0] = unit.getMeasure();
                    } else if (unit.getDesc().equals("3")) {
                        hourStat[1] = unit.getMeasure();
                    }
                    tmpStat.put(hour, hourStat);
                } else {
                    int[] hourStat = tmpStat.get(hour);
                    if (unit.getDesc().equals("1")) {
                        hourStat[0] = unit.getMeasure();
                    } else if (unit.getDesc().equals("3")) {
                        hourStat[1] = unit.getMeasure();
                    }
                }
            }

            List<Date> dateDim = new ArrayList<Date>();
            List<Integer> oldMemberMeasure = new ArrayList<Integer>();
            List<Integer> newMemberMeasure = new ArrayList<Integer>();
            List<Integer> totalMemberMeasure = new ArrayList<Integer>();
            int totalOldMemberMeasure = 0;
            int totalNewMemberMeasure = 0;
            int totalMeasure = 0;
            for (Date key : tmpStat.keySet()) {
                dateDim.add(key);
                int[] hourStat = tmpStat.get(key);
                oldMemberMeasure.add(hourStat[0]);
                newMemberMeasure.add(hourStat[1]);
                totalMemberMeasure.add(hourStat[0] + hourStat[1]);
                totalOldMemberMeasure += hourStat[0];
                totalNewMemberMeasure += hourStat[1];
                totalMeasure += (hourStat[1] + hourStat[1]);
            }

            oldMember.setDesc("老用户扫码量");
            oldMember.setTimeseries(dateDim);
            oldMember.setMeasures(oldMemberMeasure);
            oldMember.setTotalMeasure(totalOldMemberMeasure);

            newMember.setDesc("新用户扫码量");
            newMember.setTimeseries(dateDim);
            newMember.setMeasures(newMemberMeasure);
            newMember.setTotalMeasure(totalNewMemberMeasure);

            totalMember.setDesc("有效扫码量");
            totalMember.setTimeseries(dateDim);
            totalMember.setMeasures(totalMemberMeasure);
            totalMember.setTotalMeasure(totalMeasure);

            result.add(oldMember);
            result.add(newMember);
            result.add(totalMember);
        } catch (Exception e) {
            AppEvents.LogException(e, AppEvents.ScanReportService.MemberReportServiceError, String.valueOf(companyId),
                    start.toString(), end.toString());
            throw new ServiceException(e);
        }

        return result;
    }

    public TimeBasedReportStats<Integer> getEffectiveScanStatisticsUserType(PromotionRewardReportRequest request)
            throws ServiceException {
        TimeBasedReportStats<Integer> response = new TimeBasedReportStats<>();

        try {
            List<TimeBasedReportStatUnit<Integer>> enterlotteryStats = scanReportDao.getEffectiveScanStats(
                    request.getRequestHeader().getCompanyId(), request.getProductIds(), request.getPromotionIds(),
                    request.getStartDate(), request.getEndDate());

            String userType = "0";
            TimeBasedReportStatsBase<Integer> currentUserTypeStat = null;
            for (TimeBasedReportStatUnit<Integer> enterLotteryUnit : enterlotteryStats) {
                if (!enterLotteryUnit.getDesc().equals(userType)) {
                    if (currentUserTypeStat != null) {
                        response.getHourScan().add(currentUserTypeStat);
                    }
                    currentUserTypeStat = new TimeBasedReportStatsBase<>();
                    currentUserTypeStat.setMeasures(new ArrayList<Integer>());
                    currentUserTypeStat.setTimeseries(new ArrayList<Date>());
                    userType = enterLotteryUnit.getDesc();
                    currentUserTypeStat.setDesc(enterLotteryUnit.getDesc());
                    currentUserTypeStat.setTotalMeasure(0);
                }
                currentUserTypeStat.getTimeseries().add(enterLotteryUnit.getHour());
                currentUserTypeStat.getMeasures().add(enterLotteryUnit.getMeasure());
                currentUserTypeStat.setTotalMeasure(
                        currentUserTypeStat.getTotalMeasure().intValue() + enterLotteryUnit.getMeasure().intValue());
            }
            response.getHourScan().add(currentUserTypeStat);

            for (TimeBasedReportStatsBase<Integer> stat : response.getHourScan()) {
                if (stat.getDesc().equals(String.valueOf(UserScanType.NewUserScan.getId()))) {
                    stat.setDesc("新用户扫码量");
                } else if (stat.getDesc().equals(String.valueOf(UserScanType.OldUserScan.getId()))) {
                    stat.setDesc("老用户扫码量");
                }
            }

            Map<Date, Integer> total = new TreeMap<Date, Integer>();
            for (TimeBasedReportStatsBase<Integer> els : response.getHourScan()) {
                Date d = null;
                for (int i = 0; i < els.getTimeseries().size(); i++) {
                    d = els.getTimeseries().get(i);
                    if (total.containsKey(d)) {
                        total.put(d, total.get(d) + els.getMeasures().get(i));
                    } else {
                        total.put(d, els.getMeasures().get(i));
                    }
                }
            }

            /*
            List<Map.Entry<Date, Integer>> mHashMapEntryList = new ArrayList<>(total.entrySet());
            Collections.sort(mHashMapEntryList, new Comparator<Map.Entry<Date,Integer>>() {
            
            	@Override
            	public int compare(Entry<Date, Integer> o1, Entry<Date, Integer> o2) {
            		return o1.getKey().compareTo(o2.getKey());
            	}
            	
            });
            */

            TimeBasedReportStatsBase<Integer> totalStats = new TimeBasedReportStatsBase<>();
            int totalMeasure = 0;
            for (Entry<Date, Integer> eltu : total.entrySet()) {
                totalStats.getTimeseries().add(eltu.getKey());
                totalStats.getMeasures().add(eltu.getValue());
                totalMeasure += eltu.getValue();
            }
            totalStats.setTotalMeasure(totalMeasure);
            totalStats.setDesc("Total");
            response.getHourScan().add(totalStats);
        } catch (Exception e) {
            AppEvents.LogException(e, AppEvents.ScanReportService.MemberReportServiceError, JSON.toJSONString(request));
            throw new ServiceException(e);
        }

        return response;
    }

    public ShopDataSet getRawActivities(PromoActivityReportRequest request) throws ServiceException {

        ShopPromoActivityRequestContext context = new ShopPromoActivityRequestContext(request.getFields());

        try {
            ShopDataSet data = scanlogDao.findTopScanlogs(request.getRequestHeader().getCompanyId(), request.getProductIds(),
                    request.getStartDate(), request.getEndDate(), context.getHbaseQueryFields(),
                    request.getRequestedDAORecordsCount());

            data.replaceValuesWithNameMap(request.getNameMap());
            ShopFriendlyLabelDisplay.executeRules(data);

            ShopDataSet result = context.truncateDataset(context.compressDataset(data));
            return result;

        } catch (Exception e) {
            AppEvents.LogException(e, AppEvents.ScanReportService.RawActivitiesQueryException, request.toString());

            throw new ServiceException(AppEvents.ServerExceptionErr);
        }
    }

    public Pair<String, String> getRawActivitiesAndCreateCsv(GenerateReportCsvRequest request) throws ServiceException {

        Pair<String, String> blobUrls = null;

        try {
            ShopDataSet activities = getRawActivities(request);
            Pair<String, String> filePaths = createActivitiesCsv(activities, request.getRequestHeader().getRequestId());
            if (StringUtils.isNotBlank(filePaths.getLeft())) {
                String requestId = request.getRequestHeader().getRequestId();
                DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
                String zipBlobName = dateFormat.format(Calendar.getInstance().getTime()) + "_" + requestId + ".zip";
                String downloadUrl = AzureOperation.uploadFileToBlob(filePaths.getLeft(), zipBlobName, request.getEnvironment());
                String pngBlobName = requestId + ".png";
                String pngDownloadUrl = AzureOperation.uploadFileToBlob(filePaths.getRight(), pngBlobName,
                        request.getEnvironment());
                blobUrls = Pair.of(downloadUrl, pngDownloadUrl);

                // remove temporary zip and png
                File zipFile = new File(filePaths.getLeft());
                zipFile.delete();
                File pngFile = new File(filePaths.getRight());
                pngFile.delete();
            }

        } catch (Exception ex) {
            blobUrls = Pair.of("", "");
            AppEvents.LogException(ex, AppEvents.ScanReportService.RawActivitiesQueryException, request.toString());

            throw new ServiceException(AppEvents.ServerExceptionErr);
        }

        return blobUrls;
    }

    public PromotionRewardAmountStats<BigDecimal> getPromotionRewardAmountStats(PromotionRewardReportRequest request)
            throws ServiceException {

        PromotionRewardAmountStats<BigDecimal> response = new PromotionRewardAmountStats<BigDecimal>();

        List<PromotionRewardAmountStatsBase<BigDecimal>> result = new ArrayList<PromotionRewardAmountStatsBase<BigDecimal>>();
        PromotionRewardAmountStatsBase<BigDecimal> phoneTopup = new PromotionRewardAmountStatsBase<BigDecimal>();
        PromotionRewardAmountStatsBase<BigDecimal> unionPay = new PromotionRewardAmountStatsBase<BigDecimal>();
        PromotionRewardAmountStatsBase<BigDecimal> wechat = new PromotionRewardAmountStatsBase<BigDecimal>();
        PromotionRewardAmountStatsBase<BigDecimal> total = new PromotionRewardAmountStatsBase<BigDecimal>();

        try {
            List<TimeBasedReportStatUnit<BigDecimal>> promotionStats = promotionDao.getRewards(
                    request.getRequestHeader().getCompanyId(), request.getProductIds(), request.getPromotionIds(),
                    request.getStartDate(), request.getEndDate());

            List<Date> dateDim = new ArrayList<Date>();
            List<BigDecimal> phoneTopupMeasure = new ArrayList<BigDecimal>();
            List<BigDecimal> unionPayMeasure = new ArrayList<BigDecimal>();
            List<BigDecimal> wechatMeasure = new ArrayList<BigDecimal>();
            List<BigDecimal> totalMeasure = new ArrayList<BigDecimal>();

            BigDecimal totalPhoneTopupMeasure = new BigDecimal(0);
            BigDecimal totalUnionPayMeasure = new BigDecimal(0);
            BigDecimal totalWechatMeasure = new BigDecimal(0);
            BigDecimal totalTotMeasure = new BigDecimal(0);

            boolean hasPhoneTopup = false;
            boolean hasUnionPay = false;
            boolean hasWechat = false;

            Map<Date, BigDecimal[]> claimedRewards = new TreeMap<Date, BigDecimal[]>();

            for (TimeBasedReportStatUnit<BigDecimal> claim : promotionStats) {
                Date hourDim = claim.getHour();
                BigDecimal claimedAmount = claim.getMeasure();
                String rewardType = claim.getDesc();

                if (!claimedRewards.containsKey(hourDim)) {
                    BigDecimal[] rewards = new BigDecimal[3]; // [0] phonetopup, [1] unionpay, [2] wechat
                    rewards[0] = new BigDecimal(0);
                    rewards[1] = new BigDecimal(0);
                    rewards[2] = new BigDecimal(0);
                    claimedRewards.put(hourDim, rewards);
                }

                if (rewardType.equals(RewardType.PhoneBill.getValue())) {
                    hasPhoneTopup = true;
                    claimedRewards.get(hourDim)[0] = claimedRewards.get(hourDim)[0].add(claimedAmount);
                } else if (rewardType.equals(RewardType.UnionPay.getValue())) {
                    hasUnionPay = true;
                    claimedRewards.get(hourDim)[1] = claimedRewards.get(hourDim)[1].add(claimedAmount);
                } else if (rewardType.equals(RewardType.WeChat.getValue())) {
                    hasWechat = true;
                    claimedRewards.get(hourDim)[2] = claimedRewards.get(hourDim)[2].add(claimedAmount);
                }
            }

            for (Entry<Date, BigDecimal[]> hourStats : claimedRewards.entrySet()) {
                dateDim.add(hourStats.getKey());

                BigDecimal hourlyPhoneTopup = hourStats.getValue()[0];
                BigDecimal hourlyUnionPay = hourStats.getValue()[1];
                BigDecimal hourlyWechat = hourStats.getValue()[2];

                BigDecimal hourlyTotal = hourlyPhoneTopup.add(hourlyUnionPay).add(hourlyWechat);

                phoneTopupMeasure.add(hourlyPhoneTopup);
                unionPayMeasure.add(hourlyUnionPay);
                wechatMeasure.add(hourlyWechat);
                totalMeasure.add(hourlyTotal);

                totalPhoneTopupMeasure = totalPhoneTopupMeasure.add(hourlyPhoneTopup);
                totalUnionPayMeasure = totalUnionPayMeasure.add(hourlyUnionPay);
                totalWechatMeasure = totalWechatMeasure.add(hourlyWechat);
                totalTotMeasure = totalTotMeasure.add(hourlyTotal);
            }

            if (hasPhoneTopup) {
                phoneTopup.setRewardType("话费充值");
                phoneTopup.setTimeseries(dateDim);
                phoneTopup.setMeasures(phoneTopupMeasure);
                phoneTopup.setTotalMeasure(totalPhoneTopupMeasure);

                result.add(phoneTopup);
            }

            if (hasUnionPay) {
                unionPay.setRewardType("银联提现");
                unionPay.setTimeseries(dateDim);
                unionPay.setMeasures(unionPayMeasure);
                unionPay.setTotalMeasure(totalUnionPayMeasure);

                result.add(unionPay);
            }

            if (hasWechat) {
                wechat.setRewardType("微信红包");
                wechat.setTimeseries(dateDim);
                wechat.setMeasures(wechatMeasure);
                wechat.setTotalMeasure(totalWechatMeasure);

                result.add(wechat);
            }

            total.setRewardType("总共");
            total.setTimeseries(dateDim);
            total.setMeasures(totalMeasure);
            total.setTotalMeasure(totalTotMeasure);

            result.add(total);

            response.setIds(new ArrayList<List<String>>());
            response.setHourScan(result);
        } catch (Exception e) {
            AppEvents.LogException(e, AppEvents.ScanReportService.FetchPromotionDataServiceError, JSON.toJSONString(request));
            throw new ServiceException(e);
        }

        return response;
    }

    public PromotionRewardAmountStats<Integer> getPromotionRewardPointStats(PromotionRewardRequest request)
            throws ServiceException {

        PromotionRewardAmountStats<Integer> response = new PromotionRewardAmountStats<Integer>();

        try {
            List<TimeBasedReportStatUnit<Integer>> pointData = promotionDao.getPoints(request.getRequestHeader().getCompanyId(),
                    request.getProductIds(), request.getStartDate(), request.getEndDate());

            if (pointData != null && pointData.size() > 0) {
                PromotionRewardAmountStatsBase<Integer> pointStats = new PromotionRewardAmountStatsBase<Integer>();

                List<Date> timeSeries = new ArrayList<Date>();
                List<Integer> points = new ArrayList<Integer>();

                int totalPoints = 0;
                for (TimeBasedReportStatUnit<Integer> hourlyPoint : pointData) {
                    totalPoints += hourlyPoint.getMeasure();

                    points.add(hourlyPoint.getMeasure());
                    timeSeries.add(hourlyPoint.getHour());
                }

                pointStats.setMeasures(points);
                pointStats.setRewardType("积分");
                pointStats.setTimeseries(timeSeries);
                pointStats.setTotalMeasure(totalPoints);

                List<PromotionRewardAmountStatsBase<Integer>> supportedMeasures = new ArrayList<PromotionRewardAmountStatsBase<Integer>>();
                supportedMeasures.add(pointStats);

                response.setHourScan(supportedMeasures);
                List<List<String>> ids = new ArrayList<List<String>>();
                response.setIds(ids);
            }

        } catch (Exception e) {
            AppEvents.LogException(e, AppEvents.ScanReportService.FetchPointDataServiceError, JSON.toJSONString(request));

            throw new ServiceException(AppEvents.ServerExceptionErr);
        }

        return response;
    }

    public ResponseReportStats<Integer> getRewards(PromotionRewardReportRequest request, RequestRewardsEnum requestRewardsEnum)
            throws ServiceException {
        ResponseReportStats<Integer> response = null;

        try {
            List<TimeBasedReportStatUnit<Integer>> cd = promotionDao.getRewards(request.getRequestHeader().getCompanyId(),
                    request.getDistributorIds(), request.getOrderIds(), request.getProductIds(), request.getPromotionIds(),
                    request.getRewardIds(), request.getStartDate(), request.getEndDate(), true, requestRewardsEnum);

            if (cd == null || cd.size() == 0) {
                return new ResponseReportStats<Integer>();
            }

            response = ResponseReportUtil.mergeAndFriendlyShow(cd, MdRewardSub.rewardMapping);
        } catch (Exception ex) {
            AppEvents.LogException(ex, AppEvents.ScanReportService.FetchPromotionDataServiceError, JSON.toJSONString(request));
            throw new ServiceException(AppEvents.ScanReportService.FetchPromotionDataServiceError);
        }

        return response;
    }

    public ResponseReportStats<Integer> getProducts(PromotionRewardReportRequest request, RequestRewardsEnum requestRewardsEnum)
            throws ServiceException {
        ResponseReportStats<Integer> response = new ResponseReportStats<>();

        try {
            List<TimeBasedReportStatUnit<Integer>> cd = promotionDao.getProducts(request.getRequestHeader().getCompanyId(),
                    request.getDistributorIds(), request.getOrderIds(), request.getProductIds(), request.getPromotionIds(),
                    request.getRewardIds(), request.getStartDate(), request.getEndDate(), true, requestRewardsEnum);

            if (cd == null || cd.size() == 0) {
                return response;
            }

            response = ResponseReportUtil.mergeAndFriendlyShow(cd, MdProductSub.productMapping);

        } catch (Exception ex) {
            AppEvents.LogException(ex, AppEvents.ScanReportService.FetchPromotionDataServiceError, JSON.toJSONString(request));
            throw new ServiceException(AppEvents.ScanReportService.FetchPromotionDataServiceError);
        }

        return response;
    }

    public TimeBasedReportStats<Integer> getEnterLotteryStatistics(PromotionRewardReportRequest request) throws ServiceException {

        TimeBasedReportStats<Integer> response = new TimeBasedReportStats<>();

        try {
            List<TimeBasedReportStatUnit<Integer>> enterlotteryStats = promotionDao.getEnterLotteryStats(
                    request.getRequestHeader().getCompanyId(), request.getDistributorIds(), request.getPromotionIds(),
                    request.getOrderIds(), request.getStartDate(), request.getEndDate());

            if (enterlotteryStats == null || enterlotteryStats.size() == 0) {
                return response;
            }

            String reward = "-1";
            TimeBasedReportStatsBase<Integer> currentEnterLotteryStat = null;
            for (TimeBasedReportStatUnit<Integer> enterLotteryUnit : enterlotteryStats) {
                if (!enterLotteryUnit.getDesc().equals(reward)) {
                    if (currentEnterLotteryStat != null) {
                        response.getHourScan().add(currentEnterLotteryStat);
                    }
                    currentEnterLotteryStat = new TimeBasedReportStatsBase<>();
                    currentEnterLotteryStat.setMeasures(new ArrayList<Integer>());
                    currentEnterLotteryStat.setTimeseries(new ArrayList<Date>());
                    reward = enterLotteryUnit.getDesc();
                    currentEnterLotteryStat.setDesc(enterLotteryUnit.getDesc());
                    currentEnterLotteryStat.setTotalMeasure(0);
                }
                currentEnterLotteryStat.getTimeseries().add(enterLotteryUnit.getHour());
                currentEnterLotteryStat.getMeasures().add(enterLotteryUnit.getMeasure());
                currentEnterLotteryStat.setTotalMeasure(
                        currentEnterLotteryStat.getTotalMeasure().intValue() + enterLotteryUnit.getMeasure().intValue());
            }
            response.getHourScan().add(currentEnterLotteryStat);

            Map<Date, Integer> total = new TreeMap<Date, Integer>();
            for (TimeBasedReportStatsBase<Integer> els : response.getHourScan()) {
                Date d = null;
                for (int i = 0; i < els.getTimeseries().size(); i++) {
                    d = els.getTimeseries().get(i);
                    if (total.containsKey(d)) {
                        total.put(d, total.get(d) + els.getMeasures().get(i));
                    } else {
                        total.put(d, els.getMeasures().get(i));
                    }
                }
            }

            /*
             * List<Map.Entry<Date, Integer>> mHashMapEntryList = new
             * ArrayList<>(total.entrySet());
             * Collections.sort(mHashMapEntryList, new
             * Comparator<Map.Entry<Date,Integer>>() {
             * 
             * @Override public int compare(Entry<Date, Integer> o1, Entry<Date,
             * Integer> o2) { return o1.getKey().compareTo(o2.getKey()); }
             * 
             * });
             */

            TimeBasedReportStatsBase<Integer> totalStats = new TimeBasedReportStatsBase<>();
            int totalMeasure = 0;
            totalStats.setTimeseries(new ArrayList<Date>());
            totalStats.setMeasures(new ArrayList<Integer>());

            for (Entry<Date, Integer> eltu : total.entrySet()) {
                totalStats.getTimeseries().add(eltu.getKey());
                totalStats.getMeasures().add(eltu.getValue());
                totalMeasure += eltu.getValue();
            }
            totalStats.setTotalMeasure(totalMeasure);
            totalStats.setDesc("总计");
            response.getHourScan().add(totalStats);
        } catch (Exception e) {
            AppEvents.LogException(e, AppEvents.ScanReportService.EnterLotteryServiceError, JSON.toJSONString(request));
            throw new ServiceException(AppEvents.ScanReportService.EnterLotteryServiceError);
        }

        return response;
    }

    public EnterLotteryDailyStats getTodayYesterdayEnterLotteryStats(BaseRequestNormal req) throws ServiceException {
        EnterLotteryDailyStats response = new EnterLotteryDailyStats();

        try {
            EnterLotteryDailyStatsBase[] result = promotionDao
                    .getTodayYesterdayEnterLotteyStats(req.getRequestHeader().getCompanyId());

            response.getDailyMeasures().add(result[0]);
            response.getDailyMeasures().add(result[2]);
            response.getDailyMeasures().add(result[1]);
        } catch (Exception e) {
            AppEvents.LogException(e, AppEvents.ScanReportService.EnterLotteryTodayYesterdayServiceError, JSON.toJSONString(req));
            throw new ServiceException(AppEvents.ScanReportService.EnterLotteryTodayYesterdayServiceError);
        }

        return response;
    }

    private Pair<String, String> createActivitiesCsv(ShopDataSet activities, String requestId) throws IOException {

        Pair<String, String> localFilePaths = null;

        String baseFileName = System.getProperty("user.dir") + "/" + requestId;
        String filePath = baseFileName + ".csv";
        String zipFilePath = baseFileName + ".zip";
        String pngFilePath = baseFileName + ".png";

        FileOutputStream stream = null;
        BufferedWriter writer = null;

        List<String[]> dataSet = activities.getData();
        List<String> fields = activities.getHeader();

        try {
            stream = new FileOutputStream(filePath);
            writer = new BufferedWriter(new OutputStreamWriter(stream, "utf-8"));

            // Write out the header
            for (int i = 0; i < fields.size(); i++) {
                writer.write("\"");
                String friendlyName = getHeaderFriendlyName(fields.get(i));
                writer.write(friendlyName);
                writer.write("\"");

                // If not the last cell, add a , delimiter
                if (i < fields.size() - 1) {
                    writer.write(",");
                }
            }

            writer.write("\n");

            // Write out the data
            for (int i = 0; i < dataSet.size(); i++) {
                String[] row = dataSet.get(i);
                for (int j = 0; j < row.length; j++) {
                    writer.write("\"");

                    if (row[j] != null) {
                        // Escape a double: " --> ""
                        row[j] = row[j].replace("\"", "\"\"");
                        writer.write(row[j]);
                    }

                    writer.write("\"");

                    // If not the last cell, add a , delimiter
                    if (j < row.length - 1) {
                        writer.write(",");
                    }
                }

                // If not the last row
                if (i < dataSet.size() - 1) {
                    writer.write("\n");
                }
            }

            writer.close();
            if (!ZipUtil.zip(filePath, zipFilePath)) {
                zipFilePath = "";
            }

            // write auxiliary png for async processing
            createImage(pngFilePath);
            localFilePaths = Pair.of(zipFilePath, pngFilePath);

        } catch (Exception ex) {
            localFilePaths = Pair.of("", "");
            AppEvents.LogException(ex, AppEvents.ScanReportServiceRequests.ErrorCreatingLocalCsv, filePath);

        }

        return localFilePaths;
    }

    private static void createImage(String filePath) throws IOException {
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setBackground(new Color(255, 0, 0));
        g2d.dispose();

        ImageIO.write(image, "png", new File(filePath));
    }

    private static String getHeaderFriendlyName(String field) {
        if (!Strings.isNullOrEmpty(field) && PromoActivityReportRequest.supportedFieldsFriendlyName.containsKey(field)) {
            return PromoActivityReportRequest.supportedFieldsFriendlyName.get(field);
        }

        return field;
    }

    @Cacheable(value = "scanCountCache", key = "'scan'+#companyId+#productIds")
    public List<ProductCountResponse> getScanCount(Integer companyId, String productIds) throws ServiceException {
        long startTime = System.currentTimeMillis();
        List<ScanLotteryCount> list = Lists.newArrayList();
        try {
            list = scanReportDao.getScanCount(companyId, productIds);
        } catch (SQLException e) {
            throw new ServiceException();
        }

        System.out.println("scanCount size is: " + list.size());

        List<ProductCountResponse> productList = dataProcessing(list);

        System.out.println("total spend time is : " + (System.currentTimeMillis() - startTime) + " ms");

        return productList;
    }

    private List<ProductCountResponse> dataProcessing(List<ScanLotteryCount> list) {
        List<ProductCountResponse> productList = new LinkedList<ProductCountResponse>();

        Map<Integer, ProductCountResponse> productMap = new HashMap<Integer, ProductCountResponse>();
        Map<String, StateCountResponse> stateMap = new ConcurrentHashMap<String, StateCountResponse>(128);

        List<CityCountResponse> cityList = new LinkedList<CityCountResponse>();
        List<StateCountResponse> stateList = new LinkedList<StateCountResponse>();

        for (ScanLotteryCount scanLotteryCount : list) {
            Integer productId = scanLotteryCount.getProduct();
            String state = scanLotteryCount.getState();
            String city = scanLotteryCount.getCity();
            Integer scanTimes = scanLotteryCount.getTotalCount();

            String stateKey = productId + state;

            if (productMap.get(productId) != null) {
                ProductCountResponse productCountResponse = productMap.get(productId);
                productCountResponse.setScanTimes(productCountResponse.getScanTimes() + scanTimes);
                //state
                if (stateMap.get(stateKey) != null) {
                    StateCountResponse stateCountResponse = stateMap.get(stateKey);
                    stateCountResponse.setScanTimes(stateCountResponse.getScanTimes() + scanTimes);

                    CityCountResponse cityCountResponse = new CityCountResponse(city, scanTimes, null);
                    cityList.add(cityCountResponse);
                } else {
                    StateCountResponse stateCountResponse = new StateCountResponse(state, scanTimes, null);
                    stateMap.put(stateKey, stateCountResponse);
                    stateList.add(stateCountResponse);

                    cityList = new LinkedList<CityCountResponse>();
                    CityCountResponse cityCountResponse = new CityCountResponse(city, scanTimes, null);
                    cityList.add(cityCountResponse);

                    stateCountResponse.setCitys(cityList);
                }
            } else {
                ProductCountResponse productCountResponse = new ProductCountResponse(productId, scanTimes, null);
                productMap.put(productId, productCountResponse);
                productList.add(productCountResponse);

                stateList = new LinkedList<StateCountResponse>();
                StateCountResponse stateCountResponse = new StateCountResponse(state, scanTimes, null);
                stateMap.put(stateKey, stateCountResponse);
                stateList.add(stateCountResponse);

                cityList = new LinkedList<CityCountResponse>();
                CityCountResponse cityCountResponse = new CityCountResponse(city, scanTimes, null);
                cityList.add(cityCountResponse);

                stateCountResponse.setCitys(cityList);
                productCountResponse.setStates(stateList);
            }
        }

        return productList;
    }
}
