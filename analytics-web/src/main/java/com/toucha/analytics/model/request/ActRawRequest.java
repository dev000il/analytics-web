package com.toucha.analytics.model.request;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.annotation.JSONField;
import com.toucha.analytics.common.model.BaseRequestNormal;
import com.toucha.analytics.common.model.ErrorInfo;
import com.toucha.analytics.common.util.AppEvents;

public class ActRawRequest extends BaseRequestNormal {

    private static final long serialVersionUID = -6450691647378401614L;

    String table;

    static {
        Map<String, String> map = new HashMap<String, String>();
        map.put("ts", "时间");
        map.put("oip", "IP地址");
        map.put("country", "国家");
        map.put("state", "省份");
        map.put("city", "城市");

        map.put("at", "用户行为类型");
        map.put("pn", "手机号");
        map.put("tu", "淘宝用户ID");
        map.put("wu", "微信用户ID");

        map.put("num", "标签序列号");
        map.put("c", "公司id");
        map.put("bid", "批次号");
        map.put("pid", "产品");
        map.put("pri", "推广ID");
        map.put("ptri", "积分ID");
        map.put("pts", "积分情况");
        map.put("rwds", "兑奖情况");

        map.put("rn", "中奖情况");
        map.put("rt", "兑奖方式");
        map.put("ra", "兑奖数量");
        map.put("okid", "兑奖状态");
        map.put("ok", "兑奖状态");
        map.put("vid", "兑奖商户ID");

        map.put("rmk", "备注");
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    @JSONField(name = "fields")
    private List<String> fields;

    @JSONField(name = "nameMap")
    private Map<String, Map<String, String>> nameMap;

    @JSONField(name = "sd")
    private Date startDate;

    @JSONField(name = "sk")
    private String startKey;

    private int topn;

    @JSONField(name = "sv")
    private String searchValue;

    public void convertStartDate() {
        startDate = new Date(startDate.getTime() + (59 * 60 + 59) * 1000);
    }

    public int getTopn() {
        return topn;
    }

    public void setTopn(int topn) {
        this.topn = topn;
    }

    public String getStartKey() {
        return startKey;
    }

    public void setStartKey(String startKey) {
        this.startKey = startKey;
    }

    public List<String> getFields() {
        return fields;
    }

    public void setFields(List<String> fields) {
        this.fields = fields;
    }

    public Map<String, Map<String, String>> getNameMap() {
        return nameMap;
    }

    public void setNameMap(Map<String, Map<String, String>> nameMap) {
        this.nameMap = nameMap;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public String getSearchValue() {
        return searchValue;
    }

    public void setSearchValue(String searchValue) {
        this.searchValue = searchValue;
    }

    @Override
    public void validateRequestCore(List<ErrorInfo> errors) {
        // TODO Auto-generated method stub
        super.validateRequestCore(errors);
        if (!fields.contains("company")) {
            errors.add(AppEvents.ScanReportServiceRequests.MissingCompanyId.toErrorInfo());
        }
    }

    @Override
    public String toString() {
        return "ActRawRequest [table=" + table + ", fields=" + Arrays.toString(fields.toArray()) + ", startDate=" + startDate
                + ", startKey=" + startKey + ", topn=" + topn + "]";
    }

}
