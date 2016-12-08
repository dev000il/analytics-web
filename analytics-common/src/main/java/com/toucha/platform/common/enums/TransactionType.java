package com.toucha.platform.common.enums;

/**
 * All transaction type enum
 * 
 * @author senhui.li
 */
public enum TransactionType {

    Unknown(-1),
    ExchangeMallReward(1),
    PointGame(2),
    ClaimPointGameReward(3),
    ClaimPointLotteryReward(4),
    PointLottery(5),
    ChanceGame(6),
    ClaimChanceGameReward(7),
    CrowdfundingActivity(8),
    CrowdfundingLottery(9),
    CrowdfundingClaim(10),
    CrowdfundingCancel(11),
    PresentedPoints(12);

    private int type;

    TransactionType(int type) {
        this.type = type;
    }

    public int getValue() {
        return this.type;
    }

    public static TransactionType getType(int type) {
        for (TransactionType tt : TransactionType.values()) {
            if (tt.type == type) {
                return tt;
            }
        }
        return null;
    }
}
