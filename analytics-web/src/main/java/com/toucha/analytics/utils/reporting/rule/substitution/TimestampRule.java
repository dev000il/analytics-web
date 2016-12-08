package com.toucha.analytics.utils.reporting.rule.substitution;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

public class TimestampRule implements FriendlyLabelRule {

    // Use thread local because Java's date formatter is not thread-safe.
    private static final ThreadLocal<SimpleDateFormat> dateFormatter = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        }
    };
    
    @Override
    public String[] getTargetFields() {
        return new String[] { "ts" };
    }

    @Override
    public void convert(int[] targetFieldsIndex, String[] row) {
        Preconditions.checkArgument(targetFieldsIndex.length == 1 && targetFieldsIndex[0] > -1
                && targetFieldsIndex[0] < row.length);
        
        int index = targetFieldsIndex[0];
        String timestamp = row[index];
        
        if (!Strings.isNullOrEmpty(timestamp)) {
            try {
                // We store the timestamp as a JavaTime in CST.
                Long timestampLong = Long.parseLong(timestamp);
                Date date = new Date(timestampLong.longValue());
                row[index] = dateFormatter.get().format(date);
            }
            catch (Exception e) {
                // Unable to convert, leave the timestamp as is.
            }
        }
    }
    
}
