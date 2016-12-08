package com.toucha.analytics.common.dao;

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
import com.toucha.analytics.common.util.AppEvents;
import com.toucha.analytics.common.util.DateHelper;
import com.toucha.analytics.common.util.QueryPredicateHelper;

/**
 * This class design for get point use etc always about point event
 * 
 * @author senhui.li
 */
public class PointReportDao {

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
        String query = "SELECT DATE_FORMAT(`timestamp`,'%Y-%m-%d') AS CntDay, SUM(`debit_point_amount`) AS Points FROM txnlogcount"
                + " WHERE txn_type = 6 AND `timestamp` >= '" + DateHelper.getPrefixDateStr(-1) + " 00:00:00' AND `timestamp` <= '"
                + DateHelper.getPrefixDateStr(0) + " 23:59:59'" + " AND company = " + companyId
                + " GROUP BY DATE_FORMAT(`timestamp`,'%Y-%m-%d') ORDER BY cntDay";
        return query;
    }

    public String getMTPointsReduceSumSql(int companyId) {
        String query = "SELECT DATE_FORMAT(`timestamp`,'%Y-%m-%d') AS CntDay, SUM(`debit_point_amount`) AS Points FROM txnlogcount"
                + " WHERE txn_type = 8 AND `timestamp` >= '" + DateHelper.getPrefixDateStr(-1) + " 00:00:00' AND `timestamp` <= '"
                + DateHelper.getPrefixDateStr(0) + " 23:59:59'" + " AND company = " + companyId
                + " GROUP BY DATE_FORMAT(`timestamp`,'%Y-%m-%d') ORDER BY cntDay";
        return query;
    }

    public String getPointsGenerateSumSql(int companyId) {
        String query = "SELECT DATE_FORMAT(`timestamp`,'%Y-%m-%d') AS CntDay, SUM(`point`) AS Points FROM scanlotterypoints"
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
            conn = DBConnection.getPipelineConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                String cntDay = rs.getString("cntDay");
                String points = rs.getString("Points");
                if (cntDay.equals(DateHelper.getPrefixDateStr(0))) {
                    statictis.setTodayMeasure(points);
                } else {
                    statictis.setYesterdayMeasure(points);
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

    public List<TimeBasedReportStatUnit<Integer>> getTYGeneratePointStats(int companyId, Date start, Date end)
            throws SQLException {
        List<TimeBasedReportStatUnit<Integer>> tyGenernatePoints = new ArrayList<TimeBasedReportStatUnit<Integer>>();
        String query = "SELECT `timestamp`, pointype, SUM(`point`) AS points FROM scanlotterypoints WHERE company = %d AND `timestamp` >= '%s' AND `timestamp` < '%s' GROUP BY  pointype, `timestamp`";
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
            conn = DBConnection.getPipelineConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                Date date = rs.getTimestamp("timestamp");
                String pointtype = rs.getString("pointype");
                int count = rs.getInt("points");
                TimeBasedReportStatUnit<Integer> unit = new TimeBasedReportStatUnit<Integer>(date, count, pointtype);
                tyGenernatePoints.add(unit);
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

        String query = "SELECT lottery_id AS lotteryId, timestamp, SUM(debit_point_amount) AS cnt FROM txnlogcount WHERE  txn_type = 6 AND company = %d %s AND timestamp >= '%s' AND timestamp < '%s' GROUP BY timestamp ORDER BY timestamp";

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
            conn = DBConnection.getPipelineConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                Date date = rs.getTimestamp("timestamp");
                String lotteryId = rs.getString("lotteryId");
                int count = rs.getInt("cnt");
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

        String query = "SELECT `timestamp`, SUM(debit_point_amount) AS pointsCnt, SUM(`count`) AS callCnt, SUM(unqiue_user_count) AS userCnt FROM txnlogcount WHERE  txn_type = 8 AND company = %d AND timestamp >= '%s' AND timestamp < '%s' GROUP BY timestamp ORDER BY timestamp";

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
            conn = DBConnection.getPipelineConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                Date date = rs.getTimestamp("timestamp");
                int pointsCnt = rs.getInt("pointsCnt");
                int callCnt = rs.getInt("callCnt");
                int userCnt = rs.getInt("userCnt");
                mtPonitReduces.add(new TimeBasedReportStatUnit<Integer>(date, pointsCnt, "point"));
                mtPonitReduces.add(new TimeBasedReportStatUnit<Integer>(date, callCnt, "call"));
                mtPonitReduces.add(new TimeBasedReportStatUnit<Integer>(date, userCnt, "user"));
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
}
