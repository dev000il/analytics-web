package com.toucha.analytics.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.toucha.analytics.common.dao.TrendStatsDao;
import com.toucha.analytics.common.exceptions.ServiceException;
import com.toucha.analytics.common.model.TimeBasedReportStatUnit;
import com.toucha.analytics.common.model.TimeBasedReportStatsBase;
import com.toucha.platform.common.enums.DateTimeUnit;

/**
 * Trend statics service
 * 
 * @author senhui.li
 */
@Service
public class TrendStatsService {

    private TrendStatsDao trendStatsDao = new TrendStatsDao();

    /**************************** New Users *********************************/
    @Cacheable(value = "trendCache", key = "'trend-user-year'+#companyId")
    public List<TimeBasedReportStatsBase<Integer>> getYearsNewUserStats(int companyId) throws ServiceException {
        return getNewUserTrendStats(companyId, DateTimeUnit.YEAR);
    }

    @Cacheable(value = "trendCache", key = "'trend-user-month'+#companyId")
    public List<TimeBasedReportStatsBase<Integer>> getMonthsNewUserStats(int companyId) throws ServiceException {
        return getNewUserTrendStats(companyId, DateTimeUnit.MONTH);
    }

    @Cacheable(value = "trendCache", key = "'trend-user-day'+#companyId")
    public List<TimeBasedReportStatsBase<Integer>> getWeeksNewUserStats(int companyId) throws ServiceException {
        return getNewUserTrendStats(companyId, DateTimeUnit.DAY);
    }

    private List<TimeBasedReportStatsBase<Integer>> getNewUserTrendStats(int companyId, DateTimeUnit range)
            throws ServiceException {
        try {
            List<TimeBasedReportStatUnit<Integer>> datas = trendStatsDao.getNewUserTotalStats(companyId, range);
            return getTrendStats(datas, "新会员量");
        } catch (SQLException e) {
            throw new ServiceException("Get enter lottery trend statics data failed.", e);
        }
    }

    /************************** Enter Lottery *****************************/
    @Cacheable(value = "trendCache", key = "'trend-enterlot-year'+#companyId")
    public List<TimeBasedReportStatsBase<Integer>> getYearsEnterLotteryStats(int companyId) throws ServiceException {
        return getEnterLotteryTrendStats(companyId, DateTimeUnit.YEAR);
    }

    @Cacheable(value = "trendCache", key = "'trend-enterlot-month'+#companyId")
    public List<TimeBasedReportStatsBase<Integer>> getMonthsEnterLotteryStats(int companyId) throws ServiceException {
        return getEnterLotteryTrendStats(companyId, DateTimeUnit.MONTH);
    }

    @Cacheable(value = "trendCache", key = "'trend-enterlot-day'+#companyId")
    public List<TimeBasedReportStatsBase<Integer>> getWeeksEnterLotteryStats(int companyId) throws ServiceException {
        return getEnterLotteryTrendStats(companyId, DateTimeUnit.DAY);
    }

    private List<TimeBasedReportStatsBase<Integer>> getEnterLotteryTrendStats(int companyId, DateTimeUnit range)
            throws ServiceException {
        try {
            List<TimeBasedReportStatUnit<Integer>> datas = trendStatsDao.getLotteryTotalStats(companyId, range);
            return getTrendStats(datas, "抽奖次数");
        } catch (SQLException e) {
            throw new ServiceException("Get enter lottery trend statics data failed.", e);
        }
    }

    /**************************** Scan Tags *********************************/
    @Cacheable(value = "trendCache", key = "'trend-scan-year'+#companyId")
    public List<TimeBasedReportStatsBase<Integer>> getYearsScanTagsStats(int companyId) throws ServiceException {
        return getScanTagsTrendStats(companyId, DateTimeUnit.YEAR);
    }

    @Cacheable(value = "trendCache", key = "'trend-scan-month'+#companyId")
    public List<TimeBasedReportStatsBase<Integer>> getMonthsScanTagsStats(int companyId) throws ServiceException {
        return getScanTagsTrendStats(companyId, DateTimeUnit.MONTH);
    }

    @Cacheable(value = "trendCache", key = "'trend-scan-day'+#companyId")
    public List<TimeBasedReportStatsBase<Integer>> getWeeksScanTagsStats(int companyId) throws ServiceException {
        return getScanTagsTrendStats(companyId, DateTimeUnit.DAY);
    }

    private List<TimeBasedReportStatsBase<Integer>> getScanTagsTrendStats(int companyId, DateTimeUnit range)
            throws ServiceException {
        try {
            List<TimeBasedReportStatUnit<Integer>> datas = trendStatsDao.getScanTagsTotalStats(companyId, range);
            return getTrendStats(datas, "标签扫码量");
        } catch (SQLException e) {
            throw new ServiceException("Get scan tags trend statics data failed.", e);
        }
    }

    private List<TimeBasedReportStatsBase<Integer>> getTrendStats(List<TimeBasedReportStatUnit<Integer>> datas, String desc)
            throws ServiceException {
        List<TimeBasedReportStatsBase<Integer>> result = new ArrayList<>();

        if (datas != null && !datas.isEmpty()) {
            List<Date> dateSerious = new ArrayList<>();
            List<Integer> measures = new ArrayList<>();
            int totalMeasure = 0;

            for (TimeBasedReportStatUnit<Integer> data : datas) {
                dateSerious.add(data.getHour());
                measures.add(data.getMeasure());
                totalMeasure += data.getMeasure();
            }

            TimeBasedReportStatsBase<Integer> report = new TimeBasedReportStatsBase<Integer>();
            report.setTimeseries(dateSerious);
            report.setMeasures(measures);
            report.setTotalMeasure(totalMeasure);
            report.setDesc(desc);
            result.add(report);
        }

        return result;
    }
}
