/*
 * Copyright (c) 2008 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the license.
 *
 * $Id: ODEInvokerWindow.java,v 1.3 2009/02/02 15:22:36 cherath Exp $
 */
package org.apache.airavata.xbaya.ode;

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

import java.awt.event.ActionEvent;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.xml.namespace.QName;

import org.apache.airavata.xbaya.XBayaConfiguration;
import org.apache.airavata.xbaya.XBayaConstants;
import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.component.ComponentException;
import org.apache.airavata.xbaya.component.ws.WSComponentPort;
import org.apache.airavata.xbaya.gpel.script.BPELScript;
import org.apache.airavata.xbaya.gpel.script.BPELScriptType;
import org.apache.airavata.xbaya.graph.GraphException;
import org.apache.airavata.xbaya.graph.gui.GraphCanvas;
import org.apache.airavata.xbaya.gui.ErrorMessages;
import org.apache.airavata.xbaya.gui.GridPanel;
import org.apache.airavata.xbaya.gui.XBayaDialog;
import org.apache.airavata.xbaya.gui.XBayaLabel;
import org.apache.airavata.xbaya.gui.XBayaTextArea;
import org.apache.airavata.xbaya.gui.XBayaTextComponent;
import org.apache.airavata.xbaya.gui.XBayaTextField;
import org.apache.airavata.xbaya.lead.LEADTypes;
import org.apache.airavata.xbaya.monitor.MonitorConfiguration;
import org.apache.airavata.xbaya.myproxy.MyProxyClient;
import org.apache.airavata.xbaya.myproxy.gui.MyProxyChecker;
import org.apache.airavata.xbaya.security.UserX509Credential;
import org.apache.airavata.xbaya.security.XBayaSecurity;
import org.apache.airavata.xbaya.util.StringUtil;
import org.apache.airavata.xbaya.util.WSDLUtil;
import org.apache.airavata.xbaya.util.XMLUtil;
import org.apache.airavata.xbaya.wf.Workflow;
import org.apache.airavata.xbaya.workflow.WorkflowClient;
import org.apache.airavata.xbaya.workflow.WorkflowEngineException;
import org.ietf.jgss.GSSCredential;
import org.xmlpull.infoset.XmlElement;

import xsul.lead.LeadResourceMapping;
import xsul.wsif.WSIFMessage;
import xsul.wsif.WSIFOperation;
import xsul.wsif.WSIFPort;
import xsul.wsif.WSIFService;
import xsul.wsif.WSIFServiceFactory;
import xsul.wsif.spi.WSIFProviderManager;
import xsul.xwsif_runtime.WSIFClient;
import xsul.xwsif_runtime.WSIFRuntime;
import xsul5.wsdl.WsdlDefinitions;
import xsul5.wsdl.WsdlException;
import xsul5.wsdl.WsdlResolver;

/**
 * @author Chathura Herath
 */
public class ODEInvokerWindow {

    private XBayaEngine engine;
    private ODEInvoker invoker;
    private MyProxyChecker myProxyChecker;
    private GridPanel parameterPanel;
    private XBayaTextField topicTextField;
    private XBayaTextField xRegistryTextField;
    private XBayaTextField gfacTextField;
    // private JButton deployNewAndInvokeButton;
    private JButton invokeButton;
    private XBayaDialog dialog;
    private Workflow workflow;
    protected List<XBayaTextComponent> parameterTextFields = new ArrayList<XBayaTextComponent>();
    private XBayaTextField resourceMappingField;
    private XBayaTextField gatekeeperField;
    private XBayaTextField jobManagerField;

    static {
        WSIFProviderManager.getInstance().addProvider(new xsul.wsif_xsul_soap_http.Provider());
    }

    /**
     * Constructs a ODEInvokerWindow.
     * 
     */
    public ODEInvokerWindow(XBayaEngine engine) {
        this.engine = engine;
        this.invoker = new ODEInvoker(engine);
        this.myProxyChecker = new MyProxyChecker(this.engine);
        initGUI();

    }

    public ODEInvokerWindow() {

    }

    protected void initGUI() {
        this.parameterPanel = new GridPanel(true);

        this.topicTextField = new XBayaTextField();
        XBayaLabel topicLabel = new XBayaLabel("Notification Topic", this.topicTextField);

        this.xRegistryTextField = new XBayaTextField();
        XBayaLabel xRegistryLabel = new XBayaLabel("XRegistry URL", this.xRegistryTextField);

        this.gfacTextField = new XBayaTextField();
        XBayaLabel gfacLabel = new XBayaLabel("GFac URL", this.gfacTextField);

        this.resourceMappingField = new XBayaTextField();
        XBayaLabel resourceMappingLabel = new XBayaLabel("Resource Mapping (optional)", this.resourceMappingField);

        this.gatekeeperField = new XBayaTextField();
        XBayaLabel gatekeeperLabel = new XBayaLabel("Gatekeeper (optional)", this.gatekeeperField);

        this.jobManagerField = new XBayaTextField();
        XBayaLabel jobMagerLabel = new XBayaLabel("Job Manager (optional)", this.jobManagerField);

        GridPanel infoPanel = new GridPanel();
        infoPanel.add(topicLabel);
        infoPanel.add(this.topicTextField);
        infoPanel.add(xRegistryLabel);
        infoPanel.add(this.xRegistryTextField);
        infoPanel.add(gfacLabel);
        infoPanel.add(this.gfacTextField);
        infoPanel.add(resourceMappingLabel);
        infoPanel.add(this.resourceMappingField);
        infoPanel.add(gatekeeperLabel);
        infoPanel.add(this.gatekeeperField);
        infoPanel.add(jobMagerLabel);
        infoPanel.add(this.jobManagerField);
        infoPanel.layout(6, 2, GridPanel.WEIGHT_NONE, 1);

        // leavign the defualts around just
        // LeadResourceMapping resourceMapping = new LeadResourceMapping(
        // "login.bigred.iu.teragrid.org");
        // leadContext.setResourceMapping(resourceMapping);
        //
        // resourceMapping
        // .setGatekeeperEPR(new URI(
        // "http://pagodatree.cs.indiana.edu:54321/axis2/services/SigiriService")
        // );
        // resourceMapping.setJobManager("Sigiri");

        GridPanel mainPanel = new GridPanel();
        mainPanel.add(this.parameterPanel);
        mainPanel.add(infoPanel);
        mainPanel.layout(2, 1, 0, 0);

        // this.deployNewAndInvokeButton = new JButton("Deploy and Invoke");
        // //TODO Is this feature necessary, if yes Enable this.
        // this.deployNewAndInvokeButton.setEnabled(false);
        // this.deployNewAndInvokeButton.addActionListener(new AbstractAction()
        // {
        // public void actionPerformed(ActionEvent e) {
        // execute(true);
        // }
        // });

        this.invokeButton = new JButton("Invoke");
        this.invokeButton.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                execute(false);
            }
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                hide();
            }
        });

        JPanel buttonPanel = new JPanel();
        // buttonPanel.add(this.deployNewAndInvokeButton);
        buttonPanel.add(this.invokeButton);
        buttonPanel.add(cancelButton);

        this.dialog = new XBayaDialog(this.engine, "Execute Workflow (BPEL)", mainPanel, buttonPanel);
    }

    protected void execute(boolean redeploy) {

        // Get various values

        List<WSComponentPort> inputs;
        try {
            inputs = this.workflow.getInputs();
        } catch (ComponentException e) {
            this.engine.getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR, e);
            hide();
            return;
        } catch (RuntimeException e) {
            this.engine.getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR, e);
            hide();
            return;
        } catch (Error e) {
            this.engine.getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR, e);
            hide();
            return;
        }

        int index = 0;
        for (XBayaTextComponent parameterTextField : this.parameterTextFields) {
            String valueString = parameterTextField.getText();
            WSComponentPort port = inputs.get(index++);
            // parse the value. parseValue pops up error if valueString is not
            // valid and return null.
            Object value = parseValue(port, valueString);
            if (value == null) {
                return;
            }
            port.setValue(value);
        }

        // Topic
        String topic = this.topicTextField.getText();
        if (topic.length() == 0) {
            this.engine.getErrorWindow().error(ErrorMessages.TOPIC_EMPTY_ERROR);
            return;
        }
        URI workfowInstanceID = URI.create(StringUtil.convertToJavaIdentifier(topic));
        this.workflow.setGPELInstanceID(workfowInstanceID);

        // XRegistry
        String xRegistry = this.xRegistryTextField.getText();
        URI xRegistryURL;
        if (xRegistry.length() == 0) {
            this.engine.getErrorWindow().error("X-registry url is required");
            return;
        } else {
            try {
                xRegistryURL = new URI(xRegistry).parseServerAuthority();

            } catch (URISyntaxException e) {
                this.engine.getErrorWindow().error(ErrorMessages.XREGISTRY_URL_WRONG, e);
                return;
            }
        }

        // GFac
        String gfac = this.gfacTextField.getText();
        URI gfacURL;
        if (gfac.length() == 0) {
            gfacURL = null;
        } else {
            try {
                gfacURL = new URI(gfac).parseServerAuthority();
            } catch (URISyntaxException e) {
                this.engine.getErrorWindow().error(ErrorMessages.GFAC_URL_WRONG, e);
                return;
            }
        }

        // Set to the config so that they will be reused.
        MonitorConfiguration monitorConfig = this.engine.getMonitor().getConfiguration();
        monitorConfig.setTopic(topic);
        XBayaConfiguration config = this.engine.getConfiguration();
        config.setXRegistryURL(xRegistryURL);
        config.setGFacURL(gfacURL);

        // Deal with the Lead resource mapping

        LeadResourceMapping resourceMapping = null;

        if (!"".equals(this.resourceMappingField.getText())) {
            resourceMapping = new LeadResourceMapping(this.resourceMappingField.getText());
            if (!"".equals(this.gatekeeperField.getText())) {
                try {
                    resourceMapping.setGatekeeperEPR(new URI(this.gatekeeperField.getText()));
                } catch (URISyntaxException e) {
                    hide();
                    this.engine.getErrorWindow().error(e);
                    return;
                }
            }

            if (!"".equals(this.jobManagerField.getText())) {
                resourceMapping.setJobManager(this.jobManagerField.getText());
            }
        }

        // its ok to pass null to resource mapping
        this.invoker.invoke(this.workflow, inputs, redeploy, resourceMapping);

        hide();

        String workflowName = this.engine.getWorkflow().getName();
        // this is the control workflow
        if (-1 != (workflowName.indexOf("Control_"))) {

            String oldWorkflowName = workflowName.substring("Control_".length());
            String oldWorkflowID = null;
            List<GraphCanvas> graphCanvases = this.engine.getGUI().getGraphCanvases();
            for (GraphCanvas graphCanvas : graphCanvases) {
                if (oldWorkflowName.equals(graphCanvas.getWorkflow().getName())) {
                    this.engine.getGUI().setFocus(graphCanvas);
                    Workflow workflow2 = this.engine.getWorkflow();
                    oldWorkflowID = workflow2.getGraph().getID();
                    break;
                }
            }

            try {
                registerQuery(inputs, oldWorkflowID);
            } catch (URISyntaxException e1) {
                this.engine.getErrorWindow().error("Invalid service location", e1);
                hide();
                return;
            }

            try {
                this.engine.getWorkflow().setGPELInstanceID(new URI(this.topicTextField.getText()));
            } catch (URISyntaxException e) {
                this.engine.getErrorWindow().error("Invalid Topic", e);
                hide();
                return;
            }

        }

    }

    public static void main(String[] args) throws WsdlException, URISyntaxException {
        WsdlDefinitions streamEngine = WsdlResolver.getInstance().loadWsdl(
                new URI("http://pagodatree.cs.indiana.edu:9999/axis2/services/StreamEngine?wsdl"));
        WSDLCleaner.cleanWSDL(streamEngine);

        WSIFService service = WSIFServiceFactory.newInstance().getService(
                WSDLUtil.wsdlDefinitions5ToWsdlDefintions3(streamEngine));
        WSIFPort port = service.getPort();
        WSIFRuntime.getDefault().newClientFor(port);

        String operationName = "registerQuery";
        WSIFOperation operation = port.createOperation(operationName);
        WSIFMessage inputMessage = operation.createInputMessage();
        WSIFMessage outputMessage = operation.createOutputMessage();
        WSIFMessage faultMessage = operation.createFaultMessage();

        inputMessage.setObjectPart("endpoint", "http://pagodatree.cs.indiana.edu:17080/ode/processes/velocityhh?wsdl");
        inputMessage.setObjectPart("startTime", "D");
        inputMessage.setObjectPart("endTime", "d");
        inputMessage.setObjectPart("epl", "select * from java.lang.String.win:length_batch(2)");
        inputMessage.setObjectPart("topic", "skkkk");

        boolean res = operation.executeRequestResponseOperation(inputMessage, outputMessage, faultMessage);
        if (res) {
            System.out.println(outputMessage.getObjectPart("return"));
        } else {
            System.out.println(faultMessage.toString());
        }

    }

    /**
     * @param inputs
     * @param workflowname
     * @throws URISyntaxException
     */
    // private void registerQuery(List<WSComponentPort> inputs, String
    // workflowname)
    // throws URISyntaxException {
    // WsdlDefinitions streamEngine = WsdlResolver
    // .getInstance()
    // .loadWsdl(
    // new URI(
    // "http://pagodatree.cs.indiana.edu:9999/axis2/services/StreamEngine?wsdl"
    // ));
    // WSDLCleaner.cleanWSDL(streamEngine);
    //
    // WSIFServiceFactory factory = WSIFServiceFactory.newInstance();
    //
    // WSIFService service = factory.getService(WSDLUtil
    // .wsdlDefinitions5ToWsdlDefintions3(streamEngine));
    // WSIFClient client = WSIFRuntime.getDefault()
    // .newClientFor(service, null);
    // WSIFPort port = client.getPort();
    // String operationName = "registerQuery";
    // WSIFOperation operation = port.createOperation(operationName);
    // WSIFMessage inputMessage = operation.createInputMessage();
    // WSIFMessage outputMessage = operation.createOutputMessage();
    // WSIFMessage faultMessage = operation.createFaultMessage();
    // for (WSComponentPort input : inputs) {
    //
    // // Somethign special for the control workflow
    // if ("epr".equals(input.getName())) {
    // String endpoint =
    // "http://pagodatree.cs.indiana.edu:17080/ode/processes/Control_"
    // + workflowname + "?wsdl";
    // System.out.println(endpoint);
    // inputMessage.setObjectPart("endpoint",
    // endpoint);
    // } else if ("startTime".equals(input.getName())) {
    // inputMessage.setObjectPart("startTime", input.getValue());
    // } else if ("endTime".equals(input.getName())) {
    // inputMessage.setObjectPart("endTime", input.getValue());
    // } else if ("eql".equals(input.getName())) {
    // inputMessage.setObjectPart("epl", input.getValue());
    // }
    // }
    // System.out.println(this.topicTextField.getText());
    // inputMessage.setObjectPart("topic", this.topicTextField.getText());
    // boolean res = operation.executeRequestResponseOperation(inputMessage,
    // outputMessage, faultMessage);
    // if (res) {
    // System.out.println(outputMessage.getObjectPart("return"));
    // }else{
    // System.out.println("Didnt receive the responce from CEPService yet");
    // }
    // }
    private void registerQuery(List<WSComponentPort> inputs, String workflowname) throws URISyntaxException {
        WsdlDefinitions streamEngine = WsdlResolver.getInstance().loadWsdl(
                new URI("http://pagodatree.cs.indiana.edu:9999/axis2/services/StreamEngine?wsdl"));
        WSDLCleaner.cleanWSDL(streamEngine);

        WSIFServiceFactory factory = WSIFServiceFactory.newInstance();

        WSIFService service = factory.getService(WSDLUtil.wsdlDefinitions5ToWsdlDefintions3(streamEngine));
        WSIFClient client = WSIFRuntime.getDefault().newClientFor(service, null);
        WSIFPort port = client.getPort();
        String operationName = "registerQuery";
        WSIFOperation operation = port.createOperation(operationName);
        WSIFMessage inputMessage = operation.createInputMessage();
        WSIFMessage outputMessage = operation.createOutputMessage();
        WSIFMessage faultMessage = operation.createFaultMessage();

        // Somethign special for the control workflow
        String endpoint = "http://pagodatree.cs.indiana.edu:17080/ode/processes/Control_" + workflowname + "?wsdl";
        System.out.println(endpoint);
        String t = "5";
        inputMessage.setObjectPart("endpoint", endpoint);
        inputMessage.setObjectPart("startTime", t);
        inputMessage.setObjectPart("endTime", t);
        inputMessage.setObjectPart("epl", "select * from java.lang.String.win:length_batch(1)");
        // inputMessage.setObjectPart("secs", t);
        inputMessage.setObjectPart("topic", this.topicTextField.getText());
        boolean res = operation.executeRequestResponseOperation(inputMessage, outputMessage, faultMessage);
        if (res) {
            System.out.println(outputMessage.getObjectPart("return"));
        } else {
            System.out.println("Didnt receive the responce from CEPService yet");
        }
    }

    public void hide() {
        this.dialog.hide();
    }

    public void show() {
        // Clean up the previous run. These cannot be in hide() because show()
        // might be called before hide() exits completly.
        this.parameterPanel.getContentPanel().removeAll();
        this.parameterTextFields.clear();

        WorkflowClient workflowClient = this.engine.getWorkflowClient();
        if (workflowClient.isSecure()) {
            // Check if the proxy is loaded.
            boolean loaded = this.myProxyChecker.loadIfNecessary();
            if (!loaded) {
                return;
            }
            // Creates a secure channel in gpel.
            MyProxyClient myProxyClient = this.engine.getMyProxyClient();
            GSSCredential proxy = myProxyClient.getProxy();
            UserX509Credential credential = new UserX509Credential(proxy, XBayaSecurity.getTrustedCertificates());
            try {
                workflowClient.setUserX509Credential(credential);
            } catch (WorkflowEngineException e) {
                this.engine.getErrorWindow().error(ErrorMessages.GPEL_ERROR, e);
                return;
            }
        }

        this.workflow = this.engine.getWorkflow();

        MonitorConfiguration notifConfig = this.engine.getMonitor().getConfiguration();
        if (notifConfig.getBrokerURL() == null) {
            this.engine.getErrorWindow().error(ErrorMessages.BROKER_URL_NOT_SET_ERROR);
            return;
        }

        BPELScript bpel = new BPELScript(this.workflow);

        // Check if there is any errors in the workflow first.
        ArrayList<String> warnings = new ArrayList<String>();
        if (!bpel.validate(warnings)) {
            StringBuilder buf = new StringBuilder();
            for (String warning : warnings) {
                buf.append("- ");
                buf.append(warning);
                buf.append("\n");
            }
            this.engine.getErrorWindow().warning(buf.toString());
            return;
        }

        try {
            // Generate a BPEL process.
            bpel.create(BPELScriptType.BPEL2);
            this.workflow.setGpelProcess(bpel.getGpelProcess());
            this.workflow.setWorkflowWSDL(bpel.getWorkflowWSDL().getWsdlDefinitions());
        } catch (GraphException e) {
            this.engine.getErrorWindow().error(ErrorMessages.GRAPH_NOT_READY_ERROR, e);
            return;
        }

        // Create a GUI without depending on the graph.
        List<WSComponentPort> inputs;
        try {

            inputs = this.workflow.getInputs();
        } catch (ComponentException e) {
            // This should not happen when we create WSDL here, but if we use
            // precompiled workflow, it might happen.
            this.engine.getErrorWindow().error(ErrorMessages.WORKFLOW_WSDL_ERROR, e);
            return;
        }
        List<Double> columnWeights = new ArrayList<Double>();
        long timeNow = System.currentTimeMillis();
        SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss z");
        for (WSComponentPort input : inputs) {

            // Somethign special for the control workflow
            if (-1 != engine.getWorkflow().getName().indexOf("Control_")) {
                if ("epr".equals(input.getName())) {
                    input.setDefaultValue("https://pagodatree.cs.indiana.edu:17443/ode/processes/"
                            + engine.getWorkflow().getName() + "?wsdl");
                } else if ("operation".equals(input.getName())) {
                    input.setDefaultValue("Run");
                } else if ("startTime".equals(input.getName())) {
                    Date now = new Date(timeNow);
                    input.setDefaultValue(format.format(now));
                } else if ("endTime".equals(input.getName())) {
                    input.setDefaultValue(format.format(new Date(timeNow + 2 * 60 * 60 * 1000)));
                } else if ("eql".equals(input.getName())) {
                    input.setDefaultValue("select * from java.lang.String.win:length_batch(1)");
                }
            }

            String id = input.getName();
            QName type = input.getType();
            JLabel paramLabel = new JLabel(id, SwingConstants.TRAILING);
            JLabel typeLabel = new JLabel(type.getLocalPart());
            XBayaTextComponent paramField;
            if (LEADTypes.isKnownType(type)) {
                paramField = new XBayaTextField();
                columnWeights.add(new Double(0));
            } else {
                paramField = new XBayaTextArea();
                columnWeights.add(new Double(1.0));
            }
            paramLabel.setLabelFor(paramField.getSwingComponent());

            // default value
            Object value = input.getDefaultValue();
            String valueString = null;
            if (value != null) {
                if (value instanceof XmlElement) {
                    XmlElement valueElement = (XmlElement) value;
                    valueString = XMLUtil.xmlElementToString(valueElement);
                } else {
                    // Only string comes here for now.
                    valueString = value.toString();
                }
            }

            if (valueString == null) {
                // show some sample URI to ease inputs.
                final String sampleURI = "gsiftp://rainier.extreme.indiana.edu//tmp/foo.txt";
                if (LEADTypes.isURIType(type)) {
                    valueString = sampleURI;
                } else if (LEADTypes.isURIArrayType(type)) {
                    StringBuffer buf = new StringBuffer();
                    for (int i = 0; i < 4; i++) {
                        buf.append(sampleURI).append(" ");
                    }
                    valueString = buf.toString();
                }
            }
            paramField.setText(valueString);

            this.parameterPanel.add(paramLabel);
            this.parameterPanel.add(typeLabel);
            this.parameterPanel.add(paramField);
            this.parameterTextFields.add(paramField);
        }
        List<Double> rowWeights = new ArrayList<Double>();
        rowWeights.add(new Double(0));
        rowWeights.add(new Double(0));
        rowWeights.add(new Double(1));
        this.parameterPanel.layout(columnWeights, rowWeights);

        XBayaConfiguration configuration = this.engine.getConfiguration();
        MonitorConfiguration monitorConfiguration = this.engine.getMonitor().getConfiguration();

        // Topic
        String topic = monitorConfiguration.getTopic();
        if (topic != null) {
            this.topicTextField.setText(topic);
        } else {
            this.topicTextField.setText(UUID.randomUUID().toString());
        }

        // XRegistry
        if (null != configuration.getXRegistryURL()) {
            this.xRegistryTextField.setText(configuration.getXRegistryURL());
        } else {
            this.xRegistryTextField.setText(XBayaConstants.DEFAULT_XREGISTRY_URL);
        }

        // GFac URL
        this.gfacTextField.setText(configuration.getGFacURL());

        this.dialog.show();
    }

    /**
     * @param input
     * @param valueString
     * @return The parsed value
     */
    private Object parseValue(WSComponentPort input, String valueString) {
        String name = input.getName();
        if (false) {
            // Some user wants to pass empty strings, so this check is disabled.
            if (valueString.length() == 0) {
                this.engine.getErrorWindow().error("Input parameter, " + name + ", cannot be empty");
                return null;
            }
        }
        QName type = input.getType();
        Object value;
        if (LEADTypes.isKnownType(type)) {
            // TODO check the type.
            value = valueString;
        } else {
            try {
                value = XMLUtil.stringToXmlElement3(valueString);
            } catch (RuntimeException e) {
                this.engine.getErrorWindow().error("Input parameter, " + name + ", is not valid XML", e);
                return null;
            }
        }
        return value;
    }
}

/*
 * Indiana University Extreme! Lab Software License, Version 1.2
 * 
 * Copyright (c) 2008 The Trustees of Indiana University. All rights reserved.
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
