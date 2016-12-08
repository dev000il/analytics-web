package com.toucha.analytics.common.shop.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.toucha.analytics.common.common.DBConnection;
import com.toucha.analytics.common.model.TimeBasedReportStatUnit;
import com.toucha.analytics.common.util.AppEvents;
import com.toucha.analytics.common.util.QueryPredicateHelper;

/**
 * Transaction statistics report data
 * 
 * @author senhui.li
 */
public class ShopTransactionReportDao {

    public List<TimeBasedReportStatUnit<Integer>> getLotteryPointReduceStatistics(int companyId, List<Integer> promIds,
            long startDate, long endDate) throws SQLException {

        List<TimeBasedReportStatUnit<Integer>> result = new ArrayList<>();

        String query = "SELECT `timestamp`, txn_type, SUM(points) AS amt FROM ja_txn_reducecount WHERE";
        String queryEnd = " `timestamp` BETWEEN FROM_UNIXTIME('%d') AND FROM_UNIXTIME('%d') GROUP BY `timestamp`, txn_type ORDER BY `timestamp`";

        queryEnd = String.format(queryEnd, startDate / 1000, endDate / 1000 + 59 * 60 + 59);

        if (companyId != -1) {
            query += (" company = " + companyId);
        }
        query = QueryPredicateHelper.buildStatementQuery(query, queryEnd, new String[] { "promotion" }, promIds);

        System.out.println(query);

        try (Connection conn = DBConnection.getShopConnection()) {
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
                while (rs.next()) {
                    Date hour = rs.getTimestamp(1);
                    String txType = rs.getString(2);
                    int points = rs.getInt(3);
                    result.add(new TimeBasedReportStatUnit<Integer>(hour, points, txType));
                }
            }
        } catch (SQLException e) {
            AppEvents.LogException(e, AppEvents.ScanReportService.FailedToFetchScanDataFromDatabase);
            throw new SQLException(e);
        }

        return result;
    }
}
