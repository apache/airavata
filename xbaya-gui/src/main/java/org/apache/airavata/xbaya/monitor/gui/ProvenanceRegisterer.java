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

package org.apache.airavata.xbaya.monitor.gui;

import java.net.URI;

import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.gui.Cancelable;
import org.apache.airavata.xbaya.gui.ErrorMessages;
import org.apache.airavata.xbaya.gui.WaitDialog;
import org.apache.airavata.xbaya.monitor.KarmaClient.Rate;
import org.apache.airavata.xbaya.monitor.Monitor;

import xsul5.MLogger;

public class ProvenanceRegisterer implements Cancelable {

    private static final MLogger logger = MLogger.getLogger();

    private XBayaEngine engine;

    private Thread connectionThread;

    private boolean canceled;

    private WaitDialog connectingDialog;

    /**
     * Constructs a GPELDeployer.
     * 
     * @param engine
     */
    public ProvenanceRegisterer(XBayaEngine engine) {
        this.engine = engine;

        this.connectingDialog = new WaitDialog(this, "Connecting to a Kerma service", "Connecting to a Kerma service. "
                + "Please wait for a moment.", this.engine);
    }

    /**
     * @see org.apache.airavata.xbaya.gui.Cancelable#cancel()
     */
    public void cancel() {
        this.canceled = true;
        this.connectionThread.interrupt();
    }

    /**
     * Saves the workflow.
     * 
     * @param redeploy
     */
    /**
     * @param url
     * @param id
     * @param rate
     */
    public void register(final URI url, final URI id, final Rate rate) {
        this.canceled = false;

        this.connectionThread = new Thread() {
            @Override
            public void run() {
                runInThread(url, id, rate);
            }
        };
        this.connectionThread.start();

        // This has to be the last because it blocks when the dialog is modal.
        this.connectingDialog.show();
    }

    /**
     * @param url
     * @param id
     * @param rate
     * @param redeploy
     * @param workflow
     */
    private void runInThread(URI url, URI id, Rate rate) {
        Monitor monitor = this.engine.getMonitor();
        try {
            monitor.startKarma(url, id, rate);
            this.connectingDialog.hide();
        } catch (RuntimeException e) {
            if (this.canceled) {
                logger.caught(e);
            } else {
                this.engine.getErrorWindow().error(ErrorMessages.KARMA_CONNECTION_ERROR, e);
                this.connectingDialog.hide();
            }
        } catch (Error e) {
            this.engine.getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR, e);
            this.connectingDialog.hide();
        }
    }
}