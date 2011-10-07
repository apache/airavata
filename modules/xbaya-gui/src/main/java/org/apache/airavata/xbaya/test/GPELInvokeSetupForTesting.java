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

import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.xml.namespace.QName;

import org.apache.airavata.common.utils.XMLUtil;
import org.apache.airavata.xbaya.XBayaConfiguration;
import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.component.ComponentException;
import org.apache.airavata.xbaya.component.ws.WSComponentPort;
import org.apache.airavata.xbaya.gpel.gui.GPELInvokeWindow;
import org.apache.airavata.xbaya.gpel.gui.GPELInvoker;
import org.apache.airavata.xbaya.gpel.script.BPELScript;
import org.apache.airavata.xbaya.gpel.script.BPELScriptType;
import org.apache.airavata.xbaya.graph.GraphException;
import org.apache.airavata.xbaya.gui.ErrorMessages;
import org.apache.airavata.xbaya.gui.XBayaTextArea;
import org.apache.airavata.xbaya.gui.XBayaTextComponent;
import org.apache.airavata.xbaya.gui.XBayaTextField;
import org.apache.airavata.xbaya.lead.LEADTypes;
import org.apache.airavata.xbaya.monitor.MonitorConfiguration;
import org.apache.airavata.xbaya.myproxy.MyProxyClient;
import org.apache.airavata.xbaya.myproxy.gui.MyProxyChecker;
import org.apache.airavata.xbaya.security.UserX509Credential;
import org.apache.airavata.xbaya.security.XBayaSecurity;
import org.apache.airavata.xbaya.workflow.WorkflowClient;
import org.apache.airavata.xbaya.workflow.WorkflowEngineException;
import org.ietf.jgss.GSSCredential;
import org.xmlpull.infoset.XmlElement;

public class GPELInvokeSetupForTesting extends GPELInvokeWindow {

    public GPELInvokeSetupForTesting(XBayaEngine engine, String topic) {
        this.engine = engine;
        this.invoker = new GPELInvoker(engine);
        this.myProxyChecker = new MyProxyChecker(this.engine);
        init(topic);
    }

    private void init(String topicStr) {
        initGUI();
        this.topicTextField.setText(topicStr);

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
        boolean compile = true;

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
        this.engine.getMonitor().getConfiguration();

        // DSC URL
        this.dscTextField.setText(configuration.getDSCURL());

        // XRegistry
//        this.xRegistryTextField.setText(configuration.getXRegistryURL());

        // GFac URL
        this.gfacTextField.setText(configuration.getGFacURL());

    }

    public void execute(boolean redeploy) {
        super.execute(redeploy);
    }

}