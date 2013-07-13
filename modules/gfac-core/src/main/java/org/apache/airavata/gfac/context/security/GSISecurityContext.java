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

import java.io.File;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Properties;

import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.credential.store.credential.Credential;
import org.apache.airavata.credential.store.credential.impl.certificate.CertificateCredential;
import org.apache.airavata.credential.store.store.CredentialReader;
import org.apache.airavata.gfac.AbstractSecurityContext;
import org.apache.airavata.gfac.Constants;
import org.apache.airavata.gfac.GFacException;
import org.apache.airavata.gfac.RequestData;
import org.globus.gsi.X509Credential;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
import org.globus.gsi.provider.GlobusProvider;
import org.globus.myproxy.GetParams;
import org.globus.myproxy.MyProxy;
import org.globus.myproxy.MyProxyException;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles GRID related security.
 */
public class GSISecurityContext extends AbstractSecurityContext {

    protected static final Logger log = LoggerFactory.getLogger(GSISecurityContext.class);
    /*
     * context name
     */
    public static final String GSI_SECURITY_CONTEXT = "gsi";

    public static int CREDENTIAL_RENEWING_THRESH_HOLD = 10 * 90;

    private GSSCredential gssCredentials = null;

    // Set trusted cert path and add provider
    static {
        Security.addProvider(new GlobusProvider());
        setUpTrustedCertificatePath();
    }

    public static void setUpTrustedCertificatePath(String trustedCertificatePath) {

        File file = new File(trustedCertificatePath);

        if (!file.exists() || !file.canRead()) {
            File f = new File(".");
            log.info("Current directory " + f.getAbsolutePath());
            throw new RuntimeException("Cannot read trusted certificate path " + trustedCertificatePath);
        } else {
            System.setProperty(Constants.TRUSTED_CERTIFICATE_SYSTEM_PROPERTY, file.getAbsolutePath());
        }
    }

    private static void setUpTrustedCertificatePath() {

        Properties properties = ServerSettings.getProperties();
        String trustedCertificatePath  = properties.getProperty(Constants.TRUSTED_CERT_LOCATION);

        setUpTrustedCertificatePath(trustedCertificatePath);
    }

    /**
     * Gets the trusted certificate path. Trusted certificate path is stored in "X509_CERT_DIR"
     * system property.
     * @return The trusted certificate path as a string.
     */
    public static String getTrustedCertificatePath() {
        return System.getProperty(Constants.TRUSTED_CERTIFICATE_SYSTEM_PROPERTY);
    }


    public GSISecurityContext(CredentialReader credentialReader, RequestData requestData) {
        super(credentialReader, requestData);
    }

    /**
     * Gets GSSCredentials. The process is as follows;
     * If credentials were queried for the first time create credentials.
     *   1. Try creating credentials using certificates stored in the credential store
     *   2. If 1 fails use user name and password to create credentials
     * If credentials are already created check the remaining life time of the credential. If
     * remaining life time is less than CREDENTIAL_RENEWING_THRESH_HOLD, then renew credentials.
     * @return GSSCredentials to be used.
     * @throws GFacException If an error occurred while creating credentials.
     */
    public GSSCredential getGssCredentials() throws GFacException {

        if (gssCredentials == null) {

            try {
                gssCredentials = getCredentialsFromStore();
            } catch (Exception e) {
                log.error("An exception occurred while retrieving credentials from the credential store. " +
                        "Will continue with my proxy user name and password.", e);
            }

            // If store does not have credentials try to get from user name and password
            if (gssCredentials == null) {
                gssCredentials = getDefaultCredentials();
            }

            // if still null, throw an exception
            if (gssCredentials == null) {
                throw new GFacException("Unable to retrieve my proxy credentials to continue operation.");
            }
        } else {
            try {
                if (gssCredentials.getRemainingLifetime() < CREDENTIAL_RENEWING_THRESH_HOLD) {
                    return renewCredentials();
                }
            } catch (GSSException e) {
                throw new GFacException("Unable to retrieve remaining life time from credentials.", e);
            }
        }

        return gssCredentials;
    }

    /**
     * Renews credentials. First try to renew credentials as a trusted renewer. If that failed
     * use user name and password to renew credentials.
     * @return Renewed credentials.
     * @throws GFacException If an error occurred while renewing credentials.
     */
    public GSSCredential renewCredentials() throws GFacException {

        // First try to renew credentials as a trusted renewer
        try {
            gssCredentials = renewCredentialsAsATrustedHost();
        } catch (Exception e) {
            log.warn("Renewing credentials as a trusted renewer failed", e);
            gssCredentials = getProxyCredentials();
        }

        return gssCredentials;
    }

    /**
     * Reads the credentials from credential store.
     * @return If token is found in the credential store, will return a valid credential. Else returns null.
     * @throws Exception If an error occurred while retrieving credentials.
     */
    public GSSCredential getCredentialsFromStore() throws Exception {

        if (getCredentialReader() == null) {
            return null;
        }

        Credential credential = getCredentialReader().getCredential(getRequestData().getGatewayId(),
                getRequestData().getTokenId());

        if (credential != null) {
            if (credential instanceof CertificateCredential) {

                log.info("Successfully found credentials for token id - " + getRequestData().getTokenId() +
                        " gateway id - " + getRequestData().getGatewayId());

                CertificateCredential certificateCredential = (CertificateCredential) credential;

                X509Certificate[] certificates = new X509Certificate[1];
                certificates[0] = certificateCredential.getCertificate();

                X509Credential newCredential = new X509Credential(certificateCredential.getPrivateKey(), certificates);

                return new GlobusGSSCredentialImpl(newCredential,
                        GSSCredential.INITIATE_AND_ACCEPT);
            } else {
                log.info("Credential type is not CertificateCredential. Cannot create mapping globus credentials. " +
                        "Credential type - " + credential.getClass().getName());
            }
        } else {
            log.info("Could not find credentials for token - " + getRequestData().getTokenId() + " and "
                    + "gateway id - " + getRequestData().getGatewayId());
        }

        return null;
    }

    /**
     * Gets the default proxy certificate.
     * @return Default my proxy credentials.
     * @throws GFacException If an error occurred while retrieving credentials.
     */
    public GSSCredential getDefaultCredentials() throws GFacException{
        MyProxy myproxy = new MyProxy(getRequestData().getMyProxyServerUrl(), getRequestData().getMyProxyPort());
        try {
            return myproxy.get(getRequestData().getMyProxyUserName(), getRequestData().getMyProxyPassword(),
                    getRequestData().getMyProxyLifeTime());
        } catch (MyProxyException e) {
            throw new GFacException("An error occurred while retrieving default security credentials.", e);
        }
    }

    /**
     * Gets a new proxy certificate given current credentials.
     * @return The short lived GSSCredentials
     * @throws GFacException If an error is occurred while retrieving credentials.
     */
    public GSSCredential getProxyCredentials() throws GFacException {

        MyProxy myproxy = new MyProxy(getRequestData().getMyProxyServerUrl(), getRequestData().getMyProxyPort());
        try {
            return myproxy.get(gssCredentials, getRequestData().getMyProxyUserName(), getRequestData().getMyProxyPassword(),
                    getRequestData().getMyProxyLifeTime());
        } catch (MyProxyException e) {
            throw new GFacException("An error occurred while renewing security credentials using user/password.", e);
        }
    }

    /**
     * Renew GSSCredentials.
     * Before executing we need to add current host as a trusted renewer. Note to renew credentials
     * we dont need user name and password.
     * To do that execute following command
     * > myproxy-logon -t <LIFETIME></LIFETIME> -s <MY PROXY SERVER> -l <USER NAME>
     * E.g :- > myproxy-logon -t 264 -s myproxy.teragrid.org -l us3
     *          Enter MyProxy pass phrase:
     *          A credential has been received for user us3 in /tmp/x509up_u501.
     * > myproxy-init -A --cert /tmp/x509up_u501 --key /tmp/x509up_u501 -l ogce -s myproxy.teragrid.org
     * @return  Renewed credentials.
     * @throws GFacException If an error occurred while renewing credentials.
     */
    public GSSCredential renewCredentialsAsATrustedHost() throws GFacException {
        MyProxy myproxy = new MyProxy(getRequestData().getMyProxyServerUrl(), getRequestData().getMyProxyPort());

        GetParams getParams = new GetParams();
        getParams.setAuthzCreds(gssCredentials);
        getParams.setUserName(getRequestData().getMyProxyUserName());
        getParams.setLifetime(getRequestData().getMyProxyLifeTime());

        try {
            return myproxy.get(gssCredentials, getParams);
        } catch (MyProxyException e) {
            throw new GFacException("An error occurred while renewing security credentials.", e);
        }
    }
}
