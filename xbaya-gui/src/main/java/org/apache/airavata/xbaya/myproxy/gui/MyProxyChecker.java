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
import org.apache.airavata.xbaya.myproxy.MyProxyClient;

public class MyProxyChecker {

    // private static final MLogger logger = MLogger.getLogger();

    private XBayaEngine engine;

    private MyProxyDialog dialog;

    /**
     * Constructs a MyProxyChecker.
     * 
     * @param engine
     */
    public MyProxyChecker(XBayaEngine engine) {
        this.engine = engine;
        this.dialog = new MyProxyDialog(this.engine);
    }

    /**
     * @return true if a proxy has been loaded and valid or if a proxy is loaded successfully; false otherwise.
     */
    public boolean loadIfNecessary() {
        if (isProxyLoaded()) {
            // no need to load.
            return true;
        } else {
            // load a proxy.
            this.dialog.show(true); // blocking
            if (isProxyLoaded()) {
                // success.
                return true;
            } else {
                // give up.
                return false;
            }
        }
    }

    private boolean isProxyLoaded() {
        MyProxyClient myProxyClient = this.engine.getMyProxyClient();
        return myProxyClient.isProxyValid();
    }

}