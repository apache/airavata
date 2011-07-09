/*
 * Copyright (c) 2008 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the license.
 *
 * $Id: GPELInvokeSetupForTesting.java,v 1.1 2008/07/09 18:51:38 cherath Exp $
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

import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.xml.namespace.QName;

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
import org.apache.airavata.xbaya.util.XMLUtil;
import org.apache.airavata.xbaya.workflow.WorkflowClient;
import org.apache.airavata.xbaya.workflow.WorkflowEngineException;
import org.ietf.jgss.GSSCredential;
import org.xmlpull.infoset.XmlElement;

/**
 * @author Satoshi Shirasuna
 */
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
        this.xRegistryTextField.setText(configuration.getXRegistryURL());

        // GFac URL
        this.gfacTextField.setText(configuration.getGFacURL());

    }

    public void execute(boolean redeploy) {
        super.execute(redeploy);
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
