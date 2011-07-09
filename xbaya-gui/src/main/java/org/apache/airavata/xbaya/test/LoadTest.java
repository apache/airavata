/*
 * Copyright (c) 2008 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the license.
 *
 * $Id: LoadTest.java,v 1.2 2008/07/09 18:51:38 cherath Exp $
 */
package org.apache.airavata.xbaya.test;

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

/**
 * @author Chathura Herath
 */
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

/*
 * Indiana University Extreme! Lab Software License, Version 1.2
 * 
 * Copyright (c) 2008 The Trustees of Indiana University. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 * 
 * 1) All redistributions of source code must retain the above copyright notice, the list of authors in the original
 * source code, this list of conditions and the disclaimer listed in this license;
 * 
 * 2) All redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * disclaimer listed in this license in the documentation and/or other materials provided with the distribution;
 * 
 * 3) Any documentation included with all redistributions must include the following acknowledgement:
 * 
 * "This product includes software developed by the Indiana University Extreme! Lab. For further information please
 * visit http://www.extreme.indiana.edu/"
 * 
 * Alternatively, this acknowledgment may appear in the software itself, and wherever such third-party acknowledgments
 * normally appear.
 * 
 * 4) The name "Indiana University" or "Indiana University Extreme! Lab" shall not be used to endorse or promote
 * products derived from this software without prior written permission from Indiana University. For written permission,
 * please contact http://www.extreme.indiana.edu/.
 * 
 * 5) Products derived from this software may not use "Indiana University" name nor may "Indiana University" appear in
 * their name, without prior written permission of the Indiana University.
 * 
 * Indiana University provides no reassurances that the source code provided does not infringe the patent or any other
 * intellectual property rights of any other entity. Indiana University disclaims any liability to any recipient for
 * claims brought by any other entity based on infringement of intellectual property rights or otherwise.
 * 
 * LICENSEE UNDERSTANDS THAT SOFTWARE IS PROVIDED "AS IS" FOR WHICH NO WARRANTIES AS TO CAPABILITIES OR ACCURACY ARE
 * MADE. INDIANA UNIVERSITY GIVES NO WARRANTIES AND MAKES NO REPRESENTATION THAT SOFTWARE IS FREE OF INFRINGEMENT OF
 * THIRD PARTY PATENT, COPYRIGHT, OR OTHER PROPRIETARY RIGHTS. INDIANA UNIVERSITY MAKES NO WARRANTIES THAT SOFTWARE IS
 * FREE FROM "BUGS", "VIRUSES", "TROJAN HORSES", "TRAP DOORS", "WORMS", OR OTHER HARMFUL CODE. LICENSEE ASSUMES THE
 * ENTIRE RISK AS TO THE PERFORMANCE OF SOFTWARE AND/OR ASSOCIATED MATERIALS, AND TO THE PERFORMANCE AND VALIDITY OF
 * INFORMATION GENERATED USING SOFTWARE.
 */
