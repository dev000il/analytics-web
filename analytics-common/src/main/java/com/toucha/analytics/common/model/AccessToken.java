package com.toucha.analytics.common.model;

import com.toucha.analytics.common.common.ApplicationConfig;
import com.toucha.analytics.common.util.DateHelper;
import com.toucha.platform.common.enums.DateTimeUnit;

import java.util.Date;

/**
 * Access token
 *
 * @author ming.gao, senhui.li
 */
public class AccessToken {

    private final String accessToken;     //accessToken 的值
    private final String refreshToken;    //刷新accessToken 的值
    private final long expiryInMillis;    //accessToken到期需要的时间
    private final Date thresholdTime;

    public AccessToken(String accessToken, String refreshToken, int expiryInSeconds) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        // refresh the access token before expire time
        this.expiryInMillis = System.currentTimeMillis() + (expiryInSeconds - 10) * 1000;

        this.thresholdTime = DateHelper.getPreviousOrLaterTime(ApplicationConfig.TokenThresholdInMinutes, DateTimeUnit.MINUTIE.getValue());
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public long getExpiryInMillis() {
        return expiryInMillis;
    }

    public Date getThresholdTime() {
        return thresholdTime;
    }

    public boolean isAvailable() {
        if(this.accessToken != null && this.expiryInMillis > System.currentTimeMillis()) {
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "AccessToken [accessToken=" + accessToken + ", refreshToken="
                + refreshToken + ", expiryInMillis=" + expiryInMillis
                + ", thresholdTime="
                + thresholdTime + "]";
    }

}

