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

import com.google.gson.Gson;
import org.apache.airavata.api.Airavata;
import org.apache.airavata.testsuite.multitenantedairavata.utils.TestFrameworkConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class FrameworkSetup {
    private static FrameworkSetup instance = new FrameworkSetup();
    private GatewayRegister gatewayRegister;
    private ComputeResourceRegister computeResourceRegister;
    private StorageResourceRegister storageResourceRegister;
    private ApplicationRegister applicationRegister;
    private Airavata.Client airavata;
    private TestFrameworkProps testFrameworkProps;
    private final static Logger logger = LoggerFactory.getLogger(FrameworkSetup.class);

    public static FrameworkSetup getInstance() {
        return instance;
    }

    private FrameworkSetup() {
        try {
            AiravataClient airavataClient = AiravataClient.getInstance();
            this.airavata = airavataClient.getAiravataClient();
            Gson gson = new Gson();
            testFrameworkProps = gson.fromJson(getTestFrameworkJSON(), TestFrameworkProps.class);

            gatewayRegister = new GatewayRegister(airavata, testFrameworkProps);
            applicationRegister = new ApplicationRegister(airavata, testFrameworkProps);
            computeResourceRegister = new ComputeResourceRegister(airavata, testFrameworkProps);
            storageResourceRegister = new StorageResourceRegister(airavata, testFrameworkProps);
        } catch (Exception e) {
            logger.error("Error while creating airavata client instance", e);
        }
    }

    private String getTestFrameworkJSON () throws IOException {
        InputStream inputStream = FrameworkSetup.class.getClassLoader().getResourceAsStream(TestFrameworkConstants.TEST_FREAMEWORK_JSON);
        if (inputStream == null){
            throw new IOException("Input stream is null");
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        String content = "";
        while ((line = reader.readLine()) != null) {
            content += line;
        }
        //System.out.println(content);
        return content;
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

    public ComputeResourceRegister getComputeResourceRegister() {
        return computeResourceRegister;
    }

    public StorageResourceRegister getStorageResourceRegister() {
        return storageResourceRegister;
    }

    public void setComputeResourceRegister(ComputeResourceRegister computeResourceRegister) {
        this.computeResourceRegister = computeResourceRegister;
    }

    public TestFrameworkProps getTestFrameworkProps() {
        return testFrameworkProps;
    }

    public void setTestFrameworkProps(TestFrameworkProps testFrameworkProps) {
        this.testFrameworkProps = testFrameworkProps;
    }
}
