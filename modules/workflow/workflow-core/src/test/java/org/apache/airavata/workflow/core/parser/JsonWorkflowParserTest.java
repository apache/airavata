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
package org.apache.airavata.workflow.core.parser;

import org.apache.airavata.workflow.core.WorkflowInfo;
import org.apache.airavata.workflow.core.dag.nodes.ApplicationNode;
import org.apache.airavata.workflow.core.dag.nodes.InputNode;
import org.apache.airavata.workflow.core.dag.nodes.OutputNode;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.List;

public class JsonWorkflowParserTest {

    private InputStream inputStream;
    @Before
    public void setUp() throws Exception {
        inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("TestWorkflow.json");
        if (inputStream == null) {
            throw new Exception("Couldn't find TestWorkflow File");
        }
    }

    @After
    public void tearDown() throws Exception {
        if (inputStream != null) {
            inputStream.close();
        }
    }

    @Test
    public void testParse() throws Exception {
        JsonWorkflowParser jwp = new JsonWorkflowParser(inputStream);

        WorkflowInfo workflowInfo = jwp.parse();
        Assert.assertNotNull(workflowInfo);
        Assert.assertEquals("name", workflowInfo.getName());
        Assert.assertEquals("default_id", workflowInfo.getId());
        Assert.assertEquals("default description", workflowInfo.getDescription());
        Assert.assertEquals("version", workflowInfo.getVersion());
        testApplications(jwp);
        testWorkflowInputs(jwp);
        testWorkflowOutputs(jwp);

    }

    private void testApplications(JsonWorkflowParser jwp) throws Exception {
        List<ApplicationNode> applicationNodes = jwp.getApplicationNodes();
        Assert.assertNotNull(applicationNodes);
        Assert.assertEquals(1, applicationNodes.size());

        ApplicationNode node = applicationNodes.get(0);
        Assert.assertEquals("App Name", node.getName());
        Assert.assertEquals("appId_1", node.getApplicationId());

    }

    private void testWorkflowInputs(JsonWorkflowParser jwp) throws Exception {
        List<InputNode> inputNodes = jwp.getInputNodes();
        Assert.assertEquals(2, inputNodes.size());

    }

    private void testWorkflowOutputs(JsonWorkflowParser jwp) throws Exception {
        List<OutputNode> outputNodes = jwp.getOutputNodes();
        Assert.assertEquals(2, outputNodes.size());

    }

}