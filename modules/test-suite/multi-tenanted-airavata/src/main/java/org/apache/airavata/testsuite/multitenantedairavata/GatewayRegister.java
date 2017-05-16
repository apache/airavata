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
package org.apache.airavata.testsuite.multitenantedairavata;


import org.apache.airavata.api.Airavata;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.DBUtil;
import org.apache.airavata.credential.store.credential.impl.ssh.SSHCredential;
import org.apache.airavata.credential.store.store.CredentialStoreException;
import org.apache.airavata.credential.store.store.impl.SSHCredentialWriter;
import org.apache.airavata.credential.store.util.TokenGenerator;
import org.apache.airavata.model.appcatalog.gatewayprofile.GatewayResourceProfile;
import org.apache.airavata.model.credential.store.CredentialOwnerType;
import org.apache.airavata.model.error.AiravataClientException;
import org.apache.airavata.model.error.AiravataSystemException;
import org.apache.airavata.model.error.InvalidRequestException;
import org.apache.airavata.model.security.AuthzToken;
import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.model.workspace.GatewayApprovalStatus;
import org.apache.airavata.model.workspace.Project;
import org.apache.airavata.testsuite.multitenantedairavata.utils.FrameworkUtils;
import org.apache.airavata.testsuite.multitenantedairavata.utils.PropertyFileType;
import org.apache.airavata.testsuite.multitenantedairavata.utils.PropertyReader;
import org.apache.airavata.testsuite.multitenantedairavata.utils.TestFrameworkConstants;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GatewayRegister {
    private final static Logger logger = LoggerFactory.getLogger(GatewayRegister.class);
    private Airavata.Client airavata;
    private PropertyReader propertyReader;
    private int gatewayCount;
    private Map<String, String> tokenMap;
    private Map<String, String> projectMap;
    private String testUser;
    private String testProject;
    private TestFrameworkProps properties;
    private AuthzToken authzToken;

    public GatewayRegister(Airavata.Client client, TestFrameworkProps props) throws Exception{
        try {
            this.airavata = client;
            this.tokenMap = new HashMap<String, String>();
            this.projectMap = new HashMap<String, String>();
            propertyReader = new PropertyReader();
            properties = props;
            testUser = properties.getTestUserName();
            testProject = properties.getTestProjectName();
            FrameworkUtils frameworkUtils = FrameworkUtils.getInstance();
            authzToken = new AuthzToken("emptyToken");
        }catch (Exception e){
            logger.error("Error while initializing setup step", e);
            throw new Exception("Error while initializing setup step", e);
        }
    }

    public void createGateway() throws Exception{
        try {
            // read gateway count from properties file
            List<GatewayResourceProfile> gateReourceProfiles = airavata.getAllGatewayResourceProfiles(authzToken);
            for(GatewayResourceProfile gatewayResourceProfile : gateReourceProfiles){
                if(gatewayResourceProfile.getGatewayID().equals(properties.getGname())){
                    createProject(gatewayResourceProfile.getGatewayID());
                    return;
                }
            }

            String genericGatewayName = properties.getGname();
            String genericGatewayDomain = properties.getGdomain();
            Gateway gateway = new Gateway();
            String gatewayId = genericGatewayName;
            gateway.setGatewayId(gatewayId);
            gateway.setGatewayName(gatewayId);
            gateway.setDomain(gatewayId + genericGatewayDomain);
            gateway.setGatewayApprovalStatus(GatewayApprovalStatus.APPROVED);
            airavata.addGateway(authzToken, gateway);
            String token = airavata.generateAndRegisterSSHKeys(authzToken, gatewayId, testUser, testUser, CredentialOwnerType.USER);
            GatewayResourceProfile gatewayResourceProfile = new GatewayResourceProfile();
            gatewayResourceProfile.setCredentialStoreToken(token);
            gatewayResourceProfile.setGatewayID(gatewayId);
            airavata.registerGatewayResourceProfile(authzToken, gatewayResourceProfile);
            createProject(gatewayId);

        } catch (AiravataSystemException e) {
            logger.error("Error while creating airavata client instance", e);
            throw new Exception("Error while creating airavata client instance", e);
        } catch (InvalidRequestException e) {
            logger.error("Invalid request for airavata client instance", e);
            throw new Exception("Invalid request for airavata client instance", e);
        } catch (AiravataClientException e) {
            logger.error("Error while creating airavata client instance", e);
            throw new Exception("Error while creating airavata client instance", e);
        } catch (TException e) {
            logger.error("Error while communicating with airavata client ", e);
            throw new Exception("Error while communicating with airavata client", e);
        }
    }

    public GatewayResourceProfile getGatewayResourceProfile() throws Exception{
        return airavata.getGatewayResourceProfile(authzToken, properties.getGname());

    }

    public Gateway getGateway(String gatewayId) throws Exception{
        Gateway gateway = airavata.getGateway(authzToken, gatewayId);
        return gateway;

    }

    public void createProject (String gatewayId) throws Exception{
        Project project = new Project();
        project.setGatewayId(gatewayId);
        project.setName(testProject);
        project.setOwner(testUser);
        String projectId = airavata.createProject(authzToken, gatewayId, project);
        projectMap.put(projectId, gatewayId);
    }

    public String writeToken() throws Exception{
        String tokenWriteLocation = properties.getTokenFileLoc();
        String fileName = tokenWriteLocation + File.separator + TestFrameworkConstants.CredentialStoreConstants.TOKEN_FILE_NAME;
        Gateway gateway = airavata.getGateway(authzToken, properties.getGname());
        PrintWriter tokenWriter = new PrintWriter(fileName, "UTF-8");
        String token = TokenGenerator.generateToken(gateway.getGatewayId(), null);
        tokenMap.put(gateway.getGatewayId(), token);
        tokenWriter.println(gateway.getGatewayId() + ":" + token);
        tokenWriter.close();
        return gateway.getGatewayId() + ":" + token +"\n";
    }

    public void registerSSHKeys () throws Exception{
        try {
            // write tokens to file
            String tokenWriteLocation = properties.getTokenFileLoc();
            String fileName = tokenWriteLocation + File.separator + TestFrameworkConstants.CredentialStoreConstants.TOKEN_FILE_NAME;

            PrintWriter tokenWriter = new PrintWriter(fileName, "UTF-8");
            // credential store related functions are not in the current api, so need to call credential store directly
            String jdbcURL = propertyReader.readProperty(TestFrameworkConstants.AiravataClientConstants.CS_JBDC_URL, PropertyFileType.AIRAVATA_SERVER);
            String jdbcDriver = propertyReader.readProperty(TestFrameworkConstants.AiravataClientConstants.CS_JBDC_DRIVER, PropertyFileType.AIRAVATA_SERVER);
            String userName = propertyReader.readProperty(TestFrameworkConstants.AiravataClientConstants.CS_DB_USERNAME, PropertyFileType.AIRAVATA_SERVER);
            String password = propertyReader.readProperty(TestFrameworkConstants.AiravataClientConstants.CS_DB_PWD, PropertyFileType.AIRAVATA_SERVER);
            String privateKeyPath = properties.getSshPrivateKeyLoc();
            String pubKeyPath = properties.getSshPubKeyLoc();
            String keyPassword = properties.getSshPassword();
            DBUtil dbUtil = new DBUtil(jdbcURL, userName, password, jdbcDriver);
            SSHCredentialWriter writer = new SSHCredentialWriter(dbUtil);
            Gateway gateway = airavata.getGateway(authzToken, properties.getGname());

            SSHCredential sshCredential = new SSHCredential();
            sshCredential.setGateway(gateway.getGatewayId());
            String token = TokenGenerator.generateToken(gateway.getGatewayId(), null);
            sshCredential.setToken(token);
            sshCredential.setPortalUserName(testUser);
            FileInputStream privateKeyStream = new FileInputStream(privateKeyPath);
            File filePri = new File(privateKeyPath);
            byte[] bFilePri = new byte[(int) filePri.length()];
            privateKeyStream.read(bFilePri);
            FileInputStream pubKeyStream = new FileInputStream(pubKeyPath);
            File filePub = new File(pubKeyPath);
            byte[] bFilePub = new byte[(int) filePub.length()];
            pubKeyStream.read(bFilePub);
            privateKeyStream.close();
            pubKeyStream.close();
            sshCredential.setPrivateKey(bFilePri);
            sshCredential.setPublicKey(bFilePub);
            sshCredential.setPassphrase(keyPassword);
            writer.writeCredentials(sshCredential);
            tokenMap.put(gateway.getGatewayId(), token);
            tokenWriter.println(gateway.getGatewayId() + ":" + token);

            tokenWriter.close();
        } catch (ClassNotFoundException e) {
            logger.error("Unable to find mysql driver", e);
            throw new Exception("Unable to find mysql driver",e);
        } catch (InstantiationException e) {
            logger.error("Error while saving SSH credentials", e);
            throw new Exception("Error while saving SSH credentials",e);
        } catch (IllegalAccessException e) {
            logger.error("Error while saving SSH credentials", e);
            throw new Exception("Error while saving SSH credentials",e);
        } catch (ApplicationSettingsException e) {
            logger.error("Unable to read airavata-client properties", e);
            throw new Exception("Unable to read airavata-client properties",e);
        } catch (AiravataSystemException e) {
            logger.error("Error occured while connecting with airavata client", e);
            throw new Exception("Error occured while connecting with airavata client",e);
        } catch (InvalidRequestException e) {
            logger.error("Error occured while connecting with airavata client", e);
            throw new Exception("Error occured while connecting with airavata client",e);
        } catch (AiravataClientException e) {
            logger.error("Error occured while connecting with airavata client", e);
            throw new Exception("Error occured while connecting with airavata client",e);
        } catch (TException e) {
            logger.error("Error occured while connecting with airavata client", e);
            throw new Exception("Error occured while connecting with airavata client",e);
        } catch (FileNotFoundException e) {
            logger.error("Could not find keys specified in the path", e);
            throw new Exception("Could not find keys specified in the path",e);
        }catch (CredentialStoreException e) {
            logger.error("Error while saving SSH credentials", e);
            throw new Exception("Error while saving SSH credentials",e);
        }
        catch (IOException e) {
            logger.error("Error while saving SSH credentials", e);
            throw new Exception("Error while saving SSH credentials",e);
        }
    }

    public int getGatewayCount() {
        return gatewayCount;
    }

    public void setGatewayCount(int gatewayCount) {
        this.gatewayCount = gatewayCount;
    }

    public Map<String, String> getTokenMap() {
        return tokenMap;
    }

    public void setTokenMap(Map<String, String> tokenMap) {
        this.tokenMap = tokenMap;
    }

    public Map<String, String> getProjectMap() {
        return projectMap;
    }

    public void setProjectMap(Map<String, String> projectMap) {
        this.projectMap = projectMap;
    }

    public Airavata.Client getAiravata() {
        return airavata;
    }

    public void setAiravata(Airavata.Client airavata) {
        this.airavata = airavata;
    }
}
