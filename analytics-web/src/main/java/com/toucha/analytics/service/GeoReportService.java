package com.toucha.analytics.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.toucha.analytics.common.dao.GeoReportDao;
import com.toucha.analytics.common.exceptions.ServiceException;
import com.toucha.analytics.common.model.DailyStatsBase;
import com.toucha.analytics.common.model.ScanGeoStats;
import com.toucha.analytics.model.response.GeoStatePair;
import com.toucha.analytics.model.response.GeoStateScanReponse;
import com.toucha.analytics.model.response.NameValuePair;

/**
 * All about the GEO data report service 
 * 
 * @author senhui.li
 */
@Service
public class GeoReportService {

    private GeoReportDao geoDao = new GeoReportDao();

    public List<DailyStatsBase> getDailyGeoCityStats(int companyId) throws ServiceException {
        List<DailyStatsBase> result = new LinkedList<>();
        try {
            // scan QR code statics
            DailyStatsBase scanStats = geoDao.getScanGeoCityCount(companyId);
            // enter lottery statics
            DailyStatsBase lotteryStats = geoDao.getEnterLotteryGeoCityCount(companyId);
            // new company user statics
            DailyStatsBase newUserStats = geoDao.getNewUserGeoCityCount(companyId);

            result.add(scanStats);
            result.add(lotteryStats);
            result.add(newUserStats);
        } catch (SQLException e) {
            throw new ServiceException(e);
        }

        return result;
    }

    public GeoStateScanReponse getEnterLotteryGeoCityStatics(int companyId, List<Integer> promIds, Date start, Date end)
            throws ServiceException {
        try {
            return getGeoCityStatics(geoDao.getEnterLotteryGeoStatsByPromotion(companyId, promIds, start, end));
        } catch (SQLException e) {
            throw new ServiceException(e);
        }
    }

    public GeoStateScanReponse getNewUserGeoCityStatics(int companyId, List<Integer> promIds, Date start, Date end)
            throws ServiceException {
        try {
            return getGeoCityStatics(geoDao.getNewUserGeoStatsByPromotion(companyId, promIds, start, end));
        } catch (SQLException e) {
            throw new ServiceException(e);
        }
    }

    public GeoStateScanReponse getGeoCityStatics(List<ScanGeoStats> geoStats) throws ServiceException {
        GeoStateScanReponse result = new GeoStateScanReponse();

        try {
            Map<String, GeoStatePair> states = new HashMap<>();
            int cityNums = 0;
            int stateNums = 0;
            for (ScanGeoStats geo : geoStats) {
                String stateName = geo.getState();
                if (states.get(stateName) == null) {
                    GeoStatePair geoPair = new GeoStatePair();
                    geoPair.setStateName(stateName);
                    geoPair.appendCity(new NameValuePair(geo.getCity(), geo.getCount()));
                    states.put(stateName, geoPair);
                    stateNums++;
                    cityNums++;
                } else {
                    GeoStatePair geoPair = states.get(stateName);
                    geoPair.appendCity(new NameValuePair(geo.getCity(), geo.getCount()));
                    states.put(stateName, geoPair);
                    cityNums++;
                }
            }

            List<GeoStatePair> geoStatePairs = new ArrayList<>();
            for (String stateName : states.keySet()) {
                geoStatePairs.add(states.get(stateName));
            }

            result.setCityNums(cityNums);
            result.setStatsNums(stateNums);
            result.setGeoStatePairs(geoStatePairs);
        } catch (Exception e) {
            throw new ServiceException(e);
        }

        return result;
    }
}
