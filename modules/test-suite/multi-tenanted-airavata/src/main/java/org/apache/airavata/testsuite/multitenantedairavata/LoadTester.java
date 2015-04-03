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

package org.apache.airavata.testsuite.multitenantedairavata;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class LoadTester {
    private final static Logger logger = LoggerFactory.getLogger(LoadTester.class);
    public static void main(String[] args) {
        try {
            FrameworkSetup frameworkSetup = FrameworkSetup.getInstance();
            Map<String, String> tokenMap = frameworkSetup.getGatewayRegister().getTokenMap();
            Map<String, String> projectMap = frameworkSetup.getGatewayRegister().getProjectMap();
            Map<String, String> appInterfaceMap = frameworkSetup.getApplicationRegister().getApplicationInterfaceListPerGateway();

            ExperimentExecution experimentExecution = new ExperimentExecution(frameworkSetup.getAiravata(),tokenMap, appInterfaceMap, projectMap);
            experimentExecution.createAmberExperiment();
            experimentExecution.createEchoExperiment();
            experimentExecution.launchExperiments();
            experimentExecution.monitorExperiments();
        } catch (Exception e) {
            logger.error("Error while submitting experiments", e);
        }


    }

}
