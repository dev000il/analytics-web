package com.toucha.analytics.common.shop.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import com.toucha.analytics.common.common.DBConnection;
import com.toucha.analytics.common.model.DailyStatsBase;
import com.toucha.analytics.common.model.ScanGeoStats;
import com.toucha.analytics.common.util.AppEvents;
import com.toucha.analytics.common.util.DateHelper;
import com.toucha.analytics.common.util.QueryPredicateHelper;

/**
 * All about GEO data report
 * 
 * @author senhui.li
 */
public class ShopGeoReportDao {

    // new user 2 old user 4 old_scan 1 new_scan 3
    public static final int Scan = 0;

    public static final int EnterLottery = 1;

    public static final int NewUser = 2;

    public ShopGeoReportDao() {
    }

    public DailyStatsBase getScanGeoCityCount(int companyId) throws SQLException {
        return getDaliyGeoCityCount("sp_scancount", companyId, Scan);
    }

    public DailyStatsBase getEnterLotteryGeoCityCount(int companyId) throws SQLException {
        return getDaliyGeoCityCount("ja_act_membercount", companyId, EnterLottery);
    }

    public DailyStatsBase getNewUserGeoCityCount(int companyId) throws SQLException {
        return getDaliyGeoCityCount("ja_act_membercount", companyId, NewUser);
    }

    private DailyStatsBase getDaliyGeoCityCount(String tableName, int companyId, int newUserStat) throws SQLException {

        DailyStatsBase result = new DailyStatsBase(0, 0);

        String yesterday = DateHelper.getPrefixDateStr(-1);
        String today = DateHelper.getPrefixDateStr(0);

        String query = "SELECT DISTINCT DATE_FORMAT(`timestamp`,'%Y-%m-%d') AS day, state, city FROM " + tableName
                + " WHERE `timestamp` >= '" + yesterday + " 00:00:00' " + "AND `timestamp` <= '" + today + " 23:59:59'";

        if (companyId != -1) {
            query += (" AND company = " + companyId);
        }

        query = judgeUserType(query, newUserStat);
        if (query.endsWith("AND ")) {
            query = query.substring(0, query.lastIndexOf("AND "));
        }

        query = ("SELECT day, COUNT(city) AS cityCnt FROM (" + query + ") AS tmp GROUP BY day");
        System.out.println("Geo daily count query: " + query);

        // Use JDK7 new specialty it can automatic closed
        try (Connection connection = DBConnection.getShopConnection()) {
            try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
                while (rs.next()) {
                    String cntDay = rs.getString("day");
                    int cityCnt = rs.getInt("cityCnt");
                    if (cntDay.equals(today)) {
                        result.setTodayMeasure(cityCnt);
                    } else {
                        result.setYesterdayMeasure(cityCnt);
                    }
                }
            }
        } catch (SQLException e) {
            AppEvents.LogException(e, AppEvents.ScanReportService.FailedToFetchScanDataFromDatabase);
            throw new SQLException(e);
        }

        return result;
    }

    public List<ScanGeoStats> getEnterLotteryGeoStatsByPromotion(int companyId, List<Integer> promotionIds, Date startDate,
            Date endDate) throws SQLException {
        return getGeoStatisticsByPromotion("ja_act_membercount", companyId, promotionIds, EnterLottery, startDate, endDate);
    }

    public List<ScanGeoStats> getNewUserGeoStatsByPromotion(int companyId, List<Integer> promotionIds, Date startDate,
            Date endDate) throws SQLException {
        return getGeoStatisticsByPromotion("ja_act_membercount", companyId, promotionIds, NewUser, startDate, endDate);
    }

    private List<ScanGeoStats> getGeoStatisticsByPromotion(String tableName, int companyId, List<Integer> promotionIds,
            int newUserStat, Date start, Date end) throws SQLException {
        List<ScanGeoStats> result = new LinkedList<>();

        end = new Date(end.getTime() + (59 * 60 + 59) * 1000);

        String query = "SELECT city, state, SUM(count) FROM %s WHERE ";
        String queryEnd = " timestamp >= '%s' AND timestamp <= '%s' GROUP BY state, city";

        if (companyId == -1) {
            query = judgeUserType(query, newUserStat);
        } else {
            query += ("company = " + companyId);
            query = judgeUserType(query, newUserStat);
        }

        query = QueryPredicateHelper.buildStatementQuery(query, queryEnd, new String[] { "promotion" }, promotionIds);
        query = String.format(query, tableName, DateHelper.DateToStr(start), DateHelper.DateToStr(end));
        System.out.println("Geo statis by promotion query: " + query);

        try (Connection connection = DBConnection.getShopConnection()) {
            try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
                while (rs.next()) {
                    result.add(new ScanGeoStats(rs.getString(1), rs.getString(2), rs.getInt(3)));
                }
            }
        } catch (SQLException e) {
            AppEvents.LogException(e, AppEvents.ScanReportService.FailedToFetchScanDataFromDatabase);
            throw new SQLException(e);
        }

        return result;
    }

    private String judgeUserType(String query, int userStat) {

        if (!query.endsWith("WHERE ")) {
            query += " AND ";
        }

        switch (userStat) {
            case EnterLottery:
                query += (" (user_type = 1 OR user_type = 3) ");
                break;
            case NewUser:
                query += (" user_type = 2 ");
                break;
            default:
                break;
        }
        return query;
    }
}
