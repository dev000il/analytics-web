package com.toucha.analytics.common.shop.dao;

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
public class ShopInternalReportsDao {

    public BigDecimal[] getNormalScanStatis(int companyId) throws ClassNotFoundException, SQLException {
        return getInternalStatis(getScanStatisSql("sp_scancount", companyId));
    }

    public BigDecimal[] getValidScanStatis(int companyId) throws ClassNotFoundException, SQLException {
        return getInternalStatis(getScanStatisSql("sp_winlotterycount", companyId));
    }

    public BigDecimal[] getNewUserScanStatis(int companyId) throws ClassNotFoundException, SQLException {
        return getInternalStatis(getUserScanStatisSql(UserScanType.NewUserScan.getId(), companyId));
    }

    public BigDecimal[] getOldUserScanStatis(int companyId) throws ClassNotFoundException, SQLException {
        return getInternalStatis(getUserScanStatisSql(UserScanType.OldUserScan.getId(), companyId));
    }

    public BigDecimal[] getUserStatis(int companyId) throws ClassNotFoundException, SQLException {
        return getInternalStatis(getUserStatisSql(companyId));
    }

    public BigDecimal[] getAwardCounts(int companyId) throws ClassNotFoundException, SQLException {
        return getInternalStatis(getAwardCountsSql(companyId));
    }

    public BigDecimal[] getAwardMonies(int companyId) throws ClassNotFoundException, SQLException {
        return getInternalStatis(getAwardMoniesSql(companyId));
    }

    public BigDecimal[] getAwardPhonebillCount(int companyId) throws ClassNotFoundException, SQLException {
        return getInternalStatis(getAwardCountSql(RewardType.PhoneBill.getValue(), companyId));
    }

    public BigDecimal[] getAwardUnionPayCount(int companyId) throws ClassNotFoundException, SQLException {
        return getInternalStatis(getAwardCountSql(RewardType.UnionPay.getValue(), companyId));
    }

    public BigDecimal[] getAwardWeChatCount(int companyId) throws ClassNotFoundException, SQLException {
        return getInternalStatis(getAwardCountSql(RewardType.WeChat.getValue(), companyId));
    }

    public BigDecimal[] getAwardPhonebillMoney(int companyId) throws ClassNotFoundException, SQLException {
        return getInternalStatis(getAwardMoneySql(RewardType.PhoneBill.getValue(), companyId));
    }

    public BigDecimal[] getAwardUnionPayMoney(int companyId) throws ClassNotFoundException, SQLException {
        return getInternalStatis(getAwardMoneySql(RewardType.UnionPay.getValue(), companyId));
    }

    public BigDecimal[] getAwardWeChatMoney(int companyId) throws ClassNotFoundException, SQLException {
        return getInternalStatis(getAwardMoneySql(RewardType.WeChat.getValue(), companyId));
    }

    public BigDecimal[] getAwardPoints(int companyId) throws ClassNotFoundException, SQLException {
        return getInternalStatis(getAwardMoneySql(RewardType.Point.getValue(), companyId));
    }

    private BigDecimal[] getInternalStatis(String statisSql) throws ClassNotFoundException, SQLException {
        BigDecimal[] statis = null;
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getShopConnection();
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

    private String getAwardMoneySql(String rewardType, int companyId) {

        String statisSql = "SELECT SUM(`amount`) AS ct FROM %1$s WHERE `timestamp` >= '%2$s 00:00:00' AND `timestamp` <= '%2$s 23:59:59' AND status = %4$d AND rewardtype = %5$s %6$s"
                + " UNION ALL "
                + " SELECT SUM(`amount`) AS ct FROM %1$s WHERE `timestamp` >= '%3$s 00:00:00' AND `timestamp` <= '%3$s 23:59:59' AND status = %4$d AND rewardtype = %5$s %6$s"
                + " UNION ALL" + " SELECT SUM(`amount`) AS ct FROM %1$s WHERE status = %4$d AND rewardtype = %5$s %6$s";

        if (companyId > 0) {
            statisSql = String.format(statisSql, "claimrewardstats", getDateStr(-1), getDateStr(0), SuccessStatus.Success.getId(),
                    rewardType, "AND company = " + companyId);
        } else {
            statisSql = String.format(statisSql, "claimrewardstats", getDateStr(-1), getDateStr(0), SuccessStatus.Success.getId(),
                    rewardType, "");
        }

        return statisSql;
    }

    private String getAwardCountSql(String rewardType, int companyId) {

        String statisSql = "SELECT SUM(`count`) AS ct FROM %1$s WHERE `timestamp` >= '%2$s 00:00:00' AND `timestamp` <= '%2$s 23:59:59' AND status = %4$d AND rewardtype = %5$s %6$s"
                + " UNION ALL "
                + " SELECT SUM(`count`) AS ct FROM %1$s WHERE `timestamp` >= '%3$s 00:00:00' AND `timestamp` <= '%3$s 23:59:59' AND status = %4$d AND rewardtype = %5$s %6$s"
                + " UNION ALL" + " SELECT SUM(`count`) AS ct FROM %1$s WHERE status = %4$d AND rewardtype = %5$s %6$s";

        if (companyId > 0) {
            statisSql = String.format(statisSql, "claimrewardstats", getDateStr(-1), getDateStr(0), SuccessStatus.Success.getId(),
                    rewardType, "AND company = " + companyId);
        } else {
            statisSql = String.format(statisSql, "claimrewardstats", getDateStr(-1), getDateStr(0), SuccessStatus.Success.getId(),
                    rewardType, "");
        }

        return statisSql;
    }

    private String getAwardMoniesSql(int companyId) {

        String statisSql = "SELECT SUM(`amount`) AS ct FROM %1$s WHERE `timestamp` >= '%2$s 00:00:00' AND `timestamp` <= '%2$s 23:59:59' AND status = %4$d %5$s"
                + " UNION ALL "
                + " SELECT SUM(`amount`) AS ct FROM %1$s WHERE `timestamp` >= '%3$s 00:00:00' AND `timestamp` <= '%3$s 23:59:59' AND status = %4$d %5$s"
                + " UNION ALL" + " SELECT SUM(`amount`) AS ct FROM %1$s WHERE status = %4$d %5$s";

        if (companyId > 0) {
            statisSql = String.format(statisSql, "claimrewardstats", getDateStr(-1), getDateStr(0), SuccessStatus.Success.getId(),
                    "AND company = " + companyId);
        } else {
            statisSql = String.format(statisSql, "claimrewardstats", getDateStr(-1), getDateStr(0), SuccessStatus.Success.getId(),
                    "");
        }

        return statisSql;
    }

    private String getAwardCountsSql(int companyId) {

        String statisSql = "SELECT SUM(`count`) AS ct FROM %1$s WHERE `timestamp` >= '%2$s 00:00:00' AND `timestamp` <= '%2$s 23:59:59' AND status = %4$d %5$s"
                + " UNION ALL "
                + " SELECT SUM(`count`) AS ct FROM %1$s WHERE `timestamp` >= '%3$s 00:00:00' AND `timestamp` <= '%3$s 23:59:59' AND status = %4$d %5$s"
                + " UNION ALL" + " SELECT SUM(`count`) AS ct FROM %1$s WHERE status = %4$d %5$s";
        if (companyId > 0) {
            statisSql = String.format(statisSql, "claimrewardstats", getDateStr(-1), getDateStr(0), SuccessStatus.Success.getId(),
                    "AND company = " + companyId);
        } else {
            statisSql = String.format(statisSql, "claimrewardstats", getDateStr(-1), getDateStr(0), SuccessStatus.Success.getId(),
                    "");
        }

        return statisSql;
    }

    private String getUserStatisSql(int companyId) {

        String statisSql = "SELECT SUM(`count`) AS ct FROM %1$s WHERE `timestamp` >= '%2$s 00:00:00' AND `timestamp` <= '%2$s 23:59:59' AND user_type = %4$d %5$s"
                + " UNION ALL "
                + " SELECT SUM(`count`) AS ct FROM %1$s WHERE `timestamp` >= '%3$s 00:00:00' AND `timestamp` <= '%3$s 23:59:59' AND user_type = %4$d %5$s"
                + " UNION ALL" + " SELECT SUM(`count`) AS ct FROM %1$s %6$s";

        if (companyId > 0) {
            statisSql = String.format(statisSql, "membercounts", getDateStr(-1), getDateStr(0), UserScanType.NewUser.getId(),
                    "AND company = " + companyId, "WHERE company = " + companyId);
        } else {
            statisSql = String.format(statisSql, "membercounts", getDateStr(-1), getDateStr(0), UserScanType.NewUser.getId(), "",
                    "");
        }

        return statisSql;
    }

    private String getUserScanStatisSql(int userScanType, int companyId) {
        String statisSql = "SELECT SUM(`count`) AS ct FROM %1$s WHERE `timestamp` >= '%2$s 00:00:00' AND `timestamp` <= '%2$s 23:59:59' AND user_type = %4$d %5$s"
                + " UNION ALL "
                + " SELECT SUM(`count`) AS ct FROM %1$s WHERE `timestamp` >= '%3$s 00:00:00' AND `timestamp` <= '%3$s 23:59:59' AND user_type = %4$d %5$s"
                + " UNION ALL" + " SELECT SUM(`count`) AS ct FROM %1$s WHERE user_type = %4$d %5$s";

        if (companyId > 0) {

            statisSql = String.format(statisSql, "membercounts", getDateStr(-1), getDateStr(0), userScanType,
                    "AND company = " + companyId);
        } else {

            statisSql = String.format(statisSql, "membercounts", getDateStr(-1), getDateStr(0), userScanType, "");
        }

        return statisSql;
    }

    private String getScanStatisSql(String tableName, int companyId) {

        String statisSql = "SELECT SUM(`count`) AS ct FROM %1$s WHERE `timestamp` >= '%2$s 00:00:00' AND `timestamp` <= '%2$s 23:59:59' %4$s"
                + " UNION ALL "
                + " SELECT SUM(`count`) AS ct FROM %1$s WHERE `timestamp` >= '%3$s 00:00:00' AND `timestamp` <= '%3$s 23:59:59' %4$s"
                + " UNION ALL" + " SELECT SUM(`count`) AS ct FROM %1$s %5$s";

        if (companyId > 0) {
            statisSql = String.format(statisSql, tableName, getDateStr(-1), getDateStr(0), "AND company = " + companyId,
                    "WHERE company = " + companyId);
        } else {
            statisSql = String.format(statisSql, tableName, getDateStr(-1), getDateStr(0), "", "");
        }

        return statisSql;
    }

    private String getDateStr(int prefixDay) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT+08:00"));
        c.add(Calendar.DAY_OF_MONTH, prefixDay);
        return sdf.format(c.getTime());
    }

}
