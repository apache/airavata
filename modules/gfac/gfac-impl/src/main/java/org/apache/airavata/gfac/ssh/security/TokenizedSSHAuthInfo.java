/**
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
 */
package org.apache.airavata.gfac.ssh.security;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.IOUtil;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.credential.store.credential.Credential;
import org.apache.airavata.credential.store.credential.impl.ssh.SSHCredential;
import org.apache.airavata.credential.store.store.CredentialReader;
import org.apache.airavata.gfac.core.GFacConstants;
import org.apache.airavata.gfac.core.GFacException;
import org.apache.airavata.gfac.core.RequestData;
import org.apache.airavata.gfac.core.GFacUtils;
import org.apache.airavata.gfac.core.authentication.SSHPublicKeyFileAuthentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Properties;

public class TokenizedSSHAuthInfo implements SSHPublicKeyFileAuthentication {
    protected static final Logger log = LoggerFactory.getLogger(TokenizedSSHAuthInfo.class);

    private String publicKeyFile;

    private String privateKeyFile;

    private String passPhrase = null;

    private SSHCredential gssCredentials = null;

    private CredentialReader credentialReader;

    private RequestData requestData;

    public TokenizedSSHAuthInfo(CredentialReader credentialReader, RequestData requestData) {
        this.credentialReader = credentialReader;
        this.requestData = requestData;
    }

    public TokenizedSSHAuthInfo(RequestData requestData) {
        this.requestData = requestData;
    }

    public String getPublicKeyFile(String userName, String hostName) {
        return publicKeyFile;
    }

    public String getPrivateKeyFile(String userName, String hostName) {
        return privateKeyFile;
    }

    public String getPassPhrase() {
        return passPhrase;
    }

    public void bannerMessage(String message) {

    }

    public SSHCredential getCredentials() throws SecurityException {

        if (gssCredentials == null) {

            try {
                gssCredentials = getCredentialsFromStore();
            } catch (Exception e) {
                log.error("An exception occurred while retrieving credentials from the credential store. " +
                        "Will continue with my proxy user name and password. Provided TokenId:" + requestData.getTokenId() + e.getMessage(), e);
            }

            if (gssCredentials == null) {
                System.out.println("Authenticating with provided token failed, so falling back to authenticate with defaultCredentials");
                try {
                    gssCredentials = getDefaultCredentials();
                } catch (Exception e) {
                    throw new SecurityException("Error retrieving my proxy using username password",e.getCause());
                }
            }
            // if still null, throw an exception
            if (gssCredentials == null) {
                throw new SecurityException("Unable to retrieve my proxy credentials to continue operation.");
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
    public SSHCredential getCredentialsFromStore() throws Exception {

        if (getCredentialReader() == null) {
            credentialReader = GFacUtils.getCredentialReader();
            if(credentialReader == null){
            	 return null;
            }
        }

        Credential credential = getCredentialReader().getCredential(getRequestData().getGatewayId(),
                getRequestData().getTokenId());

        if (credential instanceof SSHCredential) {
            SSHCredential credential1 = (SSHCredential) credential;
            this.publicKeyFile = writeFileToDisk(credential1.getPublicKey());
            this.privateKeyFile = writeFileToDisk(credential1.getPrivateKey());
            this.passPhrase = credential1.getPassphrase();
            System.out.println(this.publicKeyFile);
            System.out.println(this.privateKeyFile);
            System.out.println(this.passPhrase);
            this.getRequestData().setRequestUser(credential1.getPortalUserName());
            return credential1;
        } else {
            log.info("Could not find SSH credentials for token - " + getRequestData().getTokenId() + " and "
                    + "gateway id - " + getRequestData().getGatewayId());
        }

        return null;
    }

    /**
     * Gets the default proxy certificate.
     *
     * @return Default my proxy credentials.
     * @throws GFacException                            If an error occurred while retrieving credentials.
     * @throws org.apache.airavata.common.exception.ApplicationSettingsException
     */
    public SSHCredential getDefaultCredentials() throws GFacException, ApplicationSettingsException, IOException {
        Properties configurationProperties = ServerSettings.getProperties();
        String sshUserName = configurationProperties.getProperty(GFacConstants.SSH_USER_NAME);
        this.getRequestData().setRequestUser(sshUserName);
        this.privateKeyFile = configurationProperties.getProperty(GFacConstants.SSH_PRIVATE_KEY);
        this.publicKeyFile = configurationProperties.getProperty(GFacConstants.SSH_PUBLIC_KEY);
        this.passPhrase = configurationProperties.getProperty(GFacConstants.SSH_PRIVATE_KEY_PASS);
        this.getRequestData().setRequestUser(sshUserName);
        return new SSHCredential(IOUtil.readToByteArray(new File(this.privateKeyFile)), IOUtil.readToByteArray(new File(this.publicKeyFile)), this.passPhrase, requestData.getGatewayId(), sshUserName);
    }

    public CredentialReader getCredentialReader() {
        return credentialReader;
    }

    public RequestData getRequestData() {
        return requestData;
    }

    private String writeFileToDisk(byte[] data) {
        File temp = null;
        try {
            temp = File.createTempFile("id_rsa", "");
            //write it
            FileOutputStream bw = new FileOutputStream(temp);
            bw.write(data);
            bw.close();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return temp.getAbsolutePath();
    }
}
