package com.toucha.analytics.web;

import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONArray;
import com.toucha.analytics.common.common.GeneralConstants;
import com.toucha.analytics.common.exceptions.ServiceException;
import com.toucha.analytics.service.ConfigService;
import com.toucha.analytics.utils.ControllerUtil;

@Controller
@RequestMapping("config")
public class ConfigController {
	
	@Resource(name = "configService")
    private ConfigService configService;
	

	  @RequestMapping("getScheduledjobAeList")
	    public @ResponseBody List<Integer> getScheduledjobAeList(HttpServletRequest request,
	            HttpServletResponse response) {
	        try {
	        	return configService.getScheduledjobIgnoreAeList();
	        } catch (ServiceException ex) {
	            response.setStatus(GeneralConstants.RESPONSE_STATUS_500);
	            return null;
	        }
	    }
	  
	  @RequestMapping("updateSchedulejobAeList")
	    public @ResponseBody Boolean updateScheduledjobAeList(HttpServletRequest request,
	            HttpServletResponse response) {
	        try {
	        	JSONArray json = ControllerUtil.getJsonArray(request);
	        	String params = json.toJSONString();
	        	return configService.updateScheduledjobIgnoreAeList(params);
	        } catch (ServiceException ex) {
	            response.setStatus(GeneralConstants.RESPONSE_STATUS_500);
	            return false;
	        }
	    }
	  
	  
	  @RequestMapping("clearSchedulejobAeList")
	    public @ResponseBody Boolean clearScheduledjobAeList(HttpServletRequest request,
	            HttpServletResponse response) {
	        try {
	        	return configService.clearScheduledjobIgnoreAeList();
	        } catch (ServiceException ex) {
	            response.setStatus(GeneralConstants.RESPONSE_STATUS_500);
	            return false;
	        }
	    }
	  
}
