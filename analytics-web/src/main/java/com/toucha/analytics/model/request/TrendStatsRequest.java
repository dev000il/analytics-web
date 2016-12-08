package com.toucha.analytics.model.request;

import java.util.List;

import com.alibaba.fastjson.annotation.JSONField;
import com.toucha.analytics.common.model.BaseRequestNormal;
import com.toucha.analytics.common.model.ErrorInfo;
import com.toucha.analytics.common.util.AppEvents;
import org.elasticsearch.common.Strings;

/**
 * The trend statics about scan tags, enter lottery, new user report request.
 * 
 * @author senhui.li
 */
public class TrendStatsRequest extends BaseRequestNormal {

    private static final long serialVersionUID = -3873629582243213488L;

    @JSONField(name = "r")
    private String range;

    @Override
    public void validateRequestCore(List<ErrorInfo> errors) {
        super.validateRequestCore(errors);
        if (Strings.isEmpty(range)) {
            errors.add(AppEvents.ScanReportServiceRequests.ErrorTrendRange.toErrorInfo());
        }
    }

    public String getRange() {
        return range;
    }

    public void setRange(String range) {
        this.range = range;
    }

}
