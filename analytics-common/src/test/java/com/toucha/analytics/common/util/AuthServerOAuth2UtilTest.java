package com.toucha.analytics.common.util;

import static org.testng.Assert.assertNotNull;

import org.testng.annotations.Test;

/**
 * Test Auth2 create access token
 *
 * @author senhui.li
 */
public class AuthServerOAuth2UtilTest {

    @Test
    public void testGetAccessToken() throws Exception {
        String token = AuthServerOAuth2Util.getAccessToken();
        assertNotNull(token);
        System.out.println(token);
    }
}