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
package org.apache.airavata.gfac.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.Properties;
import java.util.UUID;

import org.apache.airavata.common.utils.DBUtil;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.credential.store.credential.Credential;
import org.apache.airavata.credential.store.credential.impl.certificate.CertificateCredential;
import org.apache.airavata.credential.store.store.CredentialReader;
import org.apache.airavata.credential.store.store.CredentialReaderFactory;
import org.apache.airavata.gfac.Constants;
import org.globus.gsi.GlobusCredential;
import org.globus.gsi.TrustedCertificates;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
import org.globus.myproxy.MyProxy;
import org.globus.myproxy.MyProxyException;
import org.ietf.jgss.GSSCredential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages communication with MyProxy. Does all the authentications.
 */
public class MyProxyManager {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final String username;
    private final String password;
    private final int port;
    private final int lifetime;
    private final String hostname;
    private String trustedCertsLoc;

    private CredentialReader credentialReader;

    public MyProxyManager(Properties configuration) {

        this.username = configuration.getProperty(Constants.MYPROXY_USER);
        this.hostname = configuration.getProperty(Constants.MYPROXY_SERVER);
        this.password = configuration.getProperty(Constants.MYPROXY_PASS);
        this.lifetime = Integer.parseInt(configuration.getProperty(Constants.MYPROXY_LIFE));
        this.trustedCertsLoc = configuration.getProperty(Constants.TRUSTED_CERT_LOCATION);

        String strPort = configuration.getProperty(Constants.MYPROXY_SERVER_PORT);

        if (strPort != null) {
            this.port = Integer.parseInt(strPort);
        } else {
            this.port = org.globus.tools.MyProxy.MYPROXY_SERVER_PORT;
        }

        init();

    }

    @SuppressWarnings("UnusedDeclaration")
    public MyProxyManager(final String username, final String password, final int port, final int lifetime,
            final String hostname) throws MyProxyException {
        this.username = username;
        this.password = password;
        this.port = port;
        this.lifetime = lifetime;
        this.hostname = hostname;

        init();
    }

    public MyProxyManager(final String username, final String password, final int lifetime,
            final String hostname, String trustedCertsLoc) {
        this.username = username;
        this.password = password;
        this.port = org.globus.tools.MyProxy.MYPROXY_SERVER_PORT;
        this.lifetime = lifetime;
        this.hostname = hostname;
        this.trustedCertsLoc = trustedCertsLoc;

        init();
    }

    private void init() {
        if (trustedCertsLoc != null) {
            TrustedCertificates certificates = TrustedCertificates.load(trustedCertsLoc);
            TrustedCertificates.setDefaultTrustedCertificates(certificates);
        }

        initCredentialStoreReader();
    }

    private void initCredentialStoreReader() {
        try {
            String dbUser = ServerSettings.getCredentialStoreDBUser();
            String password = ServerSettings.getCredentialStoreDBPassword();
            String dbUrl = ServerSettings.getCredentialStoreDBURL();
            String driver = ServerSettings.getCredentialStoreDBDriver();

            DBUtil dbUtil = new DBUtil(dbUrl, dbUser, password, driver);

            credentialReader = CredentialReaderFactory.createCredentialStoreReader(dbUtil);

        } catch (Exception e) {
            credentialReader = null;
            log.error("Unable initialize credential store connection.");
            log.warn("Continuing operations with password based my-proxy configurations");
        }
    }
    
    // not thread safe
    public GSSCredential renewProxy() throws MyProxyException, IOException {
        init();
        
        String proxyLocation = null;
        MyProxy myproxy = new MyProxy(hostname, port);
        GSSCredential proxy = myproxy.get(username, password, lifetime);
        GlobusCredential globusCred = null;
        if (proxy instanceof GlobusGSSCredentialImpl) {
            globusCred = ((GlobusGSSCredentialImpl) proxy).getGlobusCredential();
            log.debug("got proxy from myproxy for " + username + " with " + lifetime + " lifetime.");
            String uid = username;
            // uid = XpolaUtil.getSysUserid();
            log.debug("uid: " + uid);
            proxyLocation = "/tmp/x509up_u" + uid + UUID.randomUUID().toString();
            log.debug("proxy location: " + proxyLocation);
            File proxyfile = new File(proxyLocation);
            if (!proxyfile.exists()) {
                String dirpath = proxyLocation.substring(0, proxyLocation.lastIndexOf('/'));
                File dir = new File(dirpath);
                if (!dir.exists()) {
                    if (dir.mkdirs()) {
                        log.debug("new directory " + dirpath + " is created.");
                    } else {
                        log.error("error in creating directory " + dirpath);
                    }
                }

                if (!proxyfile.createNewFile()) {
                    log.error("Unable to create proxy file. File - " + proxyfile.getAbsolutePath());
                } else {
                    log.debug("new proxy file " + proxyLocation + " is created. File - " + proxyfile.getAbsolutePath());
                }
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
            Runtime.getRuntime().exec("/bin/chmod 600 " + proxyLocation);
            log.info("Proxy file renewed to " + proxyLocation + " for the user " + username + " with " + lifetime
                    + " lifetime.");
        }
        
        return proxy;
    }
    
    // this should be reused by the above method
    public GlobusCredential getGlobusCredential() throws Exception{
		init();
		String proxyloc = null;
		MyProxy myproxy = new MyProxy(hostname, port);
		GSSCredential proxy = myproxy.get(username, password, lifetime);
		GlobusCredential globusCred = null;
		if (proxy instanceof GlobusGSSCredentialImpl) {
			globusCred = ((GlobusGSSCredentialImpl) proxy)
					.getGlobusCredential();
			log.debug("got proxy from myproxy for " + username + " with "
					+ lifetime + " lifetime.");
		}

		return globusCred;
    }

    /**
     * Reads the credentials from credential store.
     * @param gatewayId The gateway id.
     * @param tokenId The token id associated with the credential.
     * @return If token is found in the credential store, will return a valid credential. Else returns null.
     * @throws Exception If an error occurred while retrieving credentials.
     */
    public GSSCredential getCredentialsFromStore(String gatewayId, String tokenId) throws Exception {

        if (credentialReader == null) {
            return null;
        }

        Credential credential = credentialReader.getCredential(gatewayId, tokenId);

        if (credential != null) {
            if (credential instanceof CertificateCredential) {

                log.info("Successfully found credentials for token id - " + tokenId +
                            " gateway id - " + gatewayId);

                CertificateCredential certificateCredential = (CertificateCredential) credential;

                X509Certificate[] certificates = new X509Certificate[1];
                certificates[0] = certificateCredential.getCertificate();

                //TODO suspecting about the certificate chain .... need to sort that out
                GlobusCredential newCredential = new GlobusCredential(certificateCredential.getPrivateKey(),
                        certificates);

                return new GlobusGSSCredentialImpl(newCredential,
                        GSSCredential.INITIATE_AND_ACCEPT);
            } else {
                log.info("Credential type is not CertificateCredential. Cannot create mapping globus credentials. " +
                        "Credential type - " + credential.getClass().getName());
            }
        } else {
            log.info("Could not find credentials for token - " + tokenId + " and "
                        + "gateway id - " + gatewayId);
        }

        return null;

    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public int getPort() {
        return port;
    }

    public int getLifetime() {
        return lifetime;
    }

    public String getHostname() {
        return hostname;
    }

    public String getTrustedCertsLoc() {
        return trustedCertsLoc;
    }

    public void setTrustedCertsLoc(String trustedCertsLoc) {
        this.trustedCertsLoc = trustedCertsLoc;
    }

    public CredentialReader getCredentialReader() {
        return credentialReader;
    }

    public void setCredentialReader(CredentialReader credentialReader) {
        this.credentialReader = credentialReader;
    }
}
