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
import com.toucha.analytics.common.model.TimeBasedReportStatUnit;
import com.toucha.analytics.common.util.DateHelper;
import com.toucha.analytics.common.util.QueryPredicateHelper;

/**
 * mobile data related SQL operational
 * 
 * @author senhui.li
 */
public class ShopMobileDataDao {

    public List<TimeBasedReportStatUnit<Integer>> getClaimCountStats(int companyId, Date start, Date end) throws SQLException {

        List<TimeBasedReportStatUnit<Integer>> result = new ArrayList<TimeBasedReportStatUnit<Integer>>();

        String query = "SELECT `timestamp`, SUM(`count`) AS cnts FROM ja_tp_mobiledatacount WHERE ";
        String queryEnd = " timestamp >= '%s' AND timestamp < '%s' GROUP BY timestamp ORDER BY timestamp";

        Calendar cal = Calendar.getInstance();
        cal.setTime(end);
        cal.add(Calendar.DAY_OF_YEAR, 1);

        queryEnd = String.format(queryEnd, DateHelper.convert2DateStr(start), DateHelper.convert2DateStr(cal.getTime()));

        if (companyId != -1) {
            query += (" company = " + companyId + " AND ");
        }

        query += queryEnd;

        System.out.println(query);

        try (Connection conn = DBConnection.getShopConnection()) {
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query);) {
                while (rs.next()) {
                    Date hour = rs.getTimestamp(1);
                    int amount = rs.getInt(2);
                    result.add(new TimeBasedReportStatUnit<Integer>(hour, amount, "count"));
                }
            }
        }

        return result;
    }

    public List<TimeBasedReportStatUnit<Integer>> getCliamAmountStats(int companyId, List<String> providerName, Date start,
            Date end) throws SQLException {

        List<TimeBasedReportStatUnit<Integer>> result = new ArrayList<TimeBasedReportStatUnit<Integer>>();

        String query = "SELECT `timestamp`, provider, SUM(amount) AS cnts FROM ja_tp_mobiledatacount WHERE ";
        String queryEnd = " timestamp >= '%s' AND timestamp < '%s' GROUP BY timestamp,provider ORDER BY timestamp";

        Calendar cal = Calendar.getInstance();
        cal.setTime(end);
        cal.add(Calendar.DAY_OF_YEAR, 1);

        queryEnd = String.format(queryEnd, DateHelper.convert2DateStr(start), DateHelper.convert2DateStr(cal.getTime()));

        if (companyId != -1) {
            query += (" company = " + companyId);
        }

        query = QueryPredicateHelper.buildStatementQuery(query, queryEnd, new String[] { "provider" }, providerName);

        System.out.println(query);

        try (Connection conn = DBConnection.getShopConnection()) {
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query);) {
                while (rs.next()) {
                    Date hour = rs.getTimestamp(1);
                    String prn = rs.getString(2);
                    int amount = rs.getInt(3);
                    result.add(new TimeBasedReportStatUnit<Integer>(hour, amount, prn));
                }
            }
        }

        return result;
    }
}
