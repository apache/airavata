/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.airavata.orchestration.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.airavata.interfaces.RegistryException;
import org.apache.airavata.model.workflow.proto.*;
import org.apache.airavata.util.TestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class WorkflowRepositoryTest extends TestBase {

    private WorkflowRepository workflowRepository;

    // Workflow related constants
    private String EXPERIMENT_ID = "sample_exp_id";
    private String SAMPLE_DESCRIPTION = "Sample description about the application";

    // Application related constants
    private String APPLICATION_PREFIX = "app_";
    private String SAMPLE_APPLICATION_INTERFACE_ID = "app_interface_1";
    private String SAMPLE_COMPUTE_RESOURCE_ID = "comp_resource_1";
    private String SAMPLE_QUEUE_NAME = "queue_1";
    private int SAMPLE_NODE_COUNT = 4;
    private int SAMPLE_CORE_COUNT = 4;
    private int SAMPLE_WALL_TIME_LIMIT = 4;
    private int SAMPLE_PHYSICAL_MEMORY = 1000;

    private String SAMPLE_APP_INPUT_NAME = "app_input";
    private String SAMPLE_APP_OUTPUT_NAME = "app_output";

    // Handler related constants
    private String HANDLER_PREFIX = "handler_";

    private String SAMPLE_HANDLER_INPUT_NAME = "handler_input";
    private String SAMPLE_HANDLER_OUTPUT_NAME = "handler_output";

    public WorkflowRepositoryTest() {
        super();
    }

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();
        workflowRepository = new WorkflowRepository();
    }

    @Test
    public void SubmitWorkflowTest() throws RegistryException {

        workflowRepository.registerWorkflow(getSimpleWorkflow(), EXPERIMENT_ID);

        AiravataWorkflow workflow = workflowRepository.getWorkflow(workflowRepository.getWorkflowId(EXPERIMENT_ID));

        // Assert workflow
        assertEquals(SAMPLE_DESCRIPTION, workflow.getDescription());

        assertEquals(2, workflow.getApplicationsCount());
        assertEquals(2, workflow.getHandlersCount());
        assertEquals(3, workflow.getConnectionsCount());

        // Assert applications
        for (WorkflowApplication app : workflow.getApplicationsList()) {
            assertEquals(SAMPLE_APPLICATION_INTERFACE_ID, app.getApplicationInterfaceId());
            assertEquals(SAMPLE_COMPUTE_RESOURCE_ID, app.getComputeResourceId());
            assertEquals(SAMPLE_QUEUE_NAME, app.getQueueName());
            assertEquals(SAMPLE_NODE_COUNT, app.getNodeCount());
            assertEquals(SAMPLE_CORE_COUNT, app.getCoreCount());
            assertEquals(SAMPLE_WALL_TIME_LIMIT, app.getWallTimeLimit());
            assertEquals(SAMPLE_PHYSICAL_MEMORY, app.getPhysicalMemory());
        }
    }

    private AiravataWorkflow getSimpleWorkflow() {

        WorkflowApplication application1 = WorkflowApplication.newBuilder()
                .setId(APPLICATION_PREFIX + 1)
                .setApplicationInterfaceId(SAMPLE_APPLICATION_INTERFACE_ID)
                .setComputeResourceId(SAMPLE_COMPUTE_RESOURCE_ID)
                .setQueueName(SAMPLE_QUEUE_NAME)
                .setNodeCount(SAMPLE_NODE_COUNT)
                .setCoreCount(SAMPLE_CORE_COUNT)
                .setWallTimeLimit(SAMPLE_WALL_TIME_LIMIT)
                .setPhysicalMemory(SAMPLE_PHYSICAL_MEMORY)
                .build();

        WorkflowApplication application2 = WorkflowApplication.newBuilder()
                .setId(APPLICATION_PREFIX + 2)
                .setApplicationInterfaceId(SAMPLE_APPLICATION_INTERFACE_ID)
                .setComputeResourceId(SAMPLE_COMPUTE_RESOURCE_ID)
                .setQueueName(SAMPLE_QUEUE_NAME)
                .setNodeCount(SAMPLE_NODE_COUNT)
                .setCoreCount(SAMPLE_CORE_COUNT)
                .setWallTimeLimit(SAMPLE_WALL_TIME_LIMIT)
                .setPhysicalMemory(SAMPLE_PHYSICAL_MEMORY)
                .build();

        WorkflowHandler handler1 = WorkflowHandler.newBuilder()
                .setId(HANDLER_PREFIX + 1)
                .setType(HandlerType.FLOW_STARTER)
                .build();

        WorkflowHandler handler2 = WorkflowHandler.newBuilder()
                .setId(HANDLER_PREFIX + 2)
                .setType(HandlerType.FLOW_TERMINATOR)
                .build();

        WorkflowConnection connection1 = WorkflowConnection.newBuilder()
                .setFromType(ComponentType.HANDLER)
                .setFromId(HANDLER_PREFIX + 1)
                .setFromOutputName(SAMPLE_HANDLER_OUTPUT_NAME)
                .setToType(ComponentType.APPLICATION)
                .setToId(APPLICATION_PREFIX + 1)
                .setToInputName(SAMPLE_APP_INPUT_NAME)
                .build();

        WorkflowConnection connection2 = WorkflowConnection.newBuilder()
                .setFromType(ComponentType.APPLICATION)
                .setFromId(APPLICATION_PREFIX + 1)
                .setFromOutputName(SAMPLE_APP_OUTPUT_NAME)
                .setToType(ComponentType.APPLICATION)
                .setToId(APPLICATION_PREFIX + 2)
                .setToInputName(SAMPLE_APP_INPUT_NAME)
                .build();

        WorkflowConnection connection3 = WorkflowConnection.newBuilder()
                .setFromType(ComponentType.APPLICATION)
                .setFromId(APPLICATION_PREFIX + 2)
                .setFromOutputName(SAMPLE_APP_OUTPUT_NAME)
                .setToType(ComponentType.HANDLER)
                .setToId(HANDLER_PREFIX + 2)
                .setToInputName(SAMPLE_HANDLER_INPUT_NAME)
                .build();

        AiravataWorkflow workflow = AiravataWorkflow.newBuilder()
                .setDescription(SAMPLE_DESCRIPTION)
                .addApplications(application1)
                .addApplications(application2)
                .addHandlers(handler1)
                .addHandlers(handler2)
                .addConnections(connection1)
                .addConnections(connection2)
                .addConnections(connection3)
                .build();

        return workflow;
    }
}
