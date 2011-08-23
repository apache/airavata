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

package org.apache.airavata.core.gfac.context.impl;

import org.apache.airavata.core.gfac.context.SecurityContext;
import org.apache.airavata.core.gfac.context.impl.utils.MyProxyManager;
import org.apache.airavata.core.gfac.exception.GfacException;
import org.apache.airavata.core.gfac.exception.GfacException.FaultCode;
import org.globus.tools.MyProxy;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GSISecurityContext implements SecurityContext {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    private MyProxyManager proxyRenewer;

    private String myproxyUserName;

    private String myproxyPasswd;

    private String myproxyServer;

    private int myproxyLifetime;

    private String trustedCertLoc;

    private GSSCredential gssCredentails;

    public GSISecurityContext() {

    }

    public GSSCredential getGssCredentails() throws GfacException, GSSException {
        try {
            System.out.println(gssCredentails);
            if (gssCredentails == null || gssCredentails.getRemainingLifetime() < 10 * 90) {
                if (proxyRenewer != null) {
                    gssCredentails = proxyRenewer.renewProxy();
                } else if (myproxyUserName != null && myproxyPasswd != null && myproxyServer != null) {
                    this.proxyRenewer = new MyProxyManager(myproxyUserName, myproxyPasswd, MyProxy.MYPROXY_SERVER_PORT,
                            myproxyLifetime, myproxyServer, trustedCertLoc);
                    log.info("loaded credentails from Proxy server");
                    gssCredentails = this.proxyRenewer.renewProxy();
                }
            }
            return gssCredentails;
        } catch (Exception e) {
            throw new GfacException(e, FaultCode.ErrorAtDependentService);
        }
    }

    public String getMyproxyUserName() {
        return myproxyUserName;
    }

    public void setMyproxyUserName(String myproxyUserName) {
        this.myproxyUserName = myproxyUserName;
    }

    public String getMyproxyPasswd() {
        return myproxyPasswd;
    }

    public void setMyproxyPasswd(String myproxyPasswd) {
        this.myproxyPasswd = myproxyPasswd;
    }

    public String getMyproxyServer() {
        return myproxyServer;
    }

    public void setMyproxyServer(String myproxyServer) {
        this.myproxyServer = myproxyServer;
    }

    public int getMyproxyLifetime() {
        return myproxyLifetime;
    }

    public void setMyproxyLifetime(int myproxyLifetime) {
        this.myproxyLifetime = myproxyLifetime;
    }
}
