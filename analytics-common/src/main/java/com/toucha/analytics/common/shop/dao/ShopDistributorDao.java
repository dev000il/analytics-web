package com.toucha.analytics.common.shop.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.common.base.Joiner;
import com.toucha.analytics.common.common.DBConnection;
import com.toucha.analytics.common.model.ScanDistributorStatsBase;
import com.toucha.analytics.common.model.ScanGeoStats;
import com.toucha.analytics.common.model.ScanStats;
import com.toucha.analytics.common.util.AppEvents;
import com.toucha.analytics.common.util.DateHelper;

public class ShopDistributorDao {

    public ScanStats<ScanDistributorStatsBase> getDistributorStats(int companyId, List<Integer> productIds,
            List<Integer> distributorIds, Date startDate, Date endDate) throws SQLException {
        ScanStats<ScanDistributorStatsBase> result = new ScanStats<ScanDistributorStatsBase>();

        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBConnection.getShopConnection();
            String query = getScanReportQuery(companyId, productIds, distributorIds, startDate, endDate)
                    + getScanTotalQuery(companyId, productIds, distributorIds, startDate, endDate);
            stmt = conn.createStatement();
            boolean hasQueryResult = stmt.execute(query);
            while (!hasQueryResult) {
                hasQueryResult = stmt.getMoreResults();
            }

            // fill in hour scan counts
            rs = stmt.getResultSet();
            FillInTimeStatistics(rs, result.getHourScan());
            rs.close();

            // fill in hour total counts
            stmt.getMoreResults();
            rs = stmt.getResultSet();
            FillInTimeTotalStatistics(rs, result.getHourScan());
        } catch (Exception e) {
            AppEvents.LogException(e, AppEvents.ScanReportService.DistributorReportServiceException);
        } finally {
            DBConnection.closeResultSet(rs);
            DBConnection.closeStatement(stmt);
            DBConnection.closeConnection(conn);
        }

        return result;
    }

    private void FillInTimeStatistics(ResultSet rs, List<ScanDistributorStatsBase> scanStatistics)
            throws NumberFormatException, SQLException {
        while (rs.next()) {
            Date scandate = rs.getTimestamp(1);
            int distributor = Integer.parseInt(rs.getString(2));
            int count = rs.getInt(3);
            if (scanStatistics.size() == 0 || scanStatistics.get(scanStatistics.size() - 1).getDistributorId() != distributor) {
                ScanDistributorStatsBase scanStats = new ScanDistributorStatsBase();
                scanStats.setDistributorId(distributor);
                List<Date> days = new ArrayList<Date>();
                days.add(scandate);
                scanStats.setTimeseries(days);
                List<Integer> counts = new ArrayList<Integer>();
                counts.add(count);
                scanStats.setCounts(counts);
                scanStats.setTotalCount(count);
                scanStatistics.add(scanStats);
            } else {
                scanStatistics.get(scanStatistics.size() - 1).getTimeseries().add(scandate);
                scanStatistics.get(scanStatistics.size() - 1).getCounts().add(count);
                scanStatistics.get(scanStatistics.size() - 1)
                        .setTotalCount(scanStatistics.get(scanStatistics.size() - 1).getTotalCount() + count);
            }
        }
    }

    private void FillInTimeTotalStatistics(ResultSet rs, List<ScanDistributorStatsBase> scanStatistics) throws SQLException {
        ScanDistributorStatsBase totalStats = new ScanDistributorStatsBase();
        while (rs.next()) {
            Date scandate = rs.getTimestamp(1);
            int count = rs.getInt(2);
            totalStats.getTimeseries().add(scandate);
            totalStats.getCounts().add(count);
            totalStats.setTotalCount(totalStats.getTotalCount() + count);
        }
        scanStatistics.add(totalStats);
    }

    public String getScanReportQuery(Integer companyId, List<Integer> productIds, List<Integer> distributorIds, Date startDate,
            Date endDate) {
        String query = "SELECT timestamp, distributor, SUM(count) FROM crossregioncounts WHERE company = %s";
        // If products were specified, include them in the query
        if (productIds != null && productIds.size() > 0) {
            String productQuery = " AND product IN (%s)";
            query = query + String.format(productQuery, Joiner.on(",").join(productIds));
        }
        // If distributors were specified, include them in the query
        if (distributorIds != null && distributorIds.size() > 0) {
            String distributorQuery = " AND distributor IN (%s)";
            query = query + String.format(distributorQuery, Joiner.on(",").join(distributorIds));
        }
        String remainingQuery = " AND timestamp >= '%s' AND timestamp < '%s' GROUP BY distributor, timestamp ORDER BY distributor, timestamp; ";
        query = query + remainingQuery;
        query = String.format(query, companyId, DateHelper.DateToStr(startDate), DateHelper.DateToStr(endDate));
        return query;
    }

    public String getScanTotalQuery(Integer companyId, List<Integer> productIds, List<Integer> distributorIds, Date startDate,
            Date endDate) {
        String query = "SELECT timestamp, SUM(count) FROM crossregioncounts WHERE company = %s";
        // If products were specified, include them in the query
        if (productIds != null && productIds.size() > 0) {
            String productQuery = " AND product IN (%s)";
            query = query + String.format(productQuery, Joiner.on(",").join(productIds));
        }
        // If distributors were specified, include them in the query
        if (distributorIds != null && distributorIds.size() > 0) {
            String distributorQuery = " AND distributor IN (%s)";
            query = query + String.format(distributorQuery, Joiner.on(",").join(distributorIds));
        }
        String remainingQuery = " AND timestamp >= '%s' AND timestamp < '%s' GROUP BY timestamp ORDER BY timestamp; ";

        query = query + remainingQuery;
        query = String.format(query, companyId, DateHelper.DateToStr(startDate), DateHelper.DateToStr(endDate));
        return query;
    }

    public List<ScanGeoStats> getGeoStatistics(int companyId, List<Integer> productIds, List<Integer> distributorIds,
            Date startDate, Date endDate) throws SQLException {
        List<ScanGeoStats> result = new ArrayList<ScanGeoStats>();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBConnection.getShopConnection();
            String query = getGeoQuery(companyId, productIds, distributorIds, startDate, endDate);
            stmt = conn.createStatement();
            rs = stmt.executeQuery(query);
            while (rs.next()) {
                result.add(new ScanGeoStats(rs.getString(1), rs.getString(2), rs.getInt(3)));
            }
        } catch (Exception e) {
            AppEvents.LogException(e, AppEvents.ScanReportService.DistributorReportServiceException);
        } finally {
            DBConnection.closeResultSet(rs);
            DBConnection.closeStatement(stmt);
            DBConnection.closeConnection(conn);
        }

        return result;
    }

    private String getGeoQuery(int companyId, List<Integer> productIds, List<Integer> distributorIds, Date startDate,
            Date endDate) {
        String query = "SELECT city, state, SUM(count) FROM crossregioncounts WHERE company= %s AND timestamp >= '%s' AND timestamp < '%s' ";
        if (distributorIds != null && distributorIds.size() > 0) {
            String distributorQuery = " AND distributor IN (%s)";
            query = query + String.format(distributorQuery, Joiner.on(",").join(distributorIds));
        }
        String remainingQuery = " GROUP BY city";
        query = query + remainingQuery;
        query = String.format(query, companyId, DateHelper.DateToStr(startDate), DateHelper.DateToStr(endDate));
        return query;
    }

}
