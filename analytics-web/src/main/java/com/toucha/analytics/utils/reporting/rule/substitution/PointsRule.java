package com.toucha.analytics.utils.reporting.rule.substitution;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

public class PointsRule implements FriendlyLabelRule {

    @Override
    public String[] getTargetFields() {
        return new String[] { "pts" };
    }

    @Override
    public void convert(int[] targetFieldsIndex, String[] row) {
        Preconditions.checkArgument(targetFieldsIndex.length == 1 && targetFieldsIndex[0] > -1
                && targetFieldsIndex[0] < row.length);
        
        int index = targetFieldsIndex[0];
        String json = row[index];
        
        if (!Strings.isNullOrEmpty(json)) {
            
            // The points are logged as JSON: {"HENGDAPOINT":100}
            // 2015.04.01 - Currently, we only have one set of points, HENGDAPOINTs.
            JSONObject jsonObj = JSON.parseObject(json);
            if (jsonObj != null && jsonObj.values() != null) {
                String result = "";
                
                for (Object value : jsonObj.values()) {
                    if (value != null) {
                        String points = value.toString();
                        result += "+" + points + "积分 ";
                    }
                }
                
                row[index] = result.trim();
            }
        }
    }
}
