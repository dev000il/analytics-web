package com.toucha.analytics.service;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.toucha.analytics.common.common.ApplicationConfig;
import com.toucha.analytics.common.exceptions.ServiceException;
import com.toucha.analytics.common.model.RealTimeStatsDao;
import com.toucha.analytics.common.model.TimeBasedReportStatUnit;
import com.toucha.analytics.common.model.TimeBasedReportStatsBase;
import com.toucha.analytics.common.model.Tuple2;
import com.toucha.analytics.common.util.HttpClientUtil;
import com.toucha.analytics.common.util.TupleUtil;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Service
public class RealTimeUpgradeService {

    private static final Logger logger = LoggerFactory.getLogger("RealTimeUpgradeService");

    private static JedisPool jedisPool;

    private static final String SCAN_TAG_CACHE_KEY = "scanact";

    private static final String ENTER_LOTTERY_CACHE_KEY = "enterlot";

    private static final String MEMBERS_CACHE_KEY = "members";

    private static final String REWARD_AMOUNT_CACHE_KEY = "rewardamt";

    private RealTimeStatsDao realTimeStatsDao = new RealTimeStatsDao();

    public RealTimeUpgradeService() {
        if (jedisPool == null) {
            JedisPoolConfig jedisPoolConf = new JedisPoolConfig();
            jedisPoolConf.setMaxTotal(100);
            jedisPoolConf.setMaxIdle(30 * 1000);
            jedisPool = new JedisPool(jedisPoolConf, ApplicationConfig.REDIS_MASTER_SERVER, ApplicationConfig.REDIS_MASTER_PORT,
                    30 * 1000, null, 2);
        }
    }

    private Tuple2<Long, Long> getStatDateRange() {

        Calendar start = Calendar.getInstance();
        start.add(Calendar.MONTH, -1);
        start.set(Calendar.DAY_OF_MONTH, 1);
        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);

        Calendar end = Calendar.getInstance();
        end.set(Calendar.MINUTE, 0);
        end.set(Calendar.SECOND, 0);

        return TupleUtil.tuple2(start.getTimeInMillis() / 1000, end.getTimeInMillis() / 1000);
    }

    public List<TimeBasedReportStatsBase<Integer>> getRealTimeScanStatics() throws ServiceException {

        Tuple2<Long, Long> dateRange = getStatDateRange();
        try {
            List<TimeBasedReportStatUnit<Integer>> data = realTimeStatsDao
                    .getRealTimeScanDateRangeStatistics(dateRange.getFirst(), dateRange.getSecond());
            logger.info("Scan data size: {}", data == null ? 0 : data.size());
            return getRealTimeStatics(data, "扫码");
        } catch (SQLException e) {
            throw new ServiceException("Get real time scan tag statics data failed.", e);
        }
    }

    public List<TimeBasedReportStatsBase<Integer>> getRealTimeEnterLotStatics() throws ServiceException {

        Tuple2<Long, Long> dateRange = getStatDateRange();
        try {
            List<TimeBasedReportStatUnit<Integer>> data = realTimeStatsDao
                    .getRealTimeEnterLotDateRangeStatistics(dateRange.getFirst(), dateRange.getSecond());
            logger.info("Enter lottery data size: {}", data == null ? 0 : data.size());
            return getRealTimeStatics(data, "抽奖");
        } catch (SQLException e) {
            throw new ServiceException("Get real time enter lottery statics data failed.", e);
        }
    }

    public List<TimeBasedReportStatsBase<Integer>> getRealTimeMembersStatics() throws ServiceException {

        Tuple2<Long, Long> dateRange = getStatDateRange();
        try {
            List<TimeBasedReportStatUnit<Integer>> data = realTimeStatsDao
                    .getRealTimeMembersDateRangeStatistics(dateRange.getFirst(), dateRange.getSecond());
            logger.info("New users data size: {}", data == null ? 0 : data.size());
            return getRealTimeStatics(data, "新会员");
        } catch (SQLException e) {
            throw new ServiceException("Get real time members statics data failed.", e);
        }
    }

    public List<TimeBasedReportStatsBase<Integer>> getRealTimeRewardsStatics() throws ServiceException {

        Tuple2<Long, Long> dateRange = getStatDateRange();
        try {
            List<TimeBasedReportStatUnit<Integer>> data = realTimeStatsDao
                    .getRealTimeRewardsDateRangeStatistics(dateRange.getFirst(), dateRange.getSecond());
            logger.info("Win rewards data size: {}", data == null ? 0 : data.size());
            return getRealTimeStatics(data, "奖品");
        } catch (SQLException e) {
            throw new ServiceException("Get real time rewards statics data failed.", e);
        }
    }

    private List<TimeBasedReportStatsBase<Integer>> getRealTimeStatics(List<TimeBasedReportStatUnit<Integer>> data, String desc)
            throws ServiceException {
        List<TimeBasedReportStatsBase<Integer>> result = new ArrayList<>();

        TimeBasedReportStatsBase<Integer> stats = new TimeBasedReportStatsBase<Integer>();
        List<Date> hourSerious = new ArrayList<>();
        List<Integer> measures = new ArrayList<>();
        int totalMeasure = 0;

        for (TimeBasedReportStatUnit<Integer> unit : data) {
            hourSerious.add(unit.getHour());
            measures.add(unit.getMeasure());
            totalMeasure += unit.getMeasure();
        }

        stats.setTimeseries(hourSerious);
        stats.setMeasures(measures);
        stats.setTotalMeasure(totalMeasure);
        stats.setDesc(desc);

        result.add(stats);

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
                    if (curCnt.containsKey(key)) {
                        result = curCnt.getIntValue(company);
                    }

                }
            }
        } catch (Exception e) {
            logger.error("cache key: {}", key);
            logger.error("Get Redis server cache count data failed.", e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return result;
    }

    public int getRealTimeScantagCount(int companyId) {
        int result = getRealTimeCacheCount(SCAN_TAG_CACHE_KEY, companyId);
        logger.info("Real time scan count: {}", result);
        return result;
    }

    public int getRealTimeEnterLotteryCount(int companyId) {
        int result = getRealTimeCacheCount(ENTER_LOTTERY_CACHE_KEY, companyId);
        logger.info("Real time enter lottery count: {}", result);
        return result;
    }

    public int getRealTimeMembersCnt(int companyId) {
        int result = 0;
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String cacheResult = jedis.get(MEMBERS_CACHE_KEY);
            if (cacheResult != null && !cacheResult.isEmpty()) {
                JSONObject membersCnt = JSON.parseObject(cacheResult);
                if (companyId == -1) {
                    for (String key : membersCnt.keySet()) {
                        result += membersCnt.getJSONArray(key).size();
                    }
                } else {
                    String key = Integer.toString(companyId);
                    if (membersCnt.containsKey(key)) {
                        result = membersCnt.getJSONArray(key).size();
                    }
                }
            }
        } catch (Exception e) {
            logger.error("cache key: {}", REWARD_AMOUNT_CACHE_KEY);
            logger.error("Get Redis server cache count data failed.", e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }

        logger.info("Real time members count: {}", result);

        return result;
    }

    public String getRealTimeRewardAmount(int companyId) {
        String result = "";
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String cacheResult = jedis.get(REWARD_AMOUNT_CACHE_KEY);
            if (cacheResult != null && !cacheResult.isEmpty()) {
                BigDecimal amount = null;
                JSONObject curAmount = JSON.parseObject(cacheResult);
                if (companyId == -1) {
                    for (String key : curAmount.keySet()) {
                        BigDecimal tmp = curAmount.getBigDecimal(key);
                        if (amount == null) {
                            amount = tmp;
                        } else {
                            amount = tmp.add(amount);
                        }
                    }
                } else {
                    String key = Integer.toString(companyId);
                    if (curAmount.containsKey(key)) {
                        amount = curAmount.getBigDecimal(key);
                    }
                }

                result = amount.toString();
            }
        } catch (Exception e) {
            logger.error("cache key: {}", REWARD_AMOUNT_CACHE_KEY);
            logger.error("Get Redis server cache count data failed.", e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }

        logger.info("Real time win reward amount count: {}", result);

        return result;
    }

    public String getRealTimeCreateTagCnt() {
        String result = "";
        String jaResult = "";
        String zbResult = "";

        String zbUrl = ApplicationConfig.PLATFORM_WEB_URL + ApplicationConfig.PLATFORM_DATAPIPELINE_URL
                + ApplicationConfig.PLATFORM_TAGS_URL;
        String jaUrl = ApplicationConfig.PLATFORM_JA_WEB_URL + ApplicationConfig.PLATFORM_DATAPIPELINE_URL
                + ApplicationConfig.PLATFORM_TAGS_URL;
        try {
            String zbTemp = HttpClientUtil.signPost(zbUrl, "", HttpClientUtil.APPLICATION_JSON);
            zbResult = JSON.parseObject(zbTemp).getString("data");

            String jaTemp = HttpClientUtil.signPost(jaUrl, "", HttpClientUtil.APPLICATION_JSON);
            jaResult = JSON.parseObject(jaTemp).getString("data");

            Long total = Long.valueOf(zbResult) + Long.valueOf(jaResult);
            result = total.toString();
        } catch (Exception e) {
            logger.error("Get tags count data failed", e);
        }

        logger.info("Real time create tag count: {}", result);

        return result;
    }
}
