/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.airavata.orchestrator.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrchestratorServerThreadPoolExecutor {
	    private final static Logger logger = LoggerFactory.getLogger(OrchestratorServerThreadPoolExecutor.class);
	    public static final String AIRAVATA_SERVER_THREAD_POOL_SIZE = "airavata.server.thread.pool.size";

	    private static ExecutorService threadPool;

	    public static ExecutorService getCachedThreadPool() {
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
