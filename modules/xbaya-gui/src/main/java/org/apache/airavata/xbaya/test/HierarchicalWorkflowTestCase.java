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
import java.security.cert.X509Certificate;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.airavata.common.utils.XMLUtil;
import org.apache.airavata.xbaya.XBayaConstants;
import org.apache.airavata.xbaya.XBayaException;
import org.apache.airavata.xbaya.component.Component;
import org.apache.airavata.xbaya.component.ComponentException;
import org.apache.airavata.xbaya.component.registry.ComponentRegistryException;
import org.apache.airavata.xbaya.component.registry.LocalComponentRegistry;
import org.apache.airavata.xbaya.component.system.InputComponent;
import org.apache.airavata.xbaya.component.system.OutputComponent;
import org.apache.airavata.xbaya.component.ws.WSComponentPort;
import org.apache.airavata.xbaya.component.ws.WorkflowComponent;
import org.apache.airavata.xbaya.file.XBayaPathConstants;
import org.apache.airavata.xbaya.gpel.component.GPELRegistry;
import org.apache.airavata.xbaya.graph.Graph;
import org.apache.airavata.xbaya.graph.GraphException;
import org.apache.airavata.xbaya.graph.Node;
import org.apache.airavata.xbaya.graph.system.InputNode;
import org.apache.airavata.xbaya.graph.system.OutputNode;
import org.apache.airavata.xbaya.lead.LEADWorkflowInvoker;
import org.apache.airavata.xbaya.lead.LeadContextHeaderHelper;
import org.apache.airavata.xbaya.myproxy.MyProxyClient;
import org.apache.airavata.xbaya.security.UserX509Credential;
import org.apache.airavata.xbaya.security.XBayaSecurity;
import org.apache.airavata.xbaya.test.service.adder.Adder;
import org.apache.airavata.xbaya.test.util.WorkflowCreator;
import org.apache.airavata.xbaya.wf.Workflow;
import org.apache.airavata.xbaya.workflow.WorkflowClient;
import org.apache.airavata.xbaya.workflow.WorkflowEngineManager;
import org.gpel.client.GcInstance;
import org.ietf.jgss.GSSCredential;

import xsul.lead.LeadContextHeader;
import xsul.wsif.WSIFMessage;
import xsul5.MLogger;
import xsul5.wsdl.WsdlDefinitions;

public class HierarchicalWorkflowTestCase extends XBayaTestCase {

    private static final MLogger logger = MLogger.getLogger();

    private WorkflowClient workflowClient;

    private WorkflowCreator workflowCreator;

    private GPELRegistry gpelComponentRegistry;

    private LocalComponentRegistry componentRegistry;

    /**
     * @see org.apache.airavata.xbaya.test.XBayaTestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();

        this.workflowCreator = new WorkflowCreator();

        // GPEL Setup
        X509Certificate[] trustedCertificates = XBayaSecurity.getTrustedCertificates();
        MyProxyClient client = new MyProxyClient(this.configuration.getMyProxyServer(),
                this.configuration.getMyProxyPort(), this.configuration.getMyProxyUsername(),
                this.configuration.getMyProxyPassphrase(), this.configuration.getMyProxyLifetime());
        client.load();
        GSSCredential proxy = client.getProxy();
        UserX509Credential credential = new UserX509Credential(proxy, trustedCertificates);
        this.workflowClient = WorkflowEngineManager.getWorkflowClient(XBayaConstants.DEFAULT_GPEL_ENGINE_URL,
                credential);

        this.gpelComponentRegistry = new GPELRegistry(null, WorkflowClient.WorkflowType.TEMPLATE, 100);
        this.componentRegistry = new LocalComponentRegistry(XBayaPathConstants.WSDL_DIRECTORY);
    }

    /**
     * @throws IOException
     * @throws XBayaException
     */
    public void test() throws IOException, XBayaException {
        Workflow subWorkflow = this.workflowCreator.createSimpleMathWorkflow();

        URI subWorkflowID = this.workflowClient.createScriptAndDeploy(subWorkflow, false);
        Workflow workflow = createHierarchicalWorkflow(subWorkflowID);
        File file = new File(this.temporalDirectory, "hierarchical-test.xwf");
        XMLUtil.saveXML(workflow.toXML(), file);

        URI workflowTemplateID = this.workflowClient.createScriptAndDeploy(workflow, false);
        logger.info("workflowTemplateID: " + workflowTemplateID);

        // Instantiate the workflow template.
        GcInstance instance = this.workflowClient.instantiate(workflow, this.configuration.getDSCURL());

        // ID to retrieve the workflow instance
        URI instanceID = instance.getInstanceId();
        logger.info("instanceID: " + instanceID);

        // Start the workflow instance.
        WsdlDefinitions wsdl = this.workflowClient.start(instance);

        // Create lead context.
        LeadContextHeaderHelper leadContextHelper = new LeadContextHeaderHelper();
        leadContextHelper.setXBayaConfiguration(this.configuration);
        LeadContextHeader leadContext = leadContextHelper.getLeadContextHeader();

        URI messageBoxURL = null;
        if (this.configuration.isPullMode()) {
            messageBoxURL = this.configuration.getMessageBoxURL();
        }

        // Create a invoke to invoke the workflow instance.
        LEADWorkflowInvoker invoker = new LEADWorkflowInvoker(wsdl, leadContext, messageBoxURL);

        // Set the input values to the invoker.
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

            String defaultValue = input.getDefaultValue();
            logger.info("defaultValue: " + defaultValue);

            // Set a value to each input.
            input.setValue(defaultValue);
        }
        invoker.setInputs(inputs);

        // Invoke the workflow. This will block, so you may want to do it in a
        // thread.
        boolean success = invoker.invoke();
        logger.info("success: " + success);

        // We don't need to wait for the outputs.

        if (success) {
            List<WSComponentPort> outputs = invoker.getOutputs();
            for (WSComponentPort output : outputs) {
                String name = output.getName();
                logger.info("name: " + name);
                Object value = output.getValue();
                logger.info("value: " + value);
            }
        } else {
            WSIFMessage fault = invoker.getFault();
            logger.info("fault: " + fault);
        }

    }

    private Workflow createHierarchicalWorkflow(URI subWorkflowID) throws ComponentRegistryException,
            ComponentException, GraphException {
        Workflow workflow = new Workflow();

        // Name, description
        workflow.setName("Hierarchical workflow");
        workflow.setDescription("Hierarchical workflow");

        Graph graph = workflow.getGraph();

        // Input 1
        InputComponent inputComponent = new InputComponent();
        InputNode input1 = (InputNode) workflow.addNode(inputComponent);
        input1.setPosition(new Point(50, 50));

        // Input 2
        InputNode input2 = (InputNode) workflow.addNode(inputComponent);
        input2.setPosition(new Point(50, 150));

        InputNode input3 = (InputNode) workflow.addNode(inputComponent);
        input3.setPosition(new Point(50, 250));

        // Adder node
        Component adderComp = this.componentRegistry.getComponent(Adder.WSDL_PATH);
        Node adder = workflow.addNode(adderComp);
        adder.setPosition(new Point(200, 100));

        // Sub workflow
        WorkflowComponent subWorkflowComponent = this.gpelComponentRegistry.getComponent(subWorkflowID);
        Node subWorkflow = workflow.addNode(subWorkflowComponent);
        subWorkflow.setPosition(new Point(350, 150));

        // Output
        OutputComponent outputComponent = new OutputComponent();
        OutputNode outParamNode = (OutputNode) workflow.addNode(outputComponent);
        outParamNode.setPosition(new Point(800, 150));

        // Connect ports
        graph.addEdge(input1.getOutputPort(0), adder.getInputPort(0));
        graph.addEdge(input2.getOutputPort(0), adder.getInputPort(1));
        graph.addEdge(adder.getOutputPort(0), subWorkflow.getInputPort(0));
        graph.addEdge(input3.getOutputPort(0), subWorkflow.getInputPort(1));
        graph.addEdge(subWorkflow.getOutputPort(0), outParamNode.getInputPort(0));

        // Set the default values
        // This needs to be after connection.
        input1.setDefaultValue("2");
        input2.setDefaultValue("3");
        input3.setDefaultValue("4");
        return workflow;
    }

}