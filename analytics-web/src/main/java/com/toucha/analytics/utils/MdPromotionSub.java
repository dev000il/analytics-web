package com.toucha.analytics.utils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import com.google.common.collect.Maps;
import com.toucha.analytics.common.common.ApplicationConfig;

public class MdPromotionSub {
    public static final Map<String, String> promotionMapping = Maps.newHashMap();

    static {
        try {
            InputStream in = new FileInputStream(ApplicationConfig.MAPPING_FOLDER + "promotion.csv");
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            String s = null;
            while ((s = reader.readLine()) != null) {
                String ss[] = s.split(",");
                promotionMapping.put(ss[0], ss[1]);
            }
            reader.close();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
