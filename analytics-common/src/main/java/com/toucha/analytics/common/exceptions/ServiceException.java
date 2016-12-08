package com.toucha.analytics.common.exceptions;

import com.toucha.analytics.common.util.AppEvents;
import com.toucha.analytics.common.exceptions.UserDefinedExceptionBase;

/**
 * 系统中自定义的异常类型，用于区分是底层抛出的异常，还是当前系统根据业务逻辑判断抛出的异常
 * 
 * @author Hoctor
 * 
 */
public class ServiceException extends UserDefinedExceptionBase {

	private static final long serialVersionUID = -858125452085781550L;

	
	public ServiceException() {
		super();
	}

	public ServiceException(String message) {
		super(message);
	}

	public ServiceException(Throwable cause) {
		super(cause);
	}
	
	public  ServiceException(AppEvents appEvents){
	    super(appEvents);
	}

	public ServiceException(String message, Throwable cause) {
		super(message, cause);
	}

	
}
