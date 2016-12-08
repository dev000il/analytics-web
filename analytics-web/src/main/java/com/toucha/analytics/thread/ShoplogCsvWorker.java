package com.toucha.analytics.thread;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.google.common.base.Strings;
import com.toucha.analytics.common.azure.AzureOperation;
import com.toucha.analytics.common.dao.DataSet;
import com.toucha.analytics.common.util.AppEvents;
import com.toucha.analytics.common.util.ZipUtil;
import com.toucha.analytics.model.request.ShoplogCsvRequest;
import com.toucha.analytics.service.ShoplogService;

public class ShoplogCsvWorker implements Runnable {
    private String zipBlobName;
    private String pngBlobName;
    private ShoplogCsvRequest request;
    private ShoplogService service;

    public ShoplogCsvWorker(String zipBlobName, String pngBlobName, ShoplogCsvRequest request, ShoplogService service) {
        super();
        this.zipBlobName = zipBlobName;
        this.pngBlobName = pngBlobName;
        this.request = request;
        this.service = service;
    }

    public String getZipBlobName() {
        return zipBlobName;
    }

    public void setZipBlobName(String zipBlobName) {
        this.zipBlobName = zipBlobName;
    }

    public String getPngBlobName() {
        return pngBlobName;
    }

    public void setPngBlobName(String pngBlobName) {
        this.pngBlobName = pngBlobName;
    }

    public ShoplogCsvRequest getRequest() {
        return request;
    }

    public void setRequest(ShoplogCsvRequest request) {
        this.request = request;
    }

    public ShoplogService getService() {
        return service;
    }

    public void setService(ShoplogService service) {
        this.service = service;
    }

    @Override
    public void run() {

        try {
            DataSet activities = service.getRawActivities(request);
            Pair<String, String> filePaths = createActivitiesCsv(activities, request.getRequestHeader().getRequestId());
            if (StringUtils.isNotBlank(filePaths.getLeft())) {
                AzureOperation.uploadFileToBlob(filePaths.getLeft(), zipBlobName, request.getEnvironment());
                AzureOperation.uploadFileToBlob(filePaths.getRight(), pngBlobName, request.getEnvironment());

                File zipFile = new File(filePaths.getLeft());
                zipFile.delete();
                File pngFile = new File(filePaths.getRight());
                pngFile.delete();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            AppEvents.LogException(ex, AppEvents.ScanReportService.RawActivitiesQueryException, request.toString());
        }
    }

    private Pair<String, String> createActivitiesCsv(DataSet activities, String requestId) throws IOException {

        Pair<String, String> localFilePaths = null;

        String baseFileName = System.getProperty("user.dir") + "/" + requestId;
        String filePath = baseFileName + ".csv";
        String zipFilePath = baseFileName + ".zip";
        String pngFilePath = baseFileName + ".png";

        FileOutputStream stream = null;
        BufferedWriter writer = null;

        List<String[]> dataSet = activities.getData();
        List<String> fields = activities.getHeader();

        try {
            stream = new FileOutputStream(filePath);
            // write BOM
            final byte[] bom = new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF };
            stream.write(bom);
            writer = new BufferedWriter(new OutputStreamWriter(stream, "utf-8"));

            // Write out the header
            for (int i = 0; i < fields.size(); i++) {
                writer.write("\"");
                String friendlyName = getHeaderFriendlyName(fields.get(i));
                writer.write(friendlyName);
                writer.write("\"");

                // If not the last cell, add a , delimiter
                if (i < fields.size() - 1) {
                    writer.write(",");
                }
            }

            writer.write("\n");

            // Write out the data
            for (int i = 0; i < dataSet.size(); i++) {
                String[] row = dataSet.get(i);
                for (int j = 0; j < row.length; j++) {
                    writer.write("\"");

                    if (row[j] != null) {
                        // Escape a double: " --> ""
                        row[j] = row[j].replace("\"", "\"\"");
                        writer.write(row[j]);
                    }

                    writer.write("\"");

                    // If not the last cell, add a , delimiter
                    if (j < row.length - 1) {
                        writer.write(",");
                    }
                }

                // If not the last row
                if (i < dataSet.size() - 1) {
                    writer.write("\n");
                }
            }

            writer.close();
            if (!ZipUtil.zip(filePath, zipFilePath)) {
                zipFilePath = "";
            }

            // write auxiliary png for async processing
            createImage(pngFilePath);
            localFilePaths = Pair.of(zipFilePath, pngFilePath);

        } catch (Exception ex) {
            localFilePaths = Pair.of("", "");
            ex.printStackTrace();
            AppEvents.LogException(ex, AppEvents.ScanReportServiceRequests.ErrorCreatingLocalCsv, filePath);

        }

        return localFilePaths;
    }

    private static void createImage(String filePath) throws IOException {
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setBackground(new Color(255, 0, 0));
        g2d.dispose();

        ImageIO.write(image, "png", new File(filePath));
    }

    private static String getHeaderFriendlyName(String field) {
        if (!Strings.isNullOrEmpty(field) && ShoplogCsvRequest.supportedFieldsFriendlyName.containsKey(field)) {
            return ShoplogCsvRequest.supportedFieldsFriendlyName.get(field);
        }

        return field;
    }

}
