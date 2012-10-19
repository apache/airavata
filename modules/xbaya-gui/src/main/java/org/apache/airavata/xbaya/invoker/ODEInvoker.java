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

package org.apache.airavata.xbaya.invoker;

import java.net.URI;
import java.util.List;

import org.apache.airavata.common.utils.StringUtil;
import org.apache.airavata.workflow.model.component.ComponentException;
import org.apache.airavata.workflow.model.component.ws.WSComponentPort;
import org.apache.airavata.workflow.model.exceptions.WorkflowException;
import org.apache.airavata.workflow.model.wf.Workflow;
import org.apache.airavata.xbaya.XBayaConfiguration;
import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.lead.LEADWorkflowInvoker;
import org.apache.airavata.xbaya.monitor.MonitorConfiguration;
import org.apache.airavata.xbaya.monitor.MonitorException;
import org.apache.airavata.xbaya.myproxy.MyProxyClient;
import org.apache.airavata.xbaya.security.XBayaSecurity;
import org.apache.airavata.xbaya.ui.dialogs.WaitDialog;
import org.apache.airavata.xbaya.ui.utils.Cancelable;
import org.apache.airavata.xbaya.ui.utils.ErrorMessages;
import org.apache.airavata.xbaya.util.XBayaUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import xsul.XmlConstants;
import xsul.invoker.gsi.GsiInvoker;
import xsul.lead.LeadContextHeader;
import xsul.lead.LeadResourceMapping;
import xsul5.wsdl.WsdlDefinitions;

public class ODEInvoker implements Cancelable {

    private XBayaEngine engine;

    private static final Log logger = LogFactory.getLog(ODEInvoker.class);

    private Thread invokeThread;

    private boolean canceled;

    private WaitDialog invokingDialog;

    public ODEInvoker(XBayaEngine engine) {
        this.engine = engine;

        this.invokingDialog = new WaitDialog(this, "Invoking the Workflow.", "Invoking the Workflow."
                + "Please wait for a moment.", this.engine.getGUI());
    }

    /**
     * @see org.apache.airavata.xbaya.ui.utils.Cancelable#cancel()
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

            LeadContextHeader leadContext = XBayaUtil.buildLeadContextHeader(this.engine, monitorConfiguration,
                    StringUtil.convertToJavaIdentifier(engine.getGUI().getWorkflow().getName()), resourceMapping);
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
                logger.error(e.getMessage(), e);
            } else {
                this.engine.getGUI().getErrorWindow().error(ErrorMessages.GRAPH_NOT_READY_ERROR, e);
                this.invokingDialog.hide();
            }
            return;
        } catch (RuntimeException e) {
            if (this.canceled) {
                logger.error(e.getMessage(), e);
            } else {
                this.engine.getGUI().getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR, e);
                this.invokingDialog.hide();
            }
            return;
        } catch (Exception e) {
            this.engine.getGUI().getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR, e);
            this.invokingDialog.hide();
            return;
        }

        invoker.setInputs(inputs);

        // Start the monitor.
        try {
            this.engine.getMonitor().start();
        } catch (MonitorException e) {
            if (this.canceled) {
                logger.error(e.getMessage(), e);
            } else {
                this.engine.getGUI().getErrorWindow().error(ErrorMessages.MONITOR_SUBSCRIPTION_ERROR, e);
                this.invokingDialog.hide();
            }
            return;
        } catch (RuntimeException e) {
            if (this.canceled) {
                logger.error(e.getMessage(), e);
            } else {
                this.engine.getGUI().getErrorWindow().error(ErrorMessages.MONITOR_SUBSCRIPTION_ERROR, e);
                this.invokingDialog.hide();
            }
            return;
        } catch (Error e) {
            this.engine.getGUI().getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR, e);
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
                } catch (WorkflowException e) {
                    ODEInvoker.this.engine.getGUI().getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR, e);
                }
            }
        }.start();

        ODEInvoker.this.invokingDialog.hide();
    }

}