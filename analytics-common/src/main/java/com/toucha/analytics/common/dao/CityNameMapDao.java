package com.toucha.analytics.common.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import com.toucha.analytics.common.common.DBConnection;
import com.toucha.analytics.common.util.AppEvents;

/**
 * Find all city Chinese name into HashMap
 * 
 * @author senhui.li
 */
public class CityNameMapDao {

    public Map<String, String> getAllCityNames() {

        Map<String, String> result = new HashMap<>();

        String sql = "SELECT py_state,py_city,cn_state,cn_city FROM city_name_mapping";

        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getShopConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);

            while (rs.next()) {
                String key = rs.getString(1).toLowerCase() + "," + rs.getString(2).toLowerCase();
                String value = rs.getString(3) + "," + rs.getString(4);
                result.put(key, value);
            }
        } catch (SQLException e) {
            AppEvents.LogException(e, AppEvents.GenericInternalFailure);
        } finally {
            DBConnection.closeResultSet(rs);
            DBConnection.closeStatement(stmt);
            DBConnection.closeConnection(conn);
        }

        return result;
    }

}
