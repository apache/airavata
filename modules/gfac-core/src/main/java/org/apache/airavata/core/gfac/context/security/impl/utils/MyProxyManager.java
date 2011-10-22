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

package org.apache.airavata.core.gfac.context.security.impl.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

import org.globus.gsi.GlobusCredential;
import org.globus.gsi.TrustedCertificates;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
import org.globus.myproxy.MyProxy;
import org.globus.myproxy.MyProxyException;
import org.ietf.jgss.GSSCredential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyProxyManager {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final String username;
    private final String password;
    private final int port;
    private final int lifetime;
    private final String hostname;
    private String trustedCertsLoc;

    public MyProxyManager(final String username, final String password, final int port, final int lifetime,
            final String hostname) {
        this.username = username;
        this.password = password;
        this.port = port;
        this.lifetime = lifetime;
        this.hostname = hostname;
    }

    public MyProxyManager(final String username, final String password, final int port, final int lifetime,
            final String hostname, String trustedCertsLoc) {
        this.username = username;
        this.password = password;
        this.port = port;
        this.lifetime = lifetime;
        this.hostname = hostname;
        this.trustedCertsLoc = trustedCertsLoc;
    }

    private void init() {
        if (trustedCertsLoc != null) {
            TrustedCertificates certificates = TrustedCertificates.load(trustedCertsLoc);
            TrustedCertificates.setDefaultTrustedCertificates(certificates);
        }
    }

    public GSSCredential renewProxy() throws MyProxyException, IOException {

        init();
        String proxyloc = null;
        MyProxy myproxy = new MyProxy(hostname, port);
        GSSCredential proxy = myproxy.get(username, password, lifetime);
        GlobusCredential globusCred = null;
        if (proxy instanceof GlobusGSSCredentialImpl) {
            globusCred = ((GlobusGSSCredentialImpl) proxy).getGlobusCredential();
            log.info("got proxy from myproxy for " + username + " with " + lifetime + " lifetime.");
            String uid = username;
            // uid = XpolaUtil.getSysUserid();
            log.info("uid: " + uid);
            proxyloc = "/tmp/x509up_u" + uid + UUID.randomUUID().toString();
            log.info("proxy location: " + proxyloc);
            File proxyfile = new File(proxyloc);
            if (!proxyfile.exists()) {
                String dirpath = proxyloc.substring(0, proxyloc.lastIndexOf('/'));
                File dir = new File(dirpath);
                if (!dir.exists()) {
                    if (dir.mkdirs()) {
                        log.info("new directory " + dirpath + " is created.");
                    } else {
                        log.error("error in creating directory " + dirpath);
                    }
                }
                proxyfile.createNewFile();
                log.info("new proxy file " + proxyloc + " is created.");
            }
            FileOutputStream fout = null;
            try {
                fout = new FileOutputStream(proxyfile);
                globusCred.save(fout);
            } finally {
                if (fout != null) {
                    fout.close();
                }
            }
            Runtime.getRuntime().exec("/bin/chmod 600 " + proxyloc);
            log.info("Proxy file renewed to " + proxyloc + " for the user " + username + " with " + lifetime
                    + " lifetime.");

        }
        return proxy;
    }

}
