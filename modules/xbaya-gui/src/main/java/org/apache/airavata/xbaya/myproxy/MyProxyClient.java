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

package org.apache.airavata.xbaya.myproxy;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;

import org.apache.airavata.xbaya.security.XBayaSecurity;
import org.globus.gsi.TrustedCertificates;
import org.globus.myproxy.MyProxy;
import org.globus.myproxy.MyProxyException;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MyProxyClient {

    private static final Log logger = LogFactory.getLog(MyProxyClient.class);

    private boolean initialized = false;

    private X509Certificate[] trustedCertificates;

    private String server;

    private int port;

    private String username;

    private String passphrase;

    private int lifetime;

    private MyProxy myproxy;

    private GSSCredential proxy;

    /**
     * Constructs a MyProxyClient.
     */
    public MyProxyClient() {
        // Nothing
    }

    /**
     * Constructs a MyProxyClient.
     * 
     * @param myproxyServer
     * @param myproxyPort
     * @param myproxyUsername
     * @param myproxyPassphrase
     * @param myproxyLifetime
     */
    public MyProxyClient(String myproxyServer, int myproxyPort, String myproxyUsername, String myproxyPassphrase,
            int myproxyLifetime) {
        set(myproxyServer, myproxyPort, myproxyUsername, myproxyPassphrase, myproxyLifetime);
    }

    /**
     * @return The array of trusted certificates.
     */
    public X509Certificate[] getTrustedCertificates() {
        if (!this.initialized) {
            init();
        }
        return this.trustedCertificates;
    }

    /**
     * @param myproxyServer
     * @param myproxyPort
     * @param myproxyUsername
     * @param myproxyPassphrase
     * @param myproxyLifetime
     */
    public void set(String myproxyServer, int myproxyPort, String myproxyUsername, String myproxyPassphrase,
            int myproxyLifetime) {
        this.server = myproxyServer;
        this.port = myproxyPort;
        this.username = myproxyUsername;
        this.passphrase = myproxyPassphrase;
        this.lifetime = myproxyLifetime;
    }

    /**
     * Returns the lifetime.
     * 
     * @return The lifetime
     */
    public int getLifetime() {
        return this.lifetime;
    }

    /**
     * Returns the password.
     * 
     * @return The password
     */
    public String getPassphrase() {
        return this.passphrase;
    }

    /**
     * Returns the port.
     * 
     * @return The port
     */
    public int getPort() {
        return this.port;
    }

    /**
     * Returns the server.
     * 
     * @return The server
     */
    public String getServer() {
        return this.server;
    }

    /**
     * Returns the username.
     * 
     * @return The username
     */
    public String getUsername() {
        return this.username;
    }

    /**
     * @return The proxy. null if it's not loaded.
     */
    public GSSCredential getProxy() {
        return this.proxy;
    }

    /**
     * @param myproxyServer
     * @param myproxyPort
     * @param myproxyUsername
     * @param myproxyPassphrase
     * @param myproxyLifetime
     * @throws MyProxyException
     * @throws IOException
     * @throws GeneralSecurityException
     */
    public void load(String myproxyServer, int myproxyPort, String myproxyUsername, String myproxyPassphrase,
            int myproxyLifetime) throws MyProxyException {
        set(myproxyServer, myproxyPort, myproxyUsername, myproxyPassphrase, myproxyLifetime);
        load();
    }

    /**
     * @throws MyProxyException
     * @throws IOException
     * @throws GeneralSecurityException
     */
    public void load() throws MyProxyException {
        if (this.server == null || this.server.length() == 0) {
            // Should not happen
            throw new IllegalStateException();
        }
        if (this.port == 0) {
            // Should not happen
            throw new IllegalStateException();
        }
        if (this.username == null || this.username.length() == 0) {
            // Should not happen
            throw new IllegalStateException();
        }
        if (this.passphrase == null || this.passphrase.length() == 0) {
            // Should not happen
            throw new IllegalStateException();
        }
        if (!this.initialized) {
            init();
        }

        this.myproxy = new MyProxy(this.server, this.port);
        this.proxy = this.myproxy.get(this.username, this.passphrase, this.lifetime);
    }

    /**
     * @return true if a proxy has been loaded and has enough lifetime ramained; false otherwise.
     */
    public boolean isProxyValid() {
        if (this.proxy == null) {
            return false;
        } else {
            try {
                int remainingLifetime = this.proxy.getRemainingLifetime();
                if (remainingLifetime > 60 * 10 /* 10 mins */) {
                    // Good case.
                    return true;
                } else {
                    return false;
                }
            } catch (GSSException e) {
                logger.error(e.getMessage(), e);
                return false;
            }
        }
    }

    private void init() {
        // Load CA certificates from a file included in the XBaya jar.
        this.trustedCertificates = XBayaSecurity.getTrustedCertificates();

        TrustedCertificates certificatesArray = new TrustedCertificates(this.trustedCertificates);
        TrustedCertificates.setDefaultTrustedCertificates(certificatesArray);
    }
}