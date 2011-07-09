/*
 * Copyright (c) 2008 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the license.
 *
 * $Id: ODEInvoker.java,v 1.7 2009/02/02 07:05:14 cherath Exp $
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

import java.net.URI;
import java.util.List;

import org.apache.airavata.xbaya.XBayaConfiguration;
import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.XBayaException;
import org.apache.airavata.xbaya.component.ComponentException;
import org.apache.airavata.xbaya.component.ws.WSComponentPort;
import org.apache.airavata.xbaya.gui.Cancelable;
import org.apache.airavata.xbaya.gui.ErrorMessages;
import org.apache.airavata.xbaya.gui.WaitDialog;
import org.apache.airavata.xbaya.lead.LEADWorkflowInvoker;
import org.apache.airavata.xbaya.monitor.MonitorConfiguration;
import org.apache.airavata.xbaya.monitor.MonitorException;
import org.apache.airavata.xbaya.myproxy.MyProxyClient;
import org.apache.airavata.xbaya.security.XBayaSecurity;
import org.apache.airavata.xbaya.util.StringUtil;
import org.apache.airavata.xbaya.util.WSDLUtil;
import org.apache.airavata.xbaya.wf.Workflow;

import xsul.XmlConstants;
import xsul.invoker.gsi.GsiInvoker;
import xsul.lead.LeadContextHeader;
import xsul.lead.LeadResourceMapping;
import xsul5.MLogger;
import xsul5.wsdl.WsdlDefinitions;

/**
 * @author Chathura Herath
 */
public class ODEInvoker implements Cancelable {

    private XBayaEngine engine;

    private static final MLogger logger = MLogger.getLogger();

    private Thread invokeThread;

    private boolean canceled;

    private WaitDialog invokingDialog;

    public ODEInvoker(XBayaEngine engine) {
        this.engine = engine;

        this.invokingDialog = new WaitDialog(this, "Invoking the Workflow.", "Invoking the Workflow."
                + "Please wait for a moment.", this.engine);
    }

    /**
     * @see org.apache.airavata.xbaya.gui.Cancelable#cancel()
     */
    public void cancel() {
        this.canceled = true;
        this.invokeThread.interrupt();
    }

    /**
     * 
     * @param workflow
     * @param inputs
     * @param redeploy
     */

    public void invoke(final Workflow workflow, final List<WSComponentPort> inputs, boolean redeploy,
            LeadResourceMapping resourceMapping) {
        this.canceled = false;
        final LeadResourceMapping resourcemap = resourceMapping;
        this.invokeThread = new Thread() {
            @Override
            public void run() {
                runInThread(workflow, inputs, resourcemap);
            }

        };
        this.invokeThread.start();

        // This has to be the last because it blocks when the dialog is modal.
        this.invokingDialog.show();
    }

    public void invoke(final Workflow workflow, final List<WSComponentPort> inputs, boolean redeploy) {
        invoke(workflow, inputs, redeploy, null);
    }

    private void runInThread(final Workflow workflow, final List<WSComponentPort> inputs,
            LeadResourceMapping resourceMapping) {
        MonitorConfiguration monitorConfiguration = this.engine.getMonitor().getConfiguration();
        XBayaConfiguration configuration = this.engine.getConfiguration();

        // Create the invoker
        LEADWorkflowInvoker invoker = null;
        try {

            WsdlDefinitions wsdl = workflow.getOdeInvokableWSDL(configuration.getDSCURL(), configuration.getODEURL());

            LeadContextHeader leadContext = WSDLUtil.buildLeadContextHeader(this.engine, monitorConfiguration,
                    StringUtil.convertToJavaIdentifier(engine.getWorkflow().getName()), resourceMapping);
            // /////////////////////////////////////
            leadContext.setExperimentId(monitorConfiguration.getTopic());

            // ////////////////////////////////////////////////////////////

            URI messageBoxURL = null;
            if (monitorConfiguration.isPullMode()) {
                messageBoxURL = monitorConfiguration.getMessageBoxURL();
            }

            // create an invoker with LEAD Context
            GsiInvoker secureInvoker = null;
            if (this.engine.getWorkflowClient().isSecure()) {
                MyProxyClient myProxyClient = this.engine.getMyProxyClient();
                secureInvoker = new GsiInvoker(myProxyClient.getProxy(), XBayaSecurity.getTrustedCertificates());

                leadContext.setScmsUrl(URI.create("https://tyr12.cs.indiana.edu:60443/SCMS?wsdl"));

            }
            invoker = new LEADWorkflowInvoker(wsdl, leadContext, messageBoxURL, secureInvoker);
        } catch (ComponentException e) {
            if (this.canceled) {
                logger.caught(e);
            } else {
                this.engine.getErrorWindow().error(ErrorMessages.GRAPH_NOT_READY_ERROR, e);
                this.invokingDialog.hide();
            }
            return;
        } catch (RuntimeException e) {
            if (this.canceled) {
                logger.caught(e);
            } else {
                this.engine.getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR, e);
                this.invokingDialog.hide();
            }
            return;
        } catch (Exception e) {
            this.engine.getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR, e);
            this.invokingDialog.hide();
            return;
        }

        invoker.setInputs(inputs);

        // Start the monitor.
        try {
            this.engine.getMonitor().start();
        } catch (MonitorException e) {
            if (this.canceled) {
                logger.caught(e);
            } else {
                this.engine.getErrorWindow().error(ErrorMessages.MONITOR_SUBSCRIPTION_ERROR, e);
                this.invokingDialog.hide();
            }
            return;
        } catch (RuntimeException e) {
            if (this.canceled) {
                logger.caught(e);
            } else {
                this.engine.getErrorWindow().error(ErrorMessages.MONITOR_SUBSCRIPTION_ERROR, e);
                this.invokingDialog.hide();
            }
            return;
        } catch (Error e) {
            this.engine.getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR, e);
            this.invokingDialog.hide();
        }

        final LEADWorkflowInvoker workflowInvoker = invoker;
        new Thread() {
            @Override
            public synchronized void run() {
                try {
                    boolean success = workflowInvoker.invoke();
                    String result = null;
                    if (success) {
                        result = XmlConstants.BUILDER.serializeToString(workflowInvoker.getOutputMessage());
                    } else {
                        result = XmlConstants.BUILDER.serializeToString(workflowInvoker.getFaultMessage());
                    }
                    logger.info("Done with the execution. result: " + result);
                } catch (XBayaException e) {
                    ODEInvoker.this.engine.getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR, e);
                }
            }
        }.start();

        ODEInvoker.this.invokingDialog.hide();
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
