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

package org.apache.airavata.xbaya.ui.dialogs;

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

import org.apache.airavata.common.utils.StringUtil;
import org.apache.airavata.common.utils.XMLUtil;
import org.apache.airavata.workflow.model.component.ComponentException;
import org.apache.airavata.workflow.model.component.ws.WSComponentPort;
import org.apache.airavata.workflow.model.gpel.script.BPELScript;
import org.apache.airavata.workflow.model.gpel.script.BPELScriptType;
import org.apache.airavata.workflow.model.graph.GraphException;
import org.apache.airavata.workflow.model.wf.Workflow;
import org.apache.airavata.xbaya.XBayaConfiguration;
import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.invoker.ODEInvoker;
import org.apache.airavata.xbaya.lead.LEADTypes;
import org.apache.airavata.xbaya.monitor.MonitorConfiguration;
import org.apache.airavata.xbaya.myproxy.MyProxyClient;
import org.apache.airavata.xbaya.security.UserX509Credential;
import org.apache.airavata.xbaya.security.XBayaSecurity;
import org.apache.airavata.xbaya.ui.utils.ErrorMessages;
import org.apache.airavata.xbaya.ui.utils.MyProxyChecker;
import org.apache.airavata.xbaya.ui.widgets.GridPanel;
import org.apache.airavata.xbaya.ui.widgets.XBayaLabel;
import org.apache.airavata.xbaya.ui.widgets.XBayaTextArea;
import org.apache.airavata.xbaya.ui.widgets.XBayaTextComponent;
import org.apache.airavata.xbaya.ui.widgets.XBayaTextField;
import org.apache.airavata.xbaya.workflow.WorkflowClient;
import org.apache.airavata.xbaya.workflow.WorkflowEngineException;
import org.ietf.jgss.GSSCredential;
import org.xmlpull.infoset.XmlElement;

import xsul.lead.LeadResourceMapping;
import xsul.wsif.spi.WSIFProviderManager;

public class ODEInvokerWindow {

    private static final String REGISTER_QUERY = "registerQuery";
    private XBayaEngine engine;
    private ODEInvoker invoker;
    private MyProxyChecker myProxyChecker;
    private GridPanel parameterPanel;
    private XBayaTextField topicTextField;
    // private XBayaTextField xRegistryTextField;
    private XBayaTextField gfacTextField;
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

        // this.xRegistryTextField = new XBayaTextField();
        // XBayaLabel xRegistryLabel = new XBayaLabel("XRegistry URL", this.xRegistryTextField);

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
        // infoPanel.add(xRegistryLabel);
        // infoPanel.add(this.xRegistryTextField);
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
        // String xRegistry = this.xRegistryTextField.getText();
        // URI xRegistryURL;
        // if (xRegistry.length() == 0) {
        // this.engine.getErrorWindow().error("X-registry url is required");
        // return;
        // } else {
        // try {
        // xRegistryURL = new URI(xRegistry).parseServerAuthority();
        //
        // } catch (URISyntaxException e) {
        // this.engine.getErrorWindow().error(ErrorMessages.XREGISTRY_URL_WRONG, e);
        // return;
        // }
        // }

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
        // config.setXRegistryURL(xRegistryURL);
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
        // if (null != configuration.getXRegistryURL()) {
        // this.xRegistryTextField.setText(configuration.getXRegistryURL());
        // } else {
        // this.xRegistryTextField.setText(XBayaConstants.DEFAULT_XREGISTRY_URL);
        // }

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