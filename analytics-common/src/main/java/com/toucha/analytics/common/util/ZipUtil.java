package com.toucha.analytics.common.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

public class ZipUtil {
    private static Logger logger = LoggerFactory.getLogger(ZipUtil.class);
    
    public static void zip(String[] files, String zippedFile) {
        File zipped = new File(zippedFile);
        String path = "";
        File[] srcFiles = new File[files.length];
        for (int i = 0; i < files.length; i++) {
            File file = new File(files[i]);
            srcFiles[i] = file;
        }
        try {
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipped), Charset.forName("GB2312"));
            zipFiles(out, path, srcFiles);
            out.close();
        } catch (IOException ioe) {
            ioe.getStackTrace();
        }
    }

    public static boolean zip(String file, String zippedFile) {
        File zipped = new File(zippedFile);
        String path = "";
        File[] srcFiles = new File[1];
        srcFiles[0] = new File(file);
        try {
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipped), Charset.forName("GB2312"));

            boolean result = zipFiles(out, path, srcFiles);
            out.close();
            return result;
        } catch (IOException e) {
            logger.error("file not found:" + zipped);
        }
        return false;
    }
    
    public static boolean zipFiles(ZipOutputStream out, String path, File... srcFiles) {
        path = path.replaceAll("\\*", "/");
        if (!Strings.isNullOrEmpty(path) && !path.endsWith("/")) {
            path += "/";
        }
        byte[] buf = new byte[1024];
        try {
            for (int i = 0; i < srcFiles.length; i++) {
                if (srcFiles[i].isDirectory()) {
                    File[] files = srcFiles[i].listFiles();
                    String srcPath = srcFiles[i].getName();
                    srcPath = srcPath.replaceAll("\\*", "/");
                    if (!srcPath.endsWith("/")) {
                        srcPath += "/";
                    }

                    zipFiles(out, path + srcPath, files);
                } else {
                    FileInputStream in = new FileInputStream(srcFiles[i]);
                    out.putNextEntry(new ZipEntry(path + srcFiles[i].getName()));
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    out.closeEntry();
                    in.close();
                }
            }
            return true;
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        return false;
    }
}

