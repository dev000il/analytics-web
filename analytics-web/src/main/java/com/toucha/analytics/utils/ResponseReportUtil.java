package com.toucha.analytics.utils;

import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.toucha.analytics.common.model.ResponseReportStats;
import com.toucha.analytics.common.model.TimeBasedReportStatUnit;
import com.toucha.analytics.common.model.TimeBasedReportStatsBase;

public class ResponseReportUtil {

    public static ResponseReportStats<Integer> mergeAndFriendlyShow(List<TimeBasedReportStatUnit<Integer>> stats,
            Map<String, String> nameMapping) {
        ResponseReportStats<Integer> respReport = new ResponseReportStats<>();

        TimeBasedReportStatsBase<Integer> totalStats = new TimeBasedReportStatsBase<>();
        totalStats.setDesc("总计");
        List<Date> dateDims = totalStats.getTimeseries();
        List<Integer> measures = totalStats.getMeasures();
        int totalCount = 0;

        Map<Long, Integer> totalReportMap = new TreeMap<>(new MapLongKeyComparator());

        // 1. merge same desc fields,use Hash Map cache all statistics
        Map<String, TimeBasedReportStatsBase<Integer>> detailReprtMap = new HashMap<>();
        for (TimeBasedReportStatUnit<Integer> report : stats) {
            TimeBasedReportStatsBase<Integer> tmp = detailReprtMap.get(report.getDesc());
            if (tmp == null) {
                tmp = new TimeBasedReportStatsBase<>();
                tmp.setDesc(report.getDesc());
                tmp.setTotalMeasure(0);
            }

            tmp.getTimeseries().add(report.getHour());
            tmp.getMeasures().add(report.getMeasure());
            tmp.setTotalMeasure(report.getMeasure() + tmp.getTotalMeasure());

            long timestamp = report.getHour().getTime();
            int count = totalReportMap.get(timestamp) == null ? report.getMeasure() : totalReportMap.get(timestamp).intValue() + report.getMeasure();
            totalReportMap.put(timestamp, count);
            totalCount += (report.getMeasure() == null ? 0 : report.getMeasure().intValue());

            detailReprtMap.put(tmp.getDesc(), tmp);
        }

        // 2. append the hourly stats into response
        for (TimeBasedReportStatsBase<Integer> stat : detailReprtMap.values()) {
            String name = stat.getDesc();
            if (nameMapping != null && !nameMapping.isEmpty()) {
               name = nameMapping.get(name) == null ? name : nameMapping.get(name);
            }
            stat.setDesc(name);
            respReport.add(stat);
        }

        // 3. append the total stats into response
        for (long timestamp : totalReportMap.keySet()) {
            dateDims.add(new Date(timestamp));
            measures.add(totalReportMap.get(timestamp));
        }

        totalStats.setTotalMeasure(totalCount);
        respReport.add(totalStats);

        return respReport;
    }
}

class MapLongKeyComparator implements Comparator<Long> {

    @Override
    public int compare(Long key1, Long key2) {

        return key1.compareTo(key2);
    }
}
