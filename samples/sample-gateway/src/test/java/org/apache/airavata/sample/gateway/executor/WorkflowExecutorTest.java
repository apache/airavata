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

package org.apache.airavata.sample.gateway.executor;

import org.apache.airavata.sample.gateway.ExecutionParameters;
import org.apache.airavata.workflow.model.wf.Workflow;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

/**
 * User: AmilaJ (amilaj@apache.org)
 * Date: 8/27/13
 * Time: 3:34 PM
 */


public class WorkflowExecutorTest {

    //@Test
    public void testRunWorkflowTrestlesOGCE() throws Exception {

        WorkflowExecutor workflowExecutor = getSampleGatewayExecutor();
        ExecutionParameters executionParameters = getTrestlesOGCEParameters();

        Workflow workflow = workflowExecutor.setupExperiment(executionParameters);
        workflowExecutor.runWorkflow(workflow, Arrays.asList("echo_output=Hello World"));

        System.out.println("sadsd");

    }

    //@Test
    public void testRunWorkflowStampedeOGCE() throws Exception {

        WorkflowExecutor workflowExecutor = getSampleGatewayExecutor();
        ExecutionParameters executionParameters = getStampedeOGCEParameters();

        Workflow workflow = workflowExecutor.setupExperiment(executionParameters);
        workflowExecutor.runWorkflow(workflow, Arrays.asList("echo_output=Hello World"));

        System.out.println("sadsd");

    }

    //@Test
    public void testRunWorkflowStampedeUS3() throws Exception {

        WorkflowExecutor workflowExecutor = getSampleGatewayExecutor();
        ExecutionParameters executionParameters = getStampedeUS3Parameters();

        Workflow workflow = workflowExecutor.setupExperiment(executionParameters);
        workflowExecutor.runWorkflow(workflow, Arrays.asList("echo_output=Hello World"));

    }

    //@Test
    public void testRunWorkflowStampedeUS3WithToken() throws Exception {

        WorkflowExecutor workflowExecutor = getSampleGatewayExecutor();
        ExecutionParameters executionParameters = getStampedeUS3Parameters();

        Workflow workflow = workflowExecutor.setupExperiment(executionParameters);
        workflowExecutor.runWorkflow(workflow, Arrays.asList("echo_output=Hello World"),
                "a70b5c63-48d8-4a34-9b9a-d77f74894fb8X", "bunny");

    }

    private WorkflowExecutor getSampleGatewayExecutor() throws IOException {
        return new WorkflowExecutor("default");
    }

    private ExecutionParameters getStampedeUS3Parameters() {

        ExecutionParameters executionParameters = new ExecutionParameters();

        executionParameters.setHostAddress("stampede.tacc.utexas.edu");
        executionParameters.setHostName("stampede");
        executionParameters.setGateKeeperAddress("login5.stampede.tacc.utexas.edu:2119/jobmanager-slurm3");
        executionParameters.setGridftpAddress("gsiftp://data1.stampede.tacc.utexas.edu:2811/");
        executionParameters.setProjectNumber("TG-MCB070039N");
        executionParameters.setQueueName("normal");
        executionParameters.setWorkingDirectory("/scratch/01623/us3");

        return executionParameters;

    }

    private ExecutionParameters getStampedeOGCEParameters() {

        ExecutionParameters executionParameters = new ExecutionParameters();

        executionParameters.setHostAddress("stampede.tacc.utexas.edu");
        executionParameters.setHostName("stampede");
        executionParameters.setGateKeeperAddress("login5.stampede.tacc.utexas.edu:2119/jobmanager-slurm");
        executionParameters.setGridftpAddress("gsiftp://data1.stampede.tacc.utexas.edu:2811/");
        executionParameters.setProjectNumber("TG-STA110014S");
        executionParameters.setQueueName("normal");
        executionParameters.setWorkingDirectory("/scratch/01437/ogce");

        return executionParameters;

    }

    private ExecutionParameters getTrestlesOGCEParameters() {

        ExecutionParameters executionParameters = new ExecutionParameters();
        executionParameters.setHostAddress("trestles.sdsc.edu");
        executionParameters.setHostName("trestles");
        executionParameters.setGateKeeperAddress("trestles-login2.sdsc.edu:2119/jobmanager-pbstest2");
        executionParameters.setGridftpAddress("gsiftp://trestles-dm1.sdsc.edu:2811");
        executionParameters.setProjectNumber("sds128");
        executionParameters.setQueueName("shared");
        executionParameters.setWorkingDirectory("/home/ogce/scratch");

        return executionParameters;

    }
}
