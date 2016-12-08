package com.toucha.analytics.common.shop.dao;

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
import com.toucha.analytics.common.model.DailyStatsBase;
import com.toucha.analytics.common.model.EnterLotteryDailyStatsBase;
import com.toucha.analytics.common.model.TimeBasedReportStatUnit;
import com.toucha.analytics.common.util.AppEvents;
import com.toucha.analytics.common.util.QueryPredicateHelper;
import com.toucha.analytics.common.util.QueryResultHelper;
import com.toucha.platform.common.enums.RequestRewardsEnum;
import org.apache.commons.lang3.time.DateUtils;

public class ShopPromotionDao {

    public List<TimeBasedReportStatUnit<Integer>> getPoints(int companyId, List<Integer> productIds, Date start, Date end)
            throws SQLException {
        List<TimeBasedReportStatUnit<Integer>> result = new ArrayList<TimeBasedReportStatUnit<Integer>>();
        String query = "SELECT timestamp, GROUP_CONCAT(points SEPARATOR  ';') AS pts FROM ja_act_lotterycount WHERE company = %s %s AND timestamp >= '%s' AND timestamp < '%s' GROUP BY timestamp ORDER BY timestamp";

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        cal.setTime(end);
        cal.add(Calendar.DAY_OF_YEAR, 1);

        String productPredicates = QueryPredicateHelper.buildInPredicate("product", productIds);

        String sql = String.format(query, companyId, productPredicates, format.format(start), format.format(cal.getTime()));
        System.out.println(sql);

        Connection conn = null;
        Statement stmt = null;
        ResultSet resultSet = null;

        try {
            conn = DBConnection.getShopConnection();
            stmt = conn.createStatement();
            resultSet = stmt.executeQuery(sql);
            while (resultSet.next()) {
                Date hour = resultSet.getTimestamp("timestamp");
                int count = QueryResultHelper.sumJSONPoints(resultSet.getString("pts"));

                TimeBasedReportStatUnit<Integer> unit = new TimeBasedReportStatUnit<Integer>(hour, count);
                result.add(unit);
            }
        } catch (SQLException e) {
            AppEvents.LogException(e, AppEvents.ScanReportService.FailedToFetchPointDataFromDatabase);
        } finally {
            DBConnection.closeResultSet(resultSet);
            DBConnection.closeStatement(stmt);
            DBConnection.closeConnection(conn);
        }

        return result;
    }

    public List<TimeBasedReportStatUnit<BigDecimal>> getRewards(int companyId, List<Integer> productIds,
            List<Integer> promotionIds, Date start, Date end) throws SQLException {
        List<TimeBasedReportStatUnit<BigDecimal>> result = new ArrayList<>();
        String query = "SELECT timestamp, reward_type, SUM(amount) AS amt FROM ja_act_claimcount WHERE company = %s %s %s AND timestamp >= '%s' AND timestamp < '%s' AND status = 1 GROUP BY timestamp,reward_type ORDER BY timestamp";

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        cal.setTime(end);
        cal.add(Calendar.DAY_OF_YEAR, 1);

        // String productPredicates =
        // QueryPredicateHelper.buildInPredicate("product", productIds);
        String promotionPredicates = QueryPredicateHelper.buildInPredicate("promotion", promotionIds);

        String sql = String.format(query, companyId, "", promotionPredicates, format.format(start), format.format(cal.getTime()));
        System.out.println(sql);

        Connection conn = null;
        Statement stmt = null;
        ResultSet resultSet = null;

        try {
            conn = DBConnection.getShopConnection();
            stmt = conn.createStatement();
            resultSet = stmt.executeQuery(sql);
            while (resultSet.next()) {
                Date hour = resultSet.getTimestamp("timestamp");
                BigDecimal amount = resultSet.getBigDecimal("amt");
                String rewardType = resultSet.getString("rewardtype");
                TimeBasedReportStatUnit<BigDecimal> unit = new TimeBasedReportStatUnit<>(hour, amount, rewardType);
                result.add(unit);
            }
        } catch (SQLException e) {
            AppEvents.LogException(e, AppEvents.ScanReportService.FailedToFetchPromotionDataFromDatabase);
        } finally {
            DBConnection.closeResultSet(resultSet);
            DBConnection.closeStatement(stmt);
            DBConnection.closeConnection(conn);
        }

        return result;
    }

    public List<TimeBasedReportStatUnit<BigDecimal>> getEnterLotRewardsAmount(int companyId, Date start, Date end)
            throws SQLException {
        List<TimeBasedReportStatUnit<BigDecimal>> result = new ArrayList<TimeBasedReportStatUnit<BigDecimal>>();
        String query = "SELECT timestamp, SUM(amount) AS amt FROM ja_act_lotterycount WHERE ";

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
        ResultSet resultSet = null;

        try {
            conn = DBConnection.getShopConnection();
            stmt = conn.createStatement();
            resultSet = stmt.executeQuery(sql);
            while (resultSet.next()) {
                Date hour = resultSet.getTimestamp("timestamp");
                BigDecimal amount = resultSet.getBigDecimal("amt");
                TimeBasedReportStatUnit<BigDecimal> unit = new TimeBasedReportStatUnit<>(hour, amount);
                result.add(unit);
            }
        } catch (SQLException e) {
            AppEvents.LogException(e, AppEvents.ScanReportService.FailedToFetchPromotionDataFromDatabase);
        } finally {
            DBConnection.closeResultSet(resultSet);
            DBConnection.closeStatement(stmt);
            DBConnection.closeConnection(conn);
        }

        return result;
    }

    public List<TimeBasedReportStatUnit<Integer>> getRewards(int companyId, List<Long> distributorIds, List<String> orderIds,
            List<Integer> productIds, List<Integer> promotionIds, List<Integer> rewardIds, Date start, Date end, boolean success,
            RequestRewardsEnum requestRewardsEnum) {

        List<TimeBasedReportStatUnit<Integer>> result = new ArrayList<>();
        String query = "";
        String queryEnd = "";
        if (requestRewardsEnum == RequestRewardsEnum.CLAIM) {
            query += "SELECT timestamp, reward, sum(count) AS amt FROM ja_act_claimcount WHERE";
            queryEnd += " timestamp >= '%s' AND timestamp < '%s' AND status = %s  GROUP BY reward, timestamp ORDER BY reward, timestamp";
        } else if (requestRewardsEnum == RequestRewardsEnum.WINLOTTERY) {
            query += "SELECT timestamp, reward, sum(count) AS amt FROM ja_act_lotterycount WHERE";
            queryEnd += " timestamp >= '%s' AND timestamp < '%s' GROUP BY reward, timestamp ORDER BY reward, timestamp";
        } else {
            return null;
        }

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        cal.setTime(end);
        cal.add(Calendar.DAY_OF_YEAR, 1);

        if (companyId == -1) {
            query += queryEnd;
        } else {
            query += " company = " + companyId;
            query = QueryPredicateHelper.buildStatementQuery(query, queryEnd,
                    new String[] { "did", "oid", "product", "promotion", "reward" }, distributorIds, orderIds, productIds,
                    promotionIds, rewardIds);
        }

        String ok = success ? "1" : "0";
        query = requestRewardsEnum == RequestRewardsEnum.CLAIM
                ? String.format(query, format.format(start), format.format(cal.getTime()), ok)
                : String.format(query, format.format(start), format.format(cal.getTime())); // RequestRewardsEnum.WINLOTTERY
        System.out.println(query);

        Connection conn = null;
        Statement stmt = null;
        ResultSet resultSet = null;

        try {
            conn = DBConnection.getShopConnection();
            stmt = conn.createStatement();
            resultSet = stmt.executeQuery(query);
            while (resultSet.next()) {
                Date hour = resultSet.getTimestamp("timestamp");
                Integer amount = resultSet.getInt("amt");
                String reward = resultSet.getString("reward");
                TimeBasedReportStatUnit<Integer> unit = new TimeBasedReportStatUnit<>(hour, amount, reward);
                result.add(unit);
            }
        } catch (SQLException e) {
            AppEvents.LogException(e, AppEvents.ScanReportService.FailedToFetchPromotionDataFromDatabase);
        } finally {
            DBConnection.closeResultSet(resultSet);
            DBConnection.closeStatement(stmt);
            DBConnection.closeConnection(conn);
        }

        return result;
    }

    public List<TimeBasedReportStatUnit<Integer>> getProducts(int companyId, List<Long> distributorIds, List<String> orderIds,
            List<Integer> productIds, List<Integer> promotionIds, List<Integer> rewardIds, Date start, Date end, boolean success,
            RequestRewardsEnum requestRewardsEnum) {

        List<TimeBasedReportStatUnit<Integer>> result = new ArrayList<>();
        String query = "";
        String queryEnd = "";
        if (requestRewardsEnum == RequestRewardsEnum.CLAIM) {
            query += "SELECT timestamp, product, sum(count) AS amt FROM ja_act_claimcount WHERE";
            queryEnd += " timestamp >= '%s' AND timestamp < '%s' AND status = %s  GROUP BY product, timestamp ORDER BY product, timestamp";
        } else if (requestRewardsEnum == RequestRewardsEnum.WINLOTTERY) {
            query += "SELECT timestamp, product, sum(count) AS amt FROM ja_act_lotterycount WHERE";
            queryEnd += " timestamp >= '%s' AND timestamp < '%s' GROUP BY product, timestamp ORDER BY product, timestamp";
        } else {
            return null;
        }

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        cal.setTime(end);
        cal.add(Calendar.DAY_OF_YEAR, 1);

        if (companyId == -1) {
            query += queryEnd;
        } else {
            query += " company = " + companyId;
            query = QueryPredicateHelper.buildStatementQuery(query, queryEnd,
                    new String[] { "did", "oid", "product", "promotion", "reward" }, distributorIds, orderIds, productIds,
                    promotionIds, rewardIds);
        }

        String ok = success ? "1" : "0";
        query = requestRewardsEnum == RequestRewardsEnum.CLAIM
                ? String.format(query, format.format(start), format.format(cal.getTime()), ok)
                : String.format(query, format.format(start), format.format(cal.getTime())); // RequestRewardsEnum.WINLOTTERY
        System.out.println(query);

        Connection conn = null;
        Statement stmt = null;
        ResultSet resultSet = null;

        try {
            conn = DBConnection.getShopConnection();
            stmt = conn.createStatement();
            resultSet = stmt.executeQuery(query);
            while (resultSet.next()) {
                Date hour = resultSet.getTimestamp("timestamp");
                Integer amount = resultSet.getInt("amt");
                String product = resultSet.getString("product");
                TimeBasedReportStatUnit<Integer> unit = new TimeBasedReportStatUnit<>(hour, amount, product);
                result.add(unit);
            }
        } catch (SQLException e) {
            AppEvents.LogException(e, AppEvents.ScanReportService.FailedToFetchPromotionDataFromDatabase);
        } finally {
            DBConnection.closeResultSet(resultSet);
            DBConnection.closeStatement(stmt);
            DBConnection.closeConnection(conn);
        }

        return result;
    }

    public List<TimeBasedReportStatUnit<Integer>> getEnterLotteryStats(int companyId, List<Long> distributorIds,
            List<Integer> promotionIds, List<String> orderNumbers, Date start, Date end) {
        List<TimeBasedReportStatUnit<Integer>> result = new ArrayList<>();

        String query = "SELECT reward, `timestamp`, SUM(`count`) AS amt FROM ja_act_lotterycount WHERE";
        String queryEnd = " reward <> -1 AND `timestamp` >= '%s' AND `timestamp` < '%s' GROUP BY reward, `timestamp` ORDER BY reward, `timestamp`";

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        cal.setTime(end);
        cal.add(Calendar.DAY_OF_YEAR, 1);

        String distributorPred = QueryPredicateHelper.buildInPredicate("did", distributorIds);
        String promotionPred = QueryPredicateHelper.buildInPredicate("promotion", promotionIds);
        String orderNumberPred = QueryPredicateHelper.buildInPredicate("oid", orderNumbers);

        if (companyId == -1) {
            query += queryEnd;
        } else {
            query += " company = " + companyId;

            query = QueryPredicateHelper.appendQuery(query, queryEnd, distributorPred, promotionPred, orderNumberPred);
        }

        query = String.format(query, format.format(start), format.format(cal.getTime()));

        System.out.println(query);

        Connection conn = null;
        Statement stmt = null;
        ResultSet resultSet = null;

        try {
            conn = DBConnection.getShopConnection();
            stmt = conn.createStatement();
            resultSet = stmt.executeQuery(query);
            while (resultSet.next()) {
                String reward = resultSet.getString("reward");
                Date hour = resultSet.getTimestamp("timestamp");
                int count = resultSet.getInt("amt");
                TimeBasedReportStatUnit<Integer> unit = new TimeBasedReportStatUnit<>(hour, count, reward);
                result.add(unit);
            }
        } catch (SQLException e) {
            AppEvents.LogException(e, AppEvents.ScanReportService.FailedToFetchEnterLotteryStatData);
        } finally {
            DBConnection.closeResultSet(resultSet);
            DBConnection.closeStatement(stmt);
            DBConnection.closeConnection(conn);
        }

        return result;
    }

    public List<TimeBasedReportStatUnit<Integer>> getEnterLotteryStats(int companyId, Date start, Date end) {
        List<TimeBasedReportStatUnit<Integer>> result = new ArrayList<TimeBasedReportStatUnit<Integer>>();
        String query = "SELECT reward, timestamp, SUM(`count`) AS amt FROM ja_act_lotterycount WHERE timestamp >= '%s' AND timestamp < '%s' ";

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
        ResultSet resultSet = null;

        try {
            conn = DBConnection.getShopConnection();
            stmt = conn.createStatement();
            resultSet = stmt.executeQuery(sql);
            while (resultSet.next()) {
                Date hour = resultSet.getTimestamp("timestamp");
                int count = resultSet.getInt("amt");
                TimeBasedReportStatUnit<Integer> unit = new TimeBasedReportStatUnit<Integer>(hour, count);
                result.add(unit);
            }
        } catch (SQLException e) {
            AppEvents.LogException(e, AppEvents.ScanReportService.FailedToFetchEnterLotteryStatData);
        } finally {
            DBConnection.closeResultSet(resultSet);
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

        String enterlotteryQuery = "SELECT date_format(timestamp,'%Y-%m-%d') AS day, sum(count) AS cnt, FLOOR(sum(amount)) AS amt FROM ja_act_lotterycount WHERE timestamp >= '"
                + start + "' AND timestamp < '" + end + "' AND company = " + companyId
                + " GROUP BY date_format(timestamp,'%Y-%m-%d') ORDER BY day";
        String enterlotteryUniqueUserQuery = "SELECT date_format(timestamp,'%Y-%m-%d') AS day, sum(count) AS amt FROM ja_act_membercount WHERE timestamp >= '"
                + start + "' AND timestamp < '" + end + "' AND company = " + companyId
                + " AND user_type IN (2,4) GROUP BY date_format(timestamp,'%Y-%m-%d') ORDER BY day";

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
        ResultSet resultSet = null;

        try {
            conn = DBConnection.getShopConnection();
            stmt = conn.createStatement();
            resultSet = stmt.executeQuery(enterlotteryQuery);
            while (resultSet.next()) {
                if (DateUtils.isSameDay(today, sdf.parse(resultSet.getString("day")))) {
                    d[0].setTodayMeasure(resultSet.getString("cnt"));
                    d[1].setTodayMeasure(resultSet.getString("amt"));
                } else {
                    d[0].setYesterdayMeasure(resultSet.getString("cnt"));
                    d[1].setYesterdayMeasure(resultSet.getString("amt"));
                }
            }

            resultSet.close();
            stmt.close();

            System.out.println(enterlotteryUniqueUserQuery);

            stmt = conn.createStatement();
            resultSet = stmt.executeQuery(enterlotteryUniqueUserQuery);

            while (resultSet.next()) {
                if (DateUtils.isSameDay(today, sdf.parse(resultSet.getString("day")))) {
                    d[2].setTodayMeasure(resultSet.getString("amt"));
                } else {
                    d[2].setYesterdayMeasure(resultSet.getString("amt"));
                }
            }
        } catch (SQLException | ParseException e) {
            AppEvents.LogException(e, AppEvents.ScanReportService.FailedToFetchTodayYesterdayEnterLotteryStatData);
        } finally {
            DBConnection.closeResultSet(resultSet);
            DBConnection.closeStatement(stmt);
            DBConnection.closeConnection(conn);
        }

        return d;
    }

    public DailyStatsBase getYesterTodayLotteryStats(int companyId) throws Exception {
        return getYesterTodayStats(companyId, "ja_act_lotterycount", true);
    }

    public DailyStatsBase getYesterTodayClaimStats(int companyId) throws Exception {
        return getYesterTodayStats(companyId, "ja_act_claimcount", false);
    }

    public DailyStatsBase getYesterTodayStats(int companyId, String tableName, boolean isWinLot) throws SQLException {

        DailyStatsBase result = new DailyStatsBase();

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -1);
        String start = format.format(cal.getTime());
        cal.add(Calendar.DAY_OF_YEAR, 2);
        String end = format.format(cal.getTime());

        String query = "SELECT date_format(`timestamp`,'%Y-%m-%d') AS day, SUM(`count`) AS cnt FROM " + tableName + " WHERE";
        String queryEnd = " `timestamp` >= '%s' AND `timestamp` < '%s' GROUP BY day ORDER BY day";

        if (isWinLot) {
            queryEnd = " reward <> -1 AND " + queryEnd;
        }

        queryEnd = String.format(queryEnd, start, end);

        if (companyId == -1) {
            query += queryEnd;
        } else {
            query += " company = " + companyId;
            query += " AND" + queryEnd;
        }

        System.out.println(query);

        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getShopConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(query);

            Date today = Calendar.getInstance().getTime();
            while (rs.next()) {
                Date day = format.parse(rs.getString("day"));
                if (DateUtils.isSameDay(today, day)) {
                    long cnt = result.getTodayMeasure();
                    result.setTodayMeasure(cnt + rs.getLong("cnt"));
                } else {
                    long cnt = result.getYesterdayMeasure();
                    result.setYesterdayMeasure(cnt + rs.getLong("cnt"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeResultSet(rs);
            DBConnection.closeStatement(stmt);
            DBConnection.closeConnection(conn);
        }

        return result;
    }
}
