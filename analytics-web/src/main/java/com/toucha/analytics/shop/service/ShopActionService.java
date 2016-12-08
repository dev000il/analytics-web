package com.toucha.analytics.shop.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.toucha.analytics.common.exceptions.ServiceException;
import com.toucha.analytics.common.model.ScanGeoStats;
import com.toucha.analytics.common.model.TimeBasedReportStatUnit;
import com.toucha.analytics.common.model.TimeBasedReportStatsBase;
import com.toucha.analytics.common.shop.dao.ShopActionDao;
import com.toucha.analytics.model.response.GeoStatePair;
import com.toucha.analytics.model.response.GeoStateScanReponse;
import com.toucha.analytics.model.response.NameValuePair;

@Service
public class ShopActionService {

    private ShopActionDao shopActionDao = new ShopActionDao();

    public List<TimeBasedReportStatsBase<Integer>> getShopActionLineStatics(List<Long> dids, Date startDate, Date endDate)
            throws ServiceException {
        List<TimeBasedReportStatsBase<Integer>> result = new ArrayList<>();

        List<TimeBasedReportStatUnit<Integer>> stats = null;
        try {
            stats = shopActionDao.getNewAndActivedShopStats(dids, startDate.getTime() / 1000, endDate.getTime() / 1000);
        } catch (SQLException e) {
            throw new ServiceException("Query shop action data from DB failed.", e);
        }

        // filter invalid data
        if (stats != null && !stats.isEmpty()) {
            // register action
            String regType = ShopActionDao.ActionType.Register.getType();
            TimeBasedReportStatsBase<Integer> regStats = new TimeBasedReportStatsBase<>();
            List<Date> regHourSer = new ArrayList<>();
            List<Integer> regMeasure = new ArrayList<>();
            int regTotalMeasure = 0;
            regStats.setDesc(regType);

            // login action
            String loginType = ShopActionDao.ActionType.Login.getType();
            TimeBasedReportStatsBase<Integer> loginStats = new TimeBasedReportStatsBase<>();
            List<Date> loginHourSer = new ArrayList<>();
            List<Integer> loginMeasure = new ArrayList<>();
            int loginTotalMeasure = 0;
            loginStats.setDesc(loginType);

            for (TimeBasedReportStatUnit<Integer> stat : stats) {
                if (stat.getDesc().equals(regType)) {
                    regHourSer.add(stat.getHour());
                    regMeasure.add(stat.getMeasure());
                    regTotalMeasure += stat.getMeasure();
                } else {
                    loginHourSer.add(stat.getHour());
                    loginMeasure.add(stat.getMeasure());
                    loginTotalMeasure += stat.getMeasure();
                }
            }

            regStats.setTimeseries(regHourSer);
            regStats.setMeasures(regMeasure);
            regStats.setTotalMeasure(regTotalMeasure);
            result.add(regStats);

            loginStats.setTimeseries(loginHourSer);
            loginStats.setMeasures(loginMeasure);
            loginStats.setTotalMeasure(loginTotalMeasure);
            result.add(loginStats);
        }

        return result;
    }

    public List<GeoStateScanReponse> getShopActionGeoStatistics(List<Long> dids, Date startDate, Date endDate)
            throws ServiceException {
        List<GeoStateScanReponse> result = new ArrayList<>();

        Map<String, List<ScanGeoStats>> stats = null;

        try {
            stats = shopActionDao.getNewAndActivedGeoStats(dids, startDate.getTime() / 1000, endDate.getTime() / 1000);
        } catch (SQLException e) {
            throw new ServiceException("Query shop action geo data from DB failed.", e);
        }

        if (stats != null && !stats.isEmpty()) {
            result.add(getGeoState(stats.get(ShopActionDao.ActionType.Register.getType())));
            result.add(getGeoState(stats.get(ShopActionDao.ActionType.Login.getType())));
        }

        return result;
    }

    private GeoStateScanReponse getGeoState(List<ScanGeoStats> geoStats) {

        GeoStateScanReponse result = new GeoStateScanReponse();

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

        return result;
    }
}
