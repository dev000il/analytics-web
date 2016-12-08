package com.toucha.analytics.common.model;

import java.io.Serializable;

import com.alibaba.fastjson.annotation.JSONField;

public class ErrorInfo implements Serializable {

    private static final long serialVersionUID = 7158273723112831837L;

    @JSONField(name = "code")
    private String code;

    @JSONField(name = "msg")
    private String msg;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public ErrorInfo() {
    }

    public ErrorInfo(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public ErrorInfo(String code) {
        this.code = code;
    }
}
