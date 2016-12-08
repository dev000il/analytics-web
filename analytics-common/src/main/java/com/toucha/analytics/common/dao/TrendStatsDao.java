package com.toucha.analytics.common.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.toucha.analytics.common.common.DBConnection;
import com.toucha.analytics.common.model.TimeBasedReportStatUnit;
import com.toucha.analytics.common.util.DateHelper;
import com.toucha.analytics.common.util.NextPowerOf2Util;
import com.toucha.platform.common.enums.DateTimeUnit;

/**
 * This DAO design for the trend statics report Which require years, months and
 * weeks analytic statics
 * 
 * @author senhui.li
 */
public class TrendStatsDao {

    private static final String ZHABEI = "zhabei";

    private static final String JINGAN = "jingan";

    public List<TimeBasedReportStatUnit<Integer>> getNewUserTotalStats(int companyId, DateTimeUnit time) throws SQLException {
        List<TimeBasedReportStatUnit<Integer>> zhabeiList = getNewUserStats(companyId, time);
        List<TimeBasedReportStatUnit<Integer>> jinganList = getJANewUserStats(companyId, time);
        return getTrendTotalStatics(zhabeiList, jinganList);
    }

    private List<TimeBasedReportStatUnit<Integer>> getNewUserStats(int companyId, DateTimeUnit time) throws SQLException {
        String query = "SELECT DATE_FORMAT(`timestamp`, '%s') AS statsTime, SUM(`count`) AS sumCnt FROM membercounts WHERE usertype = 2 AND ";
        return getTrendStatics(query, companyId, time, ZHABEI);
    }

    //jingan new user
    private List<TimeBasedReportStatUnit<Integer>> getJANewUserStats(int companyId, DateTimeUnit time) throws SQLException {
        String query = "SELECT DATE_FORMAT(`timestamp`, '%s') AS statsTime, SUM(`count`) AS sumCnt FROM ja_act_membercount WHERE user_type = 2 AND ";
        return getTrendStatics(query, companyId, time, JINGAN);
    }

    public List<TimeBasedReportStatUnit<Integer>> getLotteryTotalStats(int companyId, DateTimeUnit time) throws SQLException {
        List<TimeBasedReportStatUnit<Integer>> zhabeiList = getEnterLotteryStats(companyId, time);
        List<TimeBasedReportStatUnit<Integer>> jinganList = getJALotteryStats(companyId, time);
        return getTrendTotalStatics(zhabeiList, jinganList);
    }

    private List<TimeBasedReportStatUnit<Integer>> getEnterLotteryStats(int companyId, DateTimeUnit time) throws SQLException {
        String query = "SELECT DATE_FORMAT(`timestamp`, '%s') AS statsTime, SUM(`count`) AS sumCnt FROM enterlotterycounts WHERE ";
        return getTrendStatics(query, companyId, time, ZHABEI);
    }

    //jingan lottery
    private List<TimeBasedReportStatUnit<Integer>> getJALotteryStats(int companyId, DateTimeUnit time) throws SQLException {
        String query = "SELECT DATE_FORMAT(`timestamp`, '%s') AS statsTime, SUM(`count`) AS sumCnt FROM ja_act_lotterycount WHERE ";
        return getTrendStatics(query, companyId, time, JINGAN);
    }

    public List<TimeBasedReportStatUnit<Integer>> getScanTagsTotalStats(int companyId, DateTimeUnit time) throws SQLException {
        List<TimeBasedReportStatUnit<Integer>> zhabeiList = getScanTagsStats(companyId, time);
        List<TimeBasedReportStatUnit<Integer>> jinganList = getJAScanTagsStats(companyId, time);
        return getTrendTotalStatics(zhabeiList, jinganList);
    }

    private List<TimeBasedReportStatUnit<Integer>> getScanTagsStats(int companyId, DateTimeUnit time) throws SQLException {
        String query = "SELECT DATE_FORMAT(`timestamp`, '%s') AS statsTime, SUM(newtagcount) AS tagsCnt FROM activitycounts WHERE ";
        return getTrendStatics(query, companyId, time, ZHABEI);
    }

    //jingan scan tags
    private List<TimeBasedReportStatUnit<Integer>> getJAScanTagsStats(int companyId, DateTimeUnit time) throws SQLException {
        String query = "SELECT DATE_FORMAT(`timestamp`, '%s') AS statsTime, SUM(newtagcount) AS tagsCnt FROM ja_act_scancount WHERE ";
        return getTrendStatics(query, companyId, time, JINGAN);
    }

    private List<TimeBasedReportStatUnit<Integer>> getTrendTotalStatics(List<TimeBasedReportStatUnit<Integer>> zhabeiList,
            List<TimeBasedReportStatUnit<Integer>> jinganList) {
        System.out.println("the size of zhabei is:" + zhabeiList.size());
        System.out.println("the size of jingan is:" + jinganList.size());

        Map<Date, Integer> map = new ConcurrentHashMap<Date, Integer>(NextPowerOf2Util.nextPowerOf2(zhabeiList.size()));
        List<TimeBasedReportStatUnit<Integer>> combineList = jinganList;

        if (zhabeiList.size() > 0 && jinganList.size() > 0) {
            for (TimeBasedReportStatUnit<Integer> zbReportStatUnit : zhabeiList) {
                map.put(zbReportStatUnit.getHour(), zbReportStatUnit.getMeasure());
            }

            combineList = structDataFromMap(map, jinganList);
        }

        if (jinganList.size() == 0) {
            combineList = zhabeiList;
        }

        List<TimeBasedReportStatUnit<Integer>> totalList = new LinkedList<TimeBasedReportStatUnit<Integer>>();
        for (TimeBasedReportStatUnit<Integer> reportStat : combineList) {
            int totalCount = reportStat.getMeasure();
            Date jaHour = reportStat.getHour();
            if (map.get(jaHour) != null) {
                totalCount = totalCount + map.get(jaHour);
                map.remove(jaHour);
            }

            TimeBasedReportStatUnit<Integer> timeBasedReportStatUnit = new TimeBasedReportStatUnit<Integer>(jaHour, totalCount);
            totalList.add(timeBasedReportStatUnit);
        }

        System.out.println("the total size is:" + totalList.size());

        return totalList;
    }

    private List<TimeBasedReportStatUnit<Integer>> structDataFromMap(Map<Date, Integer> map,
            List<TimeBasedReportStatUnit<Integer>> jinganList) {
        List<TimeBasedReportStatUnit<Integer>> fuzhuList = jinganList;
        for (Date hour : map.keySet()) {
            boolean noRecord = true;

            for (TimeBasedReportStatUnit<Integer> jaReportStatUnit : fuzhuList) {
                if (jaReportStatUnit.getHour().equals(hour) == false) {
                    continue;
                }

                noRecord = false;
                break;
            }

            if (noRecord) {
                TimeBasedReportStatUnit<Integer> timeBasedReportStatUnit = new TimeBasedReportStatUnit<Integer>(hour,
                        Integer.valueOf(0));
                jinganList.add(timeBasedReportStatUnit);
            }
        }

        jinganList = reRangeList(jinganList);
        return jinganList;
    }

    @SuppressWarnings("unchecked")
    private List<TimeBasedReportStatUnit<Integer>> reRangeList(List<TimeBasedReportStatUnit<Integer>> totalList) {
        Object[] array = totalList.toArray();
        TimeBasedReportStatUnit<Integer> temp = null;
        for (int i = 0; i < array.length - 1; i++) {
            TimeBasedReportStatUnit<Integer> iReportStatUnit = (TimeBasedReportStatUnit<Integer>) array[i];
            for (int j = i + 1; j < array.length; j++) {
                TimeBasedReportStatUnit<Integer> jReportStatUnit = (TimeBasedReportStatUnit<Integer>) array[j];
                if (iReportStatUnit.getHour().after(jReportStatUnit.getHour())) {
                    temp = (TimeBasedReportStatUnit<Integer>) array[i];
                    array[i] = array[j];
                    array[j] = temp;
                    iReportStatUnit = (TimeBasedReportStatUnit<Integer>) array[i];
                }
            }
        }

        List<TimeBasedReportStatUnit<Integer>> newTotalList = new LinkedList<TimeBasedReportStatUnit<Integer>>();
        for (int i = 0; i < array.length; i++) {
            newTotalList.add((TimeBasedReportStatUnit<Integer>) array[i]);
        }

        return newTotalList;
    }

    private List<TimeBasedReportStatUnit<Integer>> getTrendStatics(String query, int companyId, DateTimeUnit time,
            String serverName) throws SQLException {

        query = generateQuery(query, companyId, time);

        List<TimeBasedReportStatUnit<Integer>> result = new LinkedList<TimeBasedReportStatUnit<Integer>>();

        Connection conn = null;
        Statement stmt = null;
        ResultSet resultSet = null;

        try {
            if (ZHABEI.equals(serverName)) {
                conn = DBConnection.getPipelineConnection();
            } else {
                conn = DBConnection.getShopConnection();
            }
            stmt = conn.createStatement();
            resultSet = stmt.executeQuery(query);
            while (resultSet.next()) {
                String tmp = resultSet.getString(1);
                Date statsTime = null;
                if (time == DateTimeUnit.YEAR) {
                    statsTime = DateHelper.StrToMonthDate(tmp);
                } else {
                    statsTime = DateHelper.StrToShortDate(tmp);
                }
                int tagsCnt = resultSet.getInt(2);
                TimeBasedReportStatUnit<Integer> unit = new TimeBasedReportStatUnit<Integer>(statsTime, tagsCnt);
                result.add(unit);
            }
        } catch (SQLException e) {
            throw new SQLException(e);
        } finally {
            DBConnection.closeResultSet(resultSet);
            DBConnection.closeStatement(stmt);
            DBConnection.closeConnection(conn);
        }

        return result;
    }

    private String generateQuery(String query, int companyId, DateTimeUnit time) {
        String queryEnd = " `timestamp` BETWEEN '%s' AND '%s' GROUP BY statsTime ORDER BY statsTime";

        Calendar cal = Calendar.getInstance();
        String startDate = null;
        String endDate = null;

        switch (time) {
            case YEAR:
                query = String.format(query, "%Y-%m");
                cal.add(Calendar.DAY_OF_MONTH, -1);
                endDate = getEndDateTime(cal);
                cal.add(Calendar.MONTH, -12);
                startDate = getStartDateTime(cal);
                break;
            case MONTH:
                query = String.format(query, "%Y-%m-%d");
                cal.add(Calendar.DAY_OF_MONTH, -1);
                endDate = getEndDateTime(cal);
                cal.add(Calendar.MONTH, -1);
                startDate = getStartDateTime(cal);
                break;
            case DAY:
                query = String.format(query, "%Y-%m-%d");
                cal.add(Calendar.DAY_OF_MONTH, -1);
                endDate = getEndDateTime(cal);
                cal.add(Calendar.DAY_OF_MONTH, -7);
                startDate = getStartDateTime(cal);
                break;
            default:
                startDate = DateHelper.convert2DateTimeStr(cal.getTime());
                endDate = DateHelper.convert2DateTimeStr(cal.getTime());
                break;
        }

        queryEnd = String.format(queryEnd, startDate, endDate);

        if (companyId == -1) {
            query += queryEnd;
        } else {
            query += (" company = " + companyId + " AND " + queryEnd);
        }

        System.out.println("Trend Query: " + query);
        return query;
    }

    private String getStartDateTime(Calendar cal) {

        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);

        return DateHelper.convert2DateTimeStr(cal.getTime());
    }

    private String getEndDateTime(Calendar cal) {

        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);

        return DateHelper.convert2DateTimeStr(cal.getTime());
    }
}
