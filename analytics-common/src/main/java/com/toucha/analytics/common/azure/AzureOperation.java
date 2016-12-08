package com.toucha.analytics.common.azure;

import java.io.File;
import java.io.FileInputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Preconditions;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.blob.BlobContainerPermissions;
import com.microsoft.azure.storage.blob.BlobContainerPublicAccessType;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.toucha.analytics.common.common.ApplicationConfig;
import com.toucha.analytics.common.util.AppEvents;

public class AzureOperation {
    private static String azureConnectionString;
    private static String reportCsvContainer;
    private static Map<String, CloudBlobContainer> azureStorageContainerMap = new HashMap<String, CloudBlobContainer>();

    static {
        azureConnectionString = ApplicationConfig.AzureStorageConnectionString;
        reportCsvContainer = ApplicationConfig.ReportCsvContainer;

        JSONObject connectionStrings = null;
        try {
            connectionStrings = JSON.parseObject(azureConnectionString);

            if (connectionStrings != null && connectionStrings.size() > 0) {
                checkAndCreateAzureContainerMap(connectionStrings);
            }
        } catch (Exception ex) {
            AppEvents.LogException(ex, AppEvents.ScanReportServiceRequests.ErrorParsingAzureStorageConnectionStringJson,
                    azureConnectionString);
        }
    }

    public static Set<String> getSupportedEnvironments() {
        if (azureStorageContainerMap != null) {
            return azureStorageContainerMap.keySet();
        } else {
            return Collections.emptySet();
        }
    }

    public static void checkAndCreateAzureContainerMap(JSONObject connectionStrings) {
        Set<String> envs = connectionStrings.keySet();

        try {
            for (String env : envs) {

                if (azureStorageContainerMap.containsKey(env.toUpperCase()))
                    continue;

                CloudStorageAccount storageAccount = CloudStorageAccount.parse(connectionStrings.getString(env));

                CloudBlobClient blobClient = storageAccount.createCloudBlobClient();

                CloudBlobContainer container = blobClient.getContainerReference(reportCsvContainer);
                
                BlobContainerPermissions permission = new BlobContainerPermissions();
                permission.setPublicAccess(BlobContainerPublicAccessType.BLOB);

                container.createIfNotExists();
                container.uploadPermissions(permission);

                azureStorageContainerMap.put(env.toUpperCase(), container);
            }
        } catch (Exception ex) {
            AppEvents.LogException(ex, AppEvents.ScanReportServiceRequests.ErrorInitializingAzureEnvironment,
                    connectionStrings.toJSONString());
        }
    }

    public static String uploadFileToBlob(String filePath, String blobName, String environment) {
        Preconditions.checkArgument(StringUtils.isNotBlank(environment));
        String blobUrl = "";

        try {
            CloudBlobContainer container = azureStorageContainerMap.get(environment.toUpperCase());
            CloudBlockBlob blob = container.getBlockBlobReference(blobName);
            File source = new File(filePath);
            blob.upload(new FileInputStream(source), source.length());

            if (blob.exists()) {
                blobUrl = blob.getUri().toString();
            }
        } catch (Exception e) {
            AppEvents.LogException(e, AppEvents.ScanReportServiceRequests.ErrorUploadLocalFileToAzure, filePath, blobName);
        }

        return blobUrl;
    }
    
    public static String getCsvDownloadBlobUrl(String blobName, String environment) {
    	Preconditions.checkArgument(StringUtils.isNotBlank(environment));
    	
    	String blobUrl = "";
    	
    	try {
            CloudBlobContainer container = azureStorageContainerMap.get(environment.toUpperCase());
            CloudBlockBlob blob = container.getBlockBlobReference(blobName);
            
            blobUrl = blob.getUri().toString();
        } catch (Exception e) {
            AppEvents.LogException(e, AppEvents.ScanReportServiceRequests.ErrorGetBlobUrl, blobName, environment);
        }

        return blobUrl;
    }
}
