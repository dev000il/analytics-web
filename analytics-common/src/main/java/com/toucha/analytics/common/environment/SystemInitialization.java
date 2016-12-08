package com.toucha.analytics.common.environment;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SystemInitialization {
	public static ExecutorService reportCsvDownloadService = Executors.newFixedThreadPool(20);
	
	public static ExecutorService getReportCsvDownloadService() {
		return reportCsvDownloadService;
	}
}
