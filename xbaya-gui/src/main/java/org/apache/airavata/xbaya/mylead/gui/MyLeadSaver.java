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

package org.apache.airavata.xbaya.mylead.gui;

import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.gui.Cancelable;
import org.apache.airavata.xbaya.gui.ErrorMessages;
import org.apache.airavata.xbaya.gui.WaitDialog;
import org.apache.airavata.xbaya.mylead.MyLeadException;
import org.apache.airavata.xbaya.ode.gui.ODEDeploymentClient;
import org.apache.airavata.xbaya.ode.gui.ODEDeploymentWindow;
import org.apache.airavata.xbaya.wf.Workflow;
import org.apache.airavata.xbaya.workflow.WorkflowEngineException;
import org.apache.airavata.xbaya.workflow.proxy.WorkflowProxyClient;
import org.ietf.jgss.GSSCredential;

import xsul5.MLogger;

public class MyLeadSaver implements Cancelable {

    private static final MLogger logger = MLogger.getLogger();

    private XBayaEngine engine;

    private Thread loadThread;

    private boolean canceled;

    private WaitDialog savingDialog;

    /**
     * Constructs a MyLeadWorkflowLoader.
     * 
     * @param client
     */
    public MyLeadSaver(XBayaEngine client) {
        this.engine = client;

        this.savingDialog = new WaitDialog(this, "Saving the Workflow.", "Saving the Workflow. "
                + "Please wait for a moment.", this.engine);
    }

    /**
     * @see org.apache.airavata.xbaya.gui.Cancelable#cancel()
     */
    public void cancel() {
        this.canceled = true;
        this.loadThread.interrupt();
    }

    /**
     * Saves the workflow.
     * 
     * @param redeploy
     */
    public void save(final boolean redeploy) {
        this.canceled = false;

        final Workflow workflow = MyLeadSaver.this.engine.getWorkflow();
        this.loadThread = new Thread() {
            @Override
            public void run() {
                runInThread(redeploy, workflow);
            }
        };
        this.loadThread.start();

        // This has to be the last because it blocks when the dialog is modal.
        this.savingDialog.show();
    }

    /**
     * @param redeploy
     * @param workflow
     */
    private void runInThread(boolean redeploy, Workflow workflow) {
        try {
            WaitDialog waitDialog = new WaitDialog(this, "Deploying the Workflow.", "Deploying the Workflow."
                    + "Please wait for a moment.", this.engine);
            GSSCredential proxy = this.engine.getMyProxyClient().getProxy();
            new ODEDeploymentWindow();
            final WorkflowProxyClient client = new WorkflowProxyClient();
            client.setXRegistryUrl(this.engine.getConfiguration().getXRegistryURL());
            client.setEngineURL(this.engine.getConfiguration().getProxyURI());
            client.setXBayaEngine(this.engine);

            new ODEDeploymentClient(this.engine, waitDialog).deploy(client, workflow, proxy, true,
                    System.currentTimeMillis());

            this.engine.getMyLead().setProxy(proxy);
            this.engine.getMyLead().save(workflow, redeploy);
            this.savingDialog.hide();
        } catch (MyLeadException e) {
            if (MyLeadSaver.this.canceled) {
                logger.caught(e);
            } else {
                this.engine.getErrorWindow().error(ErrorMessages.MYLEAD_SAVE_TEMPLATE_ERROR, e);
                this.savingDialog.hide();
            }
        } catch (RuntimeException e) {
            if (this.canceled) {
                logger.caught(e);
            } else {
                this.engine.getErrorWindow().error(ErrorMessages.MYLEAD_SAVE_TEMPLATE_ERROR, e);
                this.savingDialog.hide();
            }
        } catch (Error e) {
            this.engine.getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR, e);
            this.savingDialog.hide();
        } catch (WorkflowEngineException e) {
            this.engine.getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR, e);
            this.savingDialog.hide();
        }
    }
}