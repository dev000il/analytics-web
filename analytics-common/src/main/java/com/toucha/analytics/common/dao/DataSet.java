package com.toucha.analytics.common.dao;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

@SuppressWarnings("unchecked")
public class DataSet {

    @JSONField(name = "fields")
    private ImmutableList<String> fields;

    @JSONField(name = "data")
    private ImmutableList<String[]> data;

    public DataSet(Collection<String> headers, Collection<String[]> dataSet) {
        this.fields = ImmutableList.copyOf(Preconditions.checkNotNull(headers));
        this.data = ImmutableList.copyOf(Preconditions.checkNotNull(dataSet));
    }

    public ImmutableList<String> getHeader() {
        return this.fields;
    }

    public ImmutableList<String[]> getData() {
        return this.data;
    }

    public void replaceValuesWithNameMap(Map<String, Map<String, String>> nameMap) {
        if (nameMap != null && !nameMap.isEmpty()) {

            List<String> fields = this.fields;

            for (int i = 0; i < fields.size(); i++) {
                // Name map entry found
                if (nameMap.containsKey(fields.get(i))) {
                    Map<String, String> friendlyNames = nameMap.get(fields.get(i));

                    // Perform replacements
                    for (int row = 0; row < data.size(); row++) {
                        String cell = data.get(row)[i];
                        if (fields.get(i).equals("rwds") && !Strings.isNullOrEmpty(cell) && !cell.equals("{}")
                                && !cell.equalsIgnoreCase("null")) {
                            String tmp = "";
                            // System.out.println("rwds:" + cell);
                            try {
                                HashMap<String, String> map = JSON.parseObject(cell, HashMap.class);
                                for (String k : map.keySet()) {
                                    tmp += k;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            cell = tmp;
                        }
                        if (cell != null && friendlyNames.containsKey(cell)) {
                            data.get(row)[i] = friendlyNames.get(cell);
                        }
                        if (fields.get(i).equals("rwds") && cell.equals("")) {
                            data.get(row)[i] = "";
                        }

                    }
                }
            }
        }
    }

    public DataSet subtract(Collection<Integer> indicesToRemove) {
        Set<Integer> indices = new HashSet<Integer>(indicesToRemove);
        List<String[]> newDataSet = Lists.newArrayList();

        for (int i = 0; i < this.getData().size(); i++) {
            if (!indices.contains(i))
                newDataSet.add(this.getData().get(i));
        }

        return new DataSet(this.getHeader(), newDataSet);
    }
}
