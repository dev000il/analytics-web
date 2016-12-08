package com.toucha.analytics.common.shop.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.toucha.analytics.common.common.DBConnection;
import com.toucha.analytics.common.model.ScanGeoStats;
import com.toucha.analytics.common.model.TimeBasedReportStatUnit;
import com.toucha.analytics.common.util.QueryPredicateHelper;

/**
 * Query the distribute shop action such as login, register etc.
 * 
 * @author senhui.li
 */
public class ShopActionDao {

    private static final Logger logger = LoggerFactory.getLogger(ShopActionDao.class);

    /**
     * Query shop login and register action statistics
     * 
     * @param distributeIds
     *            distribute id list
     * @param start
     *            begin time
     * @param end
     *            end time
     * @return statistics data
     * @throws SQLException
     */
    public List<TimeBasedReportStatUnit<Integer>> getNewAndActivedShopStats(List<Long> distributeIds, long start, long end)
            throws SQLException {
        List<TimeBasedReportStatUnit<Integer>> result = new ArrayList<>();

        String query = "SELECT `timestamp`, action, SUM(`count`) AS cnt FROM sp_actioncount WHERE action IN ('%s', '%s')";
        String queryEnd = " `timestamp` BETWEEN FROM_UNIXTIME('%d') AND FROM_UNIXTIME('%d') "
                + "GROUP BY `timestamp`, action ORDER BY `timestamp` ASC";

        query = String.format(query, ActionType.Register.getType(), ActionType.Login.getType());
        queryEnd = String.format(queryEnd, start, end + (59 * 60 + 59));
        query = QueryPredicateHelper.buildStatementQuery(query, queryEnd, new String[] { "shopID" }, distributeIds);
        logger.info("Shop action statictis SQL: {}", query);

        try (Connection conn = DBConnection.getShopConnection()) {
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
                while (rs.next()) {
                    Date hour = rs.getTimestamp(1);
                    String action = rs.getString(2);
                    int count = rs.getInt(3);
                    // return register and login action counts in hour time
                    // stamp
                    result.add(new TimeBasedReportStatUnit<Integer>(hour, count, action));
                }
            }
        }

        return result;
    }

    /**
     * Query shop action GEO statistics
     * 
     * @param distributeIds
     *            distribute id list
     * @param start
     *            begin time
     * @param end
     *            end time
     * @return statistics data
     * @throws SQLException
     */
    public Map<String, List<ScanGeoStats>> getNewAndActivedGeoStats(List<Long> distributeIds, long start, long end)
            throws SQLException {
        Map<String, List<ScanGeoStats>> result = new HashMap<>();

        String query = "SELECT action, city, state, SUM(`count`) AS cnt FROM sp_actioncount WHERE action IN ('%s', '%s') ";
        String queryEnd = " `timestamp` BETWEEN FROM_UNIXTIME('%d') AND FROM_UNIXTIME('%d')" + " GROUP BY action, city, state";

        query = String.format(query, ActionType.Register.getType(), ActionType.Login.getType());
        queryEnd = String.format(queryEnd, start, end + (59 * 60 + 59));
        query = QueryPredicateHelper.buildStatementQuery(query, queryEnd, new String[] { "shopID" }, distributeIds);
        logger.info("Shop action GEO statictis SQL: {}", query);

        String regType = ActionType.Register.getType();
        String loginType = ActionType.Login.getType();

        List<ScanGeoStats> regStats = new ArrayList<>();
        List<ScanGeoStats> loginStats = new ArrayList<>();

        try (Connection conn = DBConnection.getShopConnection()) {
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
                while (rs.next()) {
                    String type = rs.getString(1);
                    if (type.equals(regType)) {
                        regStats.add(new ScanGeoStats(rs.getString(2), rs.getString(3), rs.getInt(4)));
                    } else if (type.equals(loginType)) {
                        loginStats.add(new ScanGeoStats(rs.getString(2), rs.getString(3), rs.getInt(4)));
                    }
                }
            }
        }

        result.put(loginType, loginStats);
        result.put(regType, regStats);

        return result;
    }

    public enum ActionType {

        Register("register"), Update("update"), Close("close"), Login("login");

        private String type;

        private ActionType(String type) {
            this.type = type;
        }

        public String getType() {
            return this.type;
        }
    }
}
