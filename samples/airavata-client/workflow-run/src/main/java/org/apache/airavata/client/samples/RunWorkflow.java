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
package org.apache.airavata.client.samples;

import org.apache.airavata.client.AiravataAPIFactory;
import org.apache.airavata.client.api.AiravataAPI;
import org.apache.airavata.client.api.AiravataAPIInvocationException;
import org.apache.airavata.registry.api.PasswordCallback;
import org.apache.airavata.registry.api.workflow.ExperimentData;
import org.apache.airavata.rest.client.PasswordCallbackImpl;
import org.apache.airavata.workflow.model.wf.WorkflowInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class RunWorkflow {
    private static final Logger log = LoggerFactory.getLogger(RunWorkflow.class);

    private static int port;
    private static String serverUrl;
    private static String serverContextName;

    private static String registryURL;

    private static String gatewayName = "default";
    private static String userName = "admin";
    private static String password = "admin";

    private static AiravataAPI airavataAPI;

    public static void main(String[] args) throws AiravataAPIInvocationException, IOException, URISyntaxException {

        //creating airavata client object //
        port = Integer.parseInt("8080");
        serverUrl = "localhost";
        serverContextName = "airavata-registry";

        log.info("Configurations - port : " + port);
        log.info("Configurations - serverUrl : " + serverUrl);
        log.info("Configurations - serverContext : " + serverContextName);

        registryURL = "http://" + serverUrl + ":" + port + "/" + serverContextName + "/api";

        log.info("Configurations - Registry URL : " + registryURL);

        PasswordCallback passwordCallback = new PasswordCallbackImpl(getUserName(), getPassword());
        airavataAPI = AiravataAPIFactory.getAPI(new URI(getRegistryURL()),
                getGatewayName(), getUserName(), passwordCallback);

        String workflowName = "Echo";
        List<WorkflowInput> workflowInputs = new ArrayList<WorkflowInput>();
        String name = "echo_input";
        String type = "String";
        String value = "echo_output=ODI Test";
        WorkflowInput workflowInput = new WorkflowInput(name, (type == null ||
                type.isEmpty()) ? "String" : type, null, value, false);
        workflowInputs.add(workflowInput);
        String result
                = airavataAPI.getExecutionManager().runExperiment(workflowName, workflowInputs, "admin", "",
                workflowName);
        System.out.println("Workflow Experiment ID Returned : " + result);
        List<ExperimentData> experimentDataList = airavataAPI.getProvenanceManager().getExperimentDataList(result);
        for (ExperimentData data: experimentDataList){
            System.out.println(data.getExperimentName() + ": " + data.getTopic());
        }
    }

    public static int getPort() {
        return port;
    }

    public static String getServerUrl() {
        return serverUrl;
    }

    public static String getServerContextName() {
        return serverContextName;
    }

    public static String getRegistryURL() {
        return registryURL;
    }

    public static String getGatewayName() {
        return gatewayName;
    }

    public static String getUserName() {
        return userName;
    }

    public static String getPassword() {
        return password;
    }
}
