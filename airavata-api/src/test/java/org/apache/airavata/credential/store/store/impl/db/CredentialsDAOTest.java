/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.airavata.credential.store.store.impl.db;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.apache.airavata.common.utils.DBUtil;
import org.apache.airavata.common.utils.DatabaseTestCases;
import org.apache.airavata.common.utils.DerbyUtil;
import org.apache.airavata.common.utils.KeyStorePasswordCallback;
import org.apache.airavata.credential.store.credential.CommunityUser;
import org.apache.airavata.credential.store.credential.CredentialOwnerType;
import org.apache.airavata.credential.store.credential.impl.certificate.CertificateCredential;
import org.apache.airavata.credential.store.repository.CredentialsRepository;
import org.apache.airavata.credential.store.store.CredentialStoreException;
import org.apache.airavata.credential.store.utils.CredentialSerializationUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test class for credential repository
 */
public class CredentialsDAOTest extends DatabaseTestCases {

    private static final Logger logger = LoggerFactory.getLogger(CredentialsDAOTest.class);

    private CredentialsRepository credentialsRepository;

    private X509Certificate[] x509Certificates;
    private PrivateKey privateKey;

    @BeforeAll
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
                "        CREDENTIAL BLOB NOT NULL,\n"
                + "        PORTAL_USER_ID VARCHAR(256) NOT NULL,\n"
                + "        TIME_PERSISTED TIMESTAMP DEFAULT CURRENT_TIMESTAMP,\n"
                + "        DESCRIPTION VARCHAR(500),\n"
                + "        CREDENTIAL_OWNER_TYPE VARCHAR(10) DEFAULT 'GATEWAY' NOT NULL,\n"
                + "        PRIMARY KEY (GATEWAY_ID, TOKEN_ID)\n" + ")";

        String dropTable = "drop table CREDENTIALS";

        try {
            executeSQL(dropTable);
        } catch (Exception e) {
        }

        executeSQL(createTable);
    }

    @AfterAll
    public static void shutDownDatabase() throws Exception {
        DerbyUtil.stopDerbyServer();
    }

    @BeforeEach
    public void setUp() throws Exception {

        credentialsRepository = new CredentialsRepository();

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

        String keyStorePath =
                "src" + File.separator + "test" + File.separator + "resources" + File.separator + "airavata.p12";

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

        privateKey = (PrivateKey) ks.getKey("selfsigned", password);
        x509Certificates[0] = (X509Certificate) ks.getCertificate("selfsigned");
    }

    @Test
    public void testKeyReading() throws Exception {
        initializeKeys();
        System.out.println(privateKey.getAlgorithm());
        System.out.println(x509Certificates[0].getIssuerDN());

        assertNotNull(privateKey);
        assertNotNull(x509Certificates);
    }

    private CommunityUser getCommunityUser(String gateway, String name) {
        return new CommunityUser(gateway, name, "amila@sciencegateway.org");
    }

    private void addTestCredentials() throws Exception {

        Connection connection = getConnection();

        try {
            CertificateCredential certificateCredential = getTestCredentialObject();

            // Create credentials entity
            CredentialsEntity credentialsEntity = new CredentialsEntity();
            credentialsEntity.setGatewayId(
                    certificateCredential.getCommunityUser().getGatewayName());
            credentialsEntity.setTokenId(certificateCredential.getToken());
            credentialsEntity.setPortalUserId(certificateCredential.getPortalUserName());
            credentialsEntity.setTimePersisted(new Timestamp(new Date().getTime()));
            credentialsEntity.setDescription("Test credential");
            credentialsEntity.setCredentialOwnerType(certificateCredential.getCredentialOwnerType());

            // Serialize and encrypt the credential
            byte[] serializedCredential =
                    CredentialSerializationUtils.serializeCredentialWithEncryption(certificateCredential);
            credentialsEntity.setCredential(serializedCredential);

            credentialsRepository.create(credentialsEntity);

            // Add second credential
            certificateCredential.setToken("tom2");
            credentialsEntity = new CredentialsEntity();
            credentialsEntity.setGatewayId(
                    certificateCredential.getCommunityUser().getGatewayName());
            credentialsEntity.setTokenId(certificateCredential.getToken());
            credentialsEntity.setPortalUserId(certificateCredential.getPortalUserName());
            credentialsEntity.setTimePersisted(new Timestamp(new Date().getTime()));
            credentialsEntity.setDescription("Test credential 2");
            credentialsEntity.setCredentialOwnerType(certificateCredential.getCredentialOwnerType());

            // Serialize and encrypt the credential
            serializedCredential =
                    CredentialSerializationUtils.serializeCredentialWithEncryption(certificateCredential);
            credentialsEntity.setCredential(serializedCredential);

            credentialsRepository.create(credentialsEntity);

        } finally {
            connection.close();
        }
    }

    public CertificateCredential getTestCredentialObject() {

        CertificateCredential certificateCredential = new CertificateCredential();
        certificateCredential.setToken("tom");
        certificateCredential.setCertificates(x509Certificates);
        certificateCredential.setPrivateKey(privateKey);
        certificateCredential.setCommunityUser(getCommunityUser("gw1", "tom"));
        certificateCredential.setLifeTime(1000);
        certificateCredential.setPortalUserName("jerry");
        certificateCredential.setNotBefore("13 OCT 2012 5:34:23");
        certificateCredential.setNotAfter("14 OCT 2012 5:34:23");
        certificateCredential.setCredentialOwnerType(CredentialOwnerType.GATEWAY);

        return certificateCredential;
    }

    @Test
    public void testSerialization() throws CredentialStoreException {

        CertificateCredential certificateCredential = getTestCredentialObject();

        // Test serialization and deserialization
        byte[] array = CredentialSerializationUtils.serializeCredentialWithEncryption(certificateCredential);
        CertificateCredential readCertificateCredential =
                (CertificateCredential) CredentialSerializationUtils.deserializeCredentialWithDecryption(array);

        checkEquality(certificateCredential.getCertificates(), readCertificateCredential.getCertificates());
        assertEquals(
                certificateCredential.getCertificateRequestedTime(),
                readCertificateCredential.getCertificateRequestedTime());
        assertEquals(
                certificateCredential.getCommunityUser().getGatewayName(),
                readCertificateCredential.getCommunityUser().getGatewayName());
        assertEquals(
                certificateCredential.getCommunityUser().getUserEmail(),
                readCertificateCredential.getCommunityUser().getUserEmail());
        assertEquals(
                certificateCredential.getCommunityUser().getUserName(),
                readCertificateCredential.getCommunityUser().getUserName());
        assertEquals(certificateCredential.getLifeTime(), readCertificateCredential.getLifeTime());
        assertEquals(certificateCredential.getNotAfter(), readCertificateCredential.getNotAfter());
        assertEquals(certificateCredential.getNotBefore(), readCertificateCredential.getNotBefore());
        assertEquals(certificateCredential.getPortalUserName(), readCertificateCredential.getPortalUserName());
        assertEquals(
                certificateCredential.getCredentialOwnerType(), readCertificateCredential.getCredentialOwnerType());

        PrivateKey newKey = readCertificateCredential.getPrivateKey();

        assertNotNull(newKey);
        assertEquals(privateKey.getClass(), newKey.getClass());

        assertEquals(privateKey.getFormat(), newKey.getFormat());
        assertEquals(privateKey.getAlgorithm(), newKey.getAlgorithm());
        assertTrue(Arrays.equals(privateKey.getEncoded(), newKey.getEncoded()));
    }

    @Test
    public void testSerializationWithEncryption() throws CredentialStoreException, URISyntaxException {

        URI uri = this.getClass().getClassLoader().getResource("airavata.p12").toURI();
        String secretKeyAlias = "mykey";

        assert uri != null;

        CertificateCredential certificateCredential = getTestCredentialObject();

        // Test encryption with custom keystore
        byte[] array = CredentialSerializationUtils.serializeCredentialWithEncryption(
                certificateCredential, uri.getPath(), secretKeyAlias, new TestACSKeyStoreCallback());
        CertificateCredential readCertificateCredential =
                (CertificateCredential) CredentialSerializationUtils.deserializeCredentialWithDecryption(
                        array, uri.getPath(), secretKeyAlias, new TestACSKeyStoreCallback());

        checkEquality(certificateCredential.getCertificates(), readCertificateCredential.getCertificates());
        assertEquals(
                certificateCredential.getCertificateRequestedTime(),
                readCertificateCredential.getCertificateRequestedTime());
        assertEquals(
                certificateCredential.getCommunityUser().getGatewayName(),
                readCertificateCredential.getCommunityUser().getGatewayName());
        assertEquals(
                certificateCredential.getCommunityUser().getUserEmail(),
                readCertificateCredential.getCommunityUser().getUserEmail());
        assertEquals(
                certificateCredential.getCommunityUser().getUserName(),
                readCertificateCredential.getCommunityUser().getUserName());
        assertEquals(certificateCredential.getLifeTime(), readCertificateCredential.getLifeTime());
        assertEquals(certificateCredential.getNotAfter(), readCertificateCredential.getNotAfter());
        assertEquals(certificateCredential.getNotBefore(), readCertificateCredential.getNotBefore());
        assertEquals(certificateCredential.getPortalUserName(), readCertificateCredential.getPortalUserName());
        assertEquals(
                certificateCredential.getCredentialOwnerType(), readCertificateCredential.getCredentialOwnerType());

        PrivateKey newKey = readCertificateCredential.getPrivateKey();

        assertNotNull(newKey);
        assertEquals(privateKey.getClass(), newKey.getClass());

        assertEquals(privateKey.getFormat(), newKey.getFormat());
        assertEquals(privateKey.getAlgorithm(), newKey.getAlgorithm());
        assertTrue(Arrays.equals(privateKey.getEncoded(), newKey.getEncoded()));
    }

    private class TestACSKeyStoreCallback implements KeyStorePasswordCallback {

        @Override
        public char[] getStorePassword() {
            return "airavata".toCharArray();
        }

        @Override
        public char[] getSecretKeyPassPhrase(String keyAlias) {
            if (keyAlias.equals("mykey")) {
                return "airavatasecretkey".toCharArray();
            }

            return null;
        }
    }

    private void checkEquality(X509Certificate[] certificates1, X509Certificate[] certificates2) {

        int i = 0;

        for (X509Certificate certificate : certificates1) {
            assertEquals(certificate, certificates2[i]);
        }

        assertEquals(certificates1.length, certificates2.length);
    }

    @Test
    public void testAddCredentials() throws Exception {

        addTestCredentials();

        Connection connection = getConnection();

        try {
            List<CredentialsEntity> credentialsEntities = credentialsRepository.findByGatewayIdAndTokenId("gw1", "tom");
            assertNotNull(credentialsEntities);
            assertFalse(credentialsEntities.isEmpty());

            CredentialsEntity credentialsEntity = credentialsEntities.get(0);
            assertEquals("jerry", credentialsEntity.getPortalUserId());
            assertEquals("gw1", credentialsEntity.getGatewayId());

            // Test deserialized credential content
            CertificateCredential certificateCredential = (CertificateCredential)
                    CredentialSerializationUtils.deserializeCredentialWithDecryption(credentialsEntity.getCredential());
            checkEquality(x509Certificates, certificateCredential.getCertificates());
            assertEquals(
                    privateKey.getFormat(),
                    certificateCredential.getPrivateKey().getFormat());

        } finally {
            connection.close();
        }
    }

    @Test
    public void testDeleteCredentials() throws Exception {

        addTestCredentials();

        Connection connection = getConnection();

        try {
            List<CredentialsEntity> credentialsEntities = credentialsRepository.findByGatewayIdAndTokenId("gw1", "tom");
            assertNotNull(credentialsEntities);
            assertFalse(credentialsEntities.isEmpty());

            credentialsRepository.delete(new CredentialsEntity.CredentialsPK("gw1", "tom"));

            credentialsEntities = credentialsRepository.findByGatewayIdAndTokenId("gw1", "tom");
            assertTrue(credentialsEntities.isEmpty());

        } finally {
            connection.close();
        }
    }

    @Test
    public void testUpdateCredentials() throws Exception {

        addTestCredentials();

        Connection connection = getConnection();

        try {
            List<CredentialsEntity> credentialsEntities = credentialsRepository.findByGatewayIdAndTokenId("gw1", "tom");
            assertNotNull(credentialsEntities);
            assertFalse(credentialsEntities.isEmpty());

            CredentialsEntity credentialsEntity = credentialsEntities.get(0);
            credentialsEntity.setPortalUserId("test2");
            credentialsEntity.setCredentialOwnerType(CredentialOwnerType.USER);

            // Update serialized credential content
            CertificateCredential certificateCredential = getTestCredentialObject();
            certificateCredential.setPortalUserName("test2");
            certificateCredential.setCredentialOwnerType(CredentialOwnerType.USER);
            byte[] serializedCredential =
                    CredentialSerializationUtils.serializeCredentialWithEncryption(certificateCredential);
            credentialsEntity.setCredential(serializedCredential);

            credentialsRepository.update(credentialsEntity);

            credentialsEntities = credentialsRepository.findByGatewayIdAndTokenId("gw1", "tom");
            assertNotNull(credentialsEntities);
            assertFalse(credentialsEntities.isEmpty());

            credentialsEntity = credentialsEntities.get(0);
            assertEquals("test2", credentialsEntity.getPortalUserId());
            assertEquals(CredentialOwnerType.USER, credentialsEntity.getCredentialOwnerType());

        } finally {
            connection.close();
        }
    }

    @Test
    public void testGetCredentials() throws Exception {

        addTestCredentials();

        Connection connection = getConnection();

        try {
            List<CredentialsEntity> credentialsEntities = credentialsRepository.findByGatewayIdAndTokenId("gw1", "tom");
            assertNotNull(credentialsEntities);
            assertFalse(credentialsEntities.isEmpty());

            // Test deserialized credential content
            CredentialsEntity credentialsEntity = credentialsEntities.get(0);
            CertificateCredential certificateCredential = (CertificateCredential)
                    CredentialSerializationUtils.deserializeCredentialWithDecryption(credentialsEntity.getCredential());
            assertEquals(
                    "CN=Airavata Project, OU=IU, O=Indiana University, L=Bloomington, ST=IN, C=US",
                    certificateCredential.getCertificates()[0].getIssuerDN().toString());

        } finally {
            connection.close();
        }
    }

    @Test
    public void testGetGatewayCredentials() throws Exception {

        addTestCredentials();

        Connection connection = getConnection();

        try {
            List<CredentialsEntity> list = credentialsRepository.findByGatewayId("gw1");

            assertEquals(2, list.size());
        } finally {
            connection.close();
        }
    }

    @Test
    public void testGetGatewayCredentialsForAccessibleTokenIds() throws Exception {

        addTestCredentials();

        Connection connection = getConnection();

        try {
            // Test with single token
            List<CredentialsEntity> list = credentialsRepository.findByGatewayIdAndTokenId("gw1", "tom");
            assertEquals(1, list.size());

            // Test with multiple tokens
            list = credentialsRepository.findByGatewayId("gw1");
            assertEquals(2, list.size());

            // Test with non-existent token
            list = credentialsRepository.findByGatewayIdAndTokenId("gw1", "non-existent-token-id");
            assertEquals(0, list.size());

        } finally {
            connection.close();
        }
    }
}
