package com.toucha.analytics.utils.reporting.rule.substitution;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.toucha.analytics.utils.MdRewardSub;

public class RwdsRule implements FriendlyLabelRule {

    @Override
    public String[] getTargetFields() {
        return new String[] { "rwds" };
    }

    @Override
    public void convert(int[] targetFieldsIndex, String[] row) {
        Preconditions.checkArgument(targetFieldsIndex.length == 1 && targetFieldsIndex[0] > -1
                && targetFieldsIndex[0] < row.length);

        int index = targetFieldsIndex[0];
        String json = row[index];

        if (!Strings.isNullOrEmpty(json) && json.startsWith("{") && !json.equals("{}")) {
            // The points are logged as JSON: {"HENGDAPOINT":100}
            // 2015.04.01 - Currently, we only have one set of points,
            // HENGDAPOINTs.
            try {
                JSONObject jsonObj = JSON.parseObject(json);
                if (jsonObj != null && jsonObj.values() != null) {
                    String result = "";

                    for (Object value : jsonObj.keySet()) {
                        if (value != null) {
                            String rwds = value.toString();
                            String s = MdRewardSub.rewardMapping.get(rwds) == null ? "" : MdRewardSub.rewardMapping.get(rwds);
                            result += s;
                        }
                    }

                    row[index] = result.trim();
                }
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
    }
}
