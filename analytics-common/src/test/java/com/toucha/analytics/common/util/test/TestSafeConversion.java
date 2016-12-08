package com.toucha.analytics.common.util.test;

import java.math.BigDecimal;

import org.testng.annotations.Test;

import com.toucha.analytics.common.util.SafeConversion;

public class TestSafeConversion {

    @Test
    public void testtryGetBigDecimalEmptyParam() {

        try {
            String str = "";

            assert (SafeConversion.tryGetBigDecimal(str) == null);
        } catch (IllegalArgumentException ex) {

        }
    }

    @Test
    public void testtryGetBigDecimalInvalidParam() {
        String str = "abc";

        assert (SafeConversion.tryGetBigDecimal(str) == null);
    }

    @Test
    public void testtryGetBigDecimalCorrectParam1() {
        String str = "123";

        assert (SafeConversion.tryGetBigDecimal(str).floatValue() == 123);
    }

    @Test
    public void testtryGetBigDecimalCorrectParam2() {
        String str = "123.36";

        BigDecimal result = SafeConversion.tryGetBigDecimal(str);

        System.out.println(result);

        assert (result.floatValue() == 123.36f);
    }
}
