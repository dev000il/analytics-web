package com.toucha.analytics.common.dao;

import java.util.Collection;

public class DataSetWithKey extends DataSet {

    String key;

    public DataSetWithKey(Collection<String> headers, Collection<String[]> dataSet, String key) {
        super(headers, dataSet);
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

}
