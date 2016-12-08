package com.toucha.analytics.utils.reporting.rule.substitution;

import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;

public class RewardTypeRule implements FriendlyLabelRule {

    private static final Map<String,String> rewardTypeMap = Maps.newHashMap();
    
    static {
        rewardTypeMap.put("-1", "以后再说");
        rewardTypeMap.put("1", "话费");
        rewardTypeMap.put("2", "支付宝");
        rewardTypeMap.put("3", "实物");
        rewardTypeMap.put("4", "银联卡");
        rewardTypeMap.put("6", "积分奖品");
        rewardTypeMap.put("7", "微信红包");
        rewardTypeMap.put("8", "黄金");
        rewardTypeMap.put("9", "微信裂变红包");
        rewardTypeMap.put("10", "门店");
        rewardTypeMap.put("11", "慈善基金");
        rewardTypeMap.put("12", "其他");
        rewardTypeMap.put("13", "实物");
        rewardTypeMap.put("14", "微信提现红包");
        rewardTypeMap.put("15", "优惠券");
        rewardTypeMap.put("16", "活动机会");
        rewardTypeMap.put("17", "组合奖");
        rewardTypeMap.put("18", "流量");
        rewardTypeMap.put("19", "只中奖不兑奖");
    }
    
    @Override
    public String[] getTargetFields() {
        return new String[] { "rt" };
    }

    @Override
    public void convert(int[] targetFieldsIndex, String[] row) {
        Preconditions.checkArgument(targetFieldsIndex.length == 1 && targetFieldsIndex[0] > -1
                && targetFieldsIndex[0] < row.length);
        
        int index = targetFieldsIndex[0];
        String rewardType = row[index];
        
        if (!Strings.isNullOrEmpty(rewardType) && rewardTypeMap.containsKey(rewardType)) {
            row[index] = rewardTypeMap.get(rewardType);
        }
    }

}
