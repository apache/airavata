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
package org.apache.airavata.credential.store.cpi.SSHSummaryTest;

import junit.framework.Assert;
import org.apache.airavata.common.utils.DBUtil;
import org.apache.airavata.common.utils.DatabaseTestCases;
import org.apache.airavata.common.utils.DerbyUtil;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.credential.store.credential.impl.ssh.SSHCredential;
import org.apache.airavata.credential.store.store.impl.SSHCredentialWriter;
import org.apache.airavata.credential.store.store.impl.db.CredentialsDAO;
import org.apache.airavata.credential.store.util.TokenGenerator;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.sql.Connection;

/**
 * Created by abhandar on 10/24/16.
 */
public class SSHSummaryTest extends DatabaseTestCases {
    private static final Logger logger = LoggerFactory.getLogger(SSHSummaryTest.class);

    private CredentialsDAO credentialsDAO;

    private X509Certificate[] x509Certificates;
    private PrivateKey privateKey;

    @BeforeClass
    public static void setUpDatabase() throws Exception {
        DerbyUtil.startDerbyInServerMode(getHostAddress(), getPort(), getUserName(), getPassword());

        waitTillServerStarts();

        /*
         * String createTable = "CREATE TABLE CREDENTIALS\n" + "(\n" + "        GATEWAY_NAME VARCHAR(256) NOT NULL,\n" +
         * "        COMMUNITY_USER_NAME VARCHAR(256) NOT NULL,\n" + "        CREDENTIAL BLOB NOT NULL,\n" +
         * "        PRIVATE_KEY BLOB NOT NULL,\n" + "        NOT_BEFORE VARCHAR(256) NOT NULL,\n" +
         * "        NOT_AFTER VARCHAR(256) NOT NULL,\n" + "        LIFETIME INTEGER NOT NULL,\n" +
         * "        REQUESTING_PORTAL_USER_NAME VARCHAR(256) NOT NULL,\n" +
         * "        REQUESTED_TIME TIMESTAMP DEFAULT '0000-00-00 00:00:00',\n" +
         * "        PRIMARY KEY (GATEWAY_NAME, COMMUNITY_USER_NAME)\n" + ")";
         */
        // Adding description field as per pull request https://github.com/apache/airavata/pull/54
        String createTable = "CREATE TABLE CREDENTIALS\n" + "(\n"
                + "        GATEWAY_ID VARCHAR(256) NOT NULL,\n"
                + "        TOKEN_ID VARCHAR(256) NOT NULL,\n"
                + // Actual token used to identify the credential
                "        CREDENTIAL BLOB NOT NULL,\n" + "        PORTAL_USER_ID VARCHAR(256) NOT NULL,\n"
                + "        TIME_PERSISTED TIMESTAMP DEFAULT CURRENT_TIMESTAMP,\n"
                + "        DESCRIPTION VARCHAR(500),\n"
                + "        PRIMARY KEY (GATEWAY_ID, TOKEN_ID)\n" + ")";

        String dropTable = "drop table CREDENTIALS";

        try {
            executeSQL(dropTable);
        } catch (Exception e) {
        }

        executeSQL(createTable);

    }

    @AfterClass
    public static void shutDownDatabase() throws Exception {
        DerbyUtil.stopDerbyServer();
    }

    @Before
    public void setUp() throws Exception {

        credentialsDAO = new CredentialsDAO();

        x509Certificates = new X509Certificate[1];

        // Cleanup tables;
        Connection connection = getConnection();

        try {
            DBUtil.truncate("credentials", connection);
        } finally {
            connection.close();
        }

        initializeKeys();
    }

    private void initializeKeys() throws Exception {
        KeyStore ks = KeyStore.getInstance("JKS");
        char[] password = "password".toCharArray();

        String baseDirectory = System.getProperty("credential.module.directory");

        String keyStorePath = "src" + File.separator + "test" + File.separator + "resources" + File.separator
                + "keystore.jks";

        if (baseDirectory != null) {
            keyStorePath = baseDirectory + File.separator + keyStorePath;
        } else {
            keyStorePath = "modules" + File.separator + "credential-store" + File.separator + keyStorePath;
        }

        File keyStoreFile = new File(keyStorePath);
        if (!keyStoreFile.exists()) {
            logger.error("Unable to read keystore file " + keyStoreFile);
            throw new RuntimeException("Unable to read keystore file " + keyStoreFile);

        }

        java.io.FileInputStream fis = null;
        try {
            fis = new java.io.FileInputStream(keyStorePath);
            ks.load(fis, password);
        } finally {
            if (fis != null) {
                fis.close();
            }
        }

        fis.close();

        privateKey = (PrivateKey) ks.getKey("selfsigned", password);
        x509Certificates[0] = (X509Certificate) ks.getCertificate("selfsigned");

    }


//    @Test Change the properties in ServerProperties file and give the correct path to run the test
    public void testSSHSummary() throws Exception{
        try{
            String jdbcURL = ServerSettings.getCredentialStoreDBURL();
            String jdbcDriver = ServerSettings.getCredentialStoreDBDriver();
            String userName = ServerSettings.getCredentialStoreDBUser();
            String password = ServerSettings.getCredentialStoreDBPassword();
            String gatewayId = "phasta";
            String privateKeyPath = "/home/abhandar/Documents/Airavata/keys/id_rsa_airavata";
            String pubKeyPath = "/home/abhandar/Documents/Airavata/keys/id_rsa_airavata.pub";
            DBUtil dbUtil = new DBUtil(jdbcURL, userName, password, jdbcDriver);
            SSHCredentialWriter writer = new SSHCredentialWriter(dbUtil);
            SSHCredential sshCredential = new SSHCredential();
            sshCredential.setGateway(gatewayId);
            String token = TokenGenerator.generateToken(gatewayId, null);
            sshCredential.setToken(token);
            sshCredential.setPortalUserName("phasta");
            sshCredential.setDescription("dummy creds for testing");
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
            sshCredential.setPassphrase("ultrascan");
            writer.writeCredentials(sshCredential);
            Assert.assertEquals(token, sshCredential.getToken());
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }
}
