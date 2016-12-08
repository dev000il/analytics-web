package com.toucha.analytics.utils.reporting.rule.substitution;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.toucha.analytics.utils.MdPromotionSub;

public class PromotionRule implements FriendlyLabelRule {

    @Override
    public String[] getTargetFields() {
        return new String[] { "pri" };
    }

    @Override
    public void convert(int[] targetFieldsIndex, String[] row) {
        Preconditions.checkArgument(targetFieldsIndex.length == 1 && targetFieldsIndex[0] > -1
                && targetFieldsIndex[0] < row.length);

        int index = targetFieldsIndex[0];
        String product = row[index];

        if (!Strings.isNullOrEmpty(product) && MdPromotionSub.promotionMapping.containsKey(product)) {
            row[index] = MdPromotionSub.promotionMapping.get(product);
        }

    }

}
