package com.toucha.analytics.model.request;

import com.toucha.analytics.common.model.BaseRequest;

public class InternalReportsRequest extends BaseRequest {

    private static final long serialVersionUID = -4010702693469569780L;

    private int cid;

    private String cardName;

    public int getCid() {
        return cid;
    }

    public void setCid(int cid) {
        this.cid = cid;
    }

    public String getCardName() {
        return cardName;
    }

    public void setCardName(String cardName) {
        this.cardName = cardName;
    }

}
