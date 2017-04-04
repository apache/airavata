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
package org.apache.airavata.testsuite.multitenantedairavata;

import org.apache.airavata.api.Airavata;
import org.apache.airavata.api.client.AiravataClientFactory;
import org.apache.airavata.model.error.AiravataClientException;
import org.apache.airavata.testsuite.multitenantedairavata.utils.PropertyFileType;
import org.apache.airavata.testsuite.multitenantedairavata.utils.PropertyReader;
import org.apache.airavata.testsuite.multitenantedairavata.utils.TestFrameworkConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AiravataClient {
    private static Airavata.Client airavataClient;
    private PropertyReader propertyReader;
    private final static Logger logger = LoggerFactory.getLogger(AiravataClient.class);

    private static AiravataClient ourInstance = new AiravataClient();

    public static AiravataClient getInstance() {
        return ourInstance;
    }

    private AiravataClient() {
        propertyReader = new PropertyReader();
    }

    public Airavata.Client getAiravataClient() throws Exception{
        try {
            String airavataHost = propertyReader.readProperty(TestFrameworkConstants.AiravataClientConstants.THRIFT_SERVER_HOST, PropertyFileType.AIRAVATA_SERVER);
            int airavataport = Integer.valueOf(propertyReader.readProperty(TestFrameworkConstants.AiravataClientConstants.THRIFT_SERVER_PORT, PropertyFileType.AIRAVATA_SERVER));
            airavataClient = AiravataClientFactory.createAiravataClient(airavataHost, airavataport);
            return airavataClient;
        } catch (AiravataClientException e) {
            logger.error("Error while creating airavata client instance", e);
            throw new Exception("Error while creating airavata client instance", e);
        }
    }
}
