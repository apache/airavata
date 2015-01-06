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
package org.apache.airavata.gfac.gsissh.security;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.credential.store.credential.Credential;
import org.apache.airavata.credential.store.credential.impl.certificate.CertificateCredential;
import org.apache.airavata.credential.store.store.CredentialReader;
import org.apache.airavata.gfac.Constants;
import org.apache.airavata.gfac.GFacException;
import org.apache.airavata.gfac.RequestData;
import org.apache.airavata.gfac.core.utils.GFacUtils;
import org.apache.airavata.gsi.ssh.api.authentication.AuthenticationInfo;
import org.apache.airavata.gsi.ssh.api.authentication.GSIAuthenticationInfo;
import org.globus.gsi.X509Credential;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
import org.globus.gsi.provider.GlobusProvider;
import org.globus.myproxy.GetParams;
import org.globus.myproxy.MyProxy;
import org.globus.myproxy.MyProxyException;
import org.gridforum.jgss.ExtendedGSSCredential;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.security.Security;
import java.security.cert.X509Certificate;

public class TokenizedMyProxyAuthInfo extends GSIAuthenticationInfo {
    protected static final Logger log = LoggerFactory.getLogger(TokenizedMyProxyAuthInfo.class);

    public static int CREDENTIAL_RENEWING_THRESH_HOLD = 10 * 90;

    private GSSCredential gssCredentials = null;


    private CredentialReader credentialReader;

    private RequestData requestData;

    public static final String X509_CERT_DIR = "X509_CERT_DIR";


    static {
        Security.addProvider(new GlobusProvider());
        try {
            setUpTrustedCertificatePath();
        } catch (ApplicationSettingsException e) {
            log.error(e.getLocalizedMessage(), e);
        }
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

    private static void setUpTrustedCertificatePath() throws ApplicationSettingsException {

        String trustedCertificatePath = ServerSettings.getSetting(Constants.TRUSTED_CERT_LOCATION);

        setUpTrustedCertificatePath(trustedCertificatePath);
    }

    public TokenizedMyProxyAuthInfo(CredentialReader credentialReader, RequestData requestData) {
        this.credentialReader = credentialReader;
        this.requestData = requestData;
        try {
            properties.setProperty(X509_CERT_DIR, ServerSettings.getSetting(Constants.TRUSTED_CERT_LOCATION));
        } catch (ApplicationSettingsException e) {
            log.error("Error while  reading server properties", e);
        };
    }

    public TokenizedMyProxyAuthInfo(RequestData requestData) {
           this.requestData = requestData;
           try {
               properties.setProperty(X509_CERT_DIR, ServerSettings.getSetting(Constants.TRUSTED_CERT_LOCATION));
           } catch (ApplicationSettingsException e) {
               log.error("Error while  reading server properties", e);
           };
       }

    public GSSCredential getCredentials() throws SecurityException {

        if (gssCredentials == null) {

            try {
                gssCredentials = getCredentialsFromStore();
            } catch (Exception e) {
                log.error("An exception occurred while retrieving credentials from the credential store. " +
                        "Will continue with my proxy user name and password. Provided TokenId:" + requestData.getTokenId(), e);
            }

            if (gssCredentials == null) {
                System.out.println("Authenticating with provided token failed, so falling back to authenticate with defaultCredentials");
                try {
                    gssCredentials = getDefaultCredentials();
                } catch (Exception e) {
                    throw new SecurityException("Error retrieving my proxy using username password");
                }
            }
            // if still null, throw an exception
            if (gssCredentials == null) {
                throw new SecurityException("Unable to retrieve my proxy credentials to continue operation.");
            }
        } else {
            try {
                if (gssCredentials.getRemainingLifetime() < CREDENTIAL_RENEWING_THRESH_HOLD) {
                    try {
                        return renewCredentials();
                    } catch (Exception e) {
                        throw new SecurityException("Error renewing credentials", e);
                    }
                }
            } catch (GSSException e) {
                throw new SecurityException("Unable to retrieve remaining life time from credentials.", e);
            }
        }

        return gssCredentials;
    }


    /**
     * Reads the credentials from credential store.
     *
     * @return If token is found in the credential store, will return a valid credential. Else returns null.
     * @throws Exception If an error occurred while retrieving credentials.
     */
    public GSSCredential getCredentialsFromStore() throws Exception {

        if (getCredentialReader() == null) {
        	credentialReader = GFacUtils.getCredentialReader();
        	if(credentialReader == null){
        		return null;
        	}
        }

        Credential credential = getCredentialReader().getCredential(getRequestData().getGatewayId(),
                getRequestData().getTokenId());

        if (credential != null) {
            if (credential instanceof CertificateCredential) {

                log.info("Successfully found credentials for token id - " + getRequestData().getTokenId() +
                        " gateway id - " + getRequestData().getGatewayId());

                CertificateCredential certificateCredential = (CertificateCredential) credential;

                X509Certificate[] certificates = certificateCredential.getCertificates();
                X509Credential newCredential = new X509Credential(certificateCredential.getPrivateKey(), certificates);

                GlobusGSSCredentialImpl cred = new GlobusGSSCredentialImpl(newCredential, GSSCredential.INITIATE_AND_ACCEPT);
                System.out.print(cred.export(ExtendedGSSCredential.IMPEXP_OPAQUE));
                return cred;
                //return new GlobusGSSCredentialImpl(newCredential,
                //        GSSCredential.INITIATE_AND_ACCEPT);
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
     * Renew GSSCredentials.
     * Before executing we need to add current host as a trusted renewer. Note to renew credentials
     * we dont need user name and password.
     * To do that execute following command
     * > myproxy-logon -t <LIFETIME></LIFETIME> -s <MY PROXY SERVER> -l <USER NAME>
     * E.g :- > myproxy-logon -t 264 -s myproxy.teragrid.org -l us3
     * Enter MyProxy pass phrase:
     * A credential has been received for user us3 in /tmp/x509up_u501.
     * > myproxy-init -A --cert /tmp/x509up_u501 --key /tmp/x509up_u501 -l ogce -s myproxy.teragrid.org
     *
     * @return Renewed credentials.
     * @throws org.apache.airavata.gfac.GFacException                            If an error occurred while renewing credentials.
     * @throws org.apache.airavata.common.exception.ApplicationSettingsException
     */
    public GSSCredential renewCredentialsAsATrustedHost() throws GFacException, ApplicationSettingsException {
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


    /**
     * Gets the default proxy certificate.
     *
     * @return Default my proxy credentials.
     * @throws org.apache.airavata.gfac.GFacException                            If an error occurred while retrieving credentials.
     * @throws org.apache.airavata.common.exception.ApplicationSettingsException
     */
    public GSSCredential getDefaultCredentials() throws GFacException, ApplicationSettingsException {
        MyProxy myproxy = new MyProxy(getRequestData().getMyProxyServerUrl(), getRequestData().getMyProxyPort());
        try {
            return myproxy.get(getRequestData().getMyProxyUserName(), getRequestData().getMyProxyPassword(),
                    getRequestData().getMyProxyLifeTime());
        } catch (MyProxyException e) {
            throw new GFacException("An error occurred while retrieving default security credentials.", e);
        }
    }


    /**
     * Renews credentials. First try to renew credentials as a trusted renewer. If that failed
     * use user name and password to renew credentials.
     *
     * @return Renewed credentials.
     * @throws org.apache.airavata.gfac.GFacException                            If an error occurred while renewing credentials.
     * @throws org.apache.airavata.common.exception.ApplicationSettingsException
     */
    public GSSCredential renewCredentials() throws GFacException, ApplicationSettingsException {

        // First try to renew credentials as a trusted renewer
        try {
            gssCredentials = renewCredentialsAsATrustedHost();
        } catch (Exception e) {
            log.warn("Renewing credentials as a trusted renewer failed", e);
            gssCredentials = getDefaultCredentials();
        }

        return gssCredentials;
    }

    /**
     * Gets a new proxy certificate given current credentials.
     *
     * @return The short lived GSSCredentials
     * @throws org.apache.airavata.gfac.GFacException                            If an error is occurred while retrieving credentials.
     * @throws org.apache.airavata.common.exception.ApplicationSettingsException
     */
    public GSSCredential getProxyCredentials() throws GFacException, ApplicationSettingsException {

        MyProxy myproxy = new MyProxy(getRequestData().getMyProxyServerUrl(), getRequestData().getMyProxyPort());
        try {
            return myproxy.get(gssCredentials, getRequestData().getMyProxyUserName(), getRequestData().getMyProxyPassword(),
                    getRequestData().getMyProxyLifeTime());
        } catch (MyProxyException e) {
            throw new GFacException("An error occurred while renewing security credentials using user/password.", e);
        }
    }

    public void setGssCredentials(GSSCredential gssCredentials) {
        this.gssCredentials = gssCredentials;
    }

    public CredentialReader getCredentialReader() {
        return credentialReader;
    }

    public void setCredentialReader(CredentialReader credentialReader) {
        this.credentialReader = credentialReader;
    }

    public RequestData getRequestData() {
        return requestData;
    }

    public void setRequestData(RequestData requestData) {
        this.requestData = requestData;
    }
}
