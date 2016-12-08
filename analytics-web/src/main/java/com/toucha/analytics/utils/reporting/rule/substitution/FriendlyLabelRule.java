package com.toucha.analytics.utils.reporting.rule.substitution;


public interface FriendlyLabelRule {
    public String[] getTargetFields();
    public void convert(int[] targetFieldsIndex, String[] row);
}
