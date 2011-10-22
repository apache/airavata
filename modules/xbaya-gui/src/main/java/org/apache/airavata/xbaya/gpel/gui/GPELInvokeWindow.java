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

package org.apache.airavata.xbaya.gpel.gui;

import java.awt.event.ActionEvent;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.xml.namespace.QName;

import org.apache.airavata.common.utils.XMLUtil;
import org.apache.airavata.xbaya.XBayaConfiguration;
import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.component.ComponentException;
import org.apache.airavata.xbaya.component.ws.WSComponentPort;
import org.apache.airavata.xbaya.gpel.script.BPELScript;
import org.apache.airavata.xbaya.gpel.script.BPELScriptType;
import org.apache.airavata.xbaya.graph.GraphException;
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
import org.apache.airavata.xbaya.wf.Workflow;
import org.apache.airavata.xbaya.workflow.WorkflowClient;
import org.apache.airavata.xbaya.workflow.WorkflowEngineException;
import org.ietf.jgss.GSSCredential;
import org.xmlpull.infoset.XmlElement;

public class GPELInvokeWindow {

    // private static final Logger logger = LoggerFactory.getLogger();

    protected XBayaEngine engine;

    protected Workflow workflow;

    protected GPELInvoker invoker;

    protected MyProxyChecker myProxyChecker;

    protected XBayaDialog dialog;

    protected GridPanel parameterPanel;

    protected XBayaTextField topicTextField;

    protected XBayaTextField resourceCatalogTextField;

    // protected XBayaTextField xRegistryTextField;

    protected XBayaTextField gfacTextField;

    protected XBayaTextField dscTextField;

    protected List<XBayaTextComponent> parameterTextFields = new ArrayList<XBayaTextComponent>();

    protected JButton redeployAndInvokeButton;

    protected JButton deployNewAndInvokeButton;

    protected GPELInvokeWindow() {

    }

    /**
     * Constructs a GPELInvokeWindow.
     * 
     * @param engine
     * 
     */
    public GPELInvokeWindow(XBayaEngine engine) {
        this.engine = engine;
        this.invoker = new GPELInvoker(engine);
        this.myProxyChecker = new MyProxyChecker(this.engine);
        initGUI();
    }

    /**
     * Shows the dialog.
     * 
     * @param compile
     *            If true, compile a workflow to create a workflow WSDL and a BPEL document. If false, use the ones
     *            included in the workflow.
     */
    public void show(boolean compile) {
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

        if (compile) {
            // Check if the workflow is valid before the user types in input
            // values.
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
                bpel.create(BPELScriptType.GPEL);
                this.workflow.setGpelProcess(bpel.getGpelProcess());
                this.workflow.setWorkflowWSDL(bpel.getWorkflowWSDL().getWsdlDefinitions());
            } catch (GraphException e) {
                this.engine.getErrorWindow().error(ErrorMessages.GRAPH_NOT_READY_ERROR, e);
                return;
            }
        } else {
            if (this.workflow.getWorkflowWSDL() == null) {
                this.engine.getErrorWindow().error(ErrorMessages.WORKFLOW_WSDL_NOT_EXIST);
                return;
            }
        }

        // Enable/Disable redeploy button
        boolean enable = this.workflow.getGPELTemplateID() != null;
        this.redeployAndInvokeButton.setEnabled(enable);

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
        for (WSComponentPort input : inputs) {
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
        this.topicTextField.setText(topic);

        // DSC URL
        this.dscTextField.setText(configuration.getDSCURL());

        // XRegistry
        // this.xRegistryTextField.setText(configuration.getXRegistryURL());

        // GFac URL
        this.gfacTextField.setText(configuration.getGFacURL());

        this.dialog.show();
    }

    /**
     * Hides the dialog.
     */
    public void hide() {
        this.dialog.hide();
    }

    protected void initGUI() {
        this.parameterPanel = new GridPanel(true);

        this.topicTextField = new XBayaTextField();
        XBayaLabel topicLabel = new XBayaLabel("Notification Topic", this.topicTextField);

        this.resourceCatalogTextField = new XBayaTextField();
        XBayaLabel resourceCatalogLabel = new XBayaLabel("Resource Catalog URL", this.resourceCatalogTextField);

        // this.xRegistryTextField = new XBayaTextField();
        // XBayaLabel xRegistryLabel = new XBayaLabel("XRegistry URL", this.xRegistryTextField);

        this.gfacTextField = new XBayaTextField();
        XBayaLabel gfacLabel = new XBayaLabel("GFac URL", this.gfacTextField);

        this.dscTextField = new XBayaTextField();
        XBayaLabel dscLabel = new XBayaLabel("DSC URL", this.dscTextField);

        GridPanel infoPanel = new GridPanel();
        infoPanel.add(topicLabel);
        infoPanel.add(this.topicTextField);
        infoPanel.add(dscLabel);
        infoPanel.add(this.dscTextField);
        infoPanel.add(resourceCatalogLabel);
        infoPanel.add(this.resourceCatalogTextField);
        // infoPanel.add(xRegistryLabel);
        // infoPanel.add(this.xRegistryTextField);
        infoPanel.add(gfacLabel);
        infoPanel.add(this.gfacTextField);
        infoPanel.layout(5, 2, GridPanel.WEIGHT_NONE, 1);

        GridPanel mainPanel = new GridPanel();
        mainPanel.add(this.parameterPanel);
        mainPanel.add(infoPanel);
        mainPanel.layout(2, 1, 0, 0);

        this.deployNewAndInvokeButton = new JButton("Deploy New and Invoke");
        this.deployNewAndInvokeButton.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                execute(true);
            }
        });

        this.redeployAndInvokeButton = new JButton("Redeploy and Invoke");
        this.redeployAndInvokeButton.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                execute(true);
            }
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                hide();
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(this.deployNewAndInvokeButton);
        buttonPanel.add(this.redeployAndInvokeButton);
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

        // DSC
        String dsc = this.dscTextField.getText();
        URI dscURL;
        if (dsc.length() == 0) {
            dscURL = null;
        } else {
            try {
                dscURL = new URI(dsc).parseServerAuthority();
            } catch (URISyntaxException e) {
                this.engine.getErrorWindow().error(ErrorMessages.DSC_URL_WRONG, e);
                return;
            }
        }

        // XRegistry
        // String xRegistry = this.xRegistryTextField.getText();
        // URI xRegistryURL;
        // if (xRegistry.length() == 0) {
        // xRegistryURL = null;
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
        config.setDSCURL(dscURL);
        // config.setXRegistryURL(xRegistryURL);
        config.setGFacURL(gfacURL);

        this.invoker.invoke(this.workflow, inputs, redeploy);

        hide();
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