package com.toucha.analytics.common.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import com.toucha.analytics.common.common.DBConnection;
import com.toucha.platform.common.enums.RewardType;
import com.toucha.platform.common.enums.SuccessStatus;
import com.toucha.platform.common.enums.UserScanType;

/**
 * This DAO design for internal dash board
 * 
 * @ClassName: InternalReportsDao
 */
public class InternalReportsDao {

    private static final String ZHABEI = "zhabei";

    private static final String JINGAN = "jingan";

    public BigDecimal[] getNormalScanStatis(int companyId) throws ClassNotFoundException, SQLException {
        BigDecimal[] zbResult = getInternalStatis(getScanStatisSql("activitycounts", companyId), ZHABEI);
        BigDecimal[] jaResult = getInternalStatis(getScanStatisSql("ja_act_scancount", companyId), JINGAN);
        return combineResult(zbResult, jaResult);
    }

    public BigDecimal[] getValidScanStatis(int companyId) throws ClassNotFoundException, SQLException {
        BigDecimal[] zbResult = getInternalStatis(getScanStatisSql("enterlotterycounts", companyId), ZHABEI);
        BigDecimal[] jaResult = getInternalStatis(getScanStatisSql("ja_act_lotterycount", companyId), JINGAN);
        return combineResult(zbResult, jaResult);
    }

    public BigDecimal[] getNewUserScanStatis(int companyId) throws ClassNotFoundException, SQLException {
        BigDecimal[] zbResult = getInternalStatis(
                getUserScanStatisSql("membercounts", ZHABEI, UserScanType.NewUserScan.getId(), companyId), ZHABEI);
        BigDecimal[] jaResult = getInternalStatis(
                getUserScanStatisSql("ja_act_membercount", JINGAN, UserScanType.NewUserScan.getId(), companyId), JINGAN);
        return combineResult(zbResult, jaResult);
    }

    public BigDecimal[] getOldUserScanStatis(int companyId) throws ClassNotFoundException, SQLException {
        BigDecimal[] zbResult = getInternalStatis(
                getUserScanStatisSql("membercounts", ZHABEI, UserScanType.OldUserScan.getId(), companyId), ZHABEI);
        BigDecimal[] jaResult = getInternalStatis(
                getUserScanStatisSql("ja_act_membercount", JINGAN, UserScanType.OldUserScan.getId(), companyId), JINGAN);
        return combineResult(zbResult, jaResult);
    }

    public BigDecimal[] getUserStatis(int companyId) throws ClassNotFoundException, SQLException {
        BigDecimal[] zbResult = getInternalStatis(getUserStatisSql("membercounts", ZHABEI, companyId), ZHABEI);
        BigDecimal[] jaResult = getInternalStatis(getUserStatisSql("ja_act_membercount", JINGAN, companyId), JINGAN);
        return combineResult(zbResult, jaResult);
    }

    public BigDecimal[] getAwardCounts(int companyId) throws ClassNotFoundException, SQLException {
        BigDecimal[] zbResult = getInternalStatis(getAwardCountsSql("claimrewardstats", companyId), ZHABEI);
        BigDecimal[] jaResult = getInternalStatis(getAwardCountsSql("ja_act_claimcount", companyId), JINGAN);
        return combineResult(zbResult, jaResult);
    }

    public BigDecimal[] getAwardMonies(int companyId) throws ClassNotFoundException, SQLException {
        BigDecimal[] zbResult = getInternalStatis(getAwardMoniesSql("claimrewardstats", companyId), ZHABEI);
        BigDecimal[] jaResult = getInternalStatis(getAwardMoniesSql("ja_act_claimcount", companyId), JINGAN);
        return combineResult(zbResult, jaResult);
    }

    public BigDecimal[] getAwardPhonebillCount(int companyId) throws ClassNotFoundException, SQLException {
        BigDecimal[] zbResult = getInternalStatis(
                getAwardCountSql("claimrewardstats", ZHABEI, RewardType.PhoneBill.getValue(), companyId), ZHABEI);
        BigDecimal[] jaResult = getInternalStatis(
                getAwardCountSql("ja_act_claimcount", JINGAN, RewardType.PhoneBill.getValue(), companyId), JINGAN);
        return combineResult(zbResult, jaResult);
    }

    public BigDecimal[] getAwardUnionPayCount(int companyId) throws ClassNotFoundException, SQLException {
        BigDecimal[] zbResult = getInternalStatis(
                getAwardCountSql("claimrewardstats", ZHABEI, RewardType.UnionPay.getValue(), companyId), ZHABEI);
        BigDecimal[] jaResult = getInternalStatis(
                getAwardCountSql("ja_act_claimcount", JINGAN, RewardType.UnionPay.getValue(), companyId), JINGAN);
        return combineResult(zbResult, jaResult);
    }

    public BigDecimal[] getAwardWeChatCount(int companyId) throws ClassNotFoundException, SQLException {
        BigDecimal[] zbResult = getInternalStatis(
                getAwardCountSql("claimrewardstats", ZHABEI, RewardType.WeChat.getValue(), companyId), ZHABEI);
        BigDecimal[] jaResult = getInternalStatis(
                getAwardCountSql("ja_act_claimcount", JINGAN, RewardType.WeChat.getValue(), companyId), JINGAN);
        return combineResult(zbResult, jaResult);
    }

    public BigDecimal[] getAwardPhonebillMoney(int companyId) throws ClassNotFoundException, SQLException {
        BigDecimal[] zbResult = getInternalStatis(
                getAwardMoneySql("claimrewardstats", ZHABEI, RewardType.PhoneBill.getValue(), companyId), ZHABEI);
        BigDecimal[] jaResult = getInternalStatis(
                getAwardMoneySql("ja_act_claimcount", JINGAN, RewardType.PhoneBill.getValue(), companyId), JINGAN);
        return combineResult(zbResult, jaResult);
    }

    public BigDecimal[] getAwardUnionPayMoney(int companyId) throws ClassNotFoundException, SQLException {
        BigDecimal[] zbResult = getInternalStatis(
                getAwardMoneySql("claimrewardstats", ZHABEI, RewardType.UnionPay.getValue(), companyId), ZHABEI);
        BigDecimal[] jaResult = getInternalStatis(
                getAwardMoneySql("ja_act_claimcount", JINGAN, RewardType.UnionPay.getValue(), companyId), JINGAN);
        return combineResult(zbResult, jaResult);
    }

    public BigDecimal[] getAwardWeChatMoney(int companyId) throws ClassNotFoundException, SQLException {
        BigDecimal[] zbResult = getInternalStatis(
                getAwardMoneySql("claimrewardstats", ZHABEI, RewardType.WeChat.getValue(), companyId), ZHABEI);
        BigDecimal[] jaResult = getInternalStatis(
                getAwardMoneySql("ja_act_claimcount", JINGAN, RewardType.WeChat.getValue(), companyId), JINGAN);
        return combineResult(zbResult, jaResult);
    }

    public BigDecimal[] getAwardPoints(int companyId) throws ClassNotFoundException, SQLException {
        BigDecimal[] zbResult = getInternalStatis(
                getAwardMoneySql("claimrewardstats", ZHABEI, RewardType.Point.getValue(), companyId), ZHABEI);
        BigDecimal[] jaResult = getInternalStatis(
                getAwardMoneySql("ja_act_claimcount", JINGAN, RewardType.Point.getValue(), companyId), JINGAN);
        return combineResult(zbResult, jaResult);
    }

    private BigDecimal[] combineResult(BigDecimal[] zbResult, BigDecimal[] jaResult) {
        BigDecimal[] combineResult = new BigDecimal[3];
        if (zbResult == null && jaResult == null) {
            return combineResult;
        }

        if (zbResult == null) {
            return jaResult;
        }

        if (jaResult == null) {
            return zbResult;
        }

        combineResult[0] = zbResult[0].add(jaResult[0]);
        combineResult[1] = zbResult[1].add(jaResult[1]);
        combineResult[2] = zbResult[2].add(jaResult[2]);
        return combineResult;
    }

    private BigDecimal[] getInternalStatis(String statisSql, String serverName) throws ClassNotFoundException, SQLException {
        BigDecimal[] statis = null;
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            if (ZHABEI.equals(serverName)) {
                conn = DBConnection.getPipelineConnection();
            } else {
                conn = DBConnection.getShopConnection();
            }
            stmt = conn.createStatement();
            rs = stmt.executeQuery(statisSql);
            if (rs != null) {
                int i = 0;
                statis = new BigDecimal[3];
                while (rs.next()) {
                    BigDecimal tmp = rs.getBigDecimal(1);
                    statis[i] = tmp == null ? new BigDecimal(0) : tmp;
                    i++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeResultSet(rs);
            DBConnection.closeStatement(stmt);
            DBConnection.closeConnection(conn);
        }

        return statis;
    }

    private String getAwardMoneySql(String tableName, String serverName, String rewardType, int companyId) {

        StringBuilder sbd = new StringBuilder();
        sbd.append(" SELECT SUM(`amount`) AS ct FROM %1$s ");
        sbd.append(" WHERE `timestamp` >= '%2$s 00:00:00' ");
        sbd.append(" AND `timestamp` <= '%2$s 23:59:59' ");
        sbd.append(" AND status = %4$d ");
        if (ZHABEI.equals(serverName)) {
            sbd.append(" AND rewardtype = %5$s ");
        } else {
            sbd.append(" AND reward_type = %5$s ");
        }
        sbd.append(" %6$s ");
        sbd.append(" UNION ALL ");
        sbd.append(" SELECT SUM(`amount`) AS ct FROM %1$s ");
        sbd.append(" WHERE `timestamp` >= '%3$s 00:00:00' ");
        sbd.append(" AND `timestamp` <= '%3$s 23:59:59' ");
        sbd.append(" AND status = %4$d ");
        if (ZHABEI.equals(serverName)) {
            sbd.append(" AND rewardtype = %5$s ");
        } else {
            sbd.append(" AND reward_type = %5$s ");
        }
        sbd.append(" %6$s ");
        sbd.append(" UNION ALL ");
        sbd.append(" SELECT SUM(`amount`) AS ct FROM %1$s ");
        sbd.append(" WHERE status = %4$d ");
        if (ZHABEI.equals(serverName)) {
            sbd.append(" AND rewardtype = %5$s ");
        } else {
            sbd.append(" AND reward_type = %5$s ");
        }
        sbd.append(" %6$s ");

        String statisSql = sbd.toString();
        if (companyId > 0) {
            statisSql = String.format(statisSql, tableName, getDateStr(-1), getDateStr(0), SuccessStatus.Success.getId(),
                    rewardType, "AND company = " + companyId);
        } else {
            statisSql = String.format(statisSql, tableName, getDateStr(-1), getDateStr(0), SuccessStatus.Success.getId(),
                    rewardType, "");
        }

        System.out.println("sql is:" + statisSql);

        return statisSql;
    }

    private String getAwardCountSql(String tableName, String serverName, String rewardType, int companyId) {

        StringBuilder sbd = new StringBuilder();
        sbd.append(" SELECT SUM(`count`) AS ct FROM %1$s ");
        sbd.append(" WHERE `timestamp` >= '%2$s 00:00:00' ");
        sbd.append(" AND `timestamp` <= '%2$s 23:59:59' ");
        sbd.append(" AND status = %4$d ");
        if (ZHABEI.equals(serverName)) {
            sbd.append(" AND rewardtype = %5$s ");
        } else {
            sbd.append(" AND reward_type = %5$s ");
        }
        sbd.append(" %6$s ");
        sbd.append(" UNION ALL ");
        sbd.append(" SELECT SUM(`count`) AS ct FROM %1$s ");
        sbd.append(" WHERE `timestamp` >= '%3$s 00:00:00' ");
        sbd.append(" AND `timestamp` <= '%3$s 23:59:59' ");
        sbd.append(" AND status = %4$d ");
        if (ZHABEI.equals(serverName)) {
            sbd.append(" AND rewardtype = %5$s ");
        } else {
            sbd.append(" AND reward_type = %5$s ");
        }
        sbd.append(" %6$s ");
        sbd.append(" UNION ALL ");
        sbd.append(" SELECT SUM(`count`) AS ct FROM %1$s ");
        sbd.append(" WHERE status = %4$d ");
        if (ZHABEI.equals(serverName)) {
            sbd.append(" AND rewardtype = %5$s ");
        } else {
            sbd.append(" AND reward_type = %5$s ");
        }
        sbd.append(" %6$s ");

        String statisSql = sbd.toString();
        if (companyId > 0) {
            statisSql = String.format(statisSql, tableName, getDateStr(-1), getDateStr(0), SuccessStatus.Success.getId(),
                    rewardType, "AND company = " + companyId);
        } else {
            statisSql = String.format(statisSql, tableName, getDateStr(-1), getDateStr(0), SuccessStatus.Success.getId(),
                    rewardType, "");
        }

        System.out.println("sql is:" + statisSql);

        return statisSql;
    }

    private String getAwardMoniesSql(String tableName, int companyId) {

        StringBuilder sbd = new StringBuilder();
        sbd.append(" SELECT SUM(`amount`) AS ct FROM %1$s ");
        sbd.append(" WHERE `timestamp` >= '%2$s 00:00:00' ");
        sbd.append(" AND `timestamp` <= '%2$s 23:59:59' ");
        sbd.append(" AND status = %4$d ");
        sbd.append(" %5$s ");
        sbd.append(" UNION ALL ");
        sbd.append(" SELECT SUM(`amount`) AS ct FROM %1$s ");
        sbd.append(" WHERE `timestamp` >= '%3$s 00:00:00' ");
        sbd.append(" AND `timestamp` <= '%3$s 23:59:59' ");
        sbd.append(" AND status = %4$d ");
        sbd.append(" %5$s ");
        sbd.append(" UNION ALL ");
        sbd.append(" SELECT SUM(`amount`) AS ct FROM %1$s ");
        sbd.append(" WHERE status = %4$d ");
        sbd.append(" %5$s ");

        String statisSql = sbd.toString();
        if (companyId > 0) {
            statisSql = String.format(statisSql, tableName, getDateStr(-1), getDateStr(0), SuccessStatus.Success.getId(),
                    "AND company = " + companyId);
        } else {
            statisSql = String.format(statisSql, tableName, getDateStr(-1), getDateStr(0), SuccessStatus.Success.getId(), "");
        }

        System.out.println("sql is:" + statisSql);

        return statisSql;
    }

    private String getAwardCountsSql(String tableName, int companyId) {

        StringBuilder sbd = new StringBuilder();
        sbd.append(" SELECT SUM(`count`) AS ct FROM %1$s ");
        sbd.append(" WHERE `timestamp` >= '%2$s 00:00:00' ");
        sbd.append(" AND `timestamp` <= '%2$s 23:59:59' ");
        sbd.append(" AND status = %4$d ");
        sbd.append(" %5$s ");
        sbd.append(" UNION ALL ");
        sbd.append(" SELECT SUM(`count`) AS ct FROM %1$s ");
        sbd.append(" WHERE `timestamp` >= '%3$s 00:00:00' ");
        sbd.append(" AND `timestamp` <= '%3$s 23:59:59' ");
        sbd.append(" AND status = %4$d ");
        sbd.append(" %5$s ");
        sbd.append(" UNION ALL ");
        sbd.append(" SELECT SUM(`count`) AS ct FROM %1$s ");
        sbd.append(" WHERE status = %4$d ");
        sbd.append(" %5$s ");

        String statisSql = sbd.toString();
        if (companyId > 0) {
            statisSql = String.format(statisSql, tableName, getDateStr(-1), getDateStr(0), SuccessStatus.Success.getId(),
                    "AND company = " + companyId);
        } else {
            statisSql = String.format(statisSql, tableName, getDateStr(-1), getDateStr(0), SuccessStatus.Success.getId(), "");
        }

        System.out.println("sql is:" + statisSql);

        return statisSql;
    }

    private String getUserStatisSql(String tableName, String serverName, int companyId) {

        StringBuilder sbd = new StringBuilder();
        sbd.append(" SELECT SUM(`count`) AS ct FROM %1$s ");
        sbd.append(" WHERE `timestamp` >= '%2$s 00:00:00' ");
        sbd.append(" AND `timestamp` <= '%2$s 23:59:59' ");
        if (ZHABEI.equals(serverName)) {
            sbd.append(" AND usertype = %4$d ");
        } else {
            sbd.append(" AND user_type = %4$d ");
        }
        sbd.append(" %5$s ");
        sbd.append(" UNION ALL ");
        sbd.append(" SELECT SUM(`count`) AS ct FROM %1$s ");
        sbd.append(" WHERE `timestamp` >= '%3$s 00:00:00' ");
        sbd.append(" AND `timestamp` <= '%3$s 23:59:59' ");
        if (ZHABEI.equals(serverName)) {
            sbd.append(" AND usertype = %4$s ");
        } else {
            sbd.append(" AND user_type = %4$s ");
        }
        sbd.append(" %5$s ");
        sbd.append(" UNION ALL ");
        sbd.append(" SELECT SUM(`count`) AS ct FROM %1$s ");
        sbd.append(" %6$s ");

        String statisSql = sbd.toString();
        if (companyId > 0) {
            statisSql = String.format(statisSql, tableName, getDateStr(-1), getDateStr(0), UserScanType.NewUser.getId(),
                    "AND company = " + companyId, "WHERE company = " + companyId);
        } else {
            statisSql = String.format(statisSql, tableName, getDateStr(-1), getDateStr(0), UserScanType.NewUser.getId(), "", "");
        }

        System.out.println("sql is:" + statisSql);

        return statisSql;
    }

    private String getUserScanStatisSql(String tableName, String serverName, int userScanType, int companyId) {

        StringBuilder sbd = new StringBuilder();
        sbd.append(" SELECT SUM(`count`) AS ct FROM %1$s ");
        sbd.append(" WHERE `timestamp` >= '%2$s 00:00:00' ");
        sbd.append(" AND `timestamp` <= '%2$s 23:59:59' ");
        if (ZHABEI.equals(serverName)) {
            sbd.append(" AND usertype = %4$d ");
        } else {
            sbd.append(" AND user_type = %4$d ");
        }
        sbd.append(" %5$s ");
        sbd.append(" UNION ALL ");
        sbd.append(" SELECT SUM(`count`) AS ct FROM %1$s ");
        sbd.append(" WHERE `timestamp` >= '%3$s 00:00:00' ");
        sbd.append(" AND `timestamp` <= '%3$s 23:59:59' ");
        if (ZHABEI.equals(serverName)) {
            sbd.append(" AND usertype = %4$s ");
        } else {
            sbd.append(" AND user_type = %4$s ");
        }
        sbd.append(" %5$s ");
        sbd.append(" UNION ALL ");
        sbd.append(" SELECT SUM(`count`) AS ct FROM %1$s ");
        if (ZHABEI.equals(serverName)) {
            sbd.append(" WHERE usertype = %4$s ");
        } else {
            sbd.append(" WHERE user_type = %4$s ");
        }
        sbd.append(" %5$s ");

        String statisSql = sbd.toString();
        if (companyId > 0) {
            statisSql = String.format(statisSql, tableName, getDateStr(-1), getDateStr(0), userScanType,
                    "AND company = " + companyId);
        } else {
            statisSql = String.format(statisSql, tableName, getDateStr(-1), getDateStr(0), userScanType, "");
        }

        System.out.println("sql is:" + statisSql);

        return statisSql;
    }

    private String getScanStatisSql(String tableName, int companyId) {

        StringBuilder sbd = new StringBuilder();
        sbd.append(" SELECT SUM(`count`) AS ct FROM %1$s ");
        sbd.append(" WHERE `timestamp` >= '%2$s 00:00:00' ");
        sbd.append(" AND `timestamp` <= '%2$s 23:59:59' ");
        sbd.append(" %4$s ");
        sbd.append(" UNION ALL ");
        sbd.append(" SELECT SUM(`count`) AS ct FROM %1$s ");
        sbd.append(" WHERE `timestamp` >= '%3$s 00:00:00' ");
        sbd.append(" AND `timestamp` <= '%3$s 23:59:59' ");
        sbd.append(" %4$s ");
        sbd.append(" UNION ALL ");
        sbd.append(" SELECT SUM(`count`) AS ct FROM %1$s ");
        sbd.append(" %5$s ");

        String statisSql = sbd.toString();
        if (companyId > 0) {
            statisSql = String.format(statisSql, tableName, getDateStr(-1), getDateStr(0), "AND company = " + companyId,
                    "WHERE company = " + companyId);
        } else {
            statisSql = String.format(statisSql, tableName, getDateStr(-1), getDateStr(0), "", "");
        }

        System.out.println("sql is:" + statisSql);

        return statisSql;
    }

    private String getDateStr(int prefixDay) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT+08:00"));
        c.add(Calendar.DAY_OF_MONTH, prefixDay);
        return sdf.format(c.getTime());
    }
}
