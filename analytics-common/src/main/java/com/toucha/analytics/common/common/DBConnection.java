package com.toucha.analytics.common.common;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.toucha.analytics.common.shop.dao.ShopActionDao;
import com.toucha.analytics.common.util.AppEvents;

public class DBConnection {

    private static final Logger logger = LoggerFactory.getLogger(ShopActionDao.class);

    static {
        try {
            Class.forName(ApplicationConfig.MYSQLJDBCDRIVER);
        } catch (ClassNotFoundException e) {
            logger.error("Can't load the MySQL JDBC driver class.", e);
        }
    }

    public static Connection getPipelineConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(ApplicationConfig.MySqlConnectionStr);
        return conn;
    }

    public static Connection getShopConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(ApplicationConfig.ShopMySqlConnectionStr);
        return conn;
    }

    public static void closeResultSet(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                AppEvents.LogException(e, AppEvents.ScanReportService.DatabaseCloseException);
            }
        }
    }

    public static void closeStatement(Statement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                AppEvents.LogException(e, AppEvents.ScanReportService.DatabaseCloseException);
            }
        }
    }

    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                AppEvents.LogException(e, AppEvents.ScanReportService.DatabaseCloseException);
            }
        }
    }
}
