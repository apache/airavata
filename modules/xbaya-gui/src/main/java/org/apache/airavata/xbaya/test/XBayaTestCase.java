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

import java.io.File;
import java.net.URI;

import junit.framework.TestCase;

import org.apache.airavata.xbaya.XBayaConfiguration;
import org.slf4j.Logger;

public abstract class XBayaTestCase extends TestCase {


    protected XBayaConfiguration configuration;

    protected File temporalDirectory;

    /**
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();

        this.configuration = new XBayaConfiguration();

        // tmp directory
        this.temporalDirectory = new File("tmp/");
        this.temporalDirectory.mkdir();

        // topic
        if (this.configuration.getTopic() == null) {
            this.configuration.setTopic("xbaya-test");
        }

        // Overwrite some default setting.
        String gpelURLString = System.getProperty("gpel.url");
        if (gpelURLString != null) {
            this.configuration.setGPELEngineURL(URI.create(gpelURLString));
        }
        String dscURLString = System.getProperty("dsc.url");
        if (dscURLString != null) {
            this.configuration.setDSCURL(URI.create(dscURLString));
        }

        String myProxyUsername = System.getProperty("myproxy.username");
        if (myProxyUsername != null) {
            this.configuration.setMyProxyUsername(myProxyUsername);
        }
        String myProxyPassphrase = System.getProperty("myproxy.passphrase");
        if (myProxyPassphrase != null) {
            this.configuration.setMyProxyPassphrase(myProxyPassphrase);
        }

    }
}