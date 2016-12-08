package com.toucha.analytics.common.exceptions;

import com.toucha.analytics.common.exceptions.UserDefinedExceptionBase;
import com.toucha.analytics.common.util.AppEvents;

public class BadInputException extends UserDefinedExceptionBase {

    private static final long serialVersionUID = -858125452085781550L;


    public BadInputException() {
        super();
    }

    public BadInputException(String message) {
        super(message);
    }

    public  BadInputException(AppEvents appEvents){
        super(appEvents);
    }

    public BadInputException(Throwable cause) {
        super(cause);
    }

    public BadInputException(String message, Throwable cause) {
        super(message, cause);
    }

}
