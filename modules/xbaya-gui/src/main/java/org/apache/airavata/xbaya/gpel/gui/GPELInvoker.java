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

import java.net.URI;
import java.util.List;

import org.apache.airavata.xbaya.XBayaConfiguration;
import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.XBayaException;
import org.apache.airavata.xbaya.component.ComponentException;
import org.apache.airavata.xbaya.component.ws.WSComponentPort;
import org.apache.airavata.xbaya.graph.GraphException;
import org.apache.airavata.xbaya.gui.Cancelable;
import org.apache.airavata.xbaya.gui.ErrorMessages;
import org.apache.airavata.xbaya.gui.WaitDialog;
import org.apache.airavata.xbaya.lead.LEADWorkflowInvoker;
import org.apache.airavata.xbaya.lead.LeadContextHeaderHelper;
import org.apache.airavata.xbaya.monitor.MonitorConfiguration;
import org.apache.airavata.xbaya.monitor.MonitorException;
import org.apache.airavata.xbaya.myproxy.MyProxyClient;
import org.apache.airavata.xbaya.security.XBayaSecurity;
import org.apache.airavata.xbaya.wf.Workflow;
import org.apache.airavata.xbaya.workflow.WorkflowClient;
import org.apache.airavata.xbaya.workflow.WorkflowEngineException;
import org.gpel.client.GcInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xsul.XmlConstants;
import xsul.invoker.gsi.GsiInvoker;
import xsul.lead.LeadContextHeader;
import xsul5.wsdl.WsdlDefinitions;

public class GPELInvoker implements Cancelable {

    private static final Logger logger = LoggerFactory.getLogger(GPELInvoker.class);

    private XBayaEngine engine;

    private Thread invokeThread;

    private boolean canceled;

    private WaitDialog invokingDialog;

    /**
     * Constructs a GPELInvoker.
     * 
     * @param engine
     */
    public GPELInvoker(XBayaEngine engine) {
        this.engine = engine;

        this.invokingDialog = new WaitDialog(this, "Deploying and Invoking the Workflow.",
                "Deploying and Invoking the Workflow." + "Please wait for a moment.", this.engine);
    }

    /**
     * @see org.apache.airavata.xbaya.gui.Cancelable#cancel()
     */
    public void cancel() {
        this.canceled = true;
        this.invokeThread.interrupt();
    }

    /**
     * @param workflow
     * @param inputs
     * @param redeploy
     */
    public void invoke(final Workflow workflow, final List<WSComponentPort> inputs, final boolean redeploy) {
        this.canceled = false;

        this.invokeThread = new Thread() {
            @Override
            public void run() {
                runInThread(workflow, inputs, redeploy);
            }

        };
        this.invokeThread.start();

        // This has to be the last because it blocks when the dialog is modal.
        this.invokingDialog.show();
    }

    private void runInThread(final Workflow workflow, final List<WSComponentPort> inputs, final boolean redeploy) {

        WorkflowClient client = this.engine.getWorkflowClient();
        try {
            client.deploy(workflow, redeploy);
        } catch (WorkflowEngineException e) {
            if (this.canceled) {
                logger.error(e.getMessage(), e);
            } else {
                this.engine.getErrorWindow().error(ErrorMessages.GPEL_ERROR, e);
                this.invokingDialog.hide();
            }
            return;
        } catch (RuntimeException e) {
            if (this.canceled) {
                logger.error(e.getMessage(), e);
            } else {
                this.engine.getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR, e);
                this.invokingDialog.hide();
            }
            return;
        } catch (Error e) {
            if (this.canceled) {
                logger.error(e.getMessage(), e);
            } else {
                this.engine.getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR, e);
                this.invokingDialog.hide();
            }
            return;
        }

        MonitorConfiguration monitorConfiguration = this.engine.getMonitor().getConfiguration();
        XBayaConfiguration xbayaConfiguration = this.engine.getConfiguration();
        WsdlDefinitions wsdl;
        try {
            GcInstance instance = client.instantiate(workflow, xbayaConfiguration.getDSCURL(),
                    monitorConfiguration.getTopic());
            wsdl = client.start(instance);
        } catch (WorkflowEngineException e) {
            if (this.canceled) {
                logger.error(e.getMessage(), e);
            } else {
                this.engine.getErrorWindow().error(ErrorMessages.GPEL_ERROR, e);
                this.invokingDialog.hide();
            }
            return;
        } catch (ComponentException e) {
            if (this.canceled) {
                logger.error(e.getMessage(), e);
            } else {
                this.engine.getErrorWindow().error(ErrorMessages.GPEL_ERROR, e);
                this.invokingDialog.hide();
            }
            return;
        } catch (GraphException e) {
            if (this.canceled) {
                logger.error(e.getMessage(), e);
            } else {
                this.engine.getErrorWindow().error(ErrorMessages.GPEL_ERROR, e);
                this.invokingDialog.hide();
            }
            return;
        } catch (RuntimeException e) {
            if (this.canceled) {
                logger.error(e.getMessage(), e);
            } else {
                this.engine.getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR, e);
                this.invokingDialog.hide();
            }
            return;
        } catch (Error e) {
            this.engine.getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR, e);
            this.invokingDialog.hide();
            return;
        }

        // Create the invoker
        LEADWorkflowInvoker invoker = null;
        try {
            LeadContextHeaderHelper leadContextHelper = new LeadContextHeaderHelper();
            leadContextHelper.setXBayaConfiguration(xbayaConfiguration);
            leadContextHelper.setWorkflow(workflow);
            leadContextHelper.setMonitorConfiguration(monitorConfiguration);

            LeadContextHeader leadContext = leadContextHelper.getLeadContextHeader();

            URI messageBoxURL = null;
            if (monitorConfiguration.isPullMode()) {
                messageBoxURL = monitorConfiguration.getMessageBoxURL();
            }

            // create an invoker with LEAD Context
            GsiInvoker secureInvoker = null;
            if (this.engine.getWorkflowClient().isSecure()) {
                MyProxyClient myProxyClient = this.engine.getMyProxyClient();
                secureInvoker = new GsiInvoker(myProxyClient.getProxy(), XBayaSecurity.getTrustedCertificates());
            }
            invoker = new LEADWorkflowInvoker(wsdl, leadContext, messageBoxURL, secureInvoker);
        } catch (ComponentException e) {
            if (this.canceled) {
                logger.error(e.getMessage(), e);
            } else {
                this.engine.getErrorWindow().error(ErrorMessages.GRAPH_NOT_READY_ERROR, e);
                this.invokingDialog.hide();
            }
            return;
        } catch (RuntimeException e) {
            if (this.canceled) {
                logger.error(e.getMessage(), e);
            } else {
                this.engine.getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR, e);
                this.invokingDialog.hide();
            }
            return;
        } catch (Error e) {
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
                logger.error(e.getMessage(), e);
            } else {
                this.engine.getErrorWindow().error(ErrorMessages.MONITOR_SUBSCRIPTION_ERROR, e);
                this.invokingDialog.hide();
            }
            return;
        } catch (RuntimeException e) {
            if (this.canceled) {
                logger.error(e.getMessage(), e);
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
                    GPELInvoker.this.engine.getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR, e);
                }
            }
        }.start();

        GPELInvoker.this.invokingDialog.hide();
    }
}