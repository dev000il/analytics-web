package com.toucha.analytics.common.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.toucha.analytics.common.common.DBConnection;
import com.toucha.analytics.common.model.EnterLotteryDailyStatsBase;
import com.toucha.analytics.common.model.TimeBasedReportStatUnit;
import com.toucha.analytics.common.util.AppEvents;
import com.toucha.analytics.common.util.QueryPredicateHelper;
import org.apache.commons.lang3.time.DateUtils;

public class PromotionDao {

    public List<TimeBasedReportStatUnit<Integer>> getPoints(int companyId, List<Integer> productIds, Date start, Date end)
            throws SQLException {
        List<TimeBasedReportStatUnit<Integer>> result = new ArrayList<TimeBasedReportStatUnit<Integer>>();
        String query = "SELECT timestamp, SUM(point) AS pts FROM enterlotterycounts WHERE company = %s %s AND timestamp >= '%s' AND timestamp < '%s' GROUP BY company,timestamp ORDER BY timestamp";

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        cal.setTime(end);
        cal.add(Calendar.DAY_OF_YEAR, 1);

        String productPredicates = QueryPredicateHelper.buildInPredicate("product", productIds);
        //String promotionPredicates = QueryPredicateHelper.buildInPredicate("promotion", promotionIds);

        String sql = String.format(query, companyId, productPredicates, format.format(start), format.format(cal.getTime()));
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
                int count = rs.getInt("pts");
                TimeBasedReportStatUnit<Integer> unit = new TimeBasedReportStatUnit<Integer>(hour, count);
                result.add(unit);
            }
        } catch (SQLException e) {
            AppEvents.LogException(e, AppEvents.ScanReportService.FailedToFetchPointDataFromDatabase);
        } finally {
            DBConnection.closeResultSet(rs);
            DBConnection.closeStatement(stmt);
            DBConnection.closeConnection(conn);
        }

        return result;
    }

    public List<TimeBasedReportStatUnit<BigDecimal>> getRewards(int companyId, List<Integer> productIds,
            List<Integer> promotionIds, Date start, Date end) throws SQLException {
        List<TimeBasedReportStatUnit<BigDecimal>> result = new ArrayList<TimeBasedReportStatUnit<BigDecimal>>();
        String query = "SELECT timestamp, rewardtype, SUM(amount) AS amt FROM claimrewardstats WHERE company = %s %s %s AND timestamp >= '%s' AND timestamp < '%s' AND status = 1 AND lotterytype = 0 GROUP BY timestamp,rewardtype ORDER BY timestamp";

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
                Date hour = rs.getTimestamp("timestamp");
                BigDecimal amount = rs.getBigDecimal("amt");
                String rewardType = rs.getString("rewardtype");
                TimeBasedReportStatUnit<BigDecimal> unit = new TimeBasedReportStatUnit<BigDecimal>(hour, amount, rewardType);
                result.add(unit);
            }
        } catch (SQLException e) {
            AppEvents.LogException(e, AppEvents.ScanReportService.FailedToFetchPromotionDataFromDatabase);
        } finally {
            DBConnection.closeResultSet(rs);
            DBConnection.closeStatement(stmt);
            DBConnection.closeConnection(conn);
        }

        return result;
    }

    public List<TimeBasedReportStatUnit<BigDecimal>> getEnterLotRewardsAmount(int companyId, Date start, Date end)
            throws SQLException {
        List<TimeBasedReportStatUnit<BigDecimal>> result = new ArrayList<TimeBasedReportStatUnit<BigDecimal>>();
        String query = "SELECT timestamp, SUM(amount) AS amt FROM enterlotterycounts WHERE ";

        if (companyId != -1) {
            query += "company = %s  AND timestamp >= '%s' AND timestamp < '%s' GROUP BY timestamp ORDER BY timestamp";
        } else {
            query += " timestamp >= '%s' AND timestamp < '%s' GROUP BY timestamp ORDER BY timestamp";
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
                BigDecimal amount = rs.getBigDecimal("amt");
                TimeBasedReportStatUnit<BigDecimal> unit = new TimeBasedReportStatUnit<>(hour, amount);
                result.add(unit);
            }
        } catch (SQLException e) {
            AppEvents.LogException(e, AppEvents.ScanReportService.FailedToFetchPromotionDataFromDatabase);
        } finally {
            DBConnection.closeResultSet(rs);
            DBConnection.closeStatement(stmt);
            DBConnection.closeConnection(conn);
        }

        return result;
    }

    public List<TimeBasedReportStatUnit<Integer>> getClaimedRewards(int companyId, List<Integer> productIds,
            List<Integer> promotionIds, Date start, Date end, boolean success) {
        List<TimeBasedReportStatUnit<Integer>> result = new ArrayList<>();
        String query = "SELECT timestamp, reward, sum(count) AS amt FROM claimrewardstats WHERE company = %s %s %s AND timestamp >= '%s' AND timestamp < '%s' AND status = %s AND lotterytype = 0 GROUP BY reward, timestamp ORDER BY reward, timestamp";

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        cal.setTime(end);
        cal.add(Calendar.DAY_OF_YEAR, 1);

        String productPredicates = QueryPredicateHelper.buildInPredicate("product", productIds);
        String promotionPredicates = QueryPredicateHelper.buildInPredicate("promotion", promotionIds);

        String ok = success ? "1" : "0";
        String sql = String.format(query, companyId, productPredicates, promotionPredicates, format.format(start),
                format.format(cal.getTime()), ok);
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
                Integer amount = rs.getInt("amt");
                String reward = rs.getString("reward");
                TimeBasedReportStatUnit<Integer> unit = new TimeBasedReportStatUnit<Integer>(hour, amount, reward);
                result.add(unit);
            }
        } catch (SQLException e) {
            AppEvents.LogException(e, AppEvents.ScanReportService.FailedToFetchPromotionDataFromDatabase);
        } finally {
            DBConnection.closeResultSet(rs);
            DBConnection.closeStatement(stmt);
            DBConnection.closeConnection(conn);
        }

        return result;
    }

    public List<TimeBasedReportStatUnit<Integer>> getEnterLotteryStats(int companyId, List<Integer> productIds,
            List<Integer> promotionIds, Date start, Date end, int internal) {
        List<TimeBasedReportStatUnit<Integer>> result = new ArrayList<TimeBasedReportStatUnit<Integer>>();
        String query = "SELECT reward, timestamp, SUM(`count`) AS amt FROM enterlotterycounts WHERE company = %s %s %s AND timestamp >= '%s' AND timestamp < '%s' AND internal = %s GROUP BY reward, timestamp ORDER BY reward, timestamp";

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        cal.setTime(end);
        cal.add(Calendar.DAY_OF_YEAR, 1);

        String productPredicates = QueryPredicateHelper.buildInPredicate("product", productIds);
        String promotionPredicates = QueryPredicateHelper.buildInPredicate("promotion", promotionIds);

        String sql = String.format(query, companyId, productPredicates, promotionPredicates, format.format(start),
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
                String reward = rs.getString("reward");
                Date hour = rs.getTimestamp("timestamp");
                int count = rs.getInt("amt");
                TimeBasedReportStatUnit<Integer> unit = new TimeBasedReportStatUnit<Integer>(hour, count, reward);
                result.add(unit);
            }
        } catch (SQLException e) {
            AppEvents.LogException(e, AppEvents.ScanReportService.FailedToFetchEnterLotteryStatData);
        } finally {
            DBConnection.closeResultSet(rs);
            DBConnection.closeStatement(stmt);
            DBConnection.closeConnection(conn);
        }

        return result;
    }

    public List<TimeBasedReportStatUnit<Integer>> getEnterLotteryStats(int companyId, Date start, Date end) {
        List<TimeBasedReportStatUnit<Integer>> result = new ArrayList<TimeBasedReportStatUnit<Integer>>();
        String query = "SELECT reward, timestamp, SUM(`count`) AS amt FROM enterlotterycounts WHERE timestamp >= '%s' AND timestamp < '%s' ";

        if (companyId != -1) {
            query += " AND company = " + companyId;
        }

        query += " GROUP BY timestamp ORDER BY timestamp";

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        cal.setTime(end);
        cal.add(Calendar.DAY_OF_YEAR, 1);

        String sql = companyId == -1 ? String.format(query, format.format(start), format.format(cal.getTime()))
                : String.format(query, format.format(start), format.format(cal.getTime()), companyId);
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
            AppEvents.LogException(e, AppEvents.ScanReportService.FailedToFetchEnterLotteryStatData);
        } finally {
            DBConnection.closeResultSet(rs);
            DBConnection.closeStatement(stmt);
            DBConnection.closeConnection(conn);
        }

        return result;
    }

    public EnterLotteryDailyStatsBase[] getTodayYesterdayEnterLotteyStats(int companyId) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -1);
        String start = format.format(cal.getTime());
        cal.add(Calendar.DAY_OF_YEAR, 2);
        String end = format.format(cal.getTime());

        String enterlotteryQuery = "SELECT date_format(timestamp,'%Y-%m-%d') AS day, sum(count) AS cnt, FLOOR(sum(amount)) AS amt FROM enterlotterycounts WHERE timestamp >= '"
                + start + "' AND timestamp < '" + end + "' AND company = " + companyId
                + " GROUP BY date_format(timestamp,'%Y-%m-%d') ORDER BY day";
        String enterlotteryUniqueUserQuery = "SELECT date_format(timestamp,'%Y-%m-%d') AS day, sum(count) AS amt FROM membercounts WHERE timestamp >= '"
                + start + "' AND timestamp < '" + end + "' AND company = " + companyId
                + " AND usertype IN (2,4) GROUP BY date_format(timestamp,'%Y-%m-%d') ORDER BY day";

        //String sql = String.format(enterlotteryQuery, start, end, companyId);
        System.out.println(enterlotteryQuery);

        EnterLotteryDailyStatsBase[] d = new EnterLotteryDailyStatsBase[3];
        for (int i = 0; i < 3; i++) {
            d[i] = new EnterLotteryDailyStatsBase();
            d[i].setTodayMeasure("");
            d[i].setYesterdayMeasure("");
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        Date today = Calendar.getInstance().getTime();

        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getPipelineConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(enterlotteryQuery);
            while (rs.next()) {
                if (DateUtils.isSameDay(today, sdf.parse(rs.getString("day")))) {
                    d[0].setTodayMeasure(rs.getString("cnt"));
                    d[1].setTodayMeasure(rs.getString("amt"));
                } else {
                    d[0].setYesterdayMeasure(rs.getString("cnt"));
                    d[1].setYesterdayMeasure(rs.getString("amt"));
                }
            }

            rs.close();
            stmt.close();

            System.out.println(enterlotteryUniqueUserQuery);

            stmt = conn.createStatement();
            rs = stmt.executeQuery(enterlotteryUniqueUserQuery);

            while (rs.next()) {
                if (DateUtils.isSameDay(today, sdf.parse(rs.getString("day")))) {
                    d[2].setTodayMeasure(rs.getString("amt"));
                } else {
                    d[2].setYesterdayMeasure(rs.getString("amt"));
                }
            }
        } catch (SQLException | ParseException e) {
            AppEvents.LogException(e, AppEvents.ScanReportService.FailedToFetchTodayYesterdayEnterLotteryStatData);
        } finally {
            DBConnection.closeResultSet(rs);
            DBConnection.closeStatement(stmt);
            DBConnection.closeConnection(conn);
        }

        return d;
    }

}
