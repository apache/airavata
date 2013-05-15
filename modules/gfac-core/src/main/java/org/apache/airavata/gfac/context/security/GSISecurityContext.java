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
package org.apache.airavata.gfac.context.security;

import java.util.Properties;

import org.apache.airavata.gfac.GFacException;
import org.apache.airavata.gfac.utils.MyProxyManager;
import org.globus.gsi.GlobusCredential;
import org.ietf.jgss.GSSCredential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles GRID related security.
 */
public class GSISecurityContext extends SecurityContext {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    /*
     * context name
     */
    public static final String GSI_SECURITY_CONTEXT = "gsi";

    private MyProxyManager myProxyManager;

    private GSSCredential gssCredentials;

    private GlobusCredential globusCredential;

    private String tokenId;

    private String gatewayId;

    private String gatewayUser;

    public GSISecurityContext() {
    }

    public GSISecurityContext(Properties configuration, String token, String gateway, String user) {
        this.tokenId = token;
        this.gatewayId = gateway;
        this.gatewayUser = user;
        myProxyManager = new MyProxyManager(configuration);
    }

    public GSISecurityContext(Properties configuration) throws GFacException {

        myProxyManager = new MyProxyManager(configuration);
    }

    public GSISecurityContext(String myProxyServer, String myProxyUserName, String myProxyPassword, int myProxyLifetime, String trustedCertLoc) {

        myProxyManager = new MyProxyManager(myProxyUserName, myProxyPassword,
                myProxyLifetime, myProxyServer, trustedCertLoc);
    }

    public GSSCredential getGssCredentials() throws SecurityException {

        GSSCredential credential = null;

        try {

            credential = this.myProxyManager.getCredentialsFromStore(gatewayId, tokenId);

        } catch (Exception e) {
            log.warn("An error occurred while retrieving credentials from credential store. " +
                    "But continuing with password credentials. ", e);
        }

        if (credential == null)
            return getGssCredentialsFromUserPassword();
        else
            return credential;
    }


    public GSSCredential getGssCredentialsFromUserPassword() throws SecurityException {
        try {
            if (gssCredentials == null || gssCredentials.getRemainingLifetime() < 10 * 90) {
                gssCredentials = myProxyManager.renewProxy();
            }
            return gssCredentials;
        } catch (Exception e) {
            throw new SecurityException(e.getMessage(), e);
        }
    }


    @SuppressWarnings("UnusedDeclaration")
    public GlobusCredential getGlobusCredential() {
        try {
            if (gssCredentials == null || gssCredentials.getRemainingLifetime() < 10 * 90) {
                globusCredential = myProxyManager.getGlobusCredential();
            }
            return globusCredential;
        } catch (Exception e) {
            throw new SecurityException(e.getMessage(), e);
        }

    }


    @SuppressWarnings("UnusedDeclaration")
    public String getGatewayUser() {
        return gatewayUser;
    }

    public MyProxyManager getMyProxyManager() {
        return myProxyManager;
    }
}
