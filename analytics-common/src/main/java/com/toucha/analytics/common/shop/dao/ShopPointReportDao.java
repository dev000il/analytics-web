package com.toucha.analytics.common.shop.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.toucha.analytics.common.common.DBConnection;
import com.toucha.analytics.common.model.EnterLotteryDailyStatsBase;
import com.toucha.analytics.common.model.TimeBasedReportStatUnit;
import com.toucha.analytics.common.model.Tuple2;
import com.toucha.analytics.common.util.AppEvents;
import com.toucha.analytics.common.util.DateHelper;
import com.toucha.analytics.common.util.QueryPredicateHelper;
import com.toucha.analytics.common.util.QueryResultHelper;

/**
 * This class design for get point use etc always about point event
 * 
 * @author senhui.li
 */
public class ShopPointReportDao {

    public EnterLotteryDailyStatsBase getPointsGenerateSum(int companyId) throws SQLException {
        return getPointsSumStatics(getPointsGenerateSumSql(companyId));
    }

    public EnterLotteryDailyStatsBase getTYPointsReduceSum(int companyId) throws SQLException {
        return getPointsSumStatics(getTYPointsReduceSumSql(companyId));
    }

    public EnterLotteryDailyStatsBase getMTPointsReduceSum(int companyId) throws SQLException {
        return getPointsSumStatics(getMTPointsReduceSumSql(companyId));
    }

    public String getTYPointsReduceSumSql(int companyId) {
        String query = "SELECT DATE_FORMAT(`timestamp`,'%Y-%m-%d') AS CntDay, SUM(`points`) AS Points FROM ja_txn_reducecount"
                + " WHERE `timestamp` >= '" + DateHelper.getPrefixDateStr(-1) + " 00:00:00' AND `timestamp` <= '"
                + DateHelper.getPrefixDateStr(0) + " 23:59:59'" + " AND company = " + companyId
                + " GROUP BY DATE_FORMAT(`timestamp`,'%Y-%m-%d') ORDER BY cntDay";
        return query;
    }

    // by now we don't have any third party in JA project
    public String getMTPointsReduceSumSql(int companyId) {
        String query = "SELECT DATE_FORMAT(`timestamp`,'%Y-%m-%d') AS CntDay, SUM(`points`) AS Points FROM ja_txn_reducecount"
                + " WHERE txn_type = -2 AND `timestamp` >= '" + DateHelper.getPrefixDateStr(-1)
                + " 00:00:00' AND `timestamp` <= '" + DateHelper.getPrefixDateStr(0) + " 23:59:59'" + " AND company = "
                + companyId + " GROUP BY DATE_FORMAT(`timestamp`,'%Y-%m-%d') ORDER BY cntDay";
        return query;
    }

    public String getPointsGenerateSumSql(int companyId) {
        String query = "SELECT DATE_FORMAT(`timestamp`,'%Y-%m-%d') AS CntDay, GROUP_CONCAT(`points` SEPARATOR  ';') AS pts FROM ja_act_lotterycount"
                + " WHERE `timestamp` >= '" + DateHelper.getPrefixDateStr(-1) + " 00:00:00' AND `timestamp` <= '"
                + DateHelper.getPrefixDateStr(0) + " 23:59:59'" + " AND company = " + companyId
                + " GROUP BY DATE_FORMAT(`timestamp`,'%Y-%m-%d') ORDER BY cntDay";
        return query;
    }

    public EnterLotteryDailyStatsBase getPointsSumStatics(String sql) throws SQLException {

        EnterLotteryDailyStatsBase statictis = new EnterLotteryDailyStatsBase();
        statictis.setYesterdayMeasure("0");
        statictis.setTodayMeasure("0");

        System.out.println(sql);
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getShopConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);

            while (rs.next()) {
                String cntDay = rs.getString(1);
                String pointsStr = rs.getString(2);
                int points = pointsStr.indexOf("{") == -1 ? Integer.parseInt(pointsStr)
                        : QueryResultHelper.sumJSONPoints(pointsStr);
                if (cntDay.equals(DateHelper.getPrefixDateStr(0))) {
                    statictis.setTodayMeasure(String.valueOf(points));
                } else {
                    statictis.setYesterdayMeasure(String.valueOf(points));
                }
            }
        } catch (SQLException e) {
            AppEvents.LogException(e, AppEvents.ScanReportService.FailedToFetchMemberDataFromDatabase);
        } finally {
            DBConnection.closeResultSet(rs);
            DBConnection.closeStatement(stmt);
            DBConnection.closeConnection(conn);
        }

        return statictis;
    }

    public List<TimeBasedReportStatUnit<Integer>> getTYGeneratePointStats(int companyId, List<Long> distributorIds,
            List<Integer> promotionIds, List<String> orderNumbers, Date start, Date end) throws SQLException {
        List<TimeBasedReportStatUnit<Integer>> tyGenernatePoints = new ArrayList<>();

        String query = "SELECT `timestamp`, GROUP_CONCAT(`points` SEPARATOR  ';') AS points FROM ja_act_lotterycount WHERE";
        String queryEnd = " `timestamp` >= '%s' AND `timestamp` < '%s' GROUP BY `timestamp`";

        Calendar cal = Calendar.getInstance();
        cal.setTime(end);
        cal.add(Calendar.DAY_OF_YEAR, 1);

        if (companyId == -1) {
            query += queryEnd;
        } else {
            query += " company = " + companyId;

            query = QueryPredicateHelper.buildStatementQuery(query, queryEnd, QueryPredicateHelper.getShopStaticFields(),
                    distributorIds, promotionIds, orderNumbers);
        }

        query = String.format(query, DateHelper.convert2DateStr(start), DateHelper.convert2DateStr(cal.getTime()));
        System.out.println(query);

        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getShopConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(query);
            while (rs.next()) {
                Date date = rs.getTimestamp("timestamp");
                String pointsStr = rs.getString("points");
                Tuple2<Integer, Integer> pointAmt = QueryResultHelper.sumJSONPointsGroupByType(pointsStr);
                tyGenernatePoints
                        .add(new TimeBasedReportStatUnit<>(date, pointAmt.getFirst(), QueryResultHelper.BASIC_POINT_TYPE));
                tyGenernatePoints
                        .add(new TimeBasedReportStatUnit<>(date, pointAmt.getSecond(), QueryResultHelper.WIN_POINT_TYPE));
            }
        } catch (SQLException e) {
            AppEvents.LogException(e, AppEvents.ScanReportService.FailedToFetchMemberDataFromDatabase);
        } finally {
            DBConnection.closeResultSet(rs);
            DBConnection.closeStatement(stmt);
            DBConnection.closeConnection(conn);
        }

        return tyGenernatePoints;
    }

    public List<TimeBasedReportStatUnit<Integer>> getTYPointReduceStats(int companyId, List<Integer> lotteryIds, Date start,
            Date end) throws SQLException {

        List<TimeBasedReportStatUnit<Integer>> tyPonitReduces = new ArrayList<TimeBasedReportStatUnit<Integer>>();

        String lotteryIdPredicate = QueryPredicateHelper.buildInPredicate("lottery_id", lotteryIds);

        String query = "SELECT promotion, timestamp, SUM(points) AS amt FROM ja_txn_reducecount WHERE  txn_type = 6 AND company = %d %s AND timestamp >= '%s' AND timestamp < '%s' GROUP BY timestamp ORDER BY timestamp";

        Calendar cal = Calendar.getInstance();
        cal.setTime(end);
        cal.add(Calendar.DAY_OF_YEAR, 1);

        String sql = String.format(query, companyId, lotteryIdPredicate, DateHelper.convert2DateStr(start),
                DateHelper.convert2DateStr(cal.getTime()));
        System.out.println(sql);

        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getShopConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                Date date = rs.getTimestamp("timestamp");
                String lotteryId = rs.getString("promotion");
                int count = rs.getInt("amt");
                TimeBasedReportStatUnit<Integer> unit = new TimeBasedReportStatUnit<Integer>(date, count, lotteryId);
                tyPonitReduces.add(unit);
            }
        } catch (SQLException e) {
            AppEvents.LogException(e, AppEvents.ScanReportService.FailedToFetchMemberDataFromDatabase);
        } finally {
            DBConnection.closeResultSet(rs);
            DBConnection.closeStatement(stmt);
            DBConnection.closeConnection(conn);
        }

        return tyPonitReduces;
    }

    public List<TimeBasedReportStatUnit<Integer>> getMTPointReduceStats(int companyId, Date start, Date end) throws SQLException {

        List<TimeBasedReportStatUnit<Integer>> mtPonitReduces = new ArrayList<TimeBasedReportStatUnit<Integer>>();

        String query = "SELECT `timestamp`, SUM(points) AS pointsCnt, SUM(`count`) AS callCnt FROM ja_txn_reducecount WHERE  txn_type = 8 AND company = %d AND timestamp >= '%s' AND timestamp < '%s' GROUP BY timestamp ORDER BY timestamp";

        Calendar cal = Calendar.getInstance();
        cal.setTime(end);
        cal.add(Calendar.DAY_OF_YEAR, 1);

        String sql = String.format(query, companyId, DateHelper.convert2DateStr(start),
                DateHelper.convert2DateStr(cal.getTime()));
        System.out.println(sql);

        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getShopConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                Date date = rs.getTimestamp("timestamp");
                int pointsCnt = rs.getInt("pointsCnt");
                int callCnt = rs.getInt("callCnt");
                mtPonitReduces.add(new TimeBasedReportStatUnit<Integer>(date, pointsCnt, "point"));
                mtPonitReduces.add(new TimeBasedReportStatUnit<Integer>(date, callCnt, "call"));
            }
        } catch (SQLException e) {
            AppEvents.LogException(e, AppEvents.ScanReportService.FailedToFetchMemberDataFromDatabase);
        } finally {
            DBConnection.closeResultSet(rs);
            DBConnection.closeStatement(stmt);
            DBConnection.closeConnection(conn);
        }

        return mtPonitReduces;
    }

    public List<TimeBasedReportStatUnit<Integer>> getPointLotteryRewardStats(int companyId, List<Integer> lotteryIds,
            List<Integer> rewardIds, Date start, Date end) throws SQLException {

        List<TimeBasedReportStatUnit<Integer>> result = new ArrayList<TimeBasedReportStatUnit<Integer>>();

        String query = "SELECT `timestamp`, reward, SUM(`count`) AS cnts FROM ja_txn_lotterycount WHERE reward<>0 AND ";
        String queryEnd = " timestamp >= '%s' AND timestamp < '%s' GROUP BY timestamp,reward ORDER BY timestamp";

        Calendar cal = Calendar.getInstance();
        cal.setTime(end);
        cal.add(Calendar.DAY_OF_YEAR, 1);

        queryEnd = String.format(queryEnd, DateHelper.convert2DateStr(start), DateHelper.convert2DateStr(cal.getTime()));

        if (companyId != -1) {
            query += (" company = " + companyId);
        }

        query = QueryPredicateHelper.buildStatementQuery(query, queryEnd, new String[] { "promotion", "reward" }, lotteryIds,
                rewardIds);

        System.out.println(query);

        try (Connection conn = DBConnection.getShopConnection()) {
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query);) {
                while (rs.next()) {
                    Date hour = rs.getTimestamp(1);
                    String rewardId = rs.getString(2);
                    int points = rs.getInt(3);
                    result.add(new TimeBasedReportStatUnit<Integer>(hour, points, rewardId));
                }
            }
        }

        return result;
    }

    public List<TimeBasedReportStatUnit<Integer>> getClaimLotteryRewardStats(int companyId, List<Integer> lotteryIds,
            List<Integer> rewardIds, Date start, Date end) throws SQLException {

        List<TimeBasedReportStatUnit<Integer>> result = new ArrayList<TimeBasedReportStatUnit<Integer>>();

        String query = "SELECT `timestamp`, reward, SUM(`count`) AS cnts FROM ja_txn_claimcount WHERE ";
        String queryEnd = " timestamp >= '%s' AND timestamp < '%s' GROUP BY timestamp,reward ORDER BY timestamp";

        Calendar cal = Calendar.getInstance();
        cal.setTime(end);
        cal.add(Calendar.DAY_OF_YEAR, 1);

        queryEnd = String.format(queryEnd, DateHelper.convert2DateStr(start), DateHelper.convert2DateStr(cal.getTime()));

        if (companyId != -1) {
            query += (" company = " + companyId);
        }

        query = QueryPredicateHelper.buildStatementQuery(query, queryEnd, new String[] { "promotion", "reward" }, lotteryIds,
                rewardIds);

        System.out.println(query);

        try (Connection conn = DBConnection.getShopConnection()) {
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query);) {
                while (rs.next()) {
                    Date hour = rs.getTimestamp(1);
                    String rewardId = rs.getString(2);
                    int points = rs.getInt(3);
                    result.add(new TimeBasedReportStatUnit<Integer>(hour, points, rewardId));
                }
            }
        }

        return result;
    }
}
