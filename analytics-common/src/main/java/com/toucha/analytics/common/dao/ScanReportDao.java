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
import com.toucha.analytics.common.model.ScanGeoStats;
import com.toucha.analytics.common.model.TimeBasedReportStatUnit;
import com.toucha.analytics.common.util.AppEvents;
import com.toucha.analytics.common.util.DateHelper;
import com.toucha.analytics.common.util.QueryPredicateHelper;

public class ScanReportDao {

    static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

    public List<TimeBasedReportStatUnit<Integer>> getEnterLotteryDateRangeStatistics(int companyId, Date startDate, Date endDate)
            throws SQLException {
        Calendar cal = Calendar.getInstance();
        cal.setTime(endDate);
        cal.add(Calendar.DAY_OF_YEAR, 1);
        List<TimeBasedReportStatUnit<Integer>> lotteryStat = new ArrayList<TimeBasedReportStatUnit<Integer>>();

        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            String query = "SELECT timestamp, sum(count) AS cnt FROM enterlotterycounts WHERE timestamp >= '%s' AND timestamp < '%s' ";
            if (companyId != -1) {
                query += " AND company = " + companyId;
            }
            query += " GROUP BY timestamp ORDER BY timestamp ASC";

            conn = DBConnection.getPipelineConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(query);
            while (rs.next()) {
                Date hour = rs.getTimestamp("timestamp");
                int lotcnt = rs.getInt("cnt");
                TimeBasedReportStatUnit<Integer> lotunit = new TimeBasedReportStatUnit<>(hour, lotcnt, "lotcnt");
                lotteryStat.add(lotunit);
            }
            rs.close();
            stmt.close();
        } catch (Exception e) {

        } finally {
            DBConnection.closeResultSet(rs);
            DBConnection.closeStatement(stmt);
            DBConnection.closeConnection(conn);
        }

        return lotteryStat;
    }

    public List<TimeBasedReportStatUnit<Integer>> getDateRangeStatistics(int companyId, List<Integer> productIds, Date startDate,
            Date endDate) throws SQLException {
        Calendar cal = Calendar.getInstance();
        cal.setTime(endDate);
        cal.add(Calendar.DAY_OF_YEAR, 1);
        List<TimeBasedReportStatUnit<Integer>> scanStat = new ArrayList<TimeBasedReportStatUnit<Integer>>();

        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getPipelineConnection();
            String query = getScanReportQuery(companyId, productIds, startDate, cal.getTime());
            stmt = conn.createStatement();
            rs = stmt.executeQuery(query);
            while (rs.next()) {
                Date hour = rs.getTimestamp("timestamp");
                int scancnt = rs.getInt("cnt");
                int newtagcnt = rs.getInt("newtagcnt");
                TimeBasedReportStatUnit<Integer> scanunit = new TimeBasedReportStatUnit<Integer>(hour, scancnt, "scancnt");
                TimeBasedReportStatUnit<Integer> tagunit = new TimeBasedReportStatUnit<Integer>(hour, newtagcnt, "newtagcnt");
                scanStat.add(scanunit);
                scanStat.add(tagunit);
            }
        } catch (Exception e) {
            System.out.println(e);
            AppEvents.LogException(e, AppEvents.ScanReportService.FailedToFetchScanDataFromDatabase);
        } finally {
            DBConnection.closeResultSet(rs);
            DBConnection.closeStatement(stmt);
            DBConnection.closeConnection(conn);
        }

        return scanStat;
    }

    public List<TimeBasedReportStatUnit<Integer>> getEffectiveScanStats(int companyId, List<Integer> productIds,
            List<Integer> promotionIds, Date start, Date end) throws SQLException {
        List<TimeBasedReportStatUnit<Integer>> result = new ArrayList<TimeBasedReportStatUnit<Integer>>();

        String productIdPredicate = QueryPredicateHelper.buildInPredicate("product", productIds);
        String promotionPredicates = QueryPredicateHelper.buildInPredicate("promotion", promotionIds);

        String query = "SELECT timestamp, usertype, SUM(`count`) AS cnt FROM memberscancounts WHERE company = %s %s %s AND timestamp >= '%s' AND timestamp < '%s' and usertype IN (1,3) GROUP BY usertype,timestamp ORDER BY usertype,timestamp";

        Calendar cal = Calendar.getInstance();
        cal.setTime(end);
        cal.add(Calendar.DAY_OF_YEAR, 1);

        String sql = String.format(query, companyId, productIdPredicate, promotionPredicates, format.format(start),
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
                Date hour = rs.getTimestamp("timestamp");
                String type = rs.getString("usertype");
                int count = rs.getInt("cnt");
                TimeBasedReportStatUnit<Integer> unit = new TimeBasedReportStatUnit<Integer>(hour, count, type);
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

    private String getScanReportQuery(int companyId, List<Integer> pids, Date startDate, Date endDate) {

        if (pids != null && pids.size() > 0) {
            for (Integer pid : pids) {
                System.out.print(pid + ",");
            }
        }
        System.out.println();

        StringBuilder productIdsBuild = new StringBuilder();
        if (pids != null) {
            for (Integer p : pids) {
                productIdsBuild.append(p + ",");
            }
        }
        System.out.println("request params ==> companyId:" + companyId + " productIds:" + productIdsBuild + " startTime: "
                + startDate + " tendTime:" + endDate);

        String query = "SELECT timestamp, sum(count) cnt, sum(newtagcount) newtagcnt FROM activitycounts WHERE ";

        // If products were specified, include them in the query
        String productIds = productIdsBuild.toString();
        if (productIds.length() > 0) {
            // Remove the last "," chars that were appended above.
            productIds = productIds.substring(0, productIds.length() - 1);
            String productQuery = " product IN (%s)";
            query = query + String.format(productQuery, productIds);
        }

        if (companyId != -1) {
            String remainingQuery = " AND company = %s AND timestamp >= '%s' AND timestamp < '%s' group by timestamp order by timestamp asc";
            query = query + remainingQuery;
            query = String.format(query, companyId, format.format(startDate), format.format(endDate));
        } else {
            String remainingQuery = " timestamp >= '%s' AND timestamp < '%s' group by timestamp order by timestamp asc ";
            query = query + remainingQuery;
            query = String.format(query, format.format(startDate), format.format(endDate));
        }
        System.out.println(query);
        return query;
    }

    private String getScanReportyGeoQuery(int companyId, List<Integer> pids, Date startDate, Date endDate) {

        endDate = new Date(endDate.getTime() + (59 * 60 + 59) * 1000);

        StringBuilder productIdsBuild = new StringBuilder();
        if (pids != null) {
            for (Integer p : pids) {
                productIdsBuild.append(p + ",");
            }
        }

        String query = "SELECT city, state, SUM(count) FROM activitycounts WHERE company= %s AND timestamp >= '%s' AND timestamp <= '%s' ";

        // If products were specified, include them in the query
        String productIds = productIdsBuild.toString();
        if (productIds != null && productIds.length() > 0) {
            // Remove the last "," chars that were appended above.
            productIds = productIds.substring(0, productIds.length() - 1);

            String productQuery = " AND product IN (%s)";
            query = query + String.format(productQuery, productIds);
        }

        String remainingQuery = " GROUP BY state, city";

        query = query + remainingQuery;
        query = String.format(query, companyId, DateHelper.DateToStr(startDate), DateHelper.DateToStr(endDate));

        System.out.println(query);
        return query;
    }

    public List<ScanGeoStats> getScanGeoStatistics(int companyId, List<Integer> pids, Date startDate, Date endDate)
            throws SQLException {

        List<ScanGeoStats> result = new ArrayList<ScanGeoStats>();

        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getPipelineConnection();
            String query = getScanReportyGeoQuery(companyId, pids, startDate, endDate);
            stmt = conn.createStatement();
            rs = stmt.executeQuery(query);
            while (rs.next()) {
                result.add(new ScanGeoStats(rs.getString(1), rs.getString(2), rs.getInt(3)));
            }
        } catch (Exception e) {
            AppEvents.LogException(e, AppEvents.ScanReportService.FailedToFetchScanDataFromDatabase);
        } finally {
            DBConnection.closeResultSet(rs);
            DBConnection.closeStatement(stmt);
            DBConnection.closeConnection(conn);
        }

        return result;
    }

    public int getYesterdayScanCount(int companyId) throws SQLException {
        int result = 0;
        String sql = "SELECT SUM(`count`) AS yesterdayCnt FROM activitycounts WEHRE `timestamp` >= '%1$s 00:00:00' AND `timestamp` < '%1$s 23:59:59'";
        if (companyId != -1) {
            sql += " AND company = " + companyId;
        }

        sql = String.format(sql, DateHelper.getPrefixDateStr(-1));

        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getPipelineConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                result = rs.getInt(1);
            }
        } catch (Exception e) {
            AppEvents.LogException(e, AppEvents.ScanReportService.FailedToFetchScanDataFromDatabase);
        } finally {
            DBConnection.closeResultSet(rs);
            DBConnection.closeStatement(stmt);
            DBConnection.closeConnection(conn);
        }

        return result;
    }
}
