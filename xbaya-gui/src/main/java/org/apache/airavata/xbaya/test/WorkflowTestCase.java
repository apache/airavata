/*
 * Copyright (c) 2005-2007 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the license.
 *
 * $Id: WorkflowTestCase.java,v 1.13 2008/04/01 21:44:24 echintha Exp $
 */
package org.apache.airavata.xbaya.test;

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

/**
 * @author Satoshi Shirasuna
 */
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

/*
 * Indiana University Extreme! Lab Software License, Version 1.2
 * 
 * Copyright (c) 2005-2007 The Trustees of Indiana University. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 * 
 * 1) All redistributions of source code must retain the above copyright notice, the list of authors in the original
 * source code, this list of conditions and the disclaimer listed in this license;
 * 
 * 2) All redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * disclaimer listed in this license in the documentation and/or other materials provided with the distribution;
 * 
 * 3) Any documentation included with all redistributions must include the following acknowledgement:
 * 
 * "This product includes software developed by the Indiana University Extreme! Lab. For further information please
 * visit http://www.extreme.indiana.edu/"
 * 
 * Alternatively, this acknowledgment may appear in the software itself, and wherever such third-party acknowledgments
 * normally appear.
 * 
 * 4) The name "Indiana University" or "Indiana University Extreme! Lab" shall not be used to endorse or promote
 * products derived from this software without prior written permission from Indiana University. For written permission,
 * please contact http://www.extreme.indiana.edu/.
 * 
 * 5) Products derived from this software may not use "Indiana University" name nor may "Indiana University" appear in
 * their name, without prior written permission of the Indiana University.
 * 
 * Indiana University provides no reassurances that the source code provided does not infringe the patent or any other
 * intellectual property rights of any other entity. Indiana University disclaims any liability to any recipient for
 * claims brought by any other entity based on infringement of intellectual property rights or otherwise.
 * 
 * LICENSEE UNDERSTANDS THAT SOFTWARE IS PROVIDED "AS IS" FOR WHICH NO WARRANTIES AS TO CAPABILITIES OR ACCURACY ARE
 * MADE. INDIANA UNIVERSITY GIVES NO WARRANTIES AND MAKES NO REPRESENTATION THAT SOFTWARE IS FREE OF INFRINGEMENT OF
 * THIRD PARTY PATENT, COPYRIGHT, OR OTHER PROPRIETARY RIGHTS. INDIANA UNIVERSITY MAKES NO WARRANTIES THAT SOFTWARE IS
 * FREE FROM "BUGS", "VIRUSES", "TROJAN HORSES", "TRAP DOORS", "WORMS", OR OTHER HARMFUL CODE. LICENSEE ASSUMES THE
 * ENTIRE RISK AS TO THE PERFORMANCE OF SOFTWARE AND/OR ASSOCIATED MATERIALS, AND TO THE PERFORMANCE AND VALIDITY OF
 * INFORMATION GENERATED USING SOFTWARE.
 */
