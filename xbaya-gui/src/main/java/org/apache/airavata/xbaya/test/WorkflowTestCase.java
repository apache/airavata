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

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.apache.airavata.xbaya.component.ComponentException;
import org.apache.airavata.xbaya.component.registry.ComponentRegistryException;
import org.apache.airavata.xbaya.file.XBayaPathConstants;
import org.apache.airavata.xbaya.graph.Graph;
import org.apache.airavata.xbaya.graph.GraphException;
import org.apache.airavata.xbaya.graph.Node;
import org.apache.airavata.xbaya.graph.system.InputNode;
import org.apache.airavata.xbaya.graph.util.GraphUtil;
import org.apache.airavata.xbaya.graph.ws.WSNode;
import org.apache.airavata.xbaya.test.util.WorkflowCreator;
import org.apache.airavata.xbaya.util.XMLUtil;
import org.apache.airavata.xbaya.wf.Workflow;
import org.xmlpull.infoset.XmlElement;

import xsul5.MLogger;

public class WorkflowTestCase extends XBayaTestCase {

    private static MLogger logger = MLogger.getLogger();

    private WorkflowCreator workflowCreator;

    /*
     * @see TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        this.workflowCreator = new WorkflowCreator();
    }

    /**
     * @throws IOException
     * @throws GraphException
     * @throws ComponentException
     */
    public void testParse() throws IOException, GraphException, ComponentException {

        File file = new File(XBayaPathConstants.WORKFLOW_DIRECTORY, "complex-math.xwf");
        XmlElement workflowXML = XMLUtil.loadXML(file);

        // Parse the workflow
        Workflow workflow = new Workflow(workflowXML);

        // Take out the graph of the workflow.
        Graph graph = workflow.getGraph();

        // Extract inputs of the workflow.
        Collection<InputNode> inputNodes = GraphUtil.getInputNodes(graph);

        for (InputNode inputNode : inputNodes) {

            // Name of the input.
            String inputName = inputNode.getName();
            logger.info("inputName: " + inputName);

            // Get next nodes.
            Collection<Node> nextNodes = GraphUtil.getNextNodes(inputNode);

            for (Node nextNode : nextNodes) {

                String name = nextNode.getName();
                logger.info("name: " + name);
            }
        }
    }

    /**
     * @throws ComponentException
     * @throws GraphException
     * @throws IOException
     * @throws ComponentRegistryException
     */
    public void testSaveAndLoad() throws ComponentException, GraphException, IOException, ComponentRegistryException {

        Workflow workflow = this.workflowCreator.createComplexMathWorkflow();

        XmlElement workflowElement = workflow.toXML();
        File file = new File(this.temporalDirectory, "complex-math.xwf");
        XMLUtil.saveXML(workflowElement, file);

        XmlElement loadedWorkflowElement = XMLUtil.loadXML(file);
        Workflow loadedWorkflow = new Workflow(loadedWorkflowElement);

        Graph loadedGraph = loadedWorkflow.getGraph();
        Collection<WSNode> loadedWSNodes = GraphUtil.getWSNodes(loadedGraph);
        assertTrue(loadedWSNodes.size() != 0);

        for (WSNode loadedWSNode : loadedWSNodes) {
            assertNotNull(loadedWSNode.getComponent());
        }
    }
}