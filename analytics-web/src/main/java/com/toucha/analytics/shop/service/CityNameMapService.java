package com.toucha.analytics.shop.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.toucha.analytics.common.dao.CityNameMapDao;
import com.toucha.analytics.common.model.ScanGeoStats;

@Service
public class CityNameMapService {

    private CityNameMapDao cityNameMapDao = new CityNameMapDao();

    /**
     * Get all city name mapping dataset and then cache them about half a day
     * 
     * @return
     */
    @Cacheable(value = "mappingCache", key = "'city-mapping'")
    private Map<String, String> getNameMapping() {
        return cityNameMapDao.getAllCityNames();
    }

    public String[] getStateAndCityCNName(String pyState, String pyCity) {

        String[] cnName = new String[2];
        String key = pyState + "," + pyCity;
        String tmp = getNameMapping().get(key.toLowerCase());

        if (tmp == null) {
            cnName[0] = pyState;
            cnName[1] = pyCity;
        } else {
            cnName = tmp.split(",");
        }

        return cnName;
    }

    /**
     * merge same city count result after translate PinYin to Chinese
     * 
     * @param scanGeoStats
     * @return city count
     */
    public Map<String, Integer> getCityCount(List<ScanGeoStats> scanGeoStats) {

        Map<String, Integer> mergeCityCnt = new HashMap<>();

        for (ScanGeoStats geo : scanGeoStats) {
            String stateName = geo.getState();
            String cityName = geo.getCity();
            String[] tmp = getStateAndCityCNName(stateName, cityName);
            stateName = tmp[0].equals("-") || tmp[0].equals("None") ? "其它" : tmp[0].replace("省", "").replace("市", "");
            cityName = tmp[1].equals("-") || tmp[1].equals("None") ? "其它" : tmp[1].replace("市", "");
            String key = stateName + ";" + cityName;
            if (mergeCityCnt.get(key) != null) {
                Integer cnt = mergeCityCnt.get(key);
                mergeCityCnt.put(key, cnt.intValue() + geo.getCount());
            } else {
                mergeCityCnt.put(key, geo.getCount());
            }
        }

        return mergeCityCnt;
    }
}
