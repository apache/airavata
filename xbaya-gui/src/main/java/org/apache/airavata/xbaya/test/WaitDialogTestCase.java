/*
 * Copyright (c) 2007 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the license.
 *
 * $Id: WaitDialogTestCase.java,v 1.1 2007/03/21 09:16:04 sshirasu Exp $
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

import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.gui.Cancelable;
import org.apache.airavata.xbaya.gui.WaitDialog;

/**
 * @author Satoshi Shirasuna
 */
public class WaitDialogTestCase extends XBayaTestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // Prevent to connect to GPEL engine every time.
        this.configuration.setGPELEngineURL(null);

        this.configuration.setHeight(200);
        this.configuration.setWidth(200);
    }

    /**
     * @throws InterruptedException
     * 
     */
    public void testShowHide() throws InterruptedException {
        XBayaEngine engine = new XBayaEngine(this.configuration);
        Cancelable cancelable = new Cancelable() {
            public void cancel() {
                // Nothing
            }
        };
        final WaitDialog dialog = new WaitDialog(cancelable, "title", "message", engine);
        Thread showThread = new Thread() {
            @Override
            public void run() {
                dialog.show();
            }

        };
        showThread.start();

        Thread.sleep(1000);

        dialog.hide();

        showThread.join();
    }

    /**
     * @throws InterruptedException
     * 
     */
    public void testShowShowHide() throws InterruptedException {
        XBayaEngine engine = new XBayaEngine(this.configuration);
        Cancelable cancelable = new Cancelable() {
            public void cancel() {
                // Nothing
            }
        };
        final WaitDialog dialog = new WaitDialog(cancelable, "title", "message", engine);
        Thread showThread1 = new Thread() {
            @Override
            public void run() {
                dialog.show();
            }

        };

        Thread showThread2 = new Thread() {
            @Override
            public void run() {
                dialog.show();
            }

        };

        Thread hideThread1 = new Thread() {
            @Override
            public void run() {
                dialog.hide();
            }

        };

        showThread1.start();
        showThread2.start();

        Thread.sleep(1000);

        hideThread1.start();

        showThread1.join();
        showThread2.join();
        hideThread1.join();
    }

    /**
     * @throws InterruptedException
     * 
     */
    public void testShowHideShowHide() throws InterruptedException {
        XBayaEngine engine = new XBayaEngine(this.configuration);
        Cancelable cancelable = new Cancelable() {
            public void cancel() {
                // Nothing
            }
        };
        final WaitDialog dialog = new WaitDialog(cancelable, "title", "message", engine);
        Thread showThread1 = new Thread() {
            @Override
            public void run() {
                dialog.show();
            }

        };

        Thread showThread2 = new Thread() {
            @Override
            public void run() {
                dialog.show();
            }

        };

        Thread hideThread1 = new Thread() {
            @Override
            public void run() {
                dialog.hide();
            }

        };

        Thread hideThread2 = new Thread() {
            @Override
            public void run() {
                dialog.hide();
            }

        };

        showThread1.start();

        Thread.sleep(1000);

        hideThread1.start();

        Thread.sleep(1000);
        showThread2.start();

        Thread.sleep(1000);
        hideThread2.start();

        showThread1.join();
        showThread2.join();
        hideThread1.join();
        hideThread2.join();
    }

    /**
     * @throws InterruptedException
     */
    public void testShowHideHide() throws InterruptedException {
        XBayaEngine engine = new XBayaEngine(this.configuration);
        Cancelable cancelable = new Cancelable() {
            public void cancel() {
                // Nothing
            }
        };
        final WaitDialog dialog = new WaitDialog(cancelable, "title", "message", engine);
        Thread showThread1 = new Thread() {
            @Override
            public void run() {
                dialog.show();
            }

        };

        Thread hideThread1 = new Thread() {
            @Override
            public void run() {
                dialog.hide();
            }

        };

        Thread hideThread2 = new Thread() {
            @Override
            public void run() {
                dialog.hide();
            }

        };

        showThread1.start();

        Thread.sleep(1000);

        hideThread1.start();
        hideThread2.start();

        showThread1.join();
        hideThread1.join();
        hideThread2.join();
    }

    /**
     * @throws InterruptedException
     */
    public void testShowShowHideHide() throws InterruptedException {
        XBayaEngine engine = new XBayaEngine(this.configuration);
        Cancelable cancelable = new Cancelable() {
            public void cancel() {
                // Nothing
            }
        };
        final WaitDialog dialog = new WaitDialog(cancelable, "title", "message", engine);
        Thread showThread1 = new Thread() {
            @Override
            public void run() {
                dialog.show();
            }

        };

        Thread showThread2 = new Thread() {
            @Override
            public void run() {
                dialog.show();
            }

        };

        Thread hideThread1 = new Thread() {
            @Override
            public void run() {
                dialog.hide();
            }

        };

        Thread hideThread2 = new Thread() {
            @Override
            public void run() {
                dialog.hide();
            }

        };

        showThread1.start();
        showThread2.start();

        Thread.sleep(1000);

        hideThread1.start();
        hideThread2.start();

        showThread1.join();
        showThread2.join();
        hideThread1.join();
        hideThread2.join();
    }
}

/*
 * Indiana University Extreme! Lab Software License, Version 1.2
 * 
 * Copyright (c) 2007 The Trustees of Indiana University. All rights reserved.
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
