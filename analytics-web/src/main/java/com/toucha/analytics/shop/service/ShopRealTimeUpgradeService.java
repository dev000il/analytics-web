package com.toucha.analytics.shop.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.toucha.analytics.common.common.ApplicationConfig;
import com.toucha.analytics.common.shop.dao.ShopPromotionDao;
import com.toucha.analytics.common.shop.dao.ShopScanReportDao;
import com.toucha.analytics.common.exceptions.ServiceException;
import com.toucha.analytics.common.model.TimeBasedReportStatUnit;
import com.toucha.analytics.common.model.TimeBasedReportStatsBase;
import com.toucha.analytics.common.shop.dao.ShopMemberDao;
import com.toucha.analytics.common.util.DateHelper;
import com.toucha.analytics.common.util.HttpClientUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class ShopRealTimeUpgradeService {

    private static final Logger logger = LoggerFactory.getLogger(ShopRealTimeUpgradeService.class);

    private static JedisPool jedisPool;
    private static final String SCAN_TAG_CACHE_KEY = "scanact";
    private static final String ENTER_LOTTERY_CACHE_KEY = "enterlot";
    private static final String MEMBERS_CACHE_KEY = "members";
    private static final String REWARD_AMOUNT_CACHE_KEY = "rewardamt";

    public ShopRealTimeUpgradeService() {
        if (jedisPool == null) {
            JedisPoolConfig jedisPoolConf = new JedisPoolConfig();
            jedisPoolConf.setMaxTotal(100);
            jedisPoolConf.setMaxIdle(30 * 1000);
            jedisPool = new JedisPool(jedisPoolConf, ApplicationConfig.REDIS_MASTER_SERVER, ApplicationConfig.REDIS_MASTER_PORT,
                    30 * 1000, null, 2);
        }
    }

    private ShopScanReportDao scanReportDao = new ShopScanReportDao();
    private ShopMemberDao memberDao = new ShopMemberDao();
    private ShopPromotionDao promotionDao = new ShopPromotionDao();

    @Cacheable(value = "totaldayCache", key = "'realtime-scantag'+#companyId")
    public List<TimeBasedReportStatsBase<Integer>> getYesterdayScanTagStatics(int companyId) throws ServiceException {
        List<TimeBasedReportStatsBase<Integer>> result = new ArrayList<TimeBasedReportStatsBase<Integer>>();
        try {
            String yesterday = DateHelper.getPrefixDateStr(-1);
            List<TimeBasedReportStatUnit<Integer>> scanStatUnits = scanReportDao.getDateRangeStatistics(companyId, null, null, null,
                    DateHelper.StrToDate(yesterday + " 00:00:00"), DateHelper.StrToDate(yesterday + " 23:59:59"));

            TimeBasedReportStatsBase<Integer> scanStats = new TimeBasedReportStatsBase<Integer>();
            List<Date> hourSerious = new ArrayList<>();
            List<Integer> measures = new ArrayList<>();
            int totalMeasure = 0;

            for (TimeBasedReportStatUnit<Integer> unit : scanStatUnits) {
                if (unit.getDesc().equals("scancnt")) {
                    hourSerious.add(unit.getHour());
                    measures.add(unit.getMeasure());
                    totalMeasure += unit.getMeasure();
                }
            }

            scanStats.setTimeseries(hourSerious);
            scanStats.setMeasures(measures);
            scanStats.setTotalMeasure(totalMeasure);
            scanStats.setDesc("昨天");

            result.add(scanStats);

        } catch (SQLException e) {
            throw new ServiceException("Get yesterday scan statics count failed.", e);
        }

        return result;
    }

    public List<TimeBasedReportStatsBase<Integer>> getCurrentScanTagStatics(int companyId) throws ServiceException {
        List<TimeBasedReportStatsBase<Integer>> result = getYesterdayScanTagStatics(companyId);

        try {
            String today = DateHelper.getPrefixDateStr(0);
            List<TimeBasedReportStatUnit<Integer>> scanStatUnits = scanReportDao.getDateRangeStatistics(companyId, null, null, null,
                    DateHelper.StrToDate(today + " 00:00:00"), new Date());

            TimeBasedReportStatsBase<Integer> scanStats = new TimeBasedReportStatsBase<Integer>();
            List<Date> hourSerious = new ArrayList<Date>();
            List<Integer> measures = new ArrayList<Integer>();
            int totalMeasure = 0;

            for (TimeBasedReportStatUnit<Integer> unit : scanStatUnits) {
                if (unit.getDesc().equals("scancnt")) {
                    hourSerious.add(unit.getHour());
                    measures.add(unit.getMeasure());
                    totalMeasure += unit.getMeasure();
                }
            }

            scanStats.setTimeseries(hourSerious);
            scanStats.setMeasures(measures);
            scanStats.setTotalMeasure(totalMeasure);
            scanStats.setDesc("今天");

            result.add(scanStats);

        } catch (SQLException e) {
            throw new ServiceException("Get today scan tag statics count failed.", e);
        }

        return result;
    }

    @Cacheable(value = "totaldayCache", key = "'realtime-enterlottery'+#companyId")
    public List<TimeBasedReportStatsBase<Integer>> getYesterdayEnterLotteryStatics(int companyId) throws ServiceException {
        List<TimeBasedReportStatsBase<Integer>> result = new ArrayList<TimeBasedReportStatsBase<Integer>>();
        try {
            String yesterday = DateHelper.getPrefixDateStr(-1);
            List<TimeBasedReportStatUnit<Integer>> enterLotteryStatUnits = promotionDao.getEnterLotteryStats(companyId,
                    DateHelper.StrToDate(yesterday + " 00:00:00"), DateHelper.StrToDate(yesterday + " 23:59:59"));

            TimeBasedReportStatsBase<Integer> scanStats = new TimeBasedReportStatsBase<Integer>();
            List<Date> hourSerious = new ArrayList<Date>();
            List<Integer> measures = new ArrayList<Integer>();
            int totalMeasure = 0;

            for (TimeBasedReportStatUnit<Integer> unit : enterLotteryStatUnits) {
                    hourSerious.add(unit.getHour());
                    measures.add(unit.getMeasure());
                    totalMeasure += unit.getMeasure();
            }

            scanStats.setTimeseries(hourSerious);
            scanStats.setMeasures(measures);
            scanStats.setTotalMeasure(totalMeasure);
            scanStats.setDesc("昨天");

            result.add(scanStats);

        } catch (Exception e) {
            throw new ServiceException("Get yesterday enter lottery statics count failed.", e);
        }

        return result;
    }

    public List<TimeBasedReportStatsBase<Integer>> getCurrentEnterLotteryStatics(int companyId) throws ServiceException {
        List<TimeBasedReportStatsBase<Integer>> result = getYesterdayEnterLotteryStatics(companyId);

        try {
            String today = DateHelper.getPrefixDateStr(0);
            List<TimeBasedReportStatUnit<Integer>> enterLotteryStatUnits = promotionDao.getEnterLotteryStats(companyId,
                    DateHelper.StrToDate(today + " 00:00:00"), new Date());

            TimeBasedReportStatsBase<Integer> enterLotteryStats = new TimeBasedReportStatsBase<Integer>();
            List<Date> hourSerious = new ArrayList<Date>();
            List<Integer> measures = new ArrayList<Integer>();
            int totalMeasure = 0;

            for (TimeBasedReportStatUnit<Integer> unit : enterLotteryStatUnits) {
                    hourSerious.add(unit.getHour());
                    measures.add(unit.getMeasure());
                    totalMeasure += unit.getMeasure();
            }

            enterLotteryStats.setTimeseries(hourSerious);
            enterLotteryStats.setMeasures(measures);
            enterLotteryStats.setTotalMeasure(totalMeasure);
            enterLotteryStats.setDesc("今天");

            result.add(enterLotteryStats);

        } catch (Exception e) {
            throw new ServiceException("Get today enter lottery statics count failed.", e);
        }

        return result;
    }

    @Cacheable(value = "totaldayCache", key = "'realtime-members'+#companyId")
    public List<TimeBasedReportStatsBase<Integer>> getYesterdayMembersStatics(int companyId) throws ServiceException {
        List<TimeBasedReportStatsBase<Integer>> result = new ArrayList<TimeBasedReportStatsBase<Integer>>();
        try {
            String yesterday = DateHelper.getPrefixDateStr(-1);
            List<TimeBasedReportStatUnit<Integer>> membersStatUnits = memberDao.getUsersEnterLotteryStats(companyId, DateHelper.StrToDate(yesterday + " 00:00:00"), DateHelper.StrToDate(yesterday + " 23:59:59"));

            TimeBasedReportStatsBase<Integer> memberStats = new TimeBasedReportStatsBase<Integer>();
            List<Date> hourSerious = new ArrayList<Date>();
            List<Integer> measures = new ArrayList<Integer>();
            int totalMeasure = 0;

            for (TimeBasedReportStatUnit<Integer> unit : membersStatUnits) {
                    hourSerious.add(unit.getHour());
                    measures.add(unit.getMeasure());
                    totalMeasure += unit.getMeasure();
            }

            memberStats.setTimeseries(hourSerious);
            memberStats.setMeasures(measures);
            memberStats.setTotalMeasure(totalMeasure);
            memberStats.setDesc("昨天");

            result.add(memberStats);

        } catch (Exception e) {
            throw new ServiceException("Get yesterday members statics count failed.", e);
        }

        return result;
    }

    public List<TimeBasedReportStatsBase<Integer>> getCurrentMembersStatics(int companyId) throws ServiceException {
        List<TimeBasedReportStatsBase<Integer>> result = getYesterdayMembersStatics(companyId);

        try {
            String today = DateHelper.getPrefixDateStr(0);
            List<TimeBasedReportStatUnit<Integer>> membersStatUnits = memberDao.getUsersEnterLotteryStats(companyId,
                    DateHelper.StrToDate(today + " 00:00:00"), new Date());

            TimeBasedReportStatsBase<Integer> memberStats = new TimeBasedReportStatsBase<>();
            List<Date> hourSerious = new ArrayList<>();
            List<Integer> measures = new ArrayList<>();
            int totalMeasure = 0;

            for (TimeBasedReportStatUnit<Integer> unit : membersStatUnits) {
                    hourSerious.add(unit.getHour());
                    measures.add(unit.getMeasure());
                    totalMeasure += unit.getMeasure();
            }

            memberStats.setTimeseries(hourSerious);
            memberStats.setMeasures(measures);
            memberStats.setTotalMeasure(totalMeasure);
            memberStats.setDesc("今天");

            result.add(memberStats);

        } catch (Exception e) {
            throw new ServiceException("Get today members statics count failed.", e);
        }

        return result;
    }


    @Cacheable(value = "totaldayCache", key = "'realtime-enterlotrewardamt'+#companyId")
    public List<TimeBasedReportStatsBase<BigDecimal>> getYesterdayEnterLotRewardAmtStatics(int companyId) throws ServiceException {
        List<TimeBasedReportStatsBase<BigDecimal>> result = new ArrayList<>();
        try {
            String yesterday = DateHelper.getPrefixDateStr(-1);
            List<TimeBasedReportStatUnit<BigDecimal>> rewardAmtUnits = promotionDao.getEnterLotRewardsAmount(companyId, DateHelper.StrToDate(yesterday + " 00:00:00"), DateHelper.StrToDate(yesterday + " 23:59:59"));

            TimeBasedReportStatsBase<BigDecimal> rewardAmtStats = new TimeBasedReportStatsBase<>();
            List<Date> hourSerious = new ArrayList<>();
            List<BigDecimal> measures = new ArrayList<>();
            BigDecimal totalMeasure = new BigDecimal("0");

            for (TimeBasedReportStatUnit<BigDecimal> unit : rewardAmtUnits) {
                hourSerious.add(unit.getHour());
                measures.add(unit.getMeasure());
                totalMeasure = unit.getMeasure().add(totalMeasure);
            }

            rewardAmtStats.setTimeseries(hourSerious);
            rewardAmtStats.setMeasures(measures);
            rewardAmtStats.setTotalMeasure(totalMeasure);
            rewardAmtStats.setDesc("昨天");

            result.add(rewardAmtStats);

        } catch (Exception e) {
            throw new ServiceException("Get yesterday enter lottery reward amount statics count failed.", e);
        }

        return result;
    }

    public List<TimeBasedReportStatsBase<BigDecimal>> getCurrentEnterLotRewardAmtStatics(int companyId) throws ServiceException {
        List<TimeBasedReportStatsBase<BigDecimal>> result = getYesterdayEnterLotRewardAmtStatics(companyId);

        try {
            String today = DateHelper.getPrefixDateStr(0);
            List<TimeBasedReportStatUnit<BigDecimal>> rewardAmtUnits = promotionDao.getEnterLotRewardsAmount(companyId,
                    DateHelper.StrToDate(today + " 00:00:00"), new Date());

            TimeBasedReportStatsBase<BigDecimal> rewardAmtStats = new TimeBasedReportStatsBase<>();
            List<Date> hourSerious = new ArrayList<>();
            List<BigDecimal> measures = new ArrayList<>();
            BigDecimal totalMeasure = new BigDecimal("0");

            for (TimeBasedReportStatUnit<BigDecimal> unit : rewardAmtUnits) {
                hourSerious.add(unit.getHour());
                measures.add(unit.getMeasure());
                totalMeasure = unit.getMeasure().add(totalMeasure);
            }

            rewardAmtStats.setTimeseries(hourSerious);
            rewardAmtStats.setMeasures(measures);
            rewardAmtStats.setTotalMeasure(totalMeasure);
            rewardAmtStats.setDesc("今天");

            result.add(rewardAmtStats);

        } catch (Exception e) {
            throw new ServiceException("Get today enter lottery reward amount statics count failed.", e);
        }

        return result;
    }
    
    protected int getRealTimeCacheCount(String key, int companyId) {
        int result = 0;
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String cacheResult = jedis.get(key);
            if (cacheResult != null && !cacheResult.isEmpty()) {
                JSONObject curCnt = JSON.parseObject(cacheResult);
                if (companyId == -1) {
                    for (String company : curCnt.keySet()) {
                        result += curCnt.getIntValue(company);
                    }
                } else {
                    String company = Integer.toString(companyId);
                    if(curCnt.containsKey(key)) {
                        result = curCnt.getIntValue(company);
                    }

                }
            }
        } catch (Exception e) {
            logger.error("cache key: {}", key);
            logger.error("Get Redis server cache count data failed.", e);
        } finally {
             if(jedis != null){
                 jedis.close();
             }
        }
        return result;
    }

    public int getRealTimeScantagCount(int companyId) {
        int result = getRealTimeCacheCount(SCAN_TAG_CACHE_KEY, companyId);
        return result;
    }

    public int getRealTimeEnterLotteryCount(int companyId) {
        int result = getRealTimeCacheCount(ENTER_LOTTERY_CACHE_KEY, companyId);
        return result;
    }

    public int getRealTimeMembersCnt(int companyId) {
        int result = 0;
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String cacheResult = jedis.get(MEMBERS_CACHE_KEY);
            if(cacheResult != null && !cacheResult.isEmpty()){
                JSONObject membersCnt = JSON.parseObject(cacheResult);
                if(companyId == -1){
                    for(String key : membersCnt.keySet()){
                        result += membersCnt.getJSONArray(key).size();
                    }
                } else {
                    String key = Integer.toString(companyId);
                    if(membersCnt.containsKey(key)){
                        result = membersCnt.getJSONArray(key).size();
                    }
                }
            }
        } catch (Exception e){
            logger.error("cache key: {}", REWARD_AMOUNT_CACHE_KEY);
            logger.error("Get Redis server cache count data failed.", e);
        } finally {
            if(jedis != null){
                jedis.close();
            }
        }

        return result;
    }

    public String getRealTimeRewardAmount(int companyId) {
        String result = "";
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String cacheResult = jedis.get(REWARD_AMOUNT_CACHE_KEY);
            if(cacheResult != null && !cacheResult.isEmpty()){
                BigDecimal amount = null;
                JSONObject curAmount = JSON.parseObject(cacheResult);
                if(companyId == -1){
                    for(String key : curAmount.keySet()){
                        BigDecimal tmp = curAmount.getBigDecimal(key);
                        if(amount == null){
                            amount = tmp;
                        } else {
                            amount = tmp.add(amount);
                        }
                    }
                } else {
                    String key = Integer.toString(companyId);
                    if(curAmount.containsKey(key)){
                        amount = curAmount.getBigDecimal(key);
                    }
                }

                result = amount.toString();
            }
        } catch (Exception e){
            logger.error("cache key: {}", REWARD_AMOUNT_CACHE_KEY);
            logger.error("Get Redis server cache count data failed.", e);
        } finally {
            if(jedis != null){
                jedis.close();
            }
        }

        return result;
    }

    public String getRealTimeCreateTagCnt(){
        String result = "";
        String url = ApplicationConfig.PLATFORM_WEB_URL + ApplicationConfig.PLATFORM_DATAPIPELINE_URL + ApplicationConfig.PLATFORM_TAGS_URL;
        try {
            String temp = HttpClientUtil.signPost(url, "" , HttpClientUtil.APPLICATION_JSON);
            result = JSON.parseObject(temp).getString("data");
        } catch (Exception e) {
            logger.error("Get tags count data failed", e);
        }
        return result;
    }
}
