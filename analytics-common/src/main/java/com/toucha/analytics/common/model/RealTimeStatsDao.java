package com.toucha.analytics.common.model;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.toucha.analytics.common.common.DBConnection;
import com.toucha.analytics.common.util.NextPowerOf2Util;

/**
 * The real time statistics query
 * 
 * @author senhui.li
 */
public class RealTimeStatsDao {

    private static final String ZHABEI = "zhabei";

    private static final String JINGAN = "jingan";

    private static final String Cnd_Member = "member";

    private static final String Cnd_JA_Member = "jamember";

    private static final String Cnd_Reward = "reward";

    public List<TimeBasedReportStatUnit<Integer>> getRealTimeScanDateRangeStatistics(long startDate, long endDate)
            throws SQLException {
        List<TimeBasedReportStatUnit<Integer>> zbRealTimeScan = getRealTimeDateRangeStatistics("activitycounts", "scan",
                startDate, endDate, ZHABEI);
        List<TimeBasedReportStatUnit<Integer>> jaRealTimeScan = getRealTimeDateRangeStatistics("ja_act_scancount", "scan",
                startDate, endDate, JINGAN);
        return getRealTimeStatistics(zbRealTimeScan, jaRealTimeScan);
    }

    public List<TimeBasedReportStatUnit<Integer>> getRealTimeEnterLotDateRangeStatistics(long startDate, long endDate)
            throws SQLException {
        List<TimeBasedReportStatUnit<Integer>> zbRealTimeEnterLot = getRealTimeDateRangeStatistics("enterlotterycounts",
                "enterlot", startDate, endDate, ZHABEI);
        List<TimeBasedReportStatUnit<Integer>> jaRealTimeEnterLot = getRealTimeDateRangeStatistics("ja_act_lotterycount",
                "enterlot", startDate, endDate, JINGAN);
        return getRealTimeStatistics(zbRealTimeEnterLot, jaRealTimeEnterLot);
    }

    public List<TimeBasedReportStatUnit<Integer>> getRealTimeMembersDateRangeStatistics(long startDate, long endDate)
            throws SQLException {
        List<TimeBasedReportStatUnit<Integer>> zbRealTimeMembers = getRealTimeDateRangeStatistics("membercounts", Cnd_Member,
                startDate, endDate, ZHABEI);
        List<TimeBasedReportStatUnit<Integer>> jaRealTimeMembers = getRealTimeDateRangeStatistics("ja_act_membercount",
                Cnd_JA_Member, startDate, endDate, JINGAN);
        return getRealTimeStatistics(zbRealTimeMembers, jaRealTimeMembers);
    }

    public List<TimeBasedReportStatUnit<Integer>> getRealTimeRewardsDateRangeStatistics(long startDate, long endDate)
            throws SQLException {
        List<TimeBasedReportStatUnit<Integer>> zbRealTimeReward = getRealTimeDateRangeStatistics("enterlotterycounts", Cnd_Reward,
                startDate, endDate, ZHABEI);
        List<TimeBasedReportStatUnit<Integer>> jaRealTimeReward = getRealTimeDateRangeStatistics("ja_act_lotterycount",
                Cnd_Reward, startDate, endDate, JINGAN);
        return getRealTimeStatistics(zbRealTimeReward, jaRealTimeReward);
    }

    private List<TimeBasedReportStatUnit<Integer>> getRealTimeStatistics(
            List<TimeBasedReportStatUnit<Integer>> zbRealTimeStatisticList,
            List<TimeBasedReportStatUnit<Integer>> jaRealTimeStatisticList) {

        System.out.println("the size of zhabei is:" + zbRealTimeStatisticList.size());
        System.out.println("the size of jingan is:" + jaRealTimeStatisticList.size());

        Map<Date, Integer> map = new HashMap<Date, Integer>(NextPowerOf2Util.nextPowerOf2(zbRealTimeStatisticList.size()));
        List<TimeBasedReportStatUnit<Integer>> combineList = jaRealTimeStatisticList;

        if (jaRealTimeStatisticList.size() > 0 && zbRealTimeStatisticList.size() > 0) {
            for (TimeBasedReportStatUnit<Integer> zbReportStatUnit : zbRealTimeStatisticList) {
                map.put(zbReportStatUnit.getHour(), zbReportStatUnit.getMeasure());
            }

            combineList = structDataFromMap(map, jaRealTimeStatisticList);
        }

        if (jaRealTimeStatisticList.size() == 0) {
            combineList = zbRealTimeStatisticList;
        }

        List<TimeBasedReportStatUnit<Integer>> realTimeStatisticList = new LinkedList<TimeBasedReportStatUnit<Integer>>();
        for (TimeBasedReportStatUnit<Integer> reportStat : combineList) {
            Date jaHour = reportStat.getHour();
            Integer totalCount = reportStat.getMeasure();
            if (map.get(jaHour) != null) {
                totalCount = totalCount + map.get(jaHour);
                map.remove(jaHour);
            }

            TimeBasedReportStatUnit<Integer> reportStatUnit = new TimeBasedReportStatUnit<Integer>(jaHour, totalCount);
            realTimeStatisticList.add(reportStatUnit);
        }

        System.out.println("the ended size is:" + realTimeStatisticList.size());

        return realTimeStatisticList;
    }

    private List<TimeBasedReportStatUnit<Integer>> structDataFromMap(Map<Date, Integer> map,
            List<TimeBasedReportStatUnit<Integer>> jaRealTimeStatisticList) {
        List<TimeBasedReportStatUnit<Integer>> fuzhuList = jaRealTimeStatisticList;
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
                jaRealTimeStatisticList.add(timeBasedReportStatUnit);
            }
        }

        jaRealTimeStatisticList = reRangeList(jaRealTimeStatisticList);
        return jaRealTimeStatisticList;
    }

    @SuppressWarnings("unchecked")
    private List<TimeBasedReportStatUnit<Integer>> reRangeList(List<TimeBasedReportStatUnit<Integer>> jaRealTimeStatisticList) {
        Object[] array = jaRealTimeStatisticList.toArray();
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

        List<TimeBasedReportStatUnit<Integer>> newRealTimeList = new LinkedList<TimeBasedReportStatUnit<Integer>>();
        for (int i = 0; i < array.length; i++) {
            newRealTimeList.add((TimeBasedReportStatUnit<Integer>) array[i]);
        }

        return newRealTimeList;
    }

    /**
     * 
     * @Title: 		 getRealTimeDateRangeStatistics   
     * @Description: Get the special date range statistics data for real time report  
     * @param tableName
     * query table
     * @param condition
     * @param startDate
     * start date time(UTC)
     * @param endDate
     * end date time (UTC)
     * @param serverName
     * which environment,zhabei or jingan
     * @return report statics unit list
     * @throws SQLException   
     * @throws
     */
    private List<TimeBasedReportStatUnit<Integer>> getRealTimeDateRangeStatistics(String tableName, String condition,
            long startDate, long endDate, String serverName) throws SQLException {

        List<TimeBasedReportStatUnit<Integer>> stats = new LinkedList<TimeBasedReportStatUnit<Integer>>();
        String query = generateQuery(tableName, condition, startDate, endDate);

        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            if (RealTimeStatsDao.ZHABEI.equals(serverName)) {
                conn = DBConnection.getPipelineConnection();
            } else {
                conn = DBConnection.getShopConnection();
            }
            stmt = conn.createStatement();
            rs = stmt.executeQuery(query);
            while (rs.next()) {
                Date hour = rs.getTimestamp(1);
                int count = rs.getInt(2);
                TimeBasedReportStatUnit<Integer> statUnit = new TimeBasedReportStatUnit<Integer>(hour, count);
                stats.add(statUnit);
            }
        } catch (SQLException e) {
            throw new SQLException("Get real time date range statistics failed.", e);
        } finally {
            DBConnection.closeResultSet(rs);
            DBConnection.closeStatement(stmt);
            DBConnection.closeConnection(conn);
        }

        return stats;
    }

    private String generateQuery(String tableName, String condition, long startDate, long endDate) {
        String query = "SELECT `timestamp`, SUM(%s) AS cnt FROM %s WHERE `timestamp` BETWEEN FROM_UNIXTIME('%d') "
                + " AND FROM_UNIXTIME('%d') %s GROUP BY `timestamp` ORDER BY `timestamp` ASC";
        String sumCol = "`count`";
        String userType = "";
        if (Cnd_Member.equals(condition)) {
            userType = " AND usertype IN (1,3)";
        } else if (Cnd_JA_Member.equals(condition)) {
            userType = " AND user_type IN (1,3)";
        } else if (Cnd_Reward.equals(condition)) {
            sumCol = "amount";
        }
        query = String.format(query, sumCol, tableName, startDate, endDate, userType);

        System.out.println("Real time " + tableName + " stat query: " + query);

        return query;
    }

}
