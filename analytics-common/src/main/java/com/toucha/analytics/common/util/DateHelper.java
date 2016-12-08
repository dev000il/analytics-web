package com.toucha.analytics.common.util;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

import com.toucha.platform.common.enums.DateTimeUnit;

public class DateHelper {

    private static final ThreadLocal<SimpleDateFormat> hourDateFormatter = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyyMMddHH");
        }
    };

    public static long getHourUnixTime(long timeTick) throws ParseException {
        Date d = new Date(timeTick);
        String hourDateStr = hourDateFormatter.get().format(d);
        return hourDateFormatter.get().parse(hourDateStr).getTime();
    }

    public static Date getCurrentUtcTime() throws ParseException {

        Date utcDateTime = null;
        Date localDateTime = new Date();

        DateFormat localFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        TimeZone timeZone = TimeZone.getTimeZone("GMT-0");
        localFormat.setTimeZone(timeZone);

        String strUtcDateTime = localFormat.format(localDateTime);

        DateFormat utcFomat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        utcDateTime = utcFomat.parse(strUtcDateTime);

        return utcDateTime;
    }

    public static Date getGMTadd8() {

        Date beijingDateTime = null;

        try {

            beijingDateTime = DateUtils.addHours(getCurrentUtcTime(), 8);

        } catch (ParseException e) {
            AppEvents.LogException(e, AppEvents.ParseExceptionFunction, "getGMTadd8", "DateHelper");

            beijingDateTime = DateUtils.addHours(new Date(), 8);
        }

        return beijingDateTime;
    }

    // 获取当前时间
    public static Timestamp getCurrentTime() {
        return new java.sql.Timestamp(DateHelper.getGMTadd8().getTime());
    }

    /**
     * 重新计算当前时间前后的某个日期时间
     * 
     * @param amount
     *            变化的时间值,负值则计算before,否则after
     * @param dateTimeUnit
     *            时间单位,为DataTimeUnit里面的值
     * @return
     */
    public static Date getPreviousOrLaterTime(final int amount, final String dateTimeUnit) {
        Date date = null;
        Date currentTime = getGMTadd8();// 获取当前北京时间

        // getYear()是从1900开始算起
        if (StringUtils.isEmpty(dateTimeUnit) || amount == 0) {
            return null;
        }
        if (dateTimeUnit.equals(DateTimeUnit.YEAR.getValue())) {
            date = DateUtils.addYears(currentTime, amount);
        }
        if (dateTimeUnit.equals(DateTimeUnit.MONTH.getValue())) {
            date = DateUtils.addMonths(currentTime, amount);
        }
        if (dateTimeUnit.equals(DateTimeUnit.DAY.getValue())) {
            date = DateUtils.addDays(currentTime, amount);
        }
        if (dateTimeUnit.equals(DateTimeUnit.HOUR.getValue())) {
            date = DateUtils.addHours(currentTime, amount);
        }
        if (dateTimeUnit.equals(DateTimeUnit.MINUTIE.getValue())) {
            date = DateUtils.addMinutes(currentTime, amount);
        }
        if (dateTimeUnit.equals(DateTimeUnit.SECOND.getValue())) {
            date = DateUtils.addSeconds(currentTime, amount);
        }
        return date;
    }

    /**
     * 重新计算当前时间前后的某个日期时间 封装getPreviousOrLaterTimestamp
     * 
     * @param amount
     *            变化的时间值,负值则计算before,否则after
     * @param dateTimeUnit
     *            时间单位,为DataTimeUnit里面的值
     * @return 时间戳
     */
    public static Timestamp getPreviousOrLaterTimestamp(final int amount, final String dateTimeUnit) {
        return new java.sql.Timestamp(getPreviousOrLaterTime(amount, dateTimeUnit).getTime());
    }

    /**
     * 日期转换成字符串
     * 
     * @param date
     * @return str
     */
    public static String DateToStr(Date date) {

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String str = format.format(date);
        return str;
    }

    /**
     * 字符串转换成日期
     * 
     * @param str
     * @return date
     */
    public static Date StrToDate(String str) {

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = null;
        try {
            date = format.parse(str);
        } catch (ParseException e) {
            AppEvents.LogException(e, AppEvents.ParseExceptionFunction, "StrToDate", "DateHelper");
            e.printStackTrace();
        }
        return date;
    }

    public static Date StrToShortDate(String str) {

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;
        try {
            date = format.parse(str);
        } catch (ParseException e) {
            AppEvents.LogException(e, AppEvents.ParseExceptionFunction, "StrToShortDate", "DateHelper");
            e.printStackTrace();
        }
        return date;
    }

    public static Date StrToMonthDate(String str) {

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM");
        Date date = null;
        try {
            date = format.parse(str);
        } catch (ParseException e) {
            AppEvents.LogException(e, AppEvents.ParseExceptionFunction, "StrToMonthDate", "DateHelper");
            e.printStackTrace();
        }
        return date;
    }

    public static Date convert2Date(final Date time) {
        Date date = null;
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            String str = format.format(time);
            date = format.parse(str);
        } catch (ParseException e) {
            AppEvents.LogException(e, AppEvents.ParseExceptionFunction, "convert2Date", "DateHelper");
        }
        return date;
    }

    /**
     * convert java date to string, just date without time
     * 
     * @param time
     * @return date string
     */
    public static String convert2DateStr(final Date time) {
        String date = null;
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            date = format.format(time);
        } catch (Exception e) {
            AppEvents.LogException(e, AppEvents.ParseExceptionFunction, "convert2DateStr", "DateHelper");
        }
        return date;
    }

    /**
     * convert java date and time to string
     * 
     * @param time
     * @return date string
     */
    public static String convert2DateTimeStr(final Date time) {
        String date = null;
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            date = format.format(time);
        } catch (Exception e) {
            AppEvents.LogException(e, AppEvents.ParseExceptionFunction, "convert2DateTimeStr", "DateHelper");
        }
        return date;
    }

    /**
     * get date of month which prefix current date
     * 
     * @param prefixDay
     *            how many days you want
     * @return date string
     */
    public static String getPrefixDateStr(int prefixDay) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT+08:00"));
        c.add(Calendar.DAY_OF_MONTH, prefixDay);
        return sdf.format(c.getTime());
    }
}