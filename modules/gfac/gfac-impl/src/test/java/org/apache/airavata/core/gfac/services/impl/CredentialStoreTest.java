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
///*
// *
// * Licensed to the Apache Software Foundation (ASF) under one
// * or more contributor license agreements.  See the NOTICE file
// * distributed with this work for additional information
// * regarding copyright ownership.  The ASF licenses this file
// * to you under the Apache License, Version 2.0 (the
// * "License"); you may not use this file except in compliance
// * with the License.  You may obtain a copy of the License at
// *
// *   http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing,
// * software distributed under the License is distributed on an
// * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// * KIND, either express or implied.  See the License for the
// * specific language governing permissions and limitations
// * under the License.
// *
//*/
//package org.apache.airavata.core.gfac.services.impl;
//
//import junit.framework.Assert;
//import org.apache.airavata.client.AiravataAPIFactory;
//import org.apache.airavata.client.api.AiravataAPI;
//import org.apache.airavata.client.api.exception.AiravataAPIInvocationException;
//import org.apache.airavata.common.exception.AiravataConfigurationException;
//import org.apache.airavata.common.exception.ApplicationSettingsException;
//import org.apache.airavata.common.utils.ClientSettings;
//import org.apache.airavata.common.utils.DBUtil;
//import org.apache.airavata.common.utils.ServerSettings;
//import org.apache.airavata.credential.store.credential.impl.ssh.SSHCredential;
//import org.apache.airavata.credential.store.store.CredentialReader;
//import org.apache.airavata.credential.store.store.impl.CredentialReaderImpl;
//import org.apache.airavata.gfac.core.GFacException;
//import org.apache.airavata.gfac.core.RequestData;
//import org.apache.airavata.gfac.ssh.security.TokenizedSSHAuthInfo;
//import org.apache.airavata.gfac.ssh.api.SSHApiException;
//import org.apache.airavata.gfac.ssh.api.ServerInfo;
//import org.apache.airavata.gfac.ssh.impl.HPCRemoteCluster;
//import org.apache.airavata.gfac.ssh.util.CommonUtils;
//import org.apache.airavata.registry.api.AiravataRegistry2;
//import org.apache.airavata.registry.api.AiravataRegistryFactory;
//import org.apache.airavata.registry.api.AiravataUser;
//import org.apache.airavata.registry.api.Gateway;
//import org.apache.airavata.registry.api.exception.RegAccessorInstantiateException;
//import org.apache.airavata.registry.api.exception.RegAccessorInvalidException;
//import org.apache.airavata.registry.api.exception.RegAccessorUndefinedException;
//import org.apache.airavata.registry.api.exception.RegException;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.testng.annotations.BeforeTest;
//import org.testng.annotations.Test;
//
//import java.util.UUID;
//
//public class CredentialStoreTest {
//    private final static Logger logger = LoggerFactory.getLogger(CredentialStoreTest.class);
//
//    @BeforeTest
//    public void testGSISSHProvider() throws GFacException, IllegalAccessException, ClassNotFoundException, InstantiationException, ApplicationSettingsException, SSHApiException {
//        System.setProperty("credential.store.keystore.url", "/Users/lahirugunathilake/Downloads/airavata_sym.jks");
//        System.setProperty("credential.store.keystore.alias", "airavata");
//        System.setProperty("credential.store.keystore.password", "airavata");
//        System.setProperty("myproxy.username", "ogce");
//        System.setProperty("myproxy.password", "");
//        System.setProperty("trusted.cert.location", "/Users/lahirugunathilake/Downloads/certificates");
//        System.setProperty("credential.store.jdbc.url","jdbc:mysql://gw85.iu.xsede.org:3306/airavata_gta_prod");
//        System.setProperty("credential.store.jdbc.user","gtaAiravataUser");
//        System.setProperty("credential.store.jdbc.password","gtaAiravataPWD");
//        System.setProperty("credential.store.jdbc.driver","com.mysql.jdbc.Driver");
//
//
//
//            UUID uuid = UUID.randomUUID();
//            System.out.println("TokenId: " + uuid.toString());
////            String publicKey = registry.createCredential("default",uuid.toString(),"lginnali" );
////            System.out.println("Public-Key: " +publicKey);
////            String tokenId = uuid.toString();
//            String tokenId = "2c308fa9-99f8-4baa-92e4-d062e311483c";
//            CredentialReader credentialReader = new CredentialReaderImpl(new DBUtil("jdbc:mysql://gw85.iu.xsede.org:3306/airavata_gta_prod",
//                    "ptaAiravataUser", "ptaAiravataPWD", "com.mysql.jdbc.Driver"));
//
//
//            RequestData requestData = new RequestData();
//            requestData.setMyProxyUserName("cgateway");
//            requestData.setTokenId(tokenId);
//            requestData.setGatewayId("default");
//            TokenizedSSHAuthInfo tokenizedSSHAuthInfo = new TokenizedSSHAuthInfo(credentialReader, requestData);
//
//            SSHCredential credentials = tokenizedSSHAuthInfo.getCredentials();
//            ServerInfo serverInfo = new ServerInfo("cgateway", "bigred2.uits.iu.edu");
//
//            HPCRemoteCluster pbsCluster = new HPCRemoteCluster(serverInfo, tokenizedSSHAuthInfo, CommonUtils.getPBSJobManager("/opt/torque/bin/"));
//            Assert.assertNotNull(pbsCluster);
//            return;
//
//    }
//
//    @Test
//    public static void main(String[] args) {
//        try {
//            new CredentialStoreTest().testGSISSHProvider();
//        } catch (GFacException e) {
//            e.printStackTrace();
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        } catch (InstantiationException e) {
//            e.printStackTrace();
//        } catch (ApplicationSettingsException e) {
//            e.printStackTrace();
//        } catch (SSHApiException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private static AiravataAPI getAiravataAPI() throws AiravataAPIInvocationException, ApplicationSettingsException {
//        AiravataAPI airavataAPI;
//        try {
//            String sysUser = ClientSettings.getSetting("admin");
//            String gateway = ClientSettings.getSetting("default");
//            airavataAPI = AiravataAPIFactory.getAPI(gateway, sysUser);
//        } catch (AiravataAPIInvocationException e) {
//            logger.error("Unable to create airavata API", e.getMessage());
//            throw new AiravataAPIInvocationException(e);
//        } catch (ApplicationSettingsException e) {
//            logger.error("Unable to create airavata API", e.getMessage());
//            throw new ApplicationSettingsException(e.getMessage());
//        }
//        return airavataAPI;
//    }
//
//}
