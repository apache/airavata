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

import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.apache.airavata.common.utils.XMLUtil;
import org.apache.airavata.xbaya.component.Component;
import org.apache.airavata.xbaya.component.ComponentException;
import org.apache.airavata.xbaya.component.registry.ComponentRegistryException;
import org.apache.airavata.xbaya.component.registry.LocalComponentRegistry;
import org.apache.airavata.xbaya.component.system.InputComponent;
import org.apache.airavata.xbaya.component.system.OutputComponent;
import org.apache.airavata.xbaya.component.ws.WSComponentPort;
import org.apache.airavata.xbaya.graph.GraphException;
import org.apache.airavata.xbaya.graph.Node;
import org.apache.airavata.xbaya.graph.system.InputNode;
import org.apache.airavata.xbaya.graph.system.OutputNode;
import org.apache.airavata.xbaya.graph.ws.WSGraph;
import org.apache.airavata.xbaya.wf.Workflow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlpull.infoset.XmlElement;

public class MetadataTestCase extends TestCase {

    private static final String WSDL_DIRECTORY = "/u/sshirasu/codes/appservices/gfac2/wsdls";

    private static final String WRF_STATIC_PREPROCESSOR_WSDL = "WrfStaticPreprocessor.wsdl";

    private static final String TERRAIN_PREPROCESSOR_WSDL = "TerrainPreprocessor.wsdl";

    private static final String ADAS_INTERPOLATOR_WSDL = "ADASInterpolator.wsdl";

    private static final String LATERAL_BOUNDARY_INTERPOLATOR_WSDL = "LateralBoundaryInterpolator.wsdl";

    private static final String ARPS2WRF_INTERPOLATOR_WSDL = "ARPS2WRFInterpolator.wsdl";

    private static final String WRF_FORECASTING_MODEL_WSDL = "WRFForecastingModel.wsdl";

    private static final Logger logger = LoggerFactory.getLogger(MetadataTestCase.class);

    /**
     * @throws ComponentException
     * @throws GraphException
     * @throws IOException
     * @throws ComponentRegistryException
     */
    public void testWorkflow() throws ComponentException, GraphException, IOException, ComponentRegistryException {
        Workflow workflow = createWorkflow();

        File workflowFile = new File("tmp/ADASInitializedWRFForecast.xwf");
        XMLUtil.saveXML(workflow.toXML(), workflowFile);
    }

    /**
     * @throws ComponentException
     * @throws GraphException
     * @throws IOException
     * @throws ComponentRegistryException
     */
    public void testPreInvoke() throws ComponentException, GraphException, IOException, ComponentRegistryException {
        Workflow workflow = createWorkflow();
        workflow.createScript();

        File workflowWSDLFile = new File("tmp/ADASInitializedWRFForecast-wsdl.xml");
        XMLUtil.saveXML(workflow.getWorkflowWSDL().xml(), workflowWSDLFile);
        File bpelFile = new File("tmp/ADASInitializedWRFForecast-bpel.xml");
        XMLUtil.saveXML(workflow.getGpelProcess().xml(), bpelFile);

        // Get the metadata for input.
        XmlElement inputAppinfo = workflow.getInputMetadata();

        // Get the input information
        List<WSComponentPort> inputs = workflow.getInputs();

        for (WSComponentPort input : inputs) {
            // Show the information of each input.

            // Name
            String name = input.getName();
            logger.info("name: " + name);

            // Type
            QName type = input.getType();
            logger.info("type: " + type);

            // Metadata as XML
            XmlElement appinfo = input.getAppinfo();
            logger.info("appinfo: " + XMLUtil.xmlElementToString(appinfo));

            if (appinfo != null) {
                // Parse the simple case.
                for (XmlElement element : appinfo.requiredElementContent()) {
                    String tag = element.getName();
                    String value = element.requiredText();
                    logger.info(tag + " = " + value);
                }
            }

            // Set a value to each input.
            input.setValue("200");
        }
    }

    private Workflow createWorkflow() throws ComponentException, GraphException, ComponentRegistryException {

        Workflow workflow = new Workflow();

        // Name, description
        workflow.setName("ADASInitializedWRFForecast");
        workflow.setDescription("");

        WSGraph graph = workflow.getGraph();

        LocalComponentRegistry componentRegistry = new LocalComponentRegistry(WSDL_DIRECTORY);

        // WRF_STATIC_PREPROCESSOR_WSDL
        Component wrfPreComp = componentRegistry.getComponent(WRF_STATIC_PREPROCESSOR_WSDL);
        Node wrfPreNode = workflow.addNode(wrfPreComp);
        wrfPreNode.setPosition(new Point(313, 0));

        // TERRAIN_PREPROCESSOR_WSDL
        Component terrainPreComp = componentRegistry.getComponent(TERRAIN_PREPROCESSOR_WSDL);
        Node terrainPreNode = workflow.addNode(terrainPreComp);
        terrainPreNode.setPosition(new Point(59, 289));

        // ADAS_INTERPOLATOR_WSDL
        Component adasIntComp = componentRegistry.getComponent(ADAS_INTERPOLATOR_WSDL);
        Node adasIntNode = workflow.addNode(adasIntComp);
        adasIntNode.setPosition(new Point(373, 235));

        // LATERAL_BOUNDARY_INTERPOLATOR_WSDL
        Component lateralIntComp = componentRegistry.getComponent(LATERAL_BOUNDARY_INTERPOLATOR_WSDL);
        Node lateralIntNode = workflow.addNode(lateralIntComp);
        lateralIntNode.setPosition(new Point(371, 369));

        // ARPS2WRF_INTERPOLATOR_WSDL
        Component arp2wrfComp = componentRegistry.getComponent(ARPS2WRF_INTERPOLATOR_WSDL);
        Node arp2wrfNode = workflow.addNode(arp2wrfComp);
        arp2wrfNode.setPosition(new Point(607, 104));

        // WRF_FORECASTING_MODEL_WSDL
        Component wrfComp = componentRegistry.getComponent(WRF_FORECASTING_MODEL_WSDL);
        Node wrfNode = workflow.addNode(wrfComp);
        wrfNode.setPosition(new Point(781, 14));

        // Parameters
        Component inputComponent = new InputComponent();
        Component outputComponent = new OutputComponent();

        // Input parameter node
        InputNode confInput = (InputNode) workflow.addNode(inputComponent);
        confInput.setPosition(new Point(0, 100));

        // Input parameter node
        InputNode adasInput = (InputNode) workflow.addNode(inputComponent);
        adasInput.setPosition(new Point(286, 145));

        // Input parameter node
        InputNode namInput = (InputNode) workflow.addNode(inputComponent);
        namInput.setPosition(new Point(179, 438));

        // Output parameter
        OutputNode outParamNode = (OutputNode) workflow.addNode(outputComponent);
        outParamNode.setPosition(new Point(863, 169));

        // Connect ports
        graph.addEdge(confInput.getOutputPort(0), wrfPreNode.getInputPort(0));
        graph.addEdge(confInput.getOutputPort(0), arp2wrfNode.getInputPort(0));
        graph.addEdge(confInput.getOutputPort(0), adasIntNode.getInputPort(1));
        graph.addEdge(confInput.getOutputPort(0), lateralIntNode.getInputPort(1));
        graph.addEdge(confInput.getOutputPort(0), terrainPreNode.getInputPort(0));
        graph.addEdge(terrainPreNode.getOutputPort(0), adasIntNode.getInputPort(2));
        graph.addEdge(terrainPreNode.getOutputPort(0), lateralIntNode.getInputPort(0));
        graph.addEdge(adasInput.getOutputPort(0), adasIntNode.getInputPort(0));
        graph.addEdge(namInput.getOutputPort(0), lateralIntNode.getInputPort(2));
        graph.addEdge(wrfPreNode.getOutputPort(0), arp2wrfNode.getInputPort(1));
        graph.addEdge(adasIntNode.getOutputPort(0), arp2wrfNode.getInputPort(2));
        graph.addEdge(lateralIntNode.getOutputPort(0), arp2wrfNode.getInputPort(3));
        graph.addEdge(arp2wrfNode.getOutputPort(0), wrfNode.getInputPort(0));
        graph.addEdge(wrfNode.getOutputPort(0), outParamNode.getInputPort(0));

        // Add metadata
        String inputMetadata = "<appinfo "
                + "xmlns:lsm=\"http://www.extreme.indiana.edu/namespaces/2006/lead-service-metadata\">"
                + "<lsm:constraints>" + "<lsm:constraint type=\"temporalSync\">"
                + "<lsm:elementref name=\"ADASDataFiles\" />" + "<lsm:elementref name=\"NAMDataFiles\" />"
                + "</lsm:constraint>" + "</lsm:constraints>" + "</appinfo>";
        graph.setInputMetadata(XMLUtil.stringToXmlElement(inputMetadata));

        return workflow;

    }
}