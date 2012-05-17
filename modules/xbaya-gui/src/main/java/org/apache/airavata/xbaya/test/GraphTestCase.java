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

package org.apache.airavata.xbaya.test;

import junit.framework.TestCase;

import org.apache.airavata.workflow.model.component.ComponentException;
import org.apache.airavata.workflow.model.graph.Graph;
import org.apache.airavata.workflow.model.graph.GraphException;
import org.apache.airavata.workflow.model.graph.Node;
import org.apache.airavata.workflow.model.wf.Workflow;
import org.apache.airavata.xbaya.component.registry.ComponentRegistryException;
import org.apache.airavata.xbaya.test.util.WorkflowCreator;

public class GraphTestCase extends TestCase {

    /**
     * @throws ComponentException
     * @throws GraphException
     * @throws ComponentRegistryException
     */
    public void testRemoveNode() throws ComponentException, GraphException, ComponentRegistryException {
        WorkflowCreator creator = new WorkflowCreator();
        Workflow workflow = creator.createSimpleMathWorkflow();
        Graph graph = workflow.getGraph();

        Node node = graph.getNode("Adder_add");
        assertNotNull(node);
        int originalSize = graph.getPorts().size();
        int portNum = node.getAllPorts().size();
        graph.removeNode(node);

        assertEquals(originalSize - portNum, graph.getPorts().size());
    }
}