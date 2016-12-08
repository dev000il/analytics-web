package com.toucha.analytics.common.common;

import com.toucha.analytics.common.util.AppEvents;
import com.toucha.analytics.common.util.SettingsHelper;

public class ApplicationConfig {

    public static SettingsHelper setting = null;

    public static SettingsHelper titles = null;

    static {
        String setPath = "/application_config.properties";
        String titlePath = "/internal_reports_title.properties";
        // 初始化配置文件
        if (!LoadSetting(setPath)) {
            AppEvents.LogError(AppEvents.SettingsInitFailure, "GlobalEnvironment.LoadSetting");
        }

        if (!LoadTitles(titlePath)) {
            AppEvents.LogError(AppEvents.SettingsInitFailure, "GlobalEnvironment.LoadTitle");
        }
    }

    /**
     * initialize global setting
     * 
     * @param path
     * @return
     */
    private static boolean LoadSetting(String path) {
        if (setting == null) {
            setting = new SettingsHelper();
            if (!setting.load(path)) {
                return false;
            }
        }
        return true;
    }

    private static boolean LoadTitles(String path) {
        if (titles == null) {
            titles = new SettingsHelper();
            if (!titles.load(path)) {
                return false;
            }
        }
        return true;
    }

    public static Integer getInteger(String key) {
        String value = setting.getValue(key);
        if (value != null) {
            return Integer.valueOf(value);
        }
        return null;
    }

    public static final String JWT_RSA_SIGN_VERIFY_KEY = setting.getValue("JWT.AccessRsaSignatureVerificationCert");

    public static final String MySqlConnectionStr = setting.getValue("MySqlConnectionString");

    public static final String ShopMySqlConnectionStr = setting.getValue("ShopSqlConnectionString");

    public static final String ScanReportTable = setting.getValue("ScanReportTable");

    public static final String hbaseZookeeperQuorum = setting.getValue("hbase.zookeeper.quorum");

    public static final String AzureStorageConnectionString = setting.getValue("StorageAccountConfig.ConnectionString");

    public static final String ReportCsvContainer = setting.getValue("ReportCsvContainer");

    public static final String ElasticSearchServer = setting.getValue("elasticsearch.server");

    // Internal reports title
    public static final String INTERNAL_TODAY = titles.getValue("internal.today");

    public static final String INTERNAL_YESTODAY = titles.getValue("internal.yestoday");

    public static final String INTERNAL_TOTAL = titles.getValue("internal.total");

    public static final String INTERNAL_SCAN = titles.getValue("internal.scan");

    public static final String INTERNAL_SCAN_TITLE = titles.getValue("internal.scan.title");

    public static final String INTERNAL_NORMAL_SCAN_TITLE = titles.getValue("internal.normal.scan.title");

    public static final String INTERNAL_VALID_SCAN_TITLE = titles.getValue("internal.valid.scan.title");

    public static final String INTERNAL_NEWUSER_SCAN_TITLE = titles.getValue("internal.newuser.scan.title");

    public static final String INTERNAL_OLDUSER_SCAN_TITLE = titles.getValue("internal.olduser.scan.title");

    public static final String INTERNAL_MEMBER = titles.getValue("internal.member");

    public static final String INTERNAL_MEMBER_TITLE = titles.getValue("internal.member.title");

    public static final String INTERNAL_MEMBER_NEWUSER = titles.getValue("internal.member.newuser");

    public static final String INTERNAL_MARKET_TITLE = titles.getValue("internal.market.title");

    public static final String INTERNAL_MARKET_AWARD_COUNTS = titles.getValue("internal.market.award.counts");

    public static final String INTERNAL_MARKET_AWARD_MONEY = titles.getValue("internal.market.award.money");

    public static final String INTERNAL_MARKET_AWARD_PHONEBILL_COUNTS = titles.getValue("internal.market.award.phonebill.counts");

    public static final String INTERNAL_MARKET_AWARD_UNIONPAY_COUNTS = titles.getValue("internal.market.award.unionpay.counts");

    public static final String INTERNAL_MARKET_AWARD_WECHAT_COUNTS = titles.getValue("internal.market.award.wechat.counts");

    public static final String INTERNAL_MARKET_AWARD_PHONEBILL_MONEY = titles.getValue("internal.market.award.phonebill.money");

    public static final String INTERNAL_MARKET_AWARD_UNIONPAY_MONEY = titles.getValue("internal.market.award.unionpay.money");

    public static final String INTERNAL_MARKET_AWARD_WECHAT_MONEY = titles.getValue("internal.market.award.wechat.money");

    public static final String INTERNAL_MARKET_AWARD_POINTS = titles.getValue("internal.market.award.points");

    public static final String REDIS_MASTER_SERVER = setting.getValue("RedisMaster.server");

    public static final int REDIS_MASTER_PORT = Integer.parseInt(setting.getValue("RedisMaster.port"));

    public static final int WEBSOCKET_HEARTBEAT_TIME = getInteger("Websockt.Heartbeat.Time");

    public static final String PLATFORM_WEB_URL = setting.getValue("Platform.web.url");

    public static final String PLATFORM_DATAPIPELINE_URL = setting.getValue("Platform.datapinle.url");

    public static final String PLATFORM_TAGS_URL = setting.getValue("Platform.tags.url");

    public static final String AuthServerGetAccessToken = setting.getValue("AuthServer.GetAccessToken");

    public static final String PLATFORMClientUserName = setting.getValue("PlatForm.ClientUserName");

    public static final String PLATFORMClientPassWord = setting.getValue("PlatForm.ClientPassWord");

    public static final int AUTH_SERVER_CONNECTION_TIMEOUT = getInteger("AuthServer.ConnectionTimeOut");

    public static final int AUTH_SERVER_SOCKET_TIMEOUT = getInteger("AuthServer.SocketTimeOut");

    public static final int TokenThresholdInMinutes = getInteger("AuthServer.TokenThresholdInMinutes");

    public static final String HIVE_JDBC_URL = setting.getValue("hive.jdbc.url");

    public static final String MAPPING_FOLDER = setting.getValue("mapping.folder");

    public static final String MYSQLJDBCDRIVER = setting.getValue("MySqlJDBCDriver");

    public static final String CENTBONUSERNAME = setting.getValue("centbon.username");

    public static final String CENTBONPASSWORD = setting.getValue("centbon.password");

    public static final String PLATFORM_JA_WEB_URL = setting.getValue("Platform.ja.web.url");

    public static final String API_TIME_LIMITED_COMPANYID = setting.getValue("api.time.limited.companyid");

    public static final String ACCESS_TOKEN_USERNAME = setting.getValue("accesstoken.username");

    public static final String ACCESS_TOKEN_PASSWORD = setting.getValue("accesstoken.password");
}
