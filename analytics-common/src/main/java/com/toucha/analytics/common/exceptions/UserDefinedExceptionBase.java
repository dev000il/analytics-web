package com.toucha.analytics.common.exceptions;

import com.toucha.analytics.common.util.AppEvents;

public class UserDefinedExceptionBase extends Exception{

    /**
     * 
     */
    private static final long serialVersionUID = -9166631256195342694L;
    
    private String errorCode;
    private String errorMsg;
    
    public UserDefinedExceptionBase() {
        super();
    }

    public UserDefinedExceptionBase(String message) {
        super(message);
    }

    public UserDefinedExceptionBase(Throwable cause) {
        super(cause);
    }
    
    public  UserDefinedExceptionBase(AppEvents appEvents){
        super(appEvents.toString());
        this.errorCode=appEvents.getIdAsErrorCodeStr();
        this.errorMsg=appEvents.getDisplayMessage();
    }
    

    public UserDefinedExceptionBase(String message, Throwable cause) {
        super(message, cause);
    }

    public String getErrorCode() {
        return errorCode;
    }
    
    public String getErrorMsg() {
        return errorMsg;
    }
    

}
