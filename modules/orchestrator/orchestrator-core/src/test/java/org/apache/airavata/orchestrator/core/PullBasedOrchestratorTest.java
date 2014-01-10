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
package org.apache.airavata.orchestrator.core;

import junit.framework.Assert;
import org.apache.airavata.commons.gfac.type.ApplicationDescription;
import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.commons.gfac.type.ServiceDescription;
import org.apache.airavata.registry.api.JobRequest;
import org.apache.airavata.schemas.gfac.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class PullBasedOrchestratorTest {
    private static final Logger log = LoggerFactory.getLogger(PullBasedOrchestratorTest.class);

    private Orchestrator orchestrator;

    @BeforeTest
    public void setUp() throws Exception {
//        System.setProperty("myproxy.user", "ogce");
//        System.setProperty("myproxy.password", "");
//        System.setProperty("basedir", "/Users/lahirugunathilake/work/airavata/sandbox/gsissh");
//        System.setProperty("gsi.working.directory","/home/ogce");
        orchestrator = new PullBasedOrchestrator();
        orchestrator.initialize();
    }

    @Test
    public void noUserIDTest() throws Exception {
        ExperimentRequest experimentRequest = new ExperimentRequest();
        //experimentRequest.setUserExperimentID("test-" + UUID.randomUUID().toString());
        experimentRequest.setUserName("orchestrator");

        String systemExpID = orchestrator.createExperiment(experimentRequest);

        JobRequest jobRequest = createJobRequest(systemExpID);

        boolean b = orchestrator.launchExperiment(jobRequest);

        if(b){
            // This means orchestrator successfully accepted the job
            Assert.assertTrue(true);
        }else {
            Assert.assertFalse(true);
        }
    }

    @Test
    public void userIDTest() throws Exception {
        ExperimentRequest experimentRequest = new ExperimentRequest();
        experimentRequest.setUserExperimentID("test-" + UUID.randomUUID().toString());
        experimentRequest.setUserName("orchestrator");

        String systemExpID = orchestrator.createExperiment(experimentRequest);

        JobRequest jobRequest = createJobRequest(systemExpID);

        boolean b = orchestrator.launchExperiment(jobRequest);

        if(b){
            // This means orchestrator successfully accepted the job
            Assert.assertTrue(true);
        }else {
            Assert.assertFalse(true);
        }
    }
    private JobRequest createJobRequest(String systemExpID) {
        JobRequest jobRequest = new JobRequest();

        // creating host description
        HostDescription descriptor = new HostDescription();
        descriptor.getType().setHostName("localhost");
        descriptor.getType().setHostAddress("127.0.0.1");


        ServiceDescription serviceDescription = new ServiceDescription();
        List<InputParameterType> inputParameters = new ArrayList<InputParameterType>();
        List<OutputParameterType> outputParameters = new ArrayList<OutputParameterType>();
        serviceDescription.getType().setName("Echo");
        serviceDescription.getType().setDescription("Echo service");
        // Creating input parameters
        InputParameterType parameter = InputParameterType.Factory.newInstance();
        parameter.setParameterName("echo_input");
        parameter.setParameterDescription("echo input");
        ParameterType parameterType = parameter.addNewParameterType();
        parameterType.setType(DataType.STRING);
        parameterType.setName("String");
        inputParameters.add(parameter);

        // Creating output parameters
        OutputParameterType outputParameter = OutputParameterType.Factory.newInstance();
        outputParameter.setParameterName("echo_output");
        outputParameter.setParameterDescription("Echo output");
        ParameterType outputParaType = outputParameter.addNewParameterType();
        outputParaType.setType(DataType.STRING);
        outputParaType.setName("String");
        outputParameters.add(outputParameter);

        // Setting input and output parameters to serviceDescriptor
        serviceDescription.getType().setInputParametersArray(inputParameters.toArray(new InputParameterType[]{}));
        serviceDescription.getType().setOutputParametersArray(outputParameters.toArray(new OutputParameterType[]{}));


        ApplicationDescription applicationDeploymentDescription = new ApplicationDescription();
        ApplicationDeploymentDescriptionType applicationDeploymentDescriptionType = applicationDeploymentDescription
                .getType();
        applicationDeploymentDescriptionType.addNewApplicationName().setStringValue("EchoApplication");
        applicationDeploymentDescriptionType.setExecutableLocation("/bin/echo");
        applicationDeploymentDescriptionType.setScratchWorkingDirectory("/tmp");

        //creating input Map

        HashMap<String, Object> inputData = new HashMap<String, Object>();
        inputData.put("echo_input", "Hello World");

        HashMap<String, Object> outputData = new HashMap<String, Object>();


        // setting all the parameters to jobRequest
        jobRequest.setExperimentID(systemExpID);
        jobRequest.setHostDescription(descriptor);
        jobRequest.setServiceDescription(serviceDescription);
        jobRequest.setApplicationDescription(applicationDeploymentDescription);
        return jobRequest;
    }


}
