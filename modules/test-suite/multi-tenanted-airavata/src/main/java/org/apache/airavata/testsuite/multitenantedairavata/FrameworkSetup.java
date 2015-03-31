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

import org.apache.airavata.api.Airavata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FrameworkSetup {
    private static FrameworkSetup instance = new FrameworkSetup();
    private GatewayRegister gatewayRegister;
    private ApplicationRegister applicationRegister;
    private Airavata.Client airavata;
    private final static Logger logger = LoggerFactory.getLogger(FrameworkSetup.class);

    public static FrameworkSetup getInstance() {
        return instance;
    }

    private FrameworkSetup() {
        try {
            AiravataClient airavataClient = AiravataClient.getInstance();
            this.airavata = airavataClient.getAiravataClient();
            gatewayRegister = new GatewayRegister(airavata);
            applicationRegister = new ApplicationRegister(airavata);
        } catch (Exception e) {
            logger.error("Error while creating airavata client instance", e);
        }
    }

    public GatewayRegister getGatewayRegister() {
        return gatewayRegister;
    }

    public void setGatewayRegister(GatewayRegister gatewayRegister) {
        this.gatewayRegister = gatewayRegister;
    }

    public ApplicationRegister getApplicationRegister() {
        return applicationRegister;
    }

    public void setApplicationRegister(ApplicationRegister applicationRegister) {
        this.applicationRegister = applicationRegister;
    }

    public Airavata.Client getAiravata() {
        return airavata;
    }

    public void setAiravata(Airavata.Client airavata) {
        this.airavata = airavata;
    }
}
