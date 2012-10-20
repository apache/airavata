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

package org.apache.airavata.xbaya.ui.dialogs.myproxy;

import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.myproxy.MyProxyClient;
import org.apache.airavata.xbaya.ui.dialogs.WaitDialog;
import org.apache.airavata.xbaya.ui.utils.Cancelable;
import org.apache.airavata.xbaya.ui.utils.ErrorMessages;
import org.globus.myproxy.MyProxyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyProxyLoader implements Cancelable {

    private static final Logger logger = LoggerFactory.getLogger(MyProxyLoader.class);

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
                this.engine.getGUI());
    }

    /**
     * @see org.apache.airavata.xbaya.ui.utils.Cancelable#cancel()
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
                logger.error(e.getMessage(), e);
            } else {
                String message = ErrorMessages.MYPROXY_LOAD_ERROR + "\n" + e.getMessage();
                this.engine.getGUI().getErrorWindow().error(message, e);
                this.waitDialog.hide();
            }

        } catch (RuntimeException e) {
            if (this.canceled) {
                logger.error(e.getMessage(), e);
            } else {
                this.engine.getGUI().getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR, e);
                this.waitDialog.hide();
            }
        } catch (Error e) {
            this.engine.getGUI().getErrorWindow().error(ErrorMessages.UNEXPECTED_ERROR, e);
            this.waitDialog.hide();
        }
    }
}