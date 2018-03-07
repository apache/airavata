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
 */
package org.apache.airavata.helix.agent.storage;

import com.jcraft.jsch.Session;
import org.apache.airavata.agents.api.AgentException;
import org.apache.airavata.agents.api.StorageResourceAdaptor;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.DBUtil;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.credential.store.credential.Credential;
import org.apache.airavata.credential.store.credential.impl.ssh.SSHCredential;
import org.apache.airavata.credential.store.store.CredentialStoreException;
import org.apache.airavata.credential.store.store.impl.CredentialReaderImpl;
import org.apache.airavata.helix.agent.ssh.SshAdaptorParams;
import org.apache.airavata.helix.agent.ssh.SshAgentAdaptor;
import org.apache.airavata.model.appcatalog.storageresource.StorageResourceDescription;
import org.apache.airavata.registry.core.experiment.catalog.impl.RegistryFactory;
import org.apache.airavata.registry.cpi.AppCatalog;
import org.apache.airavata.registry.cpi.AppCatalogException;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class StorageResourceAdaptorImpl extends SshAgentAdaptor implements StorageResourceAdaptor  {

    private static final Logger logger = LogManager.getLogger(SshAgentAdaptor.class);

    private Session session = null;
    private AppCatalog appCatalog;

    @Override
    public void init(String storageResourceId, String gatewayId, String loginUser, String token) throws AgentException {

        try {
            logger.info("Initializing Storage Resource Adaptor for storage resource : "+ storageResourceId + ", gateway : " +
                    gatewayId +", user " + loginUser + ", token : " + token);
            this.appCatalog = RegistryFactory.getAppCatalog();
            StorageResourceDescription storageResource = appCatalog.getStorageResource().getStorageResource(storageResourceId);
            String hostName = storageResource.getHostName();

            String jdbcUrl = ServerSettings.getCredentialStoreDBURL();
            String jdbcUsr = ServerSettings.getCredentialStoreDBUser();
            String jdbcPass = ServerSettings.getCredentialStoreDBPassword();
            String driver = ServerSettings.getCredentialStoreDBDriver();
            CredentialReaderImpl credentialReader = new CredentialReaderImpl(new DBUtil(jdbcUrl, jdbcUsr, jdbcPass, driver));

            logger.info("Fetching credentials for cred store token " + token);

            Credential credential = credentialReader.getCredential(gatewayId, token);

            if (credential instanceof SSHCredential) {
                SSHCredential sshCredential = SSHCredential.class.cast(credential);
                SshAdaptorParams adaptorParams = new SshAdaptorParams();
                adaptorParams.setHostName(storageResource.getHostName());
                adaptorParams.setUserName(loginUser);
                adaptorParams.setPassphrase(sshCredential.getPassphrase());
                adaptorParams.setPrivateKey(sshCredential.getPrivateKey());
                adaptorParams.setPublicKey(sshCredential.getPublicKey());
                adaptorParams.setStrictHostKeyChecking(false);
                init(adaptorParams);
            }

        } catch (AppCatalogException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (CredentialStoreException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (ApplicationSettingsException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void uploadFile(String sourceFile, String destFile) throws AgentException {
        super.copyFileTo(sourceFile, destFile);
    }

    @Override
    public void downloadFile(String sourceFile, String destFile) throws AgentException {
        super.copyFileFrom(sourceFile, destFile);
    }
}
