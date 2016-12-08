package com.toucha.analytics.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.toucha.analytics.common.dao.DataSet;

public class PromoActivityRequestContext {
    private final static ImmutableList<String> requiredFields = ImmutableList.of("at", "c", "hid", "rid", "rwds");

    private final List<String> injectedFields = Lists.newArrayList();
    private final ImmutableList<String> requestedFields;
    private final ImmutableList<String> queryFields;

    public PromoActivityRequestContext(ImmutableList<String> requestFields) {
        this.requestedFields = requestFields;

        List<String> queryFields = Lists.newArrayList(this.requestedFields);

        // Add the required fields so that the query can succeed
        for (String field : requiredFields) {
            if (!queryFields.contains(field)) {
                queryFields.add(field);
                injectedFields.add(field);
            }
        }

        // Sort the fields because the HBASE query will return the results in
        // sorted column name order
        Collections.sort(queryFields);

        this.queryFields = ImmutableList.copyOf(queryFields);
    }

    public ImmutableList<String> getRequestedFields() {
        return this.requestedFields;
    }

    public ImmutableList<String> getHbaseQueryFields() {
        return this.queryFields;
    }

    public DataSet compressDataset(DataSet dataSet) {
         
        int indexOfActivityType = -1;
        int indexOfhonestId = -1;
        int indexOfRewardsAwarded = -1;
        int indexOfRewardId = -1;
        for (int i = 0; i < dataSet.getHeader().size(); i ++) {
            if ("at".equalsIgnoreCase(dataSet.getHeader().get(i))) {
                indexOfActivityType = i;
            }
            else if ("hid".equalsIgnoreCase(dataSet.getHeader().get(i))) {
                indexOfhonestId = i;
            }
            else if ("rwds".equalsIgnoreCase(dataSet.getHeader().get(i))) {
                indexOfRewardsAwarded = i;
            }
            else if ("rid".equalsIgnoreCase(dataSet.getHeader().get(i))) {
                indexOfRewardId = i;
            }
        }

        Preconditions.checkState(indexOfActivityType > -1 && indexOfhonestId > -1 && indexOfRewardsAwarded > -1 && indexOfRewardId > -1, "Required fields that were injected are not in the HBASE query results.");
        
        // Keep track of the claim reward items for quick lookup.
        // Key: <honestID><rewardID>, Value: List of indices marking 'claimReward' events
        Map<String,Integer> seen = Maps.newHashMap();
        List<Integer> rowsToRemove = Lists.newArrayList();
        
        for (int i = 0; i < dataSet.getData().size(); i++) {
            String[] row = dataSet.getData().get(i);
            
            if (!Strings.isNullOrEmpty(row[indexOfActivityType]) && "3".equals(row[indexOfActivityType])) {
                String key = buildSeenMapKey(row[indexOfhonestId], row[indexOfRewardId]);
                
                if (!seen.containsKey(key))
                    seen.put(key, i);
                else
                	rowsToRemove.add(i);
            }
        }
        
        // 2nd pass: Copy data from 'enterLottery' event to 'claimReward' events
        for (int i = 0; i < dataSet.getData().size(); i++) {
            String[] row = dataSet.getData().get(i);
            
            if (!Strings.isNullOrEmpty(row[indexOfActivityType]) && "2".equals(row[indexOfActivityType]) 
                    && !Strings.isNullOrEmpty(row[indexOfRewardsAwarded])) {
                
                String json = row[indexOfRewardsAwarded];
                List<String> rewardIds = parseRewardIds(json);
                if (rewardIds != null) {
                    boolean foundMatch = false;
                    
                    // Iterate each rewardId given out to build the lookup key.
                    for (String rewardId : rewardIds) {
                        String key = buildSeenMapKey(row[indexOfhonestId], rewardId);
                        if (key != null && seen.containsKey(key)) {
                        
                            int index = seen.get(key);
                            copyAllFields(row, dataSet.getData().get(index));
                            foundMatch = true;
                        }
                    }
                    
                    if (foundMatch)
                        rowsToRemove.add(i);
                }
            }
        }
        
        return dataSet.subtract(rowsToRemove);
    }
    
    private static String buildSeenMapKey(String honestId, String rewardId) {
        if (!Strings.isNullOrEmpty(honestId) && !Strings.isNullOrEmpty(rewardId)) {
            return honestId.trim() + rewardId.trim();
        }
        return null;
    }
    
    private static void copySelectedFields(String[] source, String[] destination, int[] excludeIndices) {
        for (int i = 0; i < source.length; i++) {
            if (!Arrays.asList(excludeIndices).contains(i) && Strings.isNullOrEmpty(destination[i])) {
                destination[i] = source[i];
            }
        }
    }
    
    private static void copyAllFields(String[] source, String[] destination) {
    	copySelectedFields(source, destination, new int[]{});
    }
    
    /**
     * Parse the rewards IDs from the 'rwds' JSON. Which is defined here:
     * http://redmine.toucha.org/projects/zhabei/wiki/Log_Format
     * Key: RewardID, Value: RewardAmount
     * 
     * @param json Entery lottery's 'rwds' field value.
     * @return
     */
    private static List<String> parseRewardIds(String json) {
        JSONObject jsonObj = JSON.parseObject(json);
        if (jsonObj != null && jsonObj.keySet() != null) {
            List<String> result = Lists.newArrayList();
            
            for (Object key : jsonObj.keySet()) {
                if (key != null) {
                    String rewardId = key.toString();
                    if (!Strings.isNullOrEmpty(rewardId))
                        result.add(rewardId);
                }
            }
            
            return result;
        }
        return null;
    }

    public DataSet truncateDataset(DataSet dataSet) {
        // The dataset should contain a superset of fields than the requested.
        // If not, no-op.
        if (dataSet.getHeader().size() >= this.requestedFields.size()) {

            // Contains the indices where the field is found in 'dataSet'
            int[] requestFieldsMapping = new int[this.requestedFields.size()];
            for (int i = 0; i < this.requestedFields.size(); i++) {
                String field = this.requestedFields.get(i);

                for (int j = 0; j < dataSet.getHeader().size(); j++) {
                    if (field.equalsIgnoreCase(dataSet.getHeader().get(j))) {
                        requestFieldsMapping[i] = j;
                    }
                }
            }

            List<String[]> logs = Lists.newArrayList();
            for (String[] row : dataSet.getData()) {
                String[] newEntry = new String[requestFieldsMapping.length];
                for (int i = 0; i < newEntry.length; i++) {
                    newEntry[i] = row[requestFieldsMapping[i]];
                }
                logs.add(newEntry);
            }
			
			return new DataSet(this.requestedFields, logs);
        }

        // No-op
        return dataSet;
    }
}
