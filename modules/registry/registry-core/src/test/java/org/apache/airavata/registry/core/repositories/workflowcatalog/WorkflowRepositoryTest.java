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
 */
package org.apache.airavata.registry.core.repositories.workflowcatalog;

import org.apache.airavata.model.workflow.*;
import org.apache.airavata.registry.core.repositories.common.TestBase;
import org.apache.airavata.registry.cpi.WorkflowCatalogException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

// TODO: fix derby initialization script so that this test can be re-enabled
@Ignore
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
        super(Database.WORKFLOW_CATALOG);
    }

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        workflowRepository = new WorkflowRepository();
    }

    @Test
    public void SubmitWorkflowTest() throws WorkflowCatalogException {

        workflowRepository.registerWorkflow(getSimpleWorkflow(), EXPERIMENT_ID);

        AiravataWorkflow workflow = workflowRepository.getWorkflow(workflowRepository.getWorkflowId(EXPERIMENT_ID));

        //Assert workflow
        assertEquals(SAMPLE_DESCRIPTION, workflow.getDescription());

        assertEquals(2, workflow.getApplicationsSize());
        assertEquals(2, workflow.getHandlersSize());
        assertEquals(3, workflow.getConnectionsSize());

        //Assert applications
        for (WorkflowApplication app : workflow.getApplications()) {
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

        AiravataWorkflow workflow = new AiravataWorkflow();

        //Adding basic workflow parameters
        workflow.setDescription(SAMPLE_DESCRIPTION);

        //Adding workflow applications
        WorkflowApplication application1 = new WorkflowApplication();
        application1.setId(APPLICATION_PREFIX + 1);
        application1.setApplicationInterfaceId(SAMPLE_APPLICATION_INTERFACE_ID);
        application1.setComputeResourceId(SAMPLE_COMPUTE_RESOURCE_ID);
        application1.setQueueName(SAMPLE_QUEUE_NAME);
        application1.setNodeCount(SAMPLE_NODE_COUNT);
        application1.setCoreCount(SAMPLE_CORE_COUNT);
        application1.setWallTimeLimit(SAMPLE_WALL_TIME_LIMIT);
        application1.setPhysicalMemory(SAMPLE_PHYSICAL_MEMORY);

        WorkflowApplication application2 = new WorkflowApplication();
        application2.setId(APPLICATION_PREFIX + 2);
        application2.setApplicationInterfaceId(SAMPLE_APPLICATION_INTERFACE_ID);
        application2.setComputeResourceId(SAMPLE_COMPUTE_RESOURCE_ID);
        application2.setQueueName(SAMPLE_QUEUE_NAME);
        application2.setNodeCount(SAMPLE_NODE_COUNT);
        application2.setCoreCount(SAMPLE_CORE_COUNT);
        application2.setWallTimeLimit(SAMPLE_WALL_TIME_LIMIT);
        application2.setPhysicalMemory(SAMPLE_PHYSICAL_MEMORY);

        workflow.addToApplications(application1);
        workflow.addToApplications(application2);

        //Adding workflow handlers
        WorkflowHandler handler1 = new WorkflowHandler();
        handler1.setId(HANDLER_PREFIX + 1);
        handler1.setType(HandlerType.FLOW_STARTER);

        WorkflowHandler handler2 = new WorkflowHandler();
        handler2.setId(HANDLER_PREFIX + 2);
        handler2.setType(HandlerType.FLOW_TERMINATOR);

        workflow.addToHandlers(handler1);
        workflow.addToHandlers(handler2);

        //Adding workflow connections
        WorkflowConnection connection1 = new WorkflowConnection();
        connection1.setFromType(ComponentType.HANDLER);
        connection1.setFromId(HANDLER_PREFIX + 1);
        connection1.setFromOutputName(SAMPLE_HANDLER_OUTPUT_NAME);
        connection1.setToType(ComponentType.APPLICATION);
        connection1.setToId(APPLICATION_PREFIX + 1);
        connection1.setToInputName(SAMPLE_APP_INPUT_NAME);

        WorkflowConnection connection2 = new WorkflowConnection();
        connection2.setFromType(ComponentType.APPLICATION);
        connection2.setFromId(APPLICATION_PREFIX + 1);
        connection2.setFromOutputName(SAMPLE_APP_OUTPUT_NAME);
        connection2.setToType(ComponentType.APPLICATION);
        connection2.setToId(APPLICATION_PREFIX + 2);
        connection2.setToInputName(SAMPLE_APP_INPUT_NAME);

        WorkflowConnection connection3 = new WorkflowConnection();
        connection3.setFromType(ComponentType.APPLICATION);
        connection3.setFromId(APPLICATION_PREFIX + 2);
        connection3.setFromOutputName(SAMPLE_APP_OUTPUT_NAME);
        connection3.setToType(ComponentType.HANDLER);
        connection3.setToId(HANDLER_PREFIX + 2);
        connection3.setToInputName(SAMPLE_HANDLER_INPUT_NAME);

        workflow.addToConnections(connection1);
        workflow.addToConnections(connection2);
        workflow.addToConnections(connection3);

        return workflow;
    }
}
