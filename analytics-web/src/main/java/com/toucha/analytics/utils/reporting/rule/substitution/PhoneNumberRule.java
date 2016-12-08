package com.toucha.analytics.utils.reporting.rule.substitution;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

public class PhoneNumberRule implements FriendlyLabelRule {

    @Override
    public String[] getTargetFields() {
        return new String[] { "pn" };
    }

    @Override
    public void convert(int[] targetFieldsIndex, String[] row) {
        Preconditions.checkArgument(targetFieldsIndex.length == 1 && targetFieldsIndex[0] > -1
                && targetFieldsIndex[0] < row.length);
        
        int index = targetFieldsIndex[0];
        String phone = row[index];
        
        if (!Strings.isNullOrEmpty(phone) && phone.length() > 3) {
            char[] digits = phone.toCharArray();
            
            // Chinese phone numbers are 11 digits, so we'll mask 4 such that it appears as such
            // 156****9657
            int count = 4;
            
            for (int i = 3; i < digits.length && count > 0; i++, count--) {
                digits[i] = '*';
            }
            
            row[index] = new String(digits);
        }
    }
}
