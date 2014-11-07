package org.apache.airavata.orchestrator.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.logger.AiravataLogger;
import org.apache.airavata.common.logger.AiravataLoggerFactory;
import org.apache.airavata.common.utils.ServerSettings;

public class OrchestratorServerThreadPoolExecutor {
	    private final static AiravataLogger logger = AiravataLoggerFactory.getLogger(OrchestratorServerThreadPoolExecutor.class);
	    public static final String AIRAVATA_SERVER_THREAD_POOL_SIZE = "airavata.server.thread.pool.size";

	    private static ExecutorService threadPool;

	    public static ExecutorService getThreadPool() {
	        if(threadPool ==null){
	            threadPool = Executors.newCachedThreadPool();
	        }
	        return threadPool;
	    }

	    public static ExecutorService getFixedThreadPool() {
	        if(threadPool ==null){
	            try {
	                threadPool = Executors.newFixedThreadPool(Integer.parseInt(ServerSettings.getSetting(AIRAVATA_SERVER_THREAD_POOL_SIZE)));
	            } catch (ApplicationSettingsException e) {
	                logger.error("Error reading " + AIRAVATA_SERVER_THREAD_POOL_SIZE+ " property");
	            }
	        }
	        return threadPool;
	    }
}
