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

import javax.xml.namespace.QName;

import org.apache.airavata.xbaya.XBayaConstants;
import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.gui.Cancelable;
import org.apache.airavata.xbaya.gui.ErrorMessages;
import org.apache.airavata.xbaya.gui.WaitDialog;
import org.apache.airavata.xbaya.mylead.MyLead;
import org.apache.airavata.xbaya.wf.Workflow;
import org.apache.airavata.xbaya.xregistry.XRegistryAccesser;

import xsul5.MLogger;

public class MyLeadLoader implements Cancelable {

    private static final MLogger logger = MLogger.getLogger();

    private XBayaEngine engine;

    private MyLead myLead;

    private Thread loadThread;

    private boolean canceled;

    private WaitDialog loadingDialog;

    /**
     * Constructs a MyLeadWorkflowLoader.
     * 
     * @param engine
     */
    public MyLeadLoader(XBayaEngine engine) {
        this(engine, engine.getMyLead());
    }

    /**
     * Constructs a MyLeadWorkflowLoader.
     * 
     * This method is used to load workflows from a specified location, not from the user's default location.
     * 
     * @param client
     * @param connection
     */
    public MyLeadLoader(XBayaEngine client, MyLead connection) {
        this.engine = client;
        this.myLead = connection;

        this.loadingDialog = new WaitDialog(this, "Loading the Workflow.", "Loading the Workflow. "
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
     * Loads the workflow.
     * 
     * @param resouceID
     * @param blocking
     *            true for blocking call, false for non-blocking call
     */
    public void load(final QName resouceID, boolean blocking) {
        this.canceled = false;

        this.loadThread = new Thread() {
            @Override
            public void run() {
                runInThread(new QName(XBayaConstants.LEAD_NS, resouceID.getLocalPart()));
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
     * @param resouceID
     */
    private void runInThread(QName resouceID) {
        try {
            Workflow workflow;
            XRegistryAccesser xregistryAccesser = new XRegistryAccesser(this.engine);
            workflow = xregistryAccesser.getWorkflow(resouceID);
            this.loadingDialog.hide();
            if (this.canceled) {
                return;
            }
            this.engine.setWorkflow(workflow);
        } catch (RuntimeException e) {
            if (this.canceled) {
                logger.caught(e);
            } else {
                this.engine.getErrorWindow().error(ErrorMessages.MYLEAD_LOAD_TEMPLATE_ERROR, e);
                this.loadingDialog.hide();
            }
        } catch (Error e) {
            this.engine.getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR, e);
            this.loadingDialog.hide();
        }
    }

}