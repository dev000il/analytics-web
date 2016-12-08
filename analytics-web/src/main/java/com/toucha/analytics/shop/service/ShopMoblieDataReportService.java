package com.toucha.analytics.shop.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;

import com.toucha.analytics.common.exceptions.ServiceException;
import com.toucha.analytics.common.model.ResponseReportStats;
import com.toucha.analytics.common.model.ScanStats;
import com.toucha.analytics.common.model.TimeBasedReportStatUnit;
import com.toucha.analytics.common.model.TimeBasedReportStatsBase;
import com.toucha.analytics.common.shop.dao.ShopMobileDataDao;
import com.toucha.analytics.common.util.AppEvents;
import com.toucha.analytics.utils.ResponseReportUtil;

/**
 * Some service about mobile data
 * 
 * @author senhui.li
 */
@Service
public class ShopMoblieDataReportService {

    private ShopMobileDataDao mobileDataDao = new ShopMobileDataDao();

    public ResponseReportStats<Integer> getClaimAmountStatistics(int companyId, List<String> providerName,
            Date startDate, Date endDate) throws ServiceException {
        ResponseReportStats<Integer> result = new ResponseReportStats<>();

        try {
            List<TimeBasedReportStatUnit<Integer>> claimAmts = mobileDataDao.getCliamAmountStats(companyId, providerName,
                    startDate, endDate);

            if (claimAmts == null || claimAmts.isEmpty()) {
                return result;
            }

            result = ResponseReportUtil.mergeAndFriendlyShow(claimAmts, null);
        } catch (Exception ex) {
            String requestParam = "CompanyId: " + companyId + ", providerName: " + providerName + ", StartDate: " + startDate
                    + ", EndDate: " + endDate;
            AppEvents.LogException(ex, AppEvents.ScanReportService.DateRangeScanReportServiceException, requestParam);

            throw new ServiceException(AppEvents.ServerExceptionErr);
        }

        return result;
    }

    public ScanStats<TimeBasedReportStatsBase<Integer>> getClaimCountStatistics(int companyId, Date startDate, Date endDate)
            throws ServiceException {
        ScanStats<TimeBasedReportStatsBase<Integer>> result = new ScanStats<TimeBasedReportStatsBase<Integer>>();
        try {
            List<TimeBasedReportStatUnit<Integer>> claimCounts = mobileDataDao.getClaimCountStats(companyId, startDate, endDate);
            TimeBasedReportStatsBase<Integer> totalStats = new TimeBasedReportStatsBase<Integer>();
            List<Date> dateDims = new ArrayList<Date>();
            List<Integer> totalMeasures = new ArrayList<Integer>();
            int totalMeasure = 0;
            totalStats.setDesc("总计");
            for (TimeBasedReportStatUnit<Integer> claimCnt : claimCounts) {

                dateDims.add(claimCnt.getHour());
                totalMeasures.add(claimCnt.getMeasure());
                totalMeasure += claimCnt.getMeasure();
            }

            totalStats.setTimeseries(dateDims);
            totalStats.setMeasures(totalMeasures);
            totalStats.setTotalMeasure(totalMeasure);
            result.getHourScan().add(totalStats);

        } catch (Exception ex) {
            String requestParam = "CompanyId: " + companyId + ", StartDate: " + startDate + ", EndDate: " + endDate;
            AppEvents.LogException(ex, AppEvents.ScanReportService.DateRangeScanReportServiceException, requestParam);

            throw new ServiceException(AppEvents.ServerExceptionErr);
        }

        return result;
    }
}
