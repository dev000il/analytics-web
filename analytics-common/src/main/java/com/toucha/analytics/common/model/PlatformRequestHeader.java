package com.toucha.analytics.common.model;

import com.alibaba.fastjson.annotation.JSONField;

public class PlatformRequestHeader {
    @JSONField(name = "uid")
    private String userId;
    @JSONField(name = "clid")
    private String clientId;
    @JSONField(name = "cid")
    private Integer companyId;
    @JSONField(name = "uip")
    private String userIp;
    @JSONField(name = "rid")
    private String requestId;

    public PlatformRequestHeader() {
        this.userId = "";
        this.clientId = "";
        this.companyId = -1;
        this.userIp = "";
        this.requestId = "";
    }

    public PlatformRequestHeader(String userId, String clientId, Integer companyId, String userIp, String requestId) {
        this.userId = userId;
        this.clientId = clientId;
        this.companyId = companyId;
        this.userIp = userIp;
        this.requestId = requestId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public Integer getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Integer companyId) {
        this.companyId = companyId;
    }

    public String getUserIp() {
        return userIp;
    }

    public void setUserIp(String userIp) {
        this.userIp = userIp;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    @Override
    public String toString() {
        return "{\"uid\":\"" + userId + "\",\"clid\":\"" + clientId + "\",\"cid\":" + companyId + ",\"uip\":\"" + userIp
                + "\",\"rid\":\"" + requestId + "\"}";
    }

}
