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

package org.apache.airavata.xbaya.myproxy.gui;

import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.gui.Cancelable;
import org.apache.airavata.xbaya.gui.ErrorMessages;
import org.apache.airavata.xbaya.gui.WaitDialog;
import org.apache.airavata.xbaya.myproxy.MyProxyClient;
import org.globus.myproxy.MyProxyException;

import xsul5.MLogger;

public class MyProxyLoader implements Cancelable {

    private static final MLogger logger = MLogger.getLogger();

    private XBayaEngine engine;

    private Thread loadingThread;

    private boolean canceled;

    private WaitDialog waitDialog;

    /**
     * Constructs a MonitorStarter.
     * 
     * @param engine
     */
    public MyProxyLoader(XBayaEngine engine) {
        this.engine = engine;

        this.waitDialog = new WaitDialog(this, "Loading Proxy", "Loading a proxy.\n" + "Please wait for a moment.",
                this.engine);
    }

    /**
     * @see org.apache.airavata.xbaya.gui.Cancelable#cancel()
     */
    public void cancel() {
        this.canceled = true;
        this.loadingThread.interrupt();
    }

    /**
     * Loads myProxy.
     */
    public void load() {
        this.canceled = false;

        this.loadingThread = new Thread() {
            @Override
            public void run() {
                runInThread();
            }
        };
        this.loadingThread.start();

        // This has to be the last because it blocks when the dialog is modal.
        this.waitDialog.show();
    }

    private void runInThread() {
        try {
            MyProxyClient client = this.engine.getMyProxyClient();
            client.load();
            this.waitDialog.hide();
        } catch (MyProxyException e) {
            if (this.canceled) {
                logger.caught(e);
            } else {
                String message = ErrorMessages.MYPROXY_LOAD_ERROR + "\n" + e.getMessage();
                this.engine.getErrorWindow().error(message, e);
                this.waitDialog.hide();
            }

        } catch (RuntimeException e) {
            if (this.canceled) {
                logger.caught(e);
            } else {
                this.engine.getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR, e);
                this.waitDialog.hide();
            }
        } catch (Error e) {
            this.engine.getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR, e);
            this.waitDialog.hide();
        }
    }
}