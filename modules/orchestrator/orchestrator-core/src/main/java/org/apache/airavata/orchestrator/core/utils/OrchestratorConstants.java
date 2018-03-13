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
package org.apache.airavata.orchestrator.core.utils;

/**
 * This class contains all the constants in orchestrator-core
 *
 */
public class OrchestratorConstants {
    public static final String AIRAVATA_PROPERTIES = "airavata-server.properties";
    public static final int hotUpdateInterval=1000;
    public static final String SUBMIT_INTERVAL = "submitter.interval";
    public static final String THREAD_POOL_SIZE = "threadpool.size";
    public static final String START_SUBMITTER = "start.submitter";
    public static final String EMBEDDED_MODE = "embedded.mode";
    public static final String ENABLE_VALIDATION = "enable.validation";
    public static final String JOB_VALIDATOR = "job.validators";

    public static final String EXPERIMENT_ERROR = "EXPERIMENT_ERROR";
    public static final String PROCESS_ERROR = "PROCESS_ERROR";
    public static final String TASK_ERROR = "TASK_ERROR";
}
