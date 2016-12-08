package com.toucha.analytics.common.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.toucha.analytics.common.common.DBConnection;
import com.toucha.analytics.common.model.TimeBasedReportStatUnit;
import com.toucha.analytics.common.util.AppEvents;
import com.toucha.analytics.common.util.QueryPredicateHelper;

public class MemberDao {

    public List<TimeBasedReportStatUnit<Integer>> getMemberStats(int companyId, Date start, Date end) throws SQLException {
        List<TimeBasedReportStatUnit<Integer>> result = new ArrayList<TimeBasedReportStatUnit<Integer>>();
        String query = "SELECT timestamp, SUM(`count`) AS membercount FROM membercounts WHERE company = %s AND timestamp >= '%s' AND timestamp < '%s' AND usertype = 2 GROUP BY timestamp ORDER BY timestamp";

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        cal.setTime(end);
        cal.add(Calendar.DAY_OF_YEAR, 1);

        String sql = String.format(query, companyId, format.format(start), format.format(cal.getTime()));
        System.out.println(sql);

        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getPipelineConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                Date hour = rs.getTimestamp("timestamp");
                int count = rs.getInt("membercount");
                TimeBasedReportStatUnit<Integer> unit = new TimeBasedReportStatUnit<Integer>(hour, count);
                result.add(unit);
            }

        } catch (SQLException e) {
            AppEvents.LogException(e, AppEvents.ScanReportService.FailedToFetchMemberDataFromDatabase);
        } finally {
            DBConnection.closeResultSet(rs);
            DBConnection.closeStatement(stmt);
            DBConnection.closeConnection(conn);
        }

        return result;
    }

    public List<TimeBasedReportStatUnit<Integer>> getUserEnterLotteryStats(int companyId, List<Integer> productIds,
            List<Integer> promotionIds, Date start, Date end) {
        List<TimeBasedReportStatUnit<Integer>> result = new ArrayList<TimeBasedReportStatUnit<Integer>>();
        String query = "SELECT usertype, timestamp, SUM(`count`) AS amt FROM membercounts WHERE company = %s %s %s AND timestamp >= '%s' AND timestamp < '%s' AND usertype IN (1,3) GROUP BY usertype, timestamp ORDER BY usertype, timestamp";

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        cal.setTime(end);
        cal.add(Calendar.DAY_OF_YEAR, 1);

        String productPredicates = QueryPredicateHelper.buildInPredicate("product", productIds);
        String promotionPredicates = QueryPredicateHelper.buildInPredicate("promotion", promotionIds);

        String sql = String.format(query, companyId, productPredicates, promotionPredicates, format.format(start),
                format.format(cal.getTime()));
        System.out.println(sql);

        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getPipelineConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                String usertype = rs.getString("usertype");
                Date hour = rs.getTimestamp("timestamp");
                int count = rs.getInt("amt");
                TimeBasedReportStatUnit<Integer> unit = new TimeBasedReportStatUnit<Integer>(hour, count, usertype);
                result.add(unit);
            }

        } catch (SQLException e) {
            AppEvents.LogException(e, AppEvents.ScanReportService.FailedToFetchEffectiveScanUserTypeData);
        } finally {
            DBConnection.closeResultSet(rs);
            DBConnection.closeStatement(stmt);
            DBConnection.closeConnection(conn);
        }

        return result;
    }

    public List<TimeBasedReportStatUnit<Integer>> getUsersEnterLotteryStats(int companyId, Date start, Date end) {
        List<TimeBasedReportStatUnit<Integer>> result = new ArrayList<TimeBasedReportStatUnit<Integer>>();
        String query = "SELECT timestamp, SUM(`count`) AS amt FROM membercounts WHERE ";

        if (companyId == -1) {
            query += " timestamp >= '%s' AND timestamp < '%s' AND usertype IN (1,3) GROUP BY timestamp ORDER BY timestamp";
        } else {
            query += "company = %s AND timestamp >= '%s' AND timestamp < '%s' AND usertype IN (1,3) GROUP BY timestamp ORDER BY timestamp";
        }

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        cal.setTime(end);
        cal.add(Calendar.DAY_OF_YEAR, 1);

        String sql = companyId == -1 ? String.format(query, format.format(start), format.format(cal.getTime()))
                : String.format(query, companyId, format.format(start), format.format(cal.getTime()));
        System.out.println(sql);

        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getPipelineConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                Date hour = rs.getTimestamp("timestamp");
                int count = rs.getInt("amt");
                TimeBasedReportStatUnit<Integer> unit = new TimeBasedReportStatUnit<Integer>(hour, count);
                result.add(unit);
            }
        } catch (SQLException e) {
            AppEvents.LogException(e, AppEvents.ScanReportService.FailedToFetchEffectiveScanUserTypeData);
        } finally {
            DBConnection.closeResultSet(rs);
            DBConnection.closeStatement(stmt);
            DBConnection.closeConnection(conn);
        }

        return result;
    }

    public List<TimeBasedReportStatUnit<Integer>> getEffectiveScanUniqueUserStats(int companyId, List<Integer> productIds,
            List<Integer> promotionIds, Date start, Date end, int internal) throws SQLException {
        List<TimeBasedReportStatUnit<Integer>> result = new ArrayList<TimeBasedReportStatUnit<Integer>>();

        String productIdPredicate = QueryPredicateHelper.buildInPredicate("product", productIds);
        String promotionPredicates = QueryPredicateHelper.buildInPredicate("promotion", promotionIds);

        String query = "SELECT usertype,timestamp,SUM(`count`) AS cnt FROM membercounts WHERE company = %s %s %s AND timestamp >= '%s' AND timestamp < '%s' and usertype IN (2,4) AND internal = %s GROUP BY usertype,timestamp ORDER BY usertype,timestamp";

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        cal.setTime(end);
        cal.add(Calendar.DAY_OF_YEAR, 1);

        String sql = String.format(query, companyId, productIdPredicate, promotionPredicates, format.format(start),
                format.format(cal.getTime()), internal);
        System.out.println(sql);

        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getPipelineConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                Date hour = rs.getTimestamp("timestamp");
                String type = rs.getString("usertype");
                int count = rs.getInt("cnt");
                TimeBasedReportStatUnit<Integer> unit = new TimeBasedReportStatUnit<Integer>(hour, count, type);
                result.add(unit);
            }
        } catch (SQLException e) {
            AppEvents.LogException(e, AppEvents.ScanReportService.FailedToFetchEffectiveScanUniqueUserData);
        } finally {
            DBConnection.closeResultSet(rs);
            DBConnection.closeStatement(stmt);
            DBConnection.closeConnection(conn);
        }

        return result;
    }

}
