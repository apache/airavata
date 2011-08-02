/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.apache.airavata.xregistry.utils;

import java.io.File;
import java.io.FileOutputStream;

import org.apache.airavata.xregistry.XregistryConstants;
import org.apache.airavata.xregistry.XregistryException;
import org.globus.gsi.GlobusCredential;
import org.globus.gsi.TrustedCertificates;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
import org.globus.myproxy.MyProxy;
import org.globus.myproxy.MyProxyException;
import org.ietf.jgss.GSSCredential;

import xsul.MLogger;
import xsul.xpola.util.XpolaUtil;

public class ProxyRenewer {

    private static MLogger log = MLogger.getLogger(XregistryConstants.LOGGER_NAME);

    private final String username;
    private final String password;
    private final int port;
    private final int lifetime;
    private final String hostname;
    private String trustedCertsLoc;

    public ProxyRenewer(final String username, final String password, final int port, final int lifetime, final String hostname) {
        super();
        this.username = username;
        this.password = password;
        this.port = port;
        this.lifetime = lifetime;
        this.hostname = hostname;
    }

    public ProxyRenewer(final String username, final String password, final int port,
            final int lifetime, final String hostname, String trustedCertsLoc) {
        super();
        this.username = username;
        this.password = password;
        this.port = port;
        this.lifetime = lifetime;
        this.hostname = hostname;
        this.trustedCertsLoc = trustedCertsLoc;
    }
    public ProxyRenewer(String username, String password, int lifetime) {
        super();
        this.username = username;
        this.password = password;
        this.lifetime = lifetime;
        hostname = "rainier.extreme.indiana.edu";
        this.port = MyProxy.DEFAULT_PORT;
    }
    private void init() {
		if(trustedCertsLoc != null){
    	TrustedCertificates certificates = TrustedCertificates.load(trustedCertsLoc);
		TrustedCertificates.setDefaultTrustedCertificates(certificates);
		}
	}

    public GSSCredential renewProxy() throws XregistryException{
    	init();
        try {
            String proxyloc = null;
            MyProxy myproxy = new MyProxy( hostname, port );
            GSSCredential proxy =
                myproxy.get(username, password, lifetime);


            GlobusCredential globusCred = null;
            if(proxy instanceof GlobusGSSCredentialImpl) {
                globusCred =
                    ((GlobusGSSCredentialImpl)proxy).getGlobusCredential();
                log.info("got proxy from myproxy for " + username
                              + " with " + lifetime + " lifetime.");

                if(proxyloc == null) {
                    String uid = XpolaUtil.getSysUserid();
                    log.info("uid: " + uid);
                    proxyloc = "/tmp/x509up_u" + uid;
                }
                log.info("proxy location: " + proxyloc);
                File proxyfile = new File(proxyloc);
                if(proxyfile.exists() == false) {
                    String dirpath = proxyloc.substring(0, proxyloc.lastIndexOf('/'));
                    File dir = new File(dirpath);
                    if(dir.exists() == false) {
                        dir.mkdirs();
                        log.info("new directory " + dirpath + " is created.");
                    }
                    proxyfile.createNewFile();
                    log.info("new proxy file " + proxyloc + " is created.");
                }
                FileOutputStream fout = new FileOutputStream(proxyfile);
                globusCred.save(fout);
                fout.close();
                Runtime.getRuntime().exec( "/bin/chmod 600 " + proxyloc );
                log.info("proxy file renewed");

                System.out.println("Proxy file renewed to "+ proxyloc + " for the user "+ username
                        + " with " + lifetime + " lifetime.");

            }
                return proxy;
        } catch (MyProxyException e) {
            throw new XregistryException(e);
        }catch (Exception e) {
            throw new XregistryException(e);
        }
    }


}

