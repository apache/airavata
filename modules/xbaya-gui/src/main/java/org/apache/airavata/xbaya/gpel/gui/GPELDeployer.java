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

import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.component.ComponentException;
import org.apache.airavata.xbaya.graph.GraphException;
import org.apache.airavata.xbaya.gui.Cancelable;
import org.apache.airavata.xbaya.gui.ErrorMessages;
import org.apache.airavata.xbaya.gui.WaitDialog;
import org.apache.airavata.xbaya.wf.Workflow;
import org.apache.airavata.xbaya.workflow.WorkflowClient;
import org.apache.airavata.xbaya.workflow.WorkflowEngineException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GPELDeployer implements Cancelable {

    private static final Logger logger = LoggerFactory.getLogger(GPELDeployer.class);

    private XBayaEngine engine;

    private Thread deployThread;

    private boolean canceled;

    private WaitDialog deployingDialog;

    /**
     * Constructs a GPELDeployer.
     * 
     * @param engine
     */
    public GPELDeployer(XBayaEngine engine) {
        this.engine = engine;

        this.deployingDialog = new WaitDialog(this, "Saving a Workflow to the GPEL Engine",
                "Saving a Workflow to the GPEL Engine. " + "Please wait for a moment.", this.engine);
    }

    /**
     * @see org.apache.airavata.xbaya.gui.Cancelable#cancel()
     */
    @Override
    public void cancel() {
        this.canceled = true;
        this.deployThread.interrupt();
    }

    /**
     * Saves the workflow.
     * 
     * @param redeploy
     */
    public void deploy(final boolean redeploy) {
        this.canceled = false;
        final Workflow workflow = this.engine.getWorkflow();

        this.deployThread = new Thread() {
            @Override
            public void run() {
                runInThread(redeploy, workflow);
            }

        };
        this.deployThread.start();

        // This has to be the last because it blocks when the dialog is modal.
        this.deployingDialog.show();
    }

    /**
     * @param redeploy
     * @param workflow
     */
    private void runInThread(boolean redeploy, Workflow workflow) {
        WorkflowClient client = this.engine.getWorkflowClient();
        try {
            client.createScriptAndDeploy(workflow, redeploy);
            if (redeploy) {
                this.engine.getSubWorkflowUpdater().update(workflow);
            }
            this.deployingDialog.hide();
        } catch (GraphException e) {
            if (this.canceled) {
                logger.error(e.getMessage(), e);
            } else {
                this.engine.getErrorWindow().error(ErrorMessages.GRAPH_NOT_READY_ERROR, e);
                this.deployingDialog.hide();
            }
        } catch (WorkflowEngineException e) {
            if (this.canceled) {
                logger.error(e.getMessage(), e);
            } else {
                this.engine.getErrorWindow().error(ErrorMessages.GPEL_ERROR, e);
                this.deployingDialog.hide();
            }
        } catch (ComponentException e) {
            if (this.canceled) {
                logger.error(e.getMessage(), e);
            } else {
                this.engine.getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR, e);
                this.deployingDialog.hide();
            }
        } catch (RuntimeException e) {
            if (this.canceled) {
                logger.error(e.getMessage(), e);
            } else {
                this.engine.getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR, e);
                this.deployingDialog.hide();
            }
        } catch (Error e) {
            this.engine.getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR, e);
            this.deployingDialog.hide();
        }
    }
}