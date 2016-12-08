package com.toucha.analytics.utils.reporting.rule.substitution;

import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;

public class OkIdRule implements FriendlyLabelRule {

    private static final Map<String,String> okIdMap = Maps.newHashMap();
    
    
    static {
        okIdMap.put("1", "成功");
        okIdMap.put("2", "失败");
        okIdMap.put("3", "以后再说");
    }
    
    @Override
    public String[] getTargetFields() {
        return new String[] { "okid" };
    }

    @Override
    public void convert(int[] targetFieldsIndex, String[] row) {
        Preconditions.checkArgument(targetFieldsIndex.length == 1 && targetFieldsIndex[0] > -1
                && targetFieldsIndex[0] < row.length);
        
        int index = targetFieldsIndex[0];
        String okid = row[index];
        
        if (!Strings.isNullOrEmpty(okid) && okIdMap.containsKey(okid)) {
            row[index] = okIdMap.get(okid);
        }
    }
}
