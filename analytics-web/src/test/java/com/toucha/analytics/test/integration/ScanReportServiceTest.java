package com.toucha.analytics.test.integration;

import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class ScanReportServiceTest {
	/*
	@Test
	public void testGetDateRangeReport() {
		Calendar cal = Calendar.getInstance();
		cal.set(2015, 1, 1);
		Date startDate = cal.getTime();
		cal.set(Calendar.DATE, 28);
		Date endDate = cal.getTime();
		int companyId = 1;
		List<ProductTagBatches> ptbs = new ArrayList<ProductTagBatches>();
		ProductTagBatches ptb = new ProductTagBatches();
		ptb.setProductId(1);
		Integer[] batchIds = {1,131,139,141,174,190,49};
		List<Integer> tagBatchIds = Arrays.asList(batchIds);
		ptb.setTagBatches(tagBatchIds);
		ptbs.add(ptb);
		
		ScanReportService scanReportService = new ScanReportService();
		try{
			DateRangeScanStatistics statistics = scanReportService.getDateRangeReport(companyId, ptbs, startDate, endDate);
			System.out.println(JSON.toJSONString(statistics));
			if(statistics != null && statistics.getDayScanStatistics() != null && statistics.getWeekScanStatistics() != null && statistics.getMonthScanStatistics() != null) {
				assert true;
			}
			else {
				assert false;
			}
		}
		catch(Exception ex) {
			assert false;
		}
		
	}
	*/
	@Test
	public void runTest() {
	    List<String[]> scanlogs = Lists.newArrayList();
	    scanlogs.add(new String[] {"3",".bvq5Opwt",null,null,"5?Cash"});
	    scanlogs.add(new String[] {"2",".bvq5Opwt","156***0379","+30??",null});
	    scanlogs.add(new String[] {"2",".FPYpRKF12","156***0379","+30??",null});
	    
	    scanlogs.add(new String[] {"2","_GheYK8_E1","156***0379","+30??",null});
	    scanlogs.add(new String[] {"3","_GheYK8_E1",null,null,"5?Cash"});
	    	            
	    Map<String,Integer> seen = Maps.newHashMap();
        List<Integer> rowsToDelete = Lists.newArrayList();
        
        for (int i = 0; i < scanlogs.size(); i++) {
            //String pn = scanlogs.get(i)[5];
            String hid = scanlogs.get(i)[1];
            
//            if (pn != null && hid != null) {
//                String key = pn + "-" + hid;
            if (hid != null) {
                String key = hid.trim();
                if (seen.containsKey(key)) {
                    String[] other = scanlogs.get(seen.get(key));
                    
                    if (other[2] == null)
                        other[2] = scanlogs.get(i)[2];
                    if (other[3] == null)
                        other[3] = scanlogs.get(i)[3];
                    if (other[4] == null)
                        other[4] = scanlogs.get(i)[4];
                    
                    rowsToDelete.add(i);
                }
                else {
                    seen.put(key, i);
                }
            }
        }
        
        if (!rowsToDelete.isEmpty()) {
            for (int i = rowsToDelete.size() - 1; i >= 0; i--) {
                scanlogs.remove(rowsToDelete.get(i).intValue());
            }
        }
        
        
	}
	
	
}
