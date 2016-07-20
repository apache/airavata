/*
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
 *
*/
package org.apache.airavata.orchestrator.core.utils;

/**
 * This class contains all the constants in orchestrator-core
 *
 */
/*public class OrchestratorConstants {
    public static final String AIRAVATA_PROPERTIES = "airavata-server.properties";
    public static final int hotUpdateInterval=1000;
    public static final String SUBMIT_INTERVAL = "submitter.interval";
    public static final String THREAD_POOL_SIZE = "threadpool.size";
    public static final String START_SUBMITTER = "start.submitter";
    public static final String EMBEDDED_MODE = "embedded.mode";
    public static final String ENABLE_VALIDATION = "enable.validation";
    public static final String JOB_VALIDATOR = "job.validators";
}*/


/**
 * This enum contains all the constants in orchestrator-core
   enum is the way about dealing with constants as its very powerful.
   Hence, a design change has been made to change the class to enum.
 *
 */
public enum OrchestratorConstants {
    AIRAVATA_PROPERTIES("airavata-server.properties"),
    hotUpdateInterval(1000),
    SUBMIT_INTERVAL("submitter.interval"),
    THREAD_POOL_SIZE("threadpool.size"),
    START_SUBMITTER("start.submitter"),
    EMBEDDED_MODE("embedded.mode"),
    ENABLE_VALIDATION("enable.validation"),
    JOB_VALIDATOR("job.validators");


    private String stringConstant;
    private int integerConstant;

    OrchestratorConstants(String stringConstantIn)
    {
      stringConstant = stringConstantIn;
    }
    OrchestratorConstants(int integerConstantIn)
    {
      integerConstant = integerConstantIn;
    }

    public String getOrchestratorStringConstant()
    {
      return stringConstant;
    }
    public int getOrchestratorIntegerConstant()
    {
      return integerConstant;
    }

}
