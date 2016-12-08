package com.toucha.analytics.common.shop.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.toucha.analytics.common.common.DBConnection;

public class ShopConfigDao {

    public static final String scheduledjobIgoreAe = "scheduledjobIgoreAe";

    public List<Integer> getScheduledjobIgnoreAeList() throws SQLException {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        String json = "";
        try {
            conn = DBConnection.getShopConnection();
            stmt = conn.createStatement();
            String sql = "select v from configs where k='" + scheduledjobIgoreAe + "'";
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                json = rs.getString(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeResultSet(rs);
            DBConnection.closeStatement(stmt);
            DBConnection.closeConnection(conn);
        }

        if (!"".equals(json)) {
            return JSON.parseArray(json, Integer.class);
        }
        return null;
    }

    public boolean updateScheduledjobIgnoreAeList(String aelist) throws SQLException {
        Connection conn = null;
        Statement stmt = null;
        boolean result = true;
        try {
            conn = DBConnection.getShopConnection();
            stmt = conn.createStatement();
            String sql = "update configs set v ='" + aelist + "' where k='" + scheduledjobIgoreAe + "'";
            stmt.executeUpdate(sql);
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        } finally {
            DBConnection.closeStatement(stmt);
            DBConnection.closeConnection(conn);
        }
        return result;
    }
}
