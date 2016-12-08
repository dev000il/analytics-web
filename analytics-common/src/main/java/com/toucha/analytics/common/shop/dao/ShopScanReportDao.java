package com.toucha.analytics.common.shop.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.toucha.analytics.common.common.ApplicationConfig;
import com.toucha.analytics.common.common.DBConnection;
import com.toucha.analytics.common.model.DailyStatsBase;
import com.toucha.analytics.common.model.ScanGeoStats;
import com.toucha.analytics.common.model.ScanLotteryCount;
import com.toucha.analytics.common.model.TimeBasedReportStatUnit;
import com.toucha.analytics.common.util.AppEvents;
import com.toucha.analytics.common.util.DateHelper;
import com.toucha.analytics.common.util.QueryPredicateHelper;
import org.apache.commons.lang3.time.DateUtils;

public class ShopScanReportDao {

    static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

    public List<TimeBasedReportStatUnit<Integer>> getEnterLotteryDateRangeStatistics(int companyId, Date startDate, Date endDate)
            throws SQLException {
        Calendar cal = Calendar.getInstance();
        cal.setTime(endDate);
        cal.add(Calendar.DAY_OF_YEAR, 1);
        List<TimeBasedReportStatUnit<Integer>> lotteryStat = new ArrayList<>();

        String query = "SELECT timestamp, sum(count) AS cnt FROM ja_act_lotterycount WHERE timestamp >= '%s' AND timestamp < '%s' ";
        if (companyId != -1) {
            query += " AND company = " + companyId;
        }
        query += " GROUP BY timestamp ORDER BY timestamp ASC";

        Connection conn = null;
        Statement stmt = null;
        ResultSet resultSet = null;

        try {
            conn = DBConnection.getShopConnection();
            stmt = conn.createStatement();
            resultSet = stmt.executeQuery(query);
            while (resultSet.next()) {
                Date hour = resultSet.getTimestamp("timestamp");
                int lotcnt = resultSet.getInt("cnt");
                TimeBasedReportStatUnit<Integer> lotunit = new TimeBasedReportStatUnit<>(hour, lotcnt, "lotcnt");
                lotteryStat.add(lotunit);
            }
        } catch (Exception e) {
        } finally {
            DBConnection.closeResultSet(resultSet);
            DBConnection.closeStatement(stmt);
            DBConnection.closeConnection(conn);
        }

        return lotteryStat;
    }

    public List<TimeBasedReportStatUnit<Integer>> getDateRangeStatistics(int companyId, List<Long> distributorIds,
            List<Integer> promotionIds, List<String> orderNumbers, Date startDate, Date endDate) throws SQLException {
        Calendar cal = Calendar.getInstance();
        cal.setTime(endDate);
        cal.add(Calendar.DAY_OF_YEAR, 1);
        List<TimeBasedReportStatUnit<Integer>> scanStat = new ArrayList<>();

        Connection conn = null;
        Statement stmt = null;
        ResultSet resultSet = null;

        try {
            String query = getScanReportQuery(companyId, distributorIds, promotionIds, orderNumbers, startDate, cal.getTime());
            conn = DBConnection.getShopConnection();
            stmt = conn.createStatement();
            resultSet = stmt.executeQuery(query);
            while (resultSet.next()) {
                Date hour = resultSet.getTimestamp("timestamp");
                int scancnt = resultSet.getInt("cnt");
                int newtagcnt = resultSet.getInt("newtagcnt");
                TimeBasedReportStatUnit<Integer> scanunit = new TimeBasedReportStatUnit<Integer>(hour, scancnt, "scancnt");
                TimeBasedReportStatUnit<Integer> tagunit = new TimeBasedReportStatUnit<Integer>(hour, newtagcnt, "newtagcnt");
                scanStat.add(scanunit);
                scanStat.add(tagunit);
            }
        } catch (Exception e) {
            AppEvents.LogException(e, AppEvents.ScanReportService.FailedToFetchScanDataFromDatabase);
        } finally {
            DBConnection.closeResultSet(resultSet);
            DBConnection.closeStatement(stmt);
            DBConnection.closeConnection(conn);
        }

        return scanStat;
    }

    public List<TimeBasedReportStatUnit<Integer>> getEffectiveScanStats(int companyId, List<Integer> productIds,
            List<Integer> promotionIds, Date start, Date end) throws SQLException {
        List<TimeBasedReportStatUnit<Integer>> result = new ArrayList<>();

        //		String productIdPredicate = QueryPredicateHelper.buildInPredicate("product", productIds);
        String promotionPredicates = QueryPredicateHelper.buildInPredicate("promotion", promotionIds);

        String query = "SELECT timestamp, user_type, SUM(`count`) AS cnt FROM ja_act_membercount WHERE company = %s %s %s AND timestamp >= '%s' AND timestamp < '%s' and user_type IN (1,3) GROUP BY user_type,timestamp ORDER BY user_type,timestamp";

        Calendar cal = Calendar.getInstance();
        cal.setTime(end);
        cal.add(Calendar.DAY_OF_YEAR, 1);

        String sql = String.format(query, companyId, promotionPredicates, format.format(start), format.format(cal.getTime()));
        System.out.println(sql);

        Connection conn = null;
        Statement stmt = null;
        ResultSet resultSet = null;

        try {
            conn = DBConnection.getShopConnection();
            stmt = conn.createStatement();
            resultSet = stmt.executeQuery(query);
            while (resultSet.next()) {
                Date hour = resultSet.getTimestamp("timestamp");
                String type = resultSet.getString("user_type");
                int count = resultSet.getInt("cnt");
                TimeBasedReportStatUnit<Integer> unit = new TimeBasedReportStatUnit<Integer>(hour, count, type);
                result.add(unit);
            }

        } catch (SQLException e) {
            AppEvents.LogException(e, AppEvents.ScanReportService.FailedToFetchMemberDataFromDatabase);
        } finally {
            DBConnection.closeResultSet(resultSet);
            DBConnection.closeStatement(stmt);
            DBConnection.closeConnection(conn);
        }

        return result;
    }

    private String getScanReportQuery(int companyId, List<Long> dids, List<Integer> proids, List<String> orns, Date startDate,
            Date endDate) {

        String query = "SELECT timestamp, sum(count) cnt, sum(newtagcount) newtagcnt FROM ja_act_scancount WHERE";
        String queryEnd = " timestamp >= '%s' AND timestamp < '%s' group by timestamp order by timestamp asc";

        if (companyId == -1) {
            query += queryEnd;
        } else {
            query += " company = " + companyId;

            query = QueryPredicateHelper.buildStatementQuery(query, queryEnd, QueryPredicateHelper.getShopStaticFields(), dids,
                    proids, orns);
        }

        query = String.format(query, format.format(startDate), format.format(endDate));

        System.out.println(query);

        return query;
    }

    private String getScanReportyGeoQuery(int companyId, List<Integer> pids, Date startDate, Date endDate) {

        endDate = new Date(endDate.getTime() + (59 * 60 + 59) * 1000);

        String queryPrefix = "SELECT (CASE WHEN t2.cn_city IS NULL THEN t1.city ELSE t2.cn_city END) AS city, (CASE WHEN t2.cn_state IS NULL THEN t1.state ELSE t2.cn_state END) AS state, t1.cnts AS cnts FROM (";

        String query = "SELECT city, state, SUM(count) AS cnts FROM ja_act_scancount WHERE company= %s AND timestamp >= '%s' AND timestamp <= '%s' ";

        String remainingQuery = " GROUP BY state, city";

        query = query + remainingQuery;
        query = String.format(query, companyId, DateHelper.DateToStr(startDate), DateHelper.DateToStr(endDate));
        String queryEnd = ") t1 LEFT JOIN city_name_mapping t2 ON t2.py_state=t1.state AND t1.city=t2.py_city";

        query = queryPrefix + query + queryEnd;
        System.out.println(query);
        return query;
    }

    public List<ScanGeoStats> getScanGeoStatistics(int companyId, List<Integer> pids, Date startDate, Date endDate)
            throws SQLException {

        endDate = new Date(endDate.getTime() + (59 * 60 + 59) * 1000);

        List<ScanGeoStats> result = new ArrayList<ScanGeoStats>();

        Connection conn = null;
        Statement stmt = null;
        ResultSet resultSet = null;

        try {
            String query = getScanReportyGeoQuery(companyId, pids, startDate, endDate);
            conn = DBConnection.getShopConnection();
            stmt = conn.createStatement();
            resultSet = stmt.executeQuery(query);
            while (resultSet.next()) {
                String city = resultSet.getString(1);
                city = city.equals("None") || city.equals("-") ? "其它" : city.replace("市", "");
                String state = resultSet.getString(2);
                state = state.equals("None") || state.equals("-") ? "其它" : state.replace("省", "").replace("市", "");
                result.add(new ScanGeoStats(city, state, resultSet.getInt(3)));
            }
        } catch (Exception e) {
            AppEvents.LogException(e, AppEvents.ScanReportService.FailedToFetchScanDataFromDatabase);
        } finally {
            DBConnection.closeResultSet(resultSet);
            DBConnection.closeStatement(stmt);
            DBConnection.closeConnection(conn);
        }

        return result;
    }

    public int getYesterdayScanCount(int companyId) throws SQLException {
        int result = 0;
        String sql = "SELECT SUM(`count`) AS yesterdayCnt FROM ja_act_scancount WEHRE `timestamp` >= '%1$s 00:00:00' AND `timestamp` < '%1$s 23:59:59'";
        if (companyId != -1) {
            sql += " AND company = " + companyId;
        }

        sql = String.format(sql, DateHelper.getPrefixDateStr(-1));

        Connection conn = null;
        Statement stmt = null;
        ResultSet resultSet = null;

        try {
            conn = DBConnection.getShopConnection();
            stmt = conn.createStatement();
            resultSet = stmt.executeQuery(sql);
            while (resultSet.next()) {
                result = resultSet.getInt(1);
            }
        } catch (Exception e) {
            AppEvents.LogException(e, AppEvents.ScanReportService.FailedToFetchScanDataFromDatabase);
        } finally {
            DBConnection.closeResultSet(resultSet);
            DBConnection.closeStatement(stmt);
            DBConnection.closeConnection(conn);
        }

        return result;
    }

    public DailyStatsBase getYesterTodayStats(int companyId) throws SQLException {

        DailyStatsBase result = new DailyStatsBase();

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -1);
        String start = format.format(cal.getTime());
        cal.add(Calendar.DAY_OF_YEAR, 2);
        String end = format.format(cal.getTime());

        String query = "SELECT date_format(`timestamp`,'%Y-%m-%d') AS day, SUM(`count`) AS cnt FROM ja_act_scancount WHERE ";
        String queryEnd = " `timestamp` >= '%s' AND `timestamp` < '%s' GROUP BY day ORDER BY day";

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
        ResultSet resultSet = null;

        try {
            conn = DBConnection.getShopConnection();
            stmt = conn.createStatement();
            resultSet = stmt.executeQuery(query);

            Date today = Calendar.getInstance().getTime();
            while (resultSet.next()) {
                Date day = format.parse(resultSet.getString("day"));
                if (DateUtils.isSameDay(today, day)) {
                    long cnt = result.getTodayMeasure();
                    result.setTodayMeasure(cnt + resultSet.getLong("cnt"));
                } else {
                    long cnt = result.getYesterdayMeasure();
                    result.setYesterdayMeasure(cnt + resultSet.getLong("cnt"));
                }
            }
            resultSet.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeResultSet(resultSet);
            DBConnection.closeStatement(stmt);
            DBConnection.closeConnection(conn);
        }

        return result;
    }

    public List<ScanLotteryCount> getScanCount(Integer companyId, String productIds) throws SQLException {

        String sql = generateSql(companyId, productIds);

        List<ScanLotteryCount> resultList = new ArrayList<ScanLotteryCount>();
        Connection conn = null;
        Statement stmt = null;
        ResultSet resultSet = null;

        try {
            long startTime = System.currentTimeMillis();
            conn = DBConnection.getShopConnection();
            stmt = conn.createStatement();
            resultSet = stmt.executeQuery(sql);
            System.out.println("Query ScanCount spend time: " + (System.currentTimeMillis() - startTime) + " ms");

            while (resultSet.next()) {
                Integer product = resultSet.getInt("product");
                String state = resultSet.getString("state");
                String city = resultSet.getString("city");
                Integer totalCount = resultSet.getInt("totalCount");
                ScanLotteryCount scanLottoryCount = new ScanLotteryCount(product, state, city, totalCount);
                resultList.add(scanLottoryCount);
            }

        } catch (SQLException e) {
            AppEvents.LogException(e, AppEvents.ScanReportService.FailedToFetchScanData);
        } finally {
            DBConnection.closeResultSet(resultSet);
            DBConnection.closeStatement(stmt);
            DBConnection.closeConnection(conn);
        }

        return resultList;
    }

    private String generateSql(Integer companyId, String productIds) {
        StringBuilder stb = new StringBuilder();
        stb.append(" SELECT ");
        stb.append(" b.product, (CASE WHEN c.cn_state IS NOT NULL THEN c.cn_state ELSE b.state END) AS state, ");
        stb.append(" (CASE WHEN c.cn_city IS NOT NULL THEN c.cn_city ELSE b.city END) AS city,b.totalCount ");
        stb.append(" FROM ");
        stb.append(" ( ");
        stb.append(" SELECT ");
        stb.append(" a.product,a.state,a.city,SUM(a.newtagcount) AS totalCount ");
        stb.append(" FROM ");
        stb.append(" ja_act_scancount a ");
        stb.append(" WHERE a.company = %s ");
        if (StringUtils.isNotBlank(productIds)) {
            stb.append(" AND a.product IN (%s) ");
        }
        //yibao command the time need start from 2016-08-01,so here add this condition
        if (ApplicationConfig.API_TIME_LIMITED_COMPANYID.contains(companyId.toString())) {
            stb.append(" AND a.timestamp >= timestamp('2016-08-01') ");
        }
        stb.append(" GROUP BY ");
        stb.append(" a.product,a.state,a.city ) b ");
        stb.append(" LEFT JOIN ");
        stb.append(" city_name_mapping c ");
        stb.append(" ON ");
        stb.append(" b.state=c.py_state ");
        stb.append(" AND b.city=c.py_city ");

        String sql = stb.toString();
        if (StringUtils.isNotBlank(productIds)) {
            sql = String.format(sql, companyId, productIds);
        } else {
            sql = String.format(stb.toString(), companyId);
        }

        System.out.println("scancount sql is:" + sql);

        return sql;
    }
}
