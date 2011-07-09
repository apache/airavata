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

import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.component.ComponentException;
import org.apache.airavata.xbaya.graph.GraphException;
import org.apache.airavata.xbaya.gui.Cancelable;
import org.apache.airavata.xbaya.gui.ErrorMessages;
import org.apache.airavata.xbaya.gui.WaitDialog;
import org.apache.airavata.xbaya.wf.Workflow;
import org.apache.airavata.xbaya.workflow.WorkflowClient;
import org.apache.airavata.xbaya.workflow.WorkflowEngineException;

import xsul5.MLogger;

public class GPELLoader implements Cancelable {

    private static final MLogger logger = MLogger.getLogger();

    private XBayaEngine engine;

    private Thread loadThread;

    private boolean canceled;

    private WaitDialog loadingDialog;

    /**
     * Constructs a GPELLoader.
     * 
     * @param engine
     */
    public GPELLoader(XBayaEngine engine) {
        this.engine = engine;

        this.loadingDialog = new WaitDialog(this, "Loading a Workflow from the GPEL Engine.",
                "Loading a Workflow from the GPEL Engine. " + "Please wait for a moment.", this.engine);
    }

    /**
     * @see org.apache.airavata.xbaya.gui.Cancelable#cancel()
     */
    public void cancel() {
        this.canceled = true;
        this.loadThread.interrupt();
    }

    /**
     * Load the workflow.
     * 
     * @param templateID
     * @param type
     * @param blocking
     */
    public void load(final URI templateID, final WorkflowClient.WorkflowType type, boolean blocking) {

        this.canceled = false;

        this.loadThread = new Thread() {
            @Override
            public void run() {
                runInThread(templateID, type);
            }
        };
        this.loadThread.start();

        // This has to be the last because it blocks when the dialog is modal.
        this.loadingDialog.show();

        if (blocking) {
            try {
                this.loadThread.join();
            } catch (InterruptedException e) {
                logger.caught(e);
            }
        }
    }

    /**
     * @param templateID
     * @param type
     */
    private void runInThread(URI templateID, WorkflowClient.WorkflowType type) {
        WorkflowClient client = GPELLoader.this.engine.getWorkflowClient();
        try {
            Workflow workflow = client.load(templateID, type);
            this.loadingDialog.hide();
            if (this.canceled) {
                return;
            }
            this.engine.setWorkflow(workflow);
            // TODO load notification in case of workflow instance
        } catch (WorkflowEngineException e) {
            if (this.canceled) {
                logger.caught(e);
            } else {
                this.engine.getErrorWindow().error(e.getMessage(), e);
                this.loadingDialog.hide();
            }
        } catch (GraphException e) {
            if (this.canceled) {
                logger.caught(e);
            } else {
                this.engine.getErrorWindow().error(ErrorMessages.GRAPH_FORMAT_ERROR, e);
                this.loadingDialog.hide();
            }
        } catch (ComponentException e) {
            if (this.canceled) {
                logger.caught(e);
            } else {
                this.engine.getErrorWindow().error(ErrorMessages.COMPONENT_FORMAT_ERROR, e);
                this.loadingDialog.hide();
            }
        } catch (RuntimeException e) {
            if (this.canceled) {
                logger.caught(e);
            } else {
                this.engine.getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR, e);
                this.loadingDialog.hide();
            }
        } catch (Error e) {
            this.engine.getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR, e);
            this.loadingDialog.hide();
        }
    }
}