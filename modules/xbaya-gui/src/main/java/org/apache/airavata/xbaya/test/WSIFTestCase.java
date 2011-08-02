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

import junit.framework.TestSuite;

import org.apache.airavata.xbaya.XBayaConstants;
import org.apache.airavata.xbaya.component.gui.ComponentTreeNode;
import org.apache.airavata.xbaya.xregistry.XRegistryComponent;

import xsul5.MLogger;

public class WSIFTestCase extends XBayaTestCase {

    private static final MLogger logger = MLogger.getLogger();

    private ComponentTreeNode resouceCatalogComponentTree;

    private ComponentTreeNode xRegistryComponentTree;

    /**
     * @param args
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(new TestSuite(WSIFTestCase.class));
    }

    /**
     * @see org.apache.airavata.xbaya.test.XBayaTestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        this.resouceCatalogComponentTree = null;
        this.xRegistryComponentTree = null;
    }

    /**
     * @throws InterruptedException
     */
    public void ttestXRegistry() throws InterruptedException {
        Thread xRegistryThread = new Thread() {
            @Override
            public void run() {
                try {
                    XRegistryComponent client = new XRegistryComponent(XBayaConstants.DEFAULT_XREGISTRY_URL,
                            XRegistryComponent.Type.ABSTRACT);
                    WSIFTestCase.this.xRegistryComponentTree = client.getComponentTree();
                } catch (Exception e) {
                    logger.caught(e);
                }
            }
        };

        xRegistryThread.start();

        // Join them otherwise the test finishes.
        xRegistryThread.join();

        assertNotNull(this.xRegistryComponentTree);
    }

    /**
     * @throws InterruptedException
     */
    public void testSynchronization() throws InterruptedException {

        Thread xRegistryThread = new Thread() {
            @Override
            public void run() {
                try {
                    XRegistryComponent client = new XRegistryComponent(XBayaConstants.DEFAULT_XREGISTRY_URL,
                            XRegistryComponent.Type.ABSTRACT);
                    WSIFTestCase.this.xRegistryComponentTree = client.getComponentTree();
                    System.err.println("ALEK c=" + WSIFTestCase.this.xRegistryComponentTree);
                } catch (Exception e) {
                    logger.caught(e);
                }
            }
        };

        xRegistryThread.start();

        System.err.println("ALEK2 c=" + WSIFTestCase.this.xRegistryComponentTree);
        System.err.println("ALEK2 r=" + WSIFTestCase.this.resouceCatalogComponentTree);

        // Join them otherwise the test finishes.
        xRegistryThread.join();

        System.err.println("ALEK3 c=" + WSIFTestCase.this.xRegistryComponentTree);
        System.err.println("ALEK3 r=" + WSIFTestCase.this.resouceCatalogComponentTree);

        assertNotNull(this.resouceCatalogComponentTree);
        assertNotNull(this.xRegistryComponentTree);
    }
}