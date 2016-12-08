package com.toucha.analytics.test.unit;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.toucha.analytics.common.model.ErrorInfo;
import com.toucha.analytics.common.model.PlatformRequestHeader;
import com.toucha.analytics.model.request.PromoActivityReportRequest;

public class PromoActivityReportRequestTests {

    @Test
    public void validate_success() {
        // With only the required fields
        PromoActivityReportRequest request = this.createValidRequest();
        assertRequestValidation(request, 0);

        // With the optional count field
        request.setCountRequested(100);
        assertRequestValidation(request, 0);

        // With the optional field name map
        Map<String, Map<String, String>> map = Maps.newHashMap();
        request.setNameMap(map);
        assertRequestValidation(request, 0);

        Map<String,String> mapping = new HashMap<String,String>();
        mapping.put("1", "ProductName1");
        mapping.put("2", "ProductName2");
        map.put("ts", mapping);
        assertRequestValidation(request, 0);
    }

    private PromoActivityReportRequest createValidRequest() {
        ImmutableList<String> requestedFields = ImmutableList.of("num");
        List<Integer> productIds = Lists.newArrayList(1);

        Calendar.getInstance().set(2015, 3, 20);
        Calendar.getInstance().add(Calendar.DAY_OF_MONTH, 7);

        Date start = Calendar.getInstance().getTime();
        Date end = Calendar.getInstance().getTime();

        PromoActivityReportRequest request = new PromoActivityReportRequest(productIds, requestedFields, start, end, "TEST");
        this.setValidHeader(request);
        return request;
    }

    @Test
    public void validate_fail_missingHeader() {
        // Missing header
        PromoActivityReportRequest request = this.createValidRequest();
        request.setRequestHeader(null);

        this.assertRequestValidation(request, 1);
    }

    @Test
    public void validate_fail_invalidHeader() {
        // Missing required field
        PromoActivityReportRequest request = this.createValidRequest();
        request.getRequestHeader().setClientId(null);

        this.assertRequestValidation(request, 1);
    }

    @Test
    public void validate_fail_invalidProductList() {
        // Invalid IDs
        PromoActivityReportRequest request = this.createValidRequest();
        request.getProductIds().add(-1);

        this.assertRequestValidation(request, 1);

        // Empty list
        request = this.createValidRequest();
        request.getProductIds().clear();

        this.assertRequestValidation(request, 1);
    }

    @SuppressWarnings("deprecation")
    @Test
    public void validate_fail_invalidRequestedFields() {
        // Unsupported field requested
        PromoActivityReportRequest request = this.createValidRequest();
        request.getFields().add("unknownField");
        this.assertRequestValidation(request, 1);

        // No fields requested
        request = this.createValidRequest();
        request.getFields().clear();
        this.assertRequestValidation(request, 1);
    }

    @Test
    public void validate_fail_invalidRequestedCount() {
        // Invalid requested row count
        ImmutableList<String> requestedFields = ImmutableList.of("pid");
        List<Integer> productIds = Lists.newArrayList(1);

        Calendar.getInstance().set(2015, 3, 20);
        Calendar.getInstance().add(Calendar.DAY_OF_MONTH, 7);

        Date start = Calendar.getInstance().getTime();
        Date end = Calendar.getInstance().getTime();

        PromoActivityReportRequest request = new PromoActivityReportRequest(productIds, requestedFields, start, end, "test");
        this.setValidHeader(request);

        request.setCountRequested(-1);
        this.assertRequestValidation(request, 1);

        request.setCountRequested(0);
        this.assertRequestValidation(request, 1);

        // Make sure that null is accepted as it is optional
        request.setCountRequested(null);
        this.assertRequestValidation(request, 0);
    }

    @Test
    public void validate_fail_invalidNameMapField() {
        Map<String,Map<String, String>> map = Maps.newHashMap();

        // Empty map
        PromoActivityReportRequest request = this.createValidRequest();
        request.setNameMap(map);
        this.assertRequestValidation(request, 0);

        // null value in map
        map.put("state", null);
        this.assertRequestValidation(request, 1);

        // Corrected
        Map<String, String> validValue = new HashMap<String,String>();
        validValue.put("1", "ProductName1");
        map.put("state", validValue);
        this.assertRequestValidation(request, 0);

        // Unsupported field in map
        map.clear();
        map.put("unknown", validValue);
        this.assertRequestValidation(request, 1);
    }

    @Test
    public void validate_JSON_deserialize() throws ParseException {

        final String validRequestJson = "{\"sd\":\"2015-03-20\",\"ed\":\"2015-03-27\",\"pids\":[1,2],\"fields\":[\"ts\",\"country\",\"pri\"],\"count\":100,\"nameMap\":{\"pid\":[{\"1\":\"FriendlyName1\"},{\"2\":\"FriendlyName2\"}]}}";
        PromoActivityReportRequest request = JSON.parseObject(validRequestJson, PromoActivityReportRequest.class);
        setValidHeader(request);

        Assert.assertNotNull(request);

//        List<ErrorInfo> errors = request.validateRequest();
        assertRequestValidation(request, 0);
        
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
        Assert.assertEquals(request.getStartDate(), f.parse("2015-03-20"));
        Assert.assertEquals(request.getEndDate(), f.parse("2015-03-27"));
        
        Assert.assertEquals(request.getProductIds().size(), 2);
        Assert.assertEquals(request.getFields().size(), 3);
        Assert.assertEquals(request.getRequestedDAORecordsCount(), 100);
        Assert.assertEquals(request.getNameMap().size(), 1);        
    }

    private void setValidHeader(PromoActivityReportRequest request) {
        PlatformRequestHeader header = new PlatformRequestHeader();
        header.setClientId("client1");
        header.setCompanyId(1);
        header.setRequestId(UUID.randomUUID().toString());
        header.setUserId("user1");
        header.setUserIp("192.168.1.1");

        request.setRequestHeader(header);
    }

    private void assertRequestValidation(PromoActivityReportRequest request, int errorCount) {
        List<ErrorInfo> errors = request.validateRequest();
        Assert.assertNotNull(errors);
        Assert.assertEquals(errors.size(), errorCount);
    }
}
