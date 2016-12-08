package com.toucha.analytics.model.request;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.annotation.JSONField;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.toucha.analytics.common.model.ErrorInfo;
import com.toucha.analytics.common.util.AppEvents;

public class PromoActivityReportRequest extends TimeRangeReportRequest {

	private static final long serialVersionUID = 124287929195594875L;

	// All fields can be found in the wiki
	private final static ImmutableSet<String> supportedFields;
	
	public final static ImmutableMap<String, String> supportedFieldsFriendlyName;

	private final static Integer MAX_RECORDS_TOPCLAUSE = Integer.valueOf(200);

	static {
	    Map<String, String> map = new HashMap<String, String>();
	    map.put("ts", "时间");
	    map.put("oip","IP地址");
	    map.put("country", "国家");
	    map.put("state","省份");
        map.put("city", "城市");
       
	    map.put("at","用户行为类型");
	    map.put("pn", "手机号");
        map.put("tu","淘宝用户ID");
	    map.put("wu","微信用户ID");
	    
	    map.put("num","标签序列号");
	    map.put("c", "公司id");
	    map.put("bid","批次号");
	    map.put("pid", "产品");
        map.put("pri","推广ID");
	    map.put("ptri","积分ID");
	    map.put("pts", "积分情况");
        map.put("rwds", "兑奖情况");
        
	    map.put("rn","中奖情况");
	    map.put("rt","兑奖方式");
	    map.put("ra","兑奖数量");
	    map.put("okid","兑奖状态");
	    map.put("ok","兑奖状态");
	    map.put("vid","兑奖商户ID");
	    
	    map.put("rmk","备注");
	    
	    supportedFields = ImmutableSet.copyOf(map.keySet());
	    supportedFieldsFriendlyName = ImmutableMap.copyOf(map);
	}
	
	@JSONField(name = "pids")
	private List<Integer> productIds;

	@JSONField(name = "fields")
	private ImmutableList<String> fields;

	@JSONField(name = "count")
	private Integer countRequested;

    @JSONField(name = "nameMap")
    private Map<String,Map<String, String>> nameMap;

	public PromoActivityReportRequest() {
	}

	public PromoActivityReportRequest(List<Integer> productIds,
	        ImmutableList<String> fields, Date startDate, Date endDate,
			String environment) {
		super(startDate, endDate);

		Preconditions.checkNotNull(productIds);
		Preconditions.checkNotNull(fields);

		this.productIds = productIds;
		this.fields = fields;
	}

	public List<Integer> getProductIds() {
		return this.productIds;
	}

	public void setProductIds(List<Integer> productIds) {
		this.productIds = productIds;
	}

	public ImmutableList<String> getFields() {
		return this.fields;
	}

	public void setFields(List<String> fields) {
		this.fields = ImmutableList.copyOf(fields);
	}

	/**
	 * This service endpoint corresponding to this request object, will only
	 * allow a max amount of records to be returned.
	 * 
	 * @return The amount of rows to return from the DAO.
	 */
	public int getRequestedDAORecordsCount() {
		// if request raw activities, No specific amount was requested, or is greater than MAX allowed.
		if (this.countRequested == null || this.countRequested > MAX_RECORDS_TOPCLAUSE) {
		    return MAX_RECORDS_TOPCLAUSE.intValue();
		}

		// Returning negative values will trigger the DAO to return all entries, prevent that by
		// adding this check here.  Not allowed on this API request.
		if (this.countRequested < Integer.valueOf(0)) {
		    return 0;
		}
		
		return this.countRequested.intValue();
	}

	protected Integer getCountRequested() {
	    return this.countRequested;
	}
	
	public void setCountRequested(Integer countRequested) {
		this.countRequested = countRequested;
	}

	public Map<String, Map<String, String>> getNameMap() {
		return nameMap;
	}

	public void setNameMap(Map<String, Map<String, String>> nameMap) {
		this.nameMap = nameMap;
	}
	
	@Override
	public void validateRequestCore(List<ErrorInfo> errors) {
		super.validateRequestCore(errors);

		// Verify that some product ids were sent.
		if (productIds == null || productIds.isEmpty()) {
			errors.add(createErrorWithUserAndCompany(AppEvents.ScanReportServiceRequests.MissingProductId));
		} else {
		    for (Integer i : productIds) {
				if (i <= 0) {
					errors.add(createErrorWithUserAndCompany(AppEvents.ScanReportServiceRequests.MissingProductId));
					break;
				}
			}
		}
		
		// Verify that the fields requested for are in our supported list.
		if (fields == null || fields.isEmpty()) {
			errors.add(AppEvents.ScanReportServiceRequests.SpecifiedFieldsNotSupported
					.toErrorInfo());
		} else {
			for (String field : fields) {
				if (Strings.isNullOrEmpty(field)
						|| !supportedFields.contains(field)) {
					errors.add(AppEvents.ScanReportServiceRequests.SpecifiedFieldsNotSupported
							.toErrorInfo());
					break;
				}
			}
			
			// Note: Currently, 'productIds' are mandatory, so we can do this check.
			// Verify that the "pid" is in the selected fields list
	        if (!fields.contains("pid")) {
	            errors.add(AppEvents.ScanReportServiceRequests.ProductIdFieldNotSpecified
	                    .toErrorInfo());
	        }
		}

		// Verify that if the requested count was set, it is a positive non-zero
		// integer
//		if (this.countRequested != null && this.countRequested <= 0) {
//			errors.add(AppEvents.ScanReportServiceRequests.General_ExpectPositiveInteger
//					.toErrorInfo("count"));
//		}

		// Verify the name map contains only valid fields and are non null.
		if (this.nameMap != null && !this.nameMap.isEmpty()) {
			for (String key : this.nameMap.keySet()) {
				if (Strings.isNullOrEmpty(key)
						|| !supportedFields.contains(key)
						|| this.nameMap.get(key) == null) {
					errors.add(AppEvents.ScanReportServiceRequests.SpecifiedFieldsNotSupported
							.toErrorInfo());
					break;
				}
			}
		}
	}

	@Override
	public String toString() {
		return "PromoActivityReportRequest [productIds=" + this.productIds
				+ ", fields=" + this.fields + ", count=" + this.countRequested
				+ ", startDate=" + this.getStartDate() + ", endDate="
				+ this.getEndDate() + "]";
	}
}
