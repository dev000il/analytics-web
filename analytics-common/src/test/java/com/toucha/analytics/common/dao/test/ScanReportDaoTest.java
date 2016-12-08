package com.toucha.analytics.common.dao.test;

import java.util.UUID;

import org.testng.annotations.Test;

import com.toucha.analytics.common.azure.AzureOperation;

public class ScanReportDaoTest {
	/*
	@Test
	public void testGetDateRangeStatistics() {
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
		
		ScanReportDao dao = new ScanReportDao();
		try{
			DateRangeScanStatistics scanStatistics = dao.getDateRangeStatistics(companyId, ptbs, startDate, endDate);
			String jsonResult = JSON.toJSONString(scanStatistics);
			Assert.assertTrue(StringUtils.isNotBlank(jsonResult));
		}
		catch (Exception ex) {
			assert false;
		}
	}
	*/
	@Test
	public void testGetBlobUrl() {
		String blobUrl = AzureOperation.getCsvDownloadBlobUrl(UUID.randomUUID().toString(), "DEV");
		System.out.println(blobUrl);
	}
}
