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

import com.toucha.analytics.common.common.DBConnection;
import com.toucha.analytics.common.model.ScanLotteryCount;
import com.toucha.analytics.common.model.TimeBasedReportStatUnit;
import com.toucha.analytics.common.util.AppEvents;
import com.toucha.analytics.common.util.QueryPredicateHelper;

public class ShopMemberDao {

    public List<TimeBasedReportStatUnit<Integer>> getMemberStats(int companyId, Date start, Date end) throws SQLException {
        List<TimeBasedReportStatUnit<Integer>> result = new ArrayList<TimeBasedReportStatUnit<Integer>>();
        String query = "SELECT `timestamp`, SUM(`count`) AS membercount FROM ja_act_membercount WHERE company = %s AND `timestamp` >= '%s' AND `timestamp` < '%s' AND user_type = 2 GROUP BY `timestamp` ORDER BY `timestamp`";

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        cal.setTime(end);
        cal.add(Calendar.DAY_OF_YEAR, 1);

        String sql = String.format(query, companyId, format.format(start), format.format(cal.getTime()));
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
                int count = resultSet.getInt("membercount");
                TimeBasedReportStatUnit<Integer> unit = new TimeBasedReportStatUnit<Integer>(hour, count);
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

    public List<TimeBasedReportStatUnit<Integer>> getUserEnterLotteryStats(int companyId, List<Integer> productIds,
            List<Integer> promotionIds, Date start, Date end) {
        List<TimeBasedReportStatUnit<Integer>> result = new ArrayList<TimeBasedReportStatUnit<Integer>>();
        String query = "SELECT user_type, `timestamp`, SUM(`count`) AS amt FROM ja_act_membercount WHERE company = %s %s %s AND `timestamp` >= '%s' AND `timestamp` < '%s' AND user_type IN (1,3) GROUP BY user_type, `timestamp` ORDER BY user_type, `timestamp`";

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        cal.setTime(end);
        cal.add(Calendar.DAY_OF_YEAR, 1);

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
                String userType = resultSet.getString("user_type");
                Date hour = resultSet.getTimestamp("timestamp");
                int count = resultSet.getInt("amt");
                TimeBasedReportStatUnit<Integer> unit = new TimeBasedReportStatUnit<Integer>(hour, count, userType);
                result.add(unit);
            }

        } catch (SQLException e) {
            AppEvents.LogException(e, AppEvents.ScanReportService.FailedToFetchEffectiveScanUserTypeData);
        } finally {
            DBConnection.closeResultSet(resultSet);
            DBConnection.closeStatement(stmt);
            DBConnection.closeConnection(conn);
        }

        return result;
    }

    public List<TimeBasedReportStatUnit<Integer>> getUsersEnterLotteryStats(int companyId, Date start, Date end) {
        List<TimeBasedReportStatUnit<Integer>> result = new ArrayList<TimeBasedReportStatUnit<Integer>>();
        String query = "SELECT `timestamp`, SUM(`count`) AS amt FROM ja_act_membercount WHERE ";

        if (companyId == -1) {
            query += " `timestamp` >= '%s' AND `timestamp` < '%s' AND user_type IN (1,3) GROUP BY `timestamp` ORDER BY `timestamp`";
        } else {
            query += "company = %s AND `timestamp` >= '%s' AND `timestamp` < '%s' AND user_type IN (1,3) GROUP BY `timestamp` ORDER BY `timestamp`";
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
                int count = resultSet.getInt("amt");
                TimeBasedReportStatUnit<Integer> unit = new TimeBasedReportStatUnit<Integer>(hour, count);
                result.add(unit);
            }

        } catch (SQLException e) {
            AppEvents.LogException(e, AppEvents.ScanReportService.FailedToFetchEffectiveScanUserTypeData);
        } finally {
            DBConnection.closeResultSet(resultSet);
            DBConnection.closeStatement(stmt);
            DBConnection.closeConnection(conn);
        }

        return result;
    }

    public List<TimeBasedReportStatUnit<Integer>> getEffectiveScanUniqueUserStats(int companyId, List<Long> distributorIds,
            List<Integer> promotionIds, List<String> orderNumbers, Date start, Date end) throws SQLException {
        List<TimeBasedReportStatUnit<Integer>> result = new ArrayList<>();

        String query = "SELECT user_type,`timestamp`,SUM(`count`) AS cnt FROM ja_act_membercount WHERE";
        String queryEnd = " `timestamp` >= '%s' AND `timestamp` < '%s' and user_type IN (2,4) GROUP BY user_type,`timestamp` ORDER BY user_type,`timestamp`";

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
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

        String sql = String.format(query, format.format(start), format.format(cal.getTime()));
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
                String type = resultSet.getString("user_type");
                int count = resultSet.getInt("cnt");
                TimeBasedReportStatUnit<Integer> unit = new TimeBasedReportStatUnit<>(hour, count, type);
                result.add(unit);
            }

        } catch (SQLException e) {
            AppEvents.LogException(e, AppEvents.ScanReportService.FailedToFetchEffectiveScanUniqueUserData);
        } finally {
            DBConnection.closeResultSet(resultSet);
            DBConnection.closeStatement(stmt);
            DBConnection.closeConnection(conn);
        }

        return result;
    }

    public List<ScanLotteryCount> getLotteryCount(int companyId, String productIds) throws SQLException {
        String sql = yibaoGenerateSql(productIds);

        List<ScanLotteryCount> resultList = new ArrayList<ScanLotteryCount>();
        Connection conn = null;
        Statement stmt = null;
        ResultSet resultSet = null;

        try {
            long startTime = System.currentTimeMillis();
            conn = DBConnection.getShopConnection();
            stmt = conn.createStatement();
            resultSet = stmt.executeQuery(sql);
            System.out.println("company:" + companyId + ",Query LotteryCount spend time: "
                    + (System.currentTimeMillis() - startTime) + " ms");

            while (resultSet.next()) {
                Integer product = resultSet.getInt("product");
                String state = resultSet.getString("state");
                String city = resultSet.getString("city");
                Integer totalCount = resultSet.getInt("count");
                ScanLotteryCount scanLottoryCount = new ScanLotteryCount(product, state, city, totalCount);
                resultList.add(scanLottoryCount);
            }

        } catch (SQLException e) {
            AppEvents.LogException(e, AppEvents.ScanReportService.FailedToFetchMemberDataFromDatabase);
        } finally {
            DBConnection.closeResultSet(resultSet);
            DBConnection.closeStatement(stmt);
            DBConnection.closeConnection(conn);
        }

        return resultList;
    }

    /**
     * 
     * @Title: 		 yibaoGenerateSql   
     * @Description: custom made,special for yibao
     * @param productIds
     * @return   
     * @throws
     */
    private String yibaoGenerateSql(String productIds) {
        StringBuilder stb = new StringBuilder();
        stb.append(" SELECT ");
        stb.append(" product,state,city,count");
        stb.append(" FROM ");
        stb.append(" ja_act_lottery_amount ");
        if (StringUtils.isNotBlank(productIds)) {
            stb.append(" WHERE product IN (%s) ");
        }

        String sql = stb.toString();
        if (StringUtils.isNotBlank(productIds)) {
            sql = String.format(sql, productIds);
        } else {
            sql = String.format(sql);
        }

        System.out.println("lotterycount sql is:" + sql);

        return sql;
    }
}
