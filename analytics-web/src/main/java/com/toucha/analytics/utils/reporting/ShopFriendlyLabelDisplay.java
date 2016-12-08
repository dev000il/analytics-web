package com.toucha.analytics.utils.reporting;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Lists;
import com.toucha.analytics.common.shop.dao.ShopDataSet;
import com.toucha.analytics.utils.reporting.rule.substitution.FriendlyLabelRule;
import com.toucha.analytics.utils.reporting.rule.substitution.OkIdRule;
import com.toucha.analytics.utils.reporting.rule.substitution.RewardTypeRule;

public class ShopFriendlyLabelDisplay {
    private final static List<FriendlyLabelRule> rules = Lists.newArrayList();

    static {
        // rules.add(new TimestampRule());
        // rules.add(new PointsRule());
        // rules.add(new PhoneNumberRule());
        rules.add(new OkIdRule());
        rules.add(new RewardTypeRule());
        // rules.add(new CharacterRemovalRule());
        // rules.add(new WechatIdRule());
    }

    public static void executeRules(ShopDataSet dataSet) {
        List<String> fields = dataSet.getHeader();
        List<Pair<FriendlyLabelRule, int[]>> activatedRules = Lists.newArrayList();

        // Check if any rule applies
        for (FriendlyLabelRule rule : rules) {
            String[] targets = rule.getTargetFields();
            int[] targetsIndex = new int[targets.length];
            Arrays.fill(targetsIndex, -1);

            // Find the target field's index
            for (int i = 0; i < targets.length; i++) {
                String target = targets[i];

                for (int j = 0; j < fields.size(); j++) {
                    if (target.equalsIgnoreCase(fields.get(j))) {
                        targetsIndex[i] = j;
                    }
                }
            }

            // Activation: Check if one of the targets was found
            for (int i = 0; i < targetsIndex.length; i++) {
                if (targetsIndex[i] > -1) {
                    activatedRules.add(Pair.of(rule, targetsIndex));
                    break;
                }
            }
        }

        if (!activatedRules.isEmpty()) {
            iterateAndApplyRules(activatedRules, dataSet.getData());
        }
    }

    private static void iterateAndApplyRules(List<Pair<FriendlyLabelRule, int[]>> activatedRules, List<String[]> dataSet) {
        for (int i = 0; i < dataSet.size(); i++) {
            String[] row = dataSet.get(i);

            for (Pair<FriendlyLabelRule, int[]> rulePair : activatedRules) {
                rulePair.getLeft().convert(rulePair.getRight(), row);
            }
        }
    }
}
