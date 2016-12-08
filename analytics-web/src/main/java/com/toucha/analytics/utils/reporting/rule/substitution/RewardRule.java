package com.toucha.analytics.utils.reporting.rule.substitution;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.toucha.analytics.utils.MdRewardSub;

public class RewardRule implements FriendlyLabelRule {

    @Override
    public String[] getTargetFields() {
        return new String[] { "rid" };
    }

    @Override
    public void convert(int[] targetFieldsIndex, String[] row) {
        Preconditions.checkArgument(targetFieldsIndex.length == 1 && targetFieldsIndex[0] > -1
                && targetFieldsIndex[0] < row.length);

        int index = targetFieldsIndex[0];
        String product = row[index];

        if (!Strings.isNullOrEmpty(product) && MdRewardSub.rewardMapping.containsKey(product)) {
            row[index] = MdRewardSub.rewardMapping.get(product);
        }

    }

}
