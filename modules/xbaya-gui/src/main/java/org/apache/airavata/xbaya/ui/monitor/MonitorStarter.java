/**
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
 */
package org.apache.airavata.xbaya.ui.monitor;

import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.messaging.MonitorException;
import org.apache.airavata.xbaya.ui.dialogs.WaitDialog;
import org.apache.airavata.xbaya.ui.utils.Cancelable;
import org.apache.airavata.xbaya.ui.utils.ErrorMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MonitorStarter implements Cancelable {

    private static final Logger logger = LoggerFactory.getLogger(MonitorStarter.class);

    private XBayaEngine engine;

    private Thread subscribingThread;

    private boolean canceled;

    private WaitDialog startingDialog;

    /**
     * Constructs a MonitorStarter.
     * 
     * @param engine
     */
    public MonitorStarter(XBayaEngine engine) {
        this.engine = engine;

        this.startingDialog = new WaitDialog(this, "Starting Monitoring", "Subscribing to notification.\n"
                + "Please wait for a moment.", this.engine.getGUI());
    }

    /**
     * @see org.apache.airavata.xbaya.ui.utils.Cancelable#cancel()
     */
    public void cancel() {
        this.canceled = true;
//        this.subscribingThread.interrupt();
    }

    /**
     * Starts monitoring.
     */
    public void start() {
        // Non blocking
        start(false);
    }

    /**
     * Starts monitoring.
     * 
     * @param blocking
     */
    public void start(final boolean blocking) {
        this.canceled = false;

        this.subscribingThread = new Thread() {
            @Override
            public void run() {
                runInThread();
            }

        };
        this.subscribingThread.start();

        // This has to be the last because it blocks when the dialog is modal.
        this.startingDialog.show();

        if (blocking) {
            try {
                this.subscribingThread.join();
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    private void runInThread() {
        try {
            this.engine.getMonitor().start();
            this.startingDialog.hide();
        } catch (MonitorException e) {
            // Probably canceled by a user.
            if (this.canceled) {
                logger.error(e.getMessage(), e);
            } else {
                this.engine.getGUI().getErrorWindow().error(ErrorMessages.MONITOR_SUBSCRIPTION_ERROR, e);
                this.startingDialog.hide();
            }
        } catch (RuntimeException e) {
            if (this.canceled) {
                logger.error(e.getMessage(), e);
            } else {
                this.engine.getGUI().getErrorWindow().error(ErrorMessages.MONITOR_SUBSCRIPTION_ERROR, e);
                this.startingDialog.hide();
            }
        } catch (Error e) {
            this.engine.getGUI().getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR, e);
            this.startingDialog.hide();
        }
    }
}