package com.toucha.analytics.common.util;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import org.testng.Assert;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

/**
 * Http client util testing
 *
 * @author senhui.li
 */
public class HttpClientUtilTest {

    private String url = "https://platform.sao.so/datapipeline/totaltagcnt";

    //@Test
    public void testPost() throws Exception {
        String result = HttpClientUtil.signPost(url, HttpClientUtil.APPLICATION_JSON, "");
        Assert.assertNotNull(result);
        JSONObject tmp = JSON.parseObject(result);
        assertTrue(tmp.getBoolean("success"));
        assertNotNull(tmp.getString("data"));
        System.out.println("data:"+tmp.getString("data"));
    }
}