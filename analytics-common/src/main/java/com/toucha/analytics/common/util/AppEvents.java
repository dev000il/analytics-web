package com.toucha.analytics.common.util;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.toucha.analytics.common.common.GeneralConstants;
import com.toucha.analytics.common.model.BaseRequest;
import com.toucha.analytics.common.model.ErrorInfo;

/***
 * 
 * This class is from platform-common and will do app event definition and logging.
 * The analytics server specific errors will start at the 50000 range so that it doesn't conflict
 * with the platform's.
 *
 */
public class AppEvents {

    private int id;

    private String displayMessage;

    private static Logger logger = LoggerFactory.getLogger(AppEvents.class);

    private static final String SplitPattern = "---";

    public static final AppEvents GenericInternalFailure = new AppEvents(2000, "Internal error occurred. %s");

    public static final AppEvents SettingsInitFailure = new AppEvents(2001,
            "Error: %s failed to initialize. The config settings are invalid.");

    public static final AppEvents ParseRequestBodyException = new AppEvents(1020,
            "Exception happened during when Parse RequestBody");

    public static final AppEvents LoadSettingConfigException = new AppEvents(1013,
            "Exception happened during LoadSettingConfig, configFileName is[%s], founction name is[%s], class is [%s]");

    public static final AppEvents SaveSettingConfigException = new AppEvents(1014,
            "Exception happened during SaveSettingConfig, configFileName is[%s], founction name is[%s], class is [%s]");

    public static final AppEvents ParseExceptionFunction = new AppEvents(13, "the abnormal function name is [%s] in Class[%s]");

    // ErrorCodeConstants 00000-00003
    public static final AppEvents ServerExceptionErr = new AppEvents(0, "service exception");

    public static final AppEvents JsonKeyMissingErr = new AppEvents(1, "json key is missing");

    public static final AppEvents JsonKeyMissing = new AppEvents(4, "json key is missing,function:[%s],class:[%s],json:[%s]");

    public static final AppEvents OneFieldLengthOversizeErr = new AppEvents(2, "one field may be oversize");

    public static final AppEvents UserIdBlankErr = new AppEvents(3, "the current operate user is missing");

    public static final AppEvents UserIpBlankErr = new AppEvents(7, "the current operate userIp is missing");

    public static final AppEvents RequestIdBlankErr = new AppEvents(8, "the current operate requestId is missing");

    public static final AppEvents PlatformRequestHeaderErr = new AppEvents(9,
            "the current operate PlatformRequestHeader is missing");

    public static final AppEvents TagBatchErr = new AppEvents(10, "the current operate tagBatch is missing");

    // Requests 50000-51999
    public static class ScanReportServiceRequests {

        public static final AppEvents MissingCompanyId = new AppEvents(50000, "Missing companyId");

        public static final AppEvents MissingProductId = new AppEvents(50001,
                "Error date range scan report request from user [%s], company [%s], missing productId");

        public static final AppEvents MissingStartDate = new AppEvents(50002,
                "Error date range scan report request from user [%s], company [%s], missing startDate");

        public static final AppEvents MissingEndDate = new AppEvents(50003,
                "Error date range scan report request from user [%s], company [%s], missing endDate");

        public static final AppEvents ErrorDateRange = new AppEvents(50004,
                "Error date range scan report request from user [%s], company [%s], start date is bigger than end date");

        public static final AppEvents ErrorDateRangeOverflow = new AppEvents(50005,
                "Error date range scan report request from user [%s], company [%s], date range is bigger than max allowed range");

        public static final AppEvents NotAdminErr = new AppEvents(50006, "User not admin");

        public static final AppEvents ClientIdBlankErr = new AppEvents(50007, "ClientId is empty");

        public static final AppEvents SpecifiedFieldsNotSupported = new AppEvents(50008,
                "The fields specified are not in the supported list or null.");

        public static final AppEvents MissingEnvironment = new AppEvents(50009, "Missing environment");

        public static final AppEvents ErrorEnvironment = new AppEvents(50010, "Error environment");

        public static final AppEvents MissingRequestId = new AppEvents(50011, "Missing request id");

        public static final AppEvents ErrorCreatingLocalCsv = new AppEvents(50012,
                "Error happened in creating local csv file [%s]");

        public static final AppEvents ErrorUploadLocalFileToAzure = new AppEvents(50013,
                "Exception occurred in uploading local file [%s] to Azure blob [%s]");

        public static final AppEvents ErrorInitializingAzureEnvironment = new AppEvents(50014,
                "Error initializing azure environment with connection string [%s]");

        public static final AppEvents ErrorParsingAzureStorageConnectionStringJson = new AppEvents(50015,
                "Error parsing azure connection json [%s]");

        public static final AppEvents ErrorEnvironmentRequested = new AppEvents(50016,
                "Error request environment [%s], available environments [%]");

        public static final AppEvents ProductIdFieldNotSpecified = new AppEvents(50017,
                "The product IDs filter is specified but not in the requested 'fields'.");

        public static final AppEvents ErrorGetBlobUrl = new AppEvents(5000017,
                "Error get blob url for blob [%s], environment [%s]");

        public static final AppEvents ErrorTrendRange = new AppEvents(5000018, "Error get the report range.");

        // General requests at 51500-51999
        public static final AppEvents General_ExpectPositiveInteger = new AppEvents(51500,
                "Expect a positive non-zero integer. field: [%s]");
    }

    // Service General 52000-59999
    public static class ScanReportService {

        public static final AppEvents GeneralScanReportException = new AppEvents(50006,
                "Unexpected error happened in processing Scan Report workflow");

        public static final AppEvents DateRangeScanReportServiceException = new AppEvents(50007,
                "Error happened in the Date Range Scan Report service, request params: [%s]");

        public static final AppEvents ScanReportDuplicateDayStatistics = new AppEvents(50008,
                "Duplicate row with same day statistic, Row data [%s]");

        public static final AppEvents ScanReportErrorGetDateFromRow = new AppEvents(50009,
                "Error get date from row, Row data [%s]");

        public static final AppEvents GeneralScanReportError = new AppEvents(50010, "Error get data in method: [%s]");

        public static final AppEvents RowDataFormatError = new AppEvents(50011, "Row format error for type [%s], Row data [%s]");

        public static final AppEvents FailedToFetchScanDataFromDatabase = new AppEvents(50012,
                "Unable to fetch the scan data for the analytics report.");

        public static final AppEvents DuplicateProducts = new AppEvents(50013,
                "Error date range scan report request duplicate product");

        public static final AppEvents ScanReportErrorTagBatchId = new AppEvents(50014,
                "Error date range scan report request, error tagBatch id");

        public static final AppEvents RawActivitiesQueryException = new AppEvents(50015,
                "Error happened when obtaining raw activities from DAO, request params: [%s]");

        public static final AppEvents GeoScanReportServiceException = new AppEvents(50016,
                "Error happened in the Geo Scan Report service, request params: [%s]");

        public static final AppEvents DistributorReportServiceException = new AppEvents(50017,
                "Error happened in the distributor Report service, request params: [%s]");

        public static final AppEvents ParameterCheckFailure = new AppEvents(50018, "Parameter checked failure");

        public static final AppEvents FailedToFetchMemberDataFromDatabase = new AppEvents(50019,
                "Unable to fetch the member data");

        public static final AppEvents MemberReportServiceError = new AppEvents(50020,
                "Error happened in member report service, request companyId:%s, startDate:%s, endDate:%s");

        public static final AppEvents GeneralExceptionInMemberStat = new AppEvents(50021,
                "General error happened in member report");

        public static final AppEvents FailedToFetchPointDataFromDatabase = new AppEvents(50022, "Unable to fetch the point data");

        public static final AppEvents FetchPointDataServiceError = new AppEvents(50023,
                "Service error in fetching the point data with request: %s");

        public static final AppEvents FailedToFetchPromotionDataFromDatabase = new AppEvents(50022,
                "Unable to fetch the promotion data");

        public static final AppEvents FetchPromotionDataServiceError = new AppEvents(50023,
                "Service error in fetching the promotion data for %s with request: %s");

        public static final AppEvents FailedToFetchTodayYesterdayEnterLotteryStatData = new AppEvents(50025,
                "Unable to fetch today and yesterday enter lottery stat: %s");

        public static final AppEvents GeneralSuccessClaimedRewardError = new AppEvents(50026,
                "General error in promotion/successclaims");

        public static final AppEvents GeneralEnterLotteryReportError = new AppEvents(50027, "General error in enterlottery");

        public static final AppEvents GeneralEnterLotteryUserTypeError = new AppEvents(50028,
                "General error in enterlottery/usertype");

        public static final AppEvents GeneralEnterLotteryUniqueUserError = new AppEvents(50029,
                "General error in enterlottery/uniqueuser");

        public static final AppEvents GeneralEnterLotteryTodayYesterdayError = new AppEvents(50030,
                "General error in enterlottery/todayyesterday");

        public static final AppEvents FailedToFetchEnterLotteryStatData = new AppEvents(50032,
                "Unable to fetch enter lottery data");

        public static final AppEvents EnterLotteryServiceError = new AppEvents(50033,
                "Service error in fetching enter lottery data with request: %s");

        public static final AppEvents EnterLotteryTodayYesterdayServiceError = new AppEvents(50035,
                "Service error in fetching enter lottery today yesterday data with request: %s");

        public static final AppEvents EffectiveScanUserTypeServiceError = new AppEvents(50036,
                "Service error in fetching effective scan user type data with request: %s");

        public static final AppEvents EffectiveScanUniqueUserServiceError = new AppEvents(50037,
                "Service error in fetching effective scan unique user data with request: %s");

        public static final AppEvents FailedToFetchEffectiveScanUserTypeData = new AppEvents(50032,
                "Unable to fetch effective scan user type data");

        public static final AppEvents FailedToFetchEffectiveScanUniqueUserData = new AppEvents(50032,
                "Unable to fetch effective unique user data");

        public static final AppEvents FailedToFetchScanData = new AppEvents(50038, "Unable to fetch scan data");

        public static final AppEvents DatabaseCloseException = new AppEvents(50039, "database close exception");
    }

    // Service General 60000-61999
    public static class InternalReportService {

        public static final AppEvents GeneralInternalReportException = new AppEvents(60000,
                "Unexpected error happened in processing Internal Scan Report workflow");
    }

    public AppEvents(int id, String displayMessage) {
        super();
        this.id = id;
        this.displayMessage = displayMessage;
    }

    public int getId() {
        return id;
    }

    public String getIdAsErrorCodeStr() {
        return "err_" + ((100000 + id) + "").substring(1);
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDisplayMessage() {
        return displayMessage;
    }

    public void setDisplayMessage(String displayMessage) {
        this.displayMessage = displayMessage;
    }

    public ErrorInfo toErrorInfo(Object... parameters) {
        String s = safeStringFormat(this.getDisplayMessage(), parameters);
        return new ErrorInfo(this.getIdAsErrorCodeStr(), s);
    }

    @Override
    public String toString() {
        return this.id + GeneralConstants.COMMA + this.displayMessage;
    }

    public static void LogInformation(AppEvents appEvent) {
        LogInformation(appEvent, null, null, null);
    }

    public static void LogWarning(AppEvents appEvent) {
        LogWarning(appEvent, null, null, null);
    }

    public static void LogError(AppEvents appEvent) {
        LogError(appEvent, null, null, null);
    }

    public static void LogException(Exception e, AppEvents appEvent) {
        LogException(e, appEvent, null, null, null);
    }

    public static void LogInformation(AppEvents appEvent, String... args) {
        LogInformation(appEvent, args, null, null);
    }

    public static void LogWarning(AppEvents appEvent, String... args) {
        LogWarning(appEvent, args, null, null);
    }

    public static void LogError(AppEvents appEvent, String... args) {
        LogError(appEvent, args, null, null);
    }

    public static void LogException(Exception e, AppEvents appEvent, String... args) {
        LogException(e, appEvent, args, null, null);
    }

    public static void LogInformation(AppEvents appEvent, Object[] args, String transId) {
        LogInformation(appEvent, args, transId, null);
    }

    public static void LogWarning(AppEvents appEvent, Object[] args, String transId) {
        LogWarning(appEvent, args, transId, null);
    }

    public static void LogError(AppEvents appEvent, Object[] args, String transId) {
        LogError(appEvent, args, transId, null);
    }

    public static void LogException(Exception e, AppEvents appEvent, Object[] args, String transId) {
        if (logger.isErrorEnabled()) {
            transId = transId == null ? "" : transId;
            StringBuffer sb = new StringBuffer();
            sb.append(appEvent.id).append(SplitPattern).append(transId).append(SplitPattern).append("exception:")
                    .append(e.toString()).append(";").append(String.format(appEvent.getDisplayMessage(), args));

            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            logger.error(sb.toString() + System.lineSeparator() + sw.toString());
            sb = null;
            sw = null;
        }
    }

    private static void publishEvent2Queue(BaseRequest baseRequest, AppEvents appEvent, String msg, String level) {
        // Coming later.
    }

    public static void LogInformation(AppEvents appEvent, Object[] args, String transId, BaseRequest baseRequest) {
        if (logger.isInfoEnabled()) {
            transId = transId == null ? "" : transId;

            String msg = buildMessage(appEvent, args, transId);

            logger.info(msg);
            publishEvent2Queue(baseRequest, appEvent, msg, "INFO");
        }
    }

    public static void LogWarning(AppEvents appEvent, Object[] args, String transId, BaseRequest baseRequest) {

        if (logger.isWarnEnabled()) {
            transId = transId == null ? "" : transId;

            String msg = buildMessage(appEvent, args, transId);

            logger.warn(msg);
            publishEvent2Queue(baseRequest, appEvent, msg, "WARN");
        }
    }

    public static void LogError(AppEvents appEvent, Object[] args, String transId, BaseRequest baseRequest) {
        if (logger.isErrorEnabled()) {
            transId = transId == null ? "" : transId;

            String msg = buildMessage(appEvent, args, transId);

            logger.error(msg);
            publishEvent2Queue(baseRequest, appEvent, msg, "ERROR");
        }
    }

    public static void LogException(Exception e, AppEvents appEvent, Object[] args, String transId, BaseRequest baseRequest) {
        if (logger.isErrorEnabled()) {
            transId = transId == null ? "" : transId;

            String msg = buildMessage(appEvent, args, transId);

            // Append call stack.
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            msg = msg + System.lineSeparator() + sw.toString();

            logger.error(msg);
            publishEvent2Queue(baseRequest, appEvent, msg, "ERROR");
        }
    }

    private static String buildMessage(AppEvents appEvent, Object[] args, String transId) {
        // Formatted event message.
        String formattedMsg = safeStringFormat(appEvent.getDisplayMessage(), args);

        // Build log message.
        StringBuffer sb = new StringBuffer();
        sb.append(appEvent.id).append(SplitPattern).append(transId).append(SplitPattern).append(formattedMsg);

        return sb.toString();
    }

    private static String safeStringFormat(String format, Object[] args) {
        try {
            return String.format(format, args);
        } catch (Exception e) {
            return format;
        }
    }
}
