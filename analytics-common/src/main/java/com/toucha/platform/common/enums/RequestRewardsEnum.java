package com.toucha.platform.common.enums;

/*
 * Get rewards report uses same code beside they query differrent db
 * This is used to tell the difference: sp_winlotterycount or sp_claimcount
 */
public enum RequestRewardsEnum {
    
    WINLOTTERY(1),
    CLAIM(2);

    private int id;

    private RequestRewardsEnum(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }
}
