package com.toucha.analytics.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.toucha.analytics.common.util.AppEvents;

public class ControllerUtil {

	
	 public static JSONArray getJsonArray(HttpServletRequest request) {
		 JSONArray checkInfo = null;
	        try {

	            BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream(), "utf-8"));
	            String line = null;
	            StringBuilder sb = new StringBuilder();
	            while ((line = br.readLine()) != null) {
	                sb.append(line);
	            }
	            // 将资料解码
	            String reqBody = sb.toString();
	            // reqBody = URLDecoder.decode(reqBody, "utf-8");
	            checkInfo = JSON.parseArray(reqBody);
	        } catch (Exception e) {
	            AppEvents.LogException(e, AppEvents.ParseRequestBodyException);
	            e.printStackTrace();
	        }
	        return checkInfo;
	    }
	 
    public static JSONObject buffer(HttpServletRequest request) {
        JSONObject checkInfo = null;
        try {

            BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream(), "utf-8"));
            String line = null;
            StringBuilder sb = new StringBuilder();
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            // 将资料解码
            String reqBody = sb.toString();
            // reqBody = URLDecoder.decode(reqBody, "utf-8");
            checkInfo = JSONObject.parseObject(reqBody);
        } catch (Exception e) {
            AppEvents.LogException(e, AppEvents.ParseRequestBodyException);
            e.printStackTrace();
        }
        return checkInfo;
    }

    public static List<JSONObject> getJsonList(HttpServletRequest request) {

        List<JSONObject> list = new ArrayList<JSONObject>();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream(), "utf-8"));
            String line = null;

            while ((line = br.readLine()) != null) {
                if (StringUtils.isNotBlank(line)) {
                    list.add(JSONObject.parseObject(line));
                }
            }
            // 将资料解码

        } catch (Exception e) {
            AppEvents.LogException(e, AppEvents.ParseRequestBodyException);
            e.printStackTrace();
        }
        return list;
    }
    
    
    /**
     * 判断字符串是否是整数
     */
    public static boolean isInteger(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    
    // check if chinese character
    public static boolean isChinese(String s) {  
    	for(char c: s.toCharArray()){
    		Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);  
    		if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS  
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS  
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A  
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION  
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION  
                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {  
            return true;  
            }  
    	}
    	return false;
    }

}
