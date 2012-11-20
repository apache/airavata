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
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.airavata.common.exception.UtilsException;
import org.apache.airavata.common.utils.WSDLUtil;
import org.apache.airavata.common.utils.XMLUtil;
import org.apache.airavata.workflow.model.component.Component;
import org.apache.airavata.workflow.model.component.ComponentException;
import org.apache.airavata.workflow.model.component.ComponentRegistryException;
import org.apache.airavata.workflow.model.component.local.LocalComponentRegistry;
import org.apache.airavata.workflow.model.component.system.OutputComponent;
import org.apache.airavata.workflow.model.gpel.DSCUtil;
import org.apache.airavata.workflow.model.graph.GraphException;
import org.apache.airavata.workflow.model.graph.Node;
import org.apache.airavata.workflow.model.graph.impl.NodeImpl;
import org.apache.airavata.workflow.model.graph.ws.WSGraph;
import org.apache.airavata.workflow.model.wf.Workflow;
import org.apache.airavata.workflow.tracking.client.Callback;
import org.apache.airavata.workflow.tracking.client.NotificationType;
import org.apache.airavata.workflow.tracking.common.InvocationContext;
import org.apache.airavata.workflow.tracking.common.InvocationEntity;
import org.apache.airavata.workflow.tracking.common.WorkflowTrackingContext;
import org.apache.airavata.workflow.tracking.impl.NotifierImpl;
import org.apache.airavata.workflow.tracking.impl.publish.LoopbackPublisher;
import org.apache.airavata.workflow.tracking.impl.publish.NotificationPublisher;
import org.apache.airavata.ws.monitor.Monitor;
import org.apache.airavata.ws.monitor.MonitorConfiguration;
import org.apache.airavata.ws.monitor.MonitorEventData;
import org.apache.airavata.ws.monitor.MonitorException;
import org.apache.airavata.ws.monitor.event.Event;
import org.apache.airavata.ws.monitor.event.Event.Type;
import org.apache.airavata.xbaya.XBayaConstants;
import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.file.XBayaPathConstants;
import org.apache.airavata.xbaya.graph.controller.NodeController;
import org.apache.airavata.xbaya.modifier.WorkflowModifier;
import org.apache.airavata.xbaya.test.service.adder.Adder;
import org.apache.airavata.xbaya.test.service.multiplier.Multiplier;
import org.apache.airavata.xbaya.test.util.WorkflowCreator;
import org.apache.airavata.xbaya.ui.graph.GraphCanvas;
import org.apache.airavata.xbaya.ui.monitor.MonitorEventHandler.NodeState;
import org.apache.xmlbeans.XmlObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlpull.infoset.view.XmlValidationException;
import org.xmlpull.v1.builder.XmlElement;

import xsul.soap11_util.Soap11Util;
import xsul.wsif.WSIFMessage;
import xsul.wsif.WSIFOperation;
import xsul.wsif.WSIFPort;
import xsul.wsif.WSIFService;
import xsul.wsif.WSIFServiceFactory;
import xsul.xbeans_util.XBeansUtil;
import xsul.xwsif_runtime.WSIFClient;
import xsul.xwsif_runtime.WSIFRuntime;
import xsul5.wsdl.WsdlDefinitions;
import xsul5.wsdl.WsdlResolver;

public class WorkflowModificationTestCase extends XBayaTestCase {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowModificationTestCase.class);

    private boolean gui = false;

    private XBayaEngine engine;

    private GraphCanvas graphCanvas;

    /**
     * @see org.apache.airavata.xbaya.test.XBayaTestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();

        String guiProperty = System.getProperty("xbaya.gui.test");
        this.gui = Boolean.parseBoolean(guiProperty);
    }

    /**
     * @throws IOException
     * @throws ComponentRegistryException
     * @throws GraphException
     * @throws ComponentException
     * @throws MonitorException
     */
    public void test() throws IOException, ComponentException, GraphException, ComponentRegistryException,
            MonitorException {

        WorkflowCreator creator = new WorkflowCreator();
        Workflow workflow = creator.createComplexMathWorkflow();
        workflow.createScript();

        Monitor monitor;
        if (this.gui) {
            this.engine = new XBayaEngine(this.configuration);
            this.graphCanvas = this.engine.getGUI().getGraphCanvas();
            this.graphCanvas.setWorkflow(workflow);
            monitor = this.engine.getMonitor();
            this.engine.getGUI().eventReceived(new Event(Type.MONITOR_STARTED));
            repaintAndWait(2);
        } else {
            MonitorConfiguration monitorConfiguration = new MonitorConfiguration(this.configuration.getBrokerURL(),
                    this.configuration.getTopic(), this.configuration.isPullMode(),
                    this.configuration.getMessageBoxURL());
            monitor = new Monitor(monitorConfiguration);
        }

        MonitorEventData eventData = monitor.getEventData();
        MonitorCallback callback = new MonitorCallback(eventData);
        LoopbackPublisher publisher = new LoopbackPublisher(callback, this.configuration.getTopic());
        MonitorNotifier notifier = new MonitorNotifier(publisher);

        executeToMiddle(workflow, notifier);

        modifyWorkflow(workflow);

        File modifiedWorkflowFile = new File(this.temporalDirectory, "complex-math-modified.xwf");
        XMLUtil.saveXML(workflow.toXML(), modifiedWorkflowFile);

        // Create a diff workflow
        WorkflowModifier modifier = new WorkflowModifier(workflow, eventData);
        Workflow diffWorkflow = modifier.createDifference();

        if (this.gui) {
            GraphCanvas canvas = this.engine.getGUI().newGraphCanvas(true);
            canvas.setWorkflow(diffWorkflow);
            repaintAndWait(5);
        }

        File diffWorkflowFile = new File(this.temporalDirectory, "complex-math-diff.xwf");
        XMLUtil.saveXML(diffWorkflow.toXML(), diffWorkflowFile);

    }

    private void executeToMiddle(Workflow workflow, MonitorNotifier notifier) throws XmlValidationException,
            ComponentException {
        WSGraph graph = workflow.getGraph();

        String adder1ID = "Adder_add";
        String adder2ID = "Adder_add_2";

        NodeImpl a = graph.getNode("a");
        NodeImpl b = graph.getNode("b");
        NodeImpl c = graph.getNode("c");
        NodeImpl d = graph.getNode("d");
        NodeImpl adder = graph.getNode(adder1ID);
        NodeImpl adder2 = graph.getNode(adder2ID);

        WsdlDefinitions workflowWSDL = workflow.getWorkflowWSDL();
        DSCUtil.convertToCWSDL(workflowWSDL, URI.create("http://example.com"));
        HashMap<String, String> inputMap = new HashMap<String, String>();
        inputMap.put("a", "2");
        inputMap.put("b", "3");
        inputMap.put("c", "4");
        inputMap.put("d", "5");

        try {
            sendNotification(workflowWSDL, null, WSDLUtil.getFirstOperation(workflowWSDL).getName(), inputMap, null,
                    notifier);
        } catch (UtilsException e) {
            e.printStackTrace();
        }

        WsdlDefinitions adderWSDL = WsdlResolver.getInstance().loadWsdl(
                new File(XBayaPathConstants.WSDL_DIRECTORY + File.separator + Adder.WSDL_PATH).toURI());

        HashMap<String, String> inputMap1 = new HashMap<String, String>();
        inputMap1.put("x", "2");
        inputMap1.put("y", "3");
        HashMap<String, String> outputMap1 = new HashMap<String, String>();
        outputMap1.put("z", "5");
        sendNotification(adderWSDL, adder1ID, "add", inputMap1, outputMap1, notifier);
        // These are needed because without GUI, the nodes' color won't be
        // changed.
        NodeController.getGUI(a).setBodyColor(NodeState.FINISHED.color);
        NodeController.getGUI(b).setBodyColor(NodeState.FINISHED.color);
        NodeController.getGUI(adder).setBodyColor(NodeState.FINISHED.color);
        repaintAndWait(3);

        HashMap<String, String> inputMap2 = new HashMap<String, String>();
        inputMap2.put("x", "4");
        inputMap2.put("y", "5");
        HashMap<String, String> outputMap2 = new HashMap<String, String>();
        outputMap2.put("z", "9");
        sendNotification(adderWSDL, adder2ID, "add", inputMap2, outputMap2, notifier);

        NodeController.getGUI(c).setBodyColor(NodeState.FINISHED.color);
        NodeController.getGUI(d).setBodyColor(NodeState.FINISHED.color);
        NodeController.getGUI(adder2).setBodyColor(NodeState.FINISHED.color);
        repaintAndWait(3);
    }

    private void modifyWorkflow(Workflow workflow) throws ComponentException, ComponentRegistryException,
            GraphException {
        WSGraph graph = workflow.getGraph();

        OutputComponent outputComponent = new OutputComponent();
        LocalComponentRegistry registry = new LocalComponentRegistry(XBayaPathConstants.WSDL_DIRECTORY);
        Component adderComponent = registry.getComponent(Adder.WSDL_PATH);
        Component multiplierComponent = registry.getComponent(Multiplier.WSDL_PATH);

        NodeImpl c = graph.getNode("c");
        NodeImpl d = graph.getNode("d");
        NodeImpl adder = graph.getNode("Adder_add");
        NodeImpl adder2 = graph.getNode("Adder_add_2");
        NodeImpl multiplier = graph.getNode("Multiplier_multiply");

        // Remove Adder_2 and replace with Multiplier_2.
        graph.removeNode(adder2);
        repaintAndWait(1);

        Node multiplier2 = workflow.addNode(multiplierComponent);
        multiplier2.setPosition(new Point(170, 210));
        repaintAndWait(1);

        graph.addEdge(c.getOutputPort(0), multiplier2.getInputPort(0));
        repaintAndWait(1);

        graph.addEdge(d.getOutputPort(0), multiplier2.getInputPort(1));
        repaintAndWait(1);

        graph.addEdge(multiplier2.getOutputPort(0), multiplier.getInputPort(1));
        repaintAndWait(1);

        // Add one more adder and an output.
        Node adder3 = workflow.addNode(adderComponent);
        adder3.setPosition(new Point(320, 300));
        repaintAndWait(1);

        graph.addEdge(adder.getOutputPort(0), adder3.getInputPort(0));
        repaintAndWait(1);

        graph.addEdge(multiplier2.getOutputPort(0), adder3.getInputPort(1));
        repaintAndWait(1);

        Node output2 = workflow.addNode(outputComponent);
        output2.setPosition(new Point(500, 300));
        repaintAndWait(1);

        graph.addEdge(adder3.getOutputPort(0), output2.getInputPort(0));
        repaintAndWait(1);
    }

    private void sendNotification(WsdlDefinitions definitions, String nodeID, String operationName,
            Map<String, String> inputMap, Map<String, String> outputMap, MonitorNotifier notifier) {

        WSIFServiceFactory factory = WSIFServiceFactory.newInstance();
        WSIFService service = factory.getService(WSDLUtil.wsdlDefinitions5ToWsdlDefintions3(definitions));
        WSIFClient client = WSIFRuntime.getDefault().newClientFor(service, null);
        WSIFPort port = client.getPort();
        WSIFOperation operation = port.createOperation(operationName);
        WSIFMessage inputMessage = operation.createInputMessage();

        for (String key : inputMap.keySet()) {
            String value = inputMap.get(key);
            inputMessage.setObjectPart(key, value);
        }

        URI myWorkflowID = null;
        URI myServiceID = URI.create(XBayaConstants.APPLICATION_SHORT_NAME);
        String myNodeID = null;
        Integer myTimestep = null;
        InvocationEntity myEntity = notifier.createEntity(myWorkflowID, myServiceID, myNodeID, myTimestep);

        URI serviceWorkflowID = null;
        URI serviceServiceID = URI.create("ServiceID");
        String serviceNodeID = nodeID;
        Integer serviceTimestep = null;
        InvocationEntity serviceEntity = notifier.createEntity(serviceWorkflowID, serviceServiceID, serviceNodeID,
                serviceTimestep);
        WorkflowTrackingContext workflowContext = notifier.createTrackingContext(new Properties(), this.configuration
                .getBrokerURL().toASCIIString(), myWorkflowID, myServiceID, myNodeID, myTimestep);

        XmlElement inputBody = (XmlElement) ((XmlElement) inputMessage).getParent();
        XmlObject inputBodyObject = XBeansUtil.xmlElementToXmlObject(inputBody);

        InvocationContext context = notifier.invokingService(workflowContext, serviceEntity, null, inputBodyObject);

        if (outputMap != null) {
            WSIFMessage outputMessage = operation.createOutputMessage();
            Soap11Util.getInstance().wrapBodyContent((XmlElement) outputMessage);
            for (String key : outputMap.keySet()) {
                String value = outputMap.get(key);
                outputMessage.setObjectPart(key, value);
            }
            XmlElement outputBody = (XmlElement) ((XmlElement) outputMessage).getParent();
            XmlObject outputBodyObject = XBeansUtil.xmlElementToXmlObject(outputBody);

            notifier.receivedResult(workflowContext, context, null, outputBodyObject);
        }
    }

    private void repaintAndWait(int second) {
        if (this.gui) {
            this.graphCanvas.repaint();
            try {
                Thread.sleep(second * 1000);
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    private class MonitorNotifier extends NotifierImpl {

        /**
         * Constructs a MonitorNotifier.
         * 
         * @param publisher
         */
        public MonitorNotifier(NotificationPublisher publisher) {
            super();
        }
    }

    private class MonitorCallback implements Callback {

        private MonitorEventData eventData;

        /**
         * Constructs a MonitorCallback.
         * 
         * @param eventData
         */
        public MonitorCallback(MonitorEventData eventData) {
            this.eventData = eventData;
        }

        /**
         * 
         * @param topic
         *            the topic to which this message was sent. This can also be retrieved from the messageObj XMlObject
         *            directly after typecasting.
         * @param type
         * @param message
         */
        public void deliverMessage(String topic, NotificationType type, XmlObject message) {

            XmlElement event = XBeansUtil.xmlObjectToXmlElement(message);
            this.eventData.addEvent(XMLUtil.xmlElement3ToXmlElement5(event));
        }

    }
}