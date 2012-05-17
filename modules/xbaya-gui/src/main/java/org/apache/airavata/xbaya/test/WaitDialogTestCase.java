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

import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.ui.Cancelable;
import org.apache.airavata.xbaya.ui.WaitDialog;

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