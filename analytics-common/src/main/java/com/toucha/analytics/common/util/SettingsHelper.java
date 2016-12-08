package com.toucha.analytics.common.util;

import java.io.FileWriter;
import java.io.InputStream;
import java.io.Writer;
import java.util.Properties;

public class SettingsHelper {
    private Properties prop = new Properties();

    public boolean load(String path) {
        try {
            InputStream in = SettingsHelper.class.getResourceAsStream(path);
            // BufferedReader reader = new BufferedReader(new FileReader(path));
            // reader.close();
            prop.load(in);
            in.close();
        } catch (Exception e) {
            AppEvents.LogException(e, AppEvents.LoadSettingConfigException, path, "load", "SettingsHelper");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean save(String path) {
        try {
            Writer writer = new FileWriter(path);
            prop.store(writer, "writer");
            writer.close();
        } catch (Exception e) {
            AppEvents.LogException(e, AppEvents.SaveSettingConfigException, path,"save", "SettingsHelper");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public String getValue(String key) {
        return prop.getProperty(key);
    }

    public void setValue(String key, String value) {
        prop.put(key, value);
    }
    
    

}
