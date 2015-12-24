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
package org.apache.airavata.file.manager.core.remote.client.gridftp.myproxy;

import java.io.*;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import org.apache.log4j.Logger;
import org.globus.gsi.X509Credential;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
import org.globus.myproxy.GetParams;
import org.globus.myproxy.MyProxy;
import org.ietf.jgss.GSSCredential;

/**
 * Class to manipulate my proxy credentials. Responsible for retrieving, creating
 * my proxy credentials.
 */
public class MyProxyCredentials implements Serializable {

    private static final long serialVersionUID = -2471014486509046212L;
    protected String myProxyHostname;
    protected String myProxyUserName;
    protected String myProxyPassword;
    protected int myProxyPortNumber;
    protected int myProxyLifeTime = 14400;
    protected String trustedCertificatePath;

    private static final Logger log = Logger.getLogger(MyProxyCredentials.class);

    /**
     * Default constructor.
     */
    public MyProxyCredentials() {
    }

    /**
     * Constructor.
     * @param myProxyServer Ip address of the my proxy server.
     * @param myProxyPort Port which my proxy server is running.
     * @param myProxyUsername User name to connect to my proxy server.
     * @param myProxyPassPhrase Password for my proxy authentication.
     * @param myProxyLifetime Lifetime of the retrieving credentials.
     * @param trustedCerts Trusted certificate location for SSL communication.
     */
    public MyProxyCredentials(String myProxyServer, int myProxyPort, String myProxyUsername, String myProxyPassPhrase,
                              int myProxyLifetime, String trustedCerts) {

        this.myProxyHostname = myProxyServer;
        this.myProxyPortNumber = myProxyPort;
        this.myProxyUserName = myProxyUsername;
        this.myProxyPassword = myProxyPassPhrase;
        this.myProxyLifeTime = myProxyLifetime;
        this.trustedCertificatePath = trustedCerts;

        init();

    }

    /**
     * Gets the default proxy certificate.
     * @return Default my proxy credentials.
     * @throws Exception If an error occurred while retrieving credentials.
     */
    public GSSCredential getDefaultCredentials() throws Exception {
        MyProxy myproxy = new MyProxy(this.myProxyHostname, this.myProxyPortNumber);
        return myproxy.get(this.myProxyUserName, this.myProxyPassword, this.myProxyLifeTime);
    }

    /**
     * Gets a new proxy certificate given current credentials.
     * @param credential The new proxy credentials.
     * @return The short lived GSSCredentials
     * @throws Exception If an error is occurred while retrieving credentials.
     */
    public GSSCredential getProxyCredentials(GSSCredential credential) throws Exception {

        MyProxy myproxy = new MyProxy(this.myProxyHostname, this.myProxyPortNumber);
        return myproxy.get(credential, this.myProxyUserName, this.myProxyPassword, this.myProxyLifeTime);
    }

    /**
     * Renew GSSCredentials.
     * @param credential Credentials to be renewed.
     * @return  Renewed credentials.
     * @throws Exception If an error occurred while renewing credentials.
     */
    public GSSCredential renewCredentials(GSSCredential credential) throws Exception {
        MyProxy myproxy = new MyProxy(this.myProxyHostname, this.myProxyPortNumber);

        GetParams getParams = new GetParams();
        getParams.setAuthzCreds(credential);
        getParams.setUserName(this.myProxyUserName);
        getParams.setLifetime(this.getMyProxyLifeTime());

        return myproxy.get(credential, getParams);
    }

    public GSSCredential createCredentials(X509Certificate[] x509Certificates, PrivateKey privateKey) throws Exception {
        X509Credential newCredential = new X509Credential(privateKey,
                x509Certificates);

        return new GlobusGSSCredentialImpl(newCredential,
                GSSCredential.INITIATE_AND_ACCEPT);

    }

    public GSSCredential createCredentials(X509Certificate x509Certificate, PrivateKey privateKey) throws Exception {

        X509Certificate[] x509Certificates = new X509Certificate[1];
        x509Certificates[0] = x509Certificate;

        return createCredentials(x509Certificates, privateKey);

    }

    public void init() {
        validateTrustedCertificatePath();
    }

    private void validateTrustedCertificatePath() {

        File file = new File(this.trustedCertificatePath);

        if (!file.exists() || !file.canRead()) {
            File f = new File(".");
            System.out.println("Current directory " + f.getAbsolutePath());
            throw new RuntimeException("Cannot read trusted certificate path " + this.trustedCertificatePath);
        } else {
            System.setProperty("X509_CERT_DIR", file.getAbsolutePath());
        }
    }


    /**
     * @return the myProxyHostname
     */
    public String getMyProxyHostname() {
        return myProxyHostname;
    }

    /**
     * @param myProxyHostname the myProxyHostname to set
     */
    public void setMyProxyHostname(String myProxyHostname) {
        this.myProxyHostname = myProxyHostname;
    }

    /**
     * @return the myProxyUserName
     */
    public String getMyProxyUserName() {
        return myProxyUserName;
    }

    /**
     * @param myProxyUserName the myProxyUserName to set
     */
    public void setMyProxyUserName(String myProxyUserName) {
        this.myProxyUserName = myProxyUserName;
    }

    /**
     * @return the myProxyPassword
     */
    public String getMyProxyPassword() {
        return myProxyPassword;
    }

    /**
     * @param myProxyPassword the myProxyPassword to set
     */
    public void setMyProxyPassword(String myProxyPassword) {
        this.myProxyPassword = myProxyPassword;
    }

    /**
     * @return the myProxyLifeTime
     */
    public int getMyProxyLifeTime() {
        return myProxyLifeTime;
    }

    /**
     * @param myProxyLifeTime the myProxyLifeTime to set
     */
    public void setMyProxyLifeTime(int myProxyLifeTime) {
        this.myProxyLifeTime = myProxyLifeTime;
    }

    /**
     * @return the myProxyPortNumber
     */
    public int getMyProxyPortNumber() {
        return myProxyPortNumber;
    }

    /**
     * @param myProxyPortNumber the myProxyPortNumber to set
     */
    public void setMyProxyPortNumber(int myProxyPortNumber) {
        this.myProxyPortNumber = myProxyPortNumber;
    }

    public String getTrustedCertificatePath() {
        return trustedCertificatePath;
    }

    public void setTrustedCertificatePath(String trustedCertificatePath) {
        this.trustedCertificatePath = trustedCertificatePath;
    }

    /**
     * @return the portalUserName
     */
    /*public String getPortalUserName() {
        return portalUserName;
    }*/

    /**
     * @param portalUserName
     *            the portalUserName to set
     */
    /*public void setPortalUserName(String portalUserName) {
        this.portalUserName = portalUserName;
    }*/

    /**
     * Returns the initialized.
     *
     * @return The initialized
     */
    /*public boolean isInitialized() {
        return this.initialized;
    }*/

    /**
     * Sets initialized.
     *
     * @param initialized
     *            The initialized to set.
     */
   /* public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }*/

    /**
     * @param hostcertsKeyFile
     *            the hostcertsKeyFile to set
     */
    /*public void setHostcertsKeyFile(String hostcertsKeyFile) {
        this.hostcertsKeyFile = hostcertsKeyFile;
    }*/

    /**
     * @return the hostcertsKeyFile
     */
    /*public String getHostcertsKeyFile() {
        return hostcertsKeyFile;
    }*/

    /**
     * @param trustedCertsFile
     *            the trustedCertsFile to set
     */
    /*public void setTrustedCertsFile(String trustedCertsFile) {
        this.trustedCertsFile = trustedCertsFile;
    }*/

    /**
     * @return the trustedCertsFile
     */
    /*public String getTrustedCertsFile() {
        return trustedCertsFile;
    }*/

}