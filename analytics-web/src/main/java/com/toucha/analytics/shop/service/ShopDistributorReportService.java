package com.toucha.analytics.shop.service;

import com.toucha.analytics.common.exceptions.ServiceException;
import com.toucha.analytics.common.model.ScanGeoStats;
import com.toucha.analytics.common.model.ScanStats;
import com.toucha.analytics.common.shop.dao.ShopDistributorDao;
import com.toucha.analytics.common.util.AppEvents;
import com.toucha.analytics.model.response.GeoScanResponse;
import com.toucha.analytics.model.response.NameValuePair;
import com.toucha.analytics.utils.ControllerUtil;
import org.springframework.stereotype.Service;

import java.util.*;

@Service("shopDistributorReportService")
public class ShopDistributorReportService {

	ShopDistributorDao distributorDao = new ShopDistributorDao();
	
	public ScanStats<?> getStatsReport(int companyId,  List<Integer> productIds, List<Integer> distributorIds, Date startDate, Date endDate) throws ServiceException{
		
		ScanStats<?> statistics = null;
	        try {
	            statistics = distributorDao.getDistributorStats(companyId, productIds, distributorIds, startDate, endDate);
	        } catch (Exception ex) {
	            String requestParam = "CompanyId: " + companyId + ", productIds: " + productIds +", distributorIds: "+distributorIds +", StartDate: " + startDate
	                    + ", EndDate: " + endDate;
	            AppEvents.LogException(ex, AppEvents.ScanReportService.DistributorReportServiceException, requestParam);

	            throw new ServiceException(AppEvents.ServerExceptionErr);
	        }
	        return statistics;
	}
	
	
    public  GeoScanResponse getGeoReport(int companyId,List<Integer> productIds, List<Integer> distributorIds, Date startDate,
            Date endDate) throws ServiceException {
    	GeoScanResponse result = new GeoScanResponse();
        try {
        	Map<String, Integer> cityStats = new HashMap<String, Integer>();
        	Map<String, Integer> stateStats = new HashMap<String, Integer>();
            List<ScanGeoStats> scanGeoStats= distributorDao.getGeoStatistics(companyId, productIds, distributorIds, startDate, endDate);
            for(ScanGeoStats s:scanGeoStats){
            	String city = s.getCity().replace("市", "");
            	String state = s.getState().replace("省", "").replace("市", "");
            	// not include foreign country scans
            	if(!ControllerUtil.isChinese(city)){
            		city = "";
            		state = "";
            	}
            	stateStats.put(state, stateStats.containsKey(state)? s.getCount()+stateStats.get(state) : s.getCount());
            	cityStats.put(city, cityStats.containsKey(city)? s.getCount()+cityStats.get(city) : s.getCount());
            }
            List<NameValuePair> cityList = new ArrayList<NameValuePair>();
            List<NameValuePair> stateList = new ArrayList<NameValuePair>(); 
            for(String key: cityStats.keySet()){
            	if(key.equals(""))
            		result.setCityEmpty(cityStats.get(key));
            	else
            		cityList.add(new NameValuePair(key, cityStats.get(key)));
            }
            for(String key: stateStats.keySet()){
            	if(key.equals(""))
            		result.setStateEmpty(stateStats.get(key));
            	else
            		stateList.add(new NameValuePair(key, stateStats.get(key)));
            }
            result.setCityStats(cityList);
            result.setStateStats(stateList);
        } catch (Exception ex) {
        	   String requestParam = "CompanyId: " + companyId + ", productIds: " + productIds +", distributorIds: "+distributorIds +", StartDate: " + startDate
	                    + ", EndDate: " + endDate;
	            AppEvents.LogException(ex, AppEvents.ScanReportService.DistributorReportServiceException, requestParam);

            throw new ServiceException(AppEvents.ServerExceptionErr);
        }

        return result;
    }
    
}
