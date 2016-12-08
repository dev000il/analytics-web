package com.toucha.analytics.test.util;

import com.alibaba.fastjson.JSONObject;
import com.toucha.analytics.common.model.PlatformRequestHeader;

public class PutHeaderUtilTest {

    public static void putHeaderAdmin(JSONObject jo) {
        jo.put("header", new PlatformRequestHeader("elaine", "Zhabei", -1, "192.168.6.74", "requestId1101"));
    }
    
    public static void putHeader(JSONObject jo) {
        jo.put("header", new PlatformRequestHeader("elaine", "hangzhou-project", 2, "192.168.6.74", "requestId1101"));
    }
}
