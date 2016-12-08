/**
 * 
 */
package com.toucha.platform.common.enums;

public enum SuccessStatus {
    Unknown(0),
    Success(1),
    Failure(2),
    ClaimLater(3);

    private int id;

    private SuccessStatus(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }
}

