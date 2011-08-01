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

package org.apache.airavata.xbaya.test;

import java.util.LinkedList;
import java.util.List;

import org.apache.airavata.xbaya.XBaya;
import org.apache.airavata.xbaya.XBayaConstants;
import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.component.ComponentException;
import org.apache.airavata.xbaya.gui.XBayaTextField;
import org.apache.airavata.xbaya.jython.gui.JythonRunnerWindow;
import org.apache.airavata.xbaya.myproxy.MyProxyClient;
import org.apache.airavata.xbaya.myproxy.gui.MyProxyLoader;

public class LoadTest {

    private int load = 1;

    public void testLoad() throws ComponentException {

        final String[] args = new String[2];
        args[0] = "-workflow";
        args[1] = "/nfs/mneme/home/users/cherath/projects/test/extremeWorkspace/graphlayout/workflows/load2.xwf";

        for (int i = 0; i < this.load; ++i) {
            final int val = i;
            new Thread() {
                @Override
                public synchronized void run() {
                    XBaya xBaya = new XBaya(args);
                    XBayaTextField topic = new XBayaTextField();
                    topic.setText("topic" + val);
                    // Iterator<WSComponentPort> iterator = xBaya.getEngine().getWorkflow().getInputs().iterator();
                    List<XBayaTextField> parameterTextFields = new LinkedList<XBayaTextField>();
                    xBaya.getEngine().getWorkflow().getWorkflowWSDL();
                    int count = 1;
                    for (int j = 0; j < 2; ++j) {
                        XBayaTextField txt = new XBayaTextField();
                        txt.setText("" + count);
                        parameterTextFields.add(txt);

                    }
                    JythonRunnerWindow window = new JythonRunnerWindow(xBaya.getEngine(), topic, parameterTextFields);
                    window.execute();
                }
            }.start();

        }
    }

    public void testGPEL() {

        final String[] args = new String[2];
        args[0] = "-workflow";
        args[1] = "/u/cherath/Desktop/mytest.xwf";

        for (int i = 0; i < this.load; ++i) {
            final int val = i;
            new Thread() {
                @Override
                public synchronized void run() {
                    XBaya xBaya = new XBaya(args);
                    XBayaEngine engine = xBaya.getEngine();
                    engine.getMyProxyClient();
                    MyProxyClient myProxyClient = engine.getMyProxyClient();
                    myProxyClient.set(XBayaConstants.DEFAULT_MYPROXY_SERVER, XBayaConstants.DEFAULT_MYPROXY_PORT,
                            "chathura", "changeme", XBayaConstants.DEFAULT_MYPROXY_LIFTTIME);
                    MyProxyLoader myProxyLoader = new MyProxyLoader(engine);
                    myProxyLoader.load();

                    GPELInvokeSetupForTesting invoker = new GPELInvokeSetupForTesting(engine, val + "topic"
                            + System.currentTimeMillis());
                    invoker.execute(true);
                }
            }.start();

        }

    }

    public static void main(String[] args) throws Exception {
        new LoadTest().testGPEL();
    }

}