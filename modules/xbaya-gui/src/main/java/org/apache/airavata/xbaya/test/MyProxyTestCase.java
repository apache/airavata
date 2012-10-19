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

import org.apache.airavata.xbaya.myproxy.MyProxyClient;
import org.globus.myproxy.MyProxyException;
import org.ietf.jgss.GSSCredential;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MyProxyTestCase extends XBayaTestCase {

    private static final Log logger = LogFactory.getLog(MyProxyTestCase.class);

    private static String username = System.getProperty("username");

    private static String passphrase = System.getProperty("passphrase");

    /**
     * @param args
     */
    public static void main(String args[]) {
        if (args.length != 2) {
            System.out.println("Usage: java " + MyProxyTestCase.class.getName() + " username passphrase");
            return;
        }
        username = args[0];
        passphrase = args[1];
        junit.textui.TestRunner.run(new TestSuite(MyProxyTestCase.class));
    }

    /**
     * @throws MyProxyException
     */
    public void test() throws MyProxyException {
        if (username == null) {
            // skip the test.
            return;
        }
        String server = this.configuration.getMyProxyServer();
        int port = this.configuration.getMyProxyPort();
        int lifetime = this.configuration.getMyProxyLifetime();

        MyProxyClient client = new MyProxyClient();
        client.load(server, port, username, passphrase, lifetime);
        GSSCredential proxy = client.getProxy();
        logger.info("proxy: " + proxy);
    }
}