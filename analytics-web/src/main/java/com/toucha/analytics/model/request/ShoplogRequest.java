package com.toucha.analytics.model.request;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.annotation.JSONField;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

public class ShoplogRequest extends TimeRangeReportRequest {

    private static final long serialVersionUID = -2814078945452733543L;

    public final static ImmutableMap<String, String> supportedFieldsFriendlyName;

    static {
        Map<String, String> map = new HashMap<String, String>();
        map.put("ts", "时间");
        map.put("city", "城市");
        map.put("wu", "微信ID");
        map.put("pid", "产品");
        map.put("pri", "活动");
        map.put("rwds", "中奖奖品");
        map.put("okid", "兑奖状态");
        map.put("orn", "订单号");
        map.put("hid", "二维码ID");
        map.put("rid", "兑奖奖品");

        supportedFieldsFriendlyName = ImmutableMap.copyOf(map);
    }

    @JSONField(name = "dids")
    private List<Integer> dids;

    private List<String> oids;

    private List<Integer> pris;

    @JSONField(name = "fields")
    private ImmutableList<String> fields;

    @JSONField(name = "count")
    private Integer countRequested;

    @JSONField(name = "nameMap")
    private Map<String, Map<String, String>> nameMap;

    private int type;

    public List<Integer> getDids() {
        return dids;
    }

    public void setDids(List<Integer> dids) {
        this.dids = dids;
    }

    public List<String> getOids() {
        return oids;
    }

    public void setOids(List<String> oids) {
        this.oids = oids;
    }

    public List<Integer> getPris() {
        return pris;
    }

    public void setPris(List<Integer> pris) {
        this.pris = pris;
    }

    public Integer getCountRequested() {
        return countRequested;
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

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public ImmutableList<String> getFields() {
        return this.fields;
    }

    public void setFields(List<String> fields) {
        this.fields = ImmutableList.copyOf(fields);
    }
}
