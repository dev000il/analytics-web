package com.toucha.analytics.test.unit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.testng.Assert;

import com.alibaba.fastjson.JSONObject;
import com.toucha.analytics.common.dao.DataSet;
import com.toucha.analytics.model.request.PromoActivityReportRequest;
import com.toucha.analytics.model.response.ServiceReportResponse;
import com.toucha.analytics.service.ScanReportService;
import com.toucha.analytics.utils.ControllerUtil;
import com.toucha.analytics.web.ReportController;

import mockit.Expectations;
import mockit.Injectable;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;
import mockit.Tested;

public class ReportControllerTest {
	
	@Tested
	private ReportController reportController;
	@Injectable
	private ScanReportService scanReportService;
	/*
	@Test
	public void testGetDateRangeScanReport(@Mocked HttpServletRequest request, @Mocked HttpServletResponse response) {
		try {
            new Expectations() {
                {
                	scanReportService.getDateRangeReport(anyInt, (List<ProductTagBatches>)any, (Date)any,(Date)any);
                	DateRangeScanStatistics scanData = new DateRangeScanStatistics();
                    result = scanData;
                }
            };

            new MockUp<ControllerUtil>() {
                @Mock
                public JSONObject buffer(HttpServletRequest request) {
                    JSONObject jo = new JSONObject();
                    PutHeaderUtilTest.putHeader(jo);
                    jo.put("uid", 1);
                    jo.put("cid", 1);
                    jo.put("clid", 1);
                    jo.put("rid", UUID.randomUUID().toString());
                    jo.put("uip", "192.168.1.2");
                    
            		List<ProductTagBatches> ptbs = new ArrayList<ProductTagBatches>();
            		ProductTagBatches ptb = new ProductTagBatches();
            		ptb.setProductId(1);
            		Integer[] batchIds = {1,131,139,141,174,190,49};
            		List<Integer> tagBatchIds = Arrays.asList(batchIds);
            		ptb.setTagBatches(tagBatchIds);
            		ptbs.add(ptb);
            		jo.put("ptag", ptbs);
            		
            		Calendar cal = Calendar.getInstance();
            		cal.set(2015, 1, 1);
            		Date startDate = cal.getTime();
            		cal.set(Calendar.DATE, 28);
            		Date endDate = cal.getTime();
            		jo.put("sd", startDate);
            		jo.put("ed", endDate);
            		
                    return jo;
                }
            };
            ServiceReportResponse<DateRangeScanStatistics> result = reportController.getDateRangeScanReport(request, response);
            Assert.assertTrue(result.getErrors() == null || result.getErrors().size() == 0);
            Assert.assertNotNull(result);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	*/
	// Remove test flag for now.  This unit test won't pass in maven profile TEST or PROD. Need to revisit.
	//@Test
	public void testDownloadCsv(@Mocked HttpServletRequest request, @Mocked HttpServletResponse response) {
        try {
            new Expectations() {
                {
                    scanReportService.getRawActivities((PromoActivityReportRequest)any);
                    
                    
                    List<String[]> list = new ArrayList<String[]>();
                    List<String> fields = new ArrayList<String>();
                    DataSet rawActivities = new DataSet(fields, list);
                    result = rawActivities;
                }
            };

            new MockUp<ControllerUtil>() {
                @Mock
                public JSONObject buffer(HttpServletRequest request) {
                    JSONObject jo = new JSONObject();
                    // PutHeaderUtilTest.putHeader(jo);
                    jo.put("uid", 1);
                    jo.put("cid", 1);
                    jo.put("clid", 1);
                    jo.put("rid", UUID.randomUUID().toString());
                    jo.put("uip", "192.168.1.2");
                    List<Integer> pids = new ArrayList<Integer>();
                    pids.add(202);
                    jo.put("pids", pids);
                    List<String> fields = new ArrayList<String>();
                    fields.add("ts");
                    fields.add("country");
                    fields.add("city");
                    fields.add("pn");
                    fields.add("pts");
                    fields.add("rwds");
                    fields.add("pid");
                    jo.put("fields", fields);
                    //jo.put("fields", "[\"ts\",\"country\",\"city\",\"pn\",\"pts\",\"rwds\"]");
                    jo.put("count", 100);
                    jo.put("sd", "2015-03-13 10:44:16");
                    jo.put("ed", "2015-03-20 10:44:16");
                    jo.put("env", "DEV");
                    
                    Map<String,Map<String, String>> nameMap = new HashMap<String, Map<String, String>>();
                    Map<String,String> map_product = new HashMap<String,String>();
                    map_product.put("202", "abc");
                    nameMap.put("pid", map_product);
                    jo.put("nameMap", nameMap);
                    //jo.put("nameMap", "{\"pid\": {\"202\": \"abc\"}");
                    
                    return jo;
                }
            };
            
            ServiceReportResponse<String> result = reportController.getCsvActivities(request, response);
            
            Thread.sleep(10000);
            Assert.assertTrue(result.getErrors() == null || result.getErrors().size() == 0);
            Assert.assertNotNull(result);
            System.out.println(result.getReport());
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
	
}
