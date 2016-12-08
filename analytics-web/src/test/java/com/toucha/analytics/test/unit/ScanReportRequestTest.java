package com.toucha.analytics.test.unit;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.testng.annotations.Test;

import com.toucha.analytics.common.model.ErrorInfo;
import com.toucha.analytics.common.model.PlatformRequestHeader;
import com.toucha.analytics.model.request.ScanReportRequest;

@Test
public class ScanReportRequestTest {
	
	/**
	 * Tests when the scan report request does not have platform header
	 */
	public void TestScanReportRequestWithoutRequestHeader() {
		ScanReportRequest request = new ScanReportRequest();
		setDateFields(request);
		
		List<ErrorInfo> errors = request.validateRequest();
		assert errors.size() > 0;
	}
	
	/**
	 * Tests when scan report request contains platform header
	 */
	public void TestScanReportRequestWithRequestHeader() {
		ScanReportRequest request = new ScanReportRequest();
		setDateFields(request);
		setPlatformRequestHeader(request);
		
		List<ErrorInfo> errors = request.validateRequest();
		assert errors.size() == 0;
	}
	
	/**
	 * Tests when scan report request contains platform header but without product tags
	 */
	public void TestScanReportRequestWithoutProductTagBatch() {
		ScanReportRequest request = new ScanReportRequest();
		setDateFields(request);
		setPlatformRequestHeader(request);
		
		List<ErrorInfo> errors = request.validateRequest();
		assert errors.size() == 0;
	}
	
	/**
	 * Tests whether all fields are set
	 */
	public void TestScanReportRequestWithAllFields() {
		ScanReportRequest request = new ScanReportRequest();
		setDateFields(request);
		setPlatformRequestHeader(request);
		
		setProductTags(request);
		List<ErrorInfo> errors = request.validateRequest();
		assert errors.size() == 0;
	}
	
	/**
	 * Tests whether product is error
	 */
	public void TestScanReportRequestWithErrorProducts() {
		
//		ScanReportRequest request = new ScanReportRequest();
//		setDateFields(request);
//		setPlatformRequestHeader(request);
//		
//		setProductTags(request);
//		request.getProductTagBatches().get(0).setProductId(-1);
//		
//		List<ErrorInfo> errors = request.validateRequest();
//		assert errors.size() > 0;
	}
	
	/**
	 * Tests when start date bigger than end date
	 */
	public void TestScanReportRequestWithErrorDates() {
		ScanReportRequest request = new ScanReportRequest();		
		setDateFields(request);
		Date tmpDate = request.getStartDate();
		request.setStartDate(request.getEndDate());
		request.setEndDate(tmpDate);
		setPlatformRequestHeader(request);
		
		setProductTags(request);
		List<ErrorInfo> errors = request.validateRequest();
		assert errors.size() > 0;
	}
	
	/**
	 * Tests when start date equals to end date
	 */
	public void TestScanReportRequestWithEqualDates() {
		ScanReportRequest request = new ScanReportRequest();		
		setDateFields(request);
		request.setStartDate(request.getEndDate());
		setPlatformRequestHeader(request);
		
		setProductTags(request);
		List<ErrorInfo> errors = request.validateRequest();
		assert errors.size() == 0;
	}
	
	/**
	 * Tests when have more than one products
	 */
	public void TestScanReportRequestWithMoreThanOneProducts() {
//		ScanReportRequest request = new ScanReportRequest();
//		setDateFields(request);
//		request.setStartDate(request.getEndDate());
//		setPlatformRequestHeader(request);
//		
//		setProductTags(request);
//		ProductTagBatches ptag = new ProductTagBatches();
//		ptag.setProductId(2);
//		List<Integer> tagBatches = new ArrayList<Integer>();
//		tagBatches.addAll(Arrays.asList(new Integer[]{1,2,3,4}));
//		ptag.setTagBatches(tagBatches);
//		request.getProductTagBatches().add(ptag);
//		
//		List<ErrorInfo> errors = request.validateRequest();
//		System.out.println(JSON.toJSONString(request));
//		
//		assert errors.size() == 0;
	}
	
	/**
	 * Tests when there are duplicate product
	 */
	public void TestScanReportRequestWithDuplicateProducts() {
//		ScanReportRequest request = new ScanReportRequest();
//		setDateFields(request);
//		request.setStartDate(request.getEndDate());
//		setPlatformRequestHeader(request);
//		
//		setProductTags(request);
//		ProductTagBatches ptag = new ProductTagBatches();
//		ptag.setProductId(1);
//		List<Integer> tagBatches = new ArrayList<Integer>();
//		tagBatches.addAll(Arrays.asList(new Integer[]{1,2,3,4}));
//		ptag.setTagBatches(tagBatches);
//		request.getProductTagBatches().add(ptag);
//		
//		List<ErrorInfo> errors = request.validateRequest();
//		assert errors.size() > 0;
	}
	
	/**
	 * Test when there is error tag batch Id
	 */
	public void TestScanReportRequestWithErrorTagBatchId() {
//		ScanReportRequest request = new ScanReportRequest();
//		setDateFields(request);
//		request.setStartDate(request.getEndDate());
//		setPlatformRequestHeader(request);
//		
//		setProductTags(request);
//		ProductTagBatches ptag = new ProductTagBatches();
//		ptag.setProductId(2);
//		List<Integer> tagBatches = new ArrayList<Integer>();
//		tagBatches.addAll(Arrays.asList(new Integer[]{1,-1,3,4}));
//		ptag.setTagBatches(tagBatches);
//		request.getProductTagBatches().add(ptag);
//		
//		List<ErrorInfo> errors = request.validateRequest();
//		assert errors.size() > 0;
	}
	
	private void setDateFields(ScanReportRequest request) {
		Calendar cal = Calendar.getInstance();
		cal.set(2015, 2, 1);
		request.setStartDate(cal.getTime());
		cal.set(2015, 2, 10);
		request.setEndDate(cal.getTime());
	}
	
	private void setPlatformRequestHeader(ScanReportRequest request) {
		PlatformRequestHeader header = new PlatformRequestHeader();
		header.setClientId("client1");
		header.setCompanyId(1);
		header.setRequestId(UUID.randomUUID().toString());
		header.setUserId("user1");
		header.setUserIp("192.168.1.1");
		
		request.setRequestHeader(header);
	}
	
	private void setProductTags(ScanReportRequest request) {
//		ProductTagBatches ptag = new ProductTagBatches();
//		ptag.setProductId(1);
//		List<Integer> tagBatches = new ArrayList<Integer>();
//		tagBatches.addAll(Arrays.asList(new Integer[]{1,2,3,4}));
//		ptag.setTagBatches(tagBatches);
//		List<ProductTagBatches> ptags = new ArrayList<ProductTagBatches>();
//		ptags.add(ptag);
//		request.setProductTagBatches(ptags);
	}
}
