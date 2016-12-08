package com.toucha.platform.common.enums;

public enum UserScanType {
    
    OldUserScan(1),
    NewUser(2),
    NewUserScan(3),
	UniqueOldUser(4);

    private int id;

    private UserScanType(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }
}
