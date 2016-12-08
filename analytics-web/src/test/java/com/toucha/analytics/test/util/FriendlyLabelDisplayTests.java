package com.toucha.analytics.test.util;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;
import com.toucha.analytics.common.dao.DataSet;
import com.toucha.analytics.utils.reporting.FriendlyLabelDisplay;

public class FriendlyLabelDisplayTests {
    
    @Test
    public void testCharacterRemovalRule() {
        List<String[]> rows = Lists.newArrayList();
        rows.add(new String[] { "上海市" });
        rows.add(new String[] { "深圳市" });
        
        DataSet data = new DataSet(Arrays.asList("city"), rows);
        FriendlyLabelDisplay.executeRules(data);
        
        Assert.assertEquals("上海", data.getData().get(0)[0]);
        Assert.assertEquals("深圳", data.getData().get(1)[0]);
    }
}
