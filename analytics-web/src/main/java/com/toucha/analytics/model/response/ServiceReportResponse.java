package com.toucha.analytics.model.response;

import java.util.Collection;
import java.util.List;

import com.alibaba.fastjson.annotation.JSONField;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.toucha.analytics.common.model.ErrorInfo;
import com.toucha.analytics.common.util.AppEvents;

public class ServiceReportResponse<T> {

    @JSONField(name = "report")
    private T report;

    @JSONField(name = "ers")
    private final List<ErrorInfo> errors = Lists.newArrayList();

    public T getReport() {
        return this.report;
    }

    public void setReport(T report) {
        this.report = report;
    }

    public List<ErrorInfo> getErrors() {
        return this.errors;
    }

    public void addError(ErrorInfo error) {
        Preconditions.checkNotNull(error);
        this.errors.add(error);
    }

    public void addError(AppEvents event, Object... eventParams) {
        Preconditions.checkNotNull(event);
        this.errors.add(event.toErrorInfo(eventParams));
    }

    public void addError(Collection<ErrorInfo> items) {
        Preconditions.checkNotNull(items);
        errors.addAll(items);
    }
}
