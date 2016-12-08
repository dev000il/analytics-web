package com.toucha.analytics.utils.reporting.rule.substitution;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

public class WechatIdRule implements FriendlyLabelRule{

	@Override
	public String[] getTargetFields() {
		 return new String[] { "wu" };	}

	@Override
	public void convert(int[] targetFieldsIndex, String[] row) {
		Preconditions.checkArgument(targetFieldsIndex.length == 1 && targetFieldsIndex[0] > -1
                && targetFieldsIndex[0] < row.length);
        
        int index = targetFieldsIndex[0];
        String wechat = row[index];
        
        if (!Strings.isNullOrEmpty(wechat) && wechat.length() > 6) {
            char[] digits = wechat.toCharArray();
            
            int count = wechat.length() - 6;
            
            for (int i = 3; i < digits.length && count > 0; i++, count--) {
                digits[i] = '*';
            }
            
            row[index] = new String(digits);
        }
	}

}
