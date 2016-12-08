package com.toucha.analytics.shop.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import org.elasticsearch.common.collect.Lists;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.toucha.analytics.common.exceptions.ServiceException;
import com.toucha.analytics.common.model.ScanLotteryCount;
import com.toucha.analytics.common.model.TimeBasedReportStatUnit;
import com.toucha.analytics.common.model.TimeBasedReportStats;
import com.toucha.analytics.common.model.TimeBasedReportStatsBase;
import com.toucha.analytics.common.shop.dao.ShopMemberDao;
import com.toucha.analytics.common.util.AppEvents;
import com.toucha.analytics.model.request.PromotionRewardReportRequest;
import com.toucha.analytics.model.response.CityCountResponse;
import com.toucha.analytics.model.response.ProductCountResponse;
import com.toucha.analytics.model.response.StateCountResponse;
import com.toucha.platform.common.enums.UserScanType;

@Service("shopMemberReportService")
public class ShopMemberReportService {

    private ShopMemberDao memberDao = new ShopMemberDao();

    public List<TimeBasedReportStatsBase<Integer>> getMemberStatistics(int companyId, Date start, Date end)
            throws ServiceException {

        List<TimeBasedReportStatsBase<Integer>> result = new ArrayList<TimeBasedReportStatsBase<Integer>>();
        TimeBasedReportStatsBase<Integer> newMember = new TimeBasedReportStatsBase<Integer>();

        try {
            List<TimeBasedReportStatUnit<Integer>> memberStats = memberDao.getMemberStats(companyId, start, end);

            newMember.setDesc("新扫码会员量");

            List<Date> timeSerious = new ArrayList<Date>();
            List<Integer> newMemberMeasures = new ArrayList<Integer>();
            int totalMeasure = 0;
            for (TimeBasedReportStatUnit<Integer> hourScan : memberStats) {
                timeSerious.add(hourScan.getHour());
                newMemberMeasures.add(hourScan.getMeasure());
                totalMeasure += hourScan.getMeasure();
            }

            newMember.setTimeseries(timeSerious);
            newMember.setMeasures(newMemberMeasures);
            newMember.setTotalMeasure(totalMeasure);

            result.add(newMember);
        } catch (SQLException e) {
            AppEvents.LogException(e, AppEvents.ScanReportService.MemberReportServiceError, String.valueOf(companyId),
                    start.toString(), end.toString());
            throw new ServiceException(e);
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
            List<TimeBasedReportStatUnit<Integer>> enterlotteryStats = memberDao.getUserEnterLotteryStats(
                    request.getRequestHeader().getCompanyId(), request.getProductIds(), request.getPromotionIds(),
                    request.getStartDate(), request.getEndDate());

            if (enterlotteryStats == null || enterlotteryStats.size() == 0) {
                return response;
            }

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
            totalStats.setMeasures(new ArrayList<Integer>());
            totalStats.setTimeseries(new ArrayList<Date>());
            for (Entry<Date, Integer> eltu : total.entrySet()) {
                totalStats.getTimeseries().add(eltu.getKey());
                totalStats.getMeasures().add(eltu.getValue());
                totalMeasure += eltu.getValue();
            }
            totalStats.setTotalMeasure(totalMeasure);
            totalStats.setDesc("总计");
            response.getHourScan().add(totalStats);
        } catch (Exception e) {
            AppEvents.LogException(e, AppEvents.ScanReportService.EffectiveScanUserTypeServiceError, JSON.toJSONString(request));
            throw new ServiceException(AppEvents.ScanReportService.EffectiveScanUserTypeServiceError);
        }

        return response;
    }

    public TimeBasedReportStats<Integer> getEffectiveScanStatisticsUniqueUser(PromotionRewardReportRequest request)
            throws ServiceException {
        TimeBasedReportStats<Integer> response = new TimeBasedReportStats<>();

        try {
            List<TimeBasedReportStatUnit<Integer>> enterlotteryStats = memberDao.getEffectiveScanUniqueUserStats(
                    request.getRequestHeader().getCompanyId(), request.getDistributorIds(), request.getPromotionIds(),
                    request.getOrderIds(), request.getStartDate(), request.getEndDate());

            if (enterlotteryStats == null || enterlotteryStats.size() == 0) {
                return response;
            }

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
                if (stat.getDesc().equals(String.valueOf(UserScanType.NewUser.getId()))) {
                    stat.setDesc("新用户");
                } else if (stat.getDesc().equals(String.valueOf(UserScanType.UniqueOldUser.getId()))) {
                    stat.setDesc("老用户");
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
            totalStats.setMeasures(new ArrayList<Integer>());
            totalStats.setTimeseries(new ArrayList<Date>());
            for (Entry<Date, Integer> eltu : total.entrySet()) {
                totalStats.getTimeseries().add(eltu.getKey());
                totalStats.getMeasures().add(eltu.getValue());
                totalMeasure += eltu.getValue();
            }
            totalStats.setTotalMeasure(totalMeasure);
            totalStats.setDesc("总计");
            response.getHourScan().add(totalStats);
        } catch (Exception e) {
            AppEvents.LogException(e, AppEvents.ScanReportService.EffectiveScanUniqueUserServiceError,
                    JSON.toJSONString(request));
            throw new ServiceException(AppEvents.ScanReportService.EffectiveScanUniqueUserServiceError);
        }

        return response;
    }

    @Cacheable(value = "lotteryCountCache", key = "'lottery'+#companyId+#productIds")
    public List<ProductCountResponse> getLotteryCount(Integer companyId, String productIds) throws ServiceException {
        long startTime = System.currentTimeMillis();
        List<ScanLotteryCount> list = Lists.newArrayList();
        try {
            list = memberDao.getLotteryCount(companyId, productIds);
            //in case when go to query data,the data just delete,haven't insert
            int maxCycleTime = 0;//the max cycle times
            while (list.size() <= 0) {
                //each time sleep 2 seconds
                Thread.sleep(2000);
                list = memberDao.getLotteryCount(companyId, productIds);
                if (maxCycleTime >= 10) {
                    break;
                }
                maxCycleTime++;
            }
        } catch (SQLException e) {
            throw new ServiceException();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("lotteryCount size is: " + list.size());

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
            Integer lotteryTimes = scanLotteryCount.getTotalCount();

            String stateKey = productId + state;

            if (productMap.get(productId) != null) {
                ProductCountResponse productCountResponse = productMap.get(productId);
                productCountResponse.setLotteryTimes(productCountResponse.getLotteryTimes() + lotteryTimes);
                //state
                if (stateMap.get(stateKey) != null) {
                    StateCountResponse stateCountResponse = stateMap.get(stateKey);
                    stateCountResponse.setLotteryTimes(stateCountResponse.getLotteryTimes() + lotteryTimes);

                    CityCountResponse cityCountResponse = new CityCountResponse(city, null, lotteryTimes);
                    cityList.add(cityCountResponse);
                } else {
                    StateCountResponse stateCountResponse = new StateCountResponse(state, null, lotteryTimes);
                    stateMap.put(stateKey, stateCountResponse);
                    stateList.add(stateCountResponse);

                    cityList = new LinkedList<CityCountResponse>();
                    CityCountResponse cityCountResponse = new CityCountResponse(city, null, lotteryTimes);
                    cityList.add(cityCountResponse);

                    stateCountResponse.setCitys(cityList);
                }
            } else {
                ProductCountResponse productCountResponse = new ProductCountResponse(productId, null, lotteryTimes);
                productMap.put(productId, productCountResponse);
                productList.add(productCountResponse);

                stateList = new LinkedList<StateCountResponse>();
                StateCountResponse stateCountResponse = new StateCountResponse(state, null, lotteryTimes);
                stateMap.put(stateKey, stateCountResponse);
                stateList.add(stateCountResponse);

                cityList = new LinkedList<CityCountResponse>();
                CityCountResponse cityCountResponse = new CityCountResponse(city, null, lotteryTimes);
                cityList.add(cityCountResponse);

                stateCountResponse.setCitys(cityList);
                productCountResponse.setStates(stateList);
            }
        }

        return productList;
    }
}
