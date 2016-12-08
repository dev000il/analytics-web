package com.toucha.analytics.utils.reporting.rule.substitution;

import com.google.common.base.Preconditions;

public class CharacterRemovalRule implements FriendlyLabelRule {
    
    @Override
    public String[] getTargetFields() {
        return new String[] { "city", "state" };
    }

    @Override
    public void convert(int[] targetFieldsIndex, String[] row) {
        Preconditions.checkArgument(targetFieldsIndex.length == 2);

        // city
        int index = targetFieldsIndex[0];
        if (index > -1 && index < row.length && row[index] != null) {
            row[index] = this.removeChars(row[index], new String[] { "市" });
        }
        
        // state
        index = targetFieldsIndex[1];
        if (index > -1 && index < row.length && row[index] != null) {
            row[index] = this.removeChars(row[index], new String[] { "省", "市" });
        }
    }
    
    private String removeChars(String value, String[] charsToRemove) {
        for (String charToRemove : charsToRemove) {
            value = value.replace(charToRemove, "");
        }
        return value;
    }
}
