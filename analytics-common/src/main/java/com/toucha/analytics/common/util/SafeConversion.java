/**
 * 
 */
package com.toucha.analytics.common.util;

import java.math.BigDecimal;
import java.text.ParseException;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Preconditions;

public class SafeConversion {

    public static BigDecimal tryGetBigDecimal(String str) {

        Preconditions.checkArgument(StringUtils.isNotBlank(str), "");

        BigDecimal result = null;

        try {
            result = new BigDecimal(str);
        } catch (Exception ex) {
            AppEvents.LogException(ex, AppEvents.ScanReportService.ParameterCheckFailure);
        }
        
        return result;
    }
    
    public static Long tryGetHourTimeTick(String timeStr) {
        
        Preconditions.checkArgument(StringUtils.isNotBlank(timeStr), "");
        
        Long timeTick = null;
        
        try {
            timeTick = DateHelper.getHourUnixTime(Long.parseLong(timeStr));
        } catch (NumberFormatException | ParseException ex) {
            AppEvents.LogException(ex, AppEvents.ScanReportService.ParameterCheckFailure);
            timeTick = null;
        }
        
        return timeTick;
    }
}
