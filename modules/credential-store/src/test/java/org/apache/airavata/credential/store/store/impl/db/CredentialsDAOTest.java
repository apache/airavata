package org.apache.airavata.credential.store.store.impl.db;

import junit.framework.Assert;
import org.apache.airavata.common.utils.DBUtil;
import org.apache.airavata.common.utils.DatabaseTestCases;
import org.apache.airavata.common.utils.DerbyUtil;
import org.apache.airavata.credential.store.credential.CommunityUser;
import org.apache.airavata.credential.store.credential.Credential;
import org.apache.airavata.credential.store.credential.impl.certificate.CertificateCredential;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.security.*;
import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.util.Arrays;
import java.util.List;

/**
 * Test class for credential class
 */
public class CredentialsDAOTest extends DatabaseTestCases {

    private static final Logger logger = LoggerFactory.getLogger(CredentialsDAOTest.class);

    private CredentialsDAO credentialsDAO;

    private X509Certificate x509Certificate;
    private PrivateKey privateKey;

    @BeforeClass
    public static void setUpDatabase() throws Exception {
        DerbyUtil.startDerbyInServerMode(getHostAddress(), getPort(), getUserName(), getPassword());

        waitTillServerStarts();

        /*String createTable = "CREATE TABLE CREDENTIALS\n" +
                "(\n" +
                "        GATEWAY_NAME VARCHAR(256) NOT NULL,\n" +
                "        COMMUNITY_USER_NAME VARCHAR(256) NOT NULL,\n" +
                "        CREDENTIAL BLOB NOT NULL,\n" +
                "        PRIVATE_KEY BLOB NOT NULL,\n" +
                "        NOT_BEFORE VARCHAR(256) NOT NULL,\n" +
                "        NOT_AFTER VARCHAR(256) NOT NULL,\n" +
                "        LIFETIME INTEGER NOT NULL,\n" +
                "        REQUESTING_PORTAL_USER_NAME VARCHAR(256) NOT NULL,\n" +
                "        REQUESTED_TIME TIMESTAMP DEFAULT '0000-00-00 00:00:00',\n" +
                "        PRIMARY KEY (GATEWAY_NAME, COMMUNITY_USER_NAME)\n" +
                ")"; */

        String createTable = "CREATE TABLE CREDENTIALS\n" +
                "(\n" +
                "        GATEWAY_ID VARCHAR(256) NOT NULL,\n" +
                "        TOKEN_ID VARCHAR(256) NOT NULL,\n" +       // Actual token used to identify the credential
                "        CREDENTIAL BLOB NOT NULL,\n" +
                "        PORTAL_USER_ID VARCHAR(256) NOT NULL,\n" +
                "        TIME_PERSISTED TIMESTAMP DEFAULT CURRENT_TIMESTAMP,\n" +
                "        PRIMARY KEY (GATEWAY_ID, TOKEN_ID)\n" +
                ")";


        String dropTable = "drop table CREDENTIALS";

        try {
            executeSQL(dropTable);
        } catch (Exception e) {}

        executeSQL(createTable);

    }

    @AfterClass
    public static void shutDownDatabase() throws Exception {
        DerbyUtil.stopDerbyServer();
    }

    @Before
    public void setUp() throws Exception {

        credentialsDAO = new CredentialsDAO();

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
        x509Certificate = (X509Certificate) ks.getCertificate("selfsigned");

    }


    @Test
    public void testKeyReading() throws Exception {
        initializeKeys();
        System.out.println(privateKey.getAlgorithm());
        System.out.println(x509Certificate.getIssuerDN());

        Assert.assertNotNull(privateKey);
        Assert.assertNotNull(x509Certificate);
    }

    private CommunityUser getCommunityUser(String gateway, String name) {
        return new CommunityUser(gateway, name, "amila@sciencegateway.org");
    }

    private void addTestCredentials() throws Exception {

        Connection connection = getConnection();

        try {
            CertificateCredential certificateCredential = getTestCredentialObject();
            credentialsDAO.addCredentials(certificateCredential.getCommunityUser().getGatewayName(),
                    certificateCredential, connection);

        } finally {
            connection.close();
        }
    }

    public CertificateCredential getTestCredentialObject() {

        CertificateCredential certificateCredential = new CertificateCredential();
        certificateCredential.setToken("tom");
        certificateCredential.setCertificate(x509Certificate);
        certificateCredential.setPrivateKey(privateKey);
        certificateCredential.setCommunityUser(getCommunityUser("gw1", "tom"));
        certificateCredential.setLifeTime(1000);
        certificateCredential.setPortalUserName("jerry");
        certificateCredential.setNotBefore("13 OCT 2012 5:34:23");
        certificateCredential.setNotAfter("14 OCT 2012 5:34:23");

        return certificateCredential;

    }

    @Test
    public void testSerialization() throws IOException, ClassNotFoundException {

        CertificateCredential certificateCredential = getTestCredentialObject();

        byte[] array = CredentialsDAO.convertObjectToByteArray(certificateCredential);
        CertificateCredential readCertificateCredential = (CertificateCredential) CredentialsDAO.convertByteArrayToObject(array);

        Assert.assertEquals(certificateCredential.getCertificate(), readCertificateCredential.getCertificate());
        Assert.assertEquals(certificateCredential.getCertificateRequestedTime(), readCertificateCredential.getCertificateRequestedTime());
        Assert.assertEquals(certificateCredential.getCommunityUser().getGatewayName(), readCertificateCredential.getCommunityUser().getGatewayName());
        Assert.assertEquals(certificateCredential.getCommunityUser().getUserEmail(), readCertificateCredential.getCommunityUser().getUserEmail());
        Assert.assertEquals(certificateCredential.getCommunityUser().getUserName(), readCertificateCredential.getCommunityUser().getUserName());
        Assert.assertEquals(certificateCredential.getLifeTime(), readCertificateCredential.getLifeTime());
        Assert.assertEquals(certificateCredential.getNotAfter(), readCertificateCredential.getNotAfter());
        Assert.assertEquals(certificateCredential.getNotBefore(), readCertificateCredential.getNotBefore());
        Assert.assertEquals(certificateCredential.getPortalUserName(), readCertificateCredential.getPortalUserName());

        PrivateKey newKey = readCertificateCredential.getPrivateKey();

        Assert.assertNotNull(newKey);
        Assert.assertEquals(privateKey.getClass(), newKey.getClass());

        Assert.assertEquals(privateKey.getFormat(), newKey.getFormat());
        Assert.assertEquals(privateKey.getAlgorithm(), newKey.getAlgorithm());
        Assert.assertTrue(Arrays.equals(privateKey.getEncoded(), newKey.getEncoded()));
    }

    @Test
    public void testAddCredentials() throws Exception {

        addTestCredentials();

        Connection connection = getConnection();

        try {
            CertificateCredential certificateCredential
                    = (CertificateCredential)credentialsDAO.getCredential("gw1", "tom", connection);
            Assert.assertNotNull(certificateCredential);
            Assert.assertEquals("jerry", certificateCredential.getPortalUserName());
            Assert.assertEquals(x509Certificate, certificateCredential.getCertificate());
            Assert.assertEquals(privateKey.getFormat(), certificateCredential.getPrivateKey().getFormat());
        } finally {
            connection.close();
        }
    }

    @Test
    public void testDeleteCredentials() throws Exception {

        addTestCredentials();

        Connection connection = getConnection();

        try {
            CertificateCredential certificateCredential
                    = (CertificateCredential)credentialsDAO.getCredential("gw1", "tom", connection);
            Assert.assertNotNull(certificateCredential);

            credentialsDAO.deleteCredentials("gw1", "tom", connection);

            certificateCredential = (CertificateCredential)credentialsDAO.getCredential("gw1", "tom", connection);
            Assert.assertNull(certificateCredential);

        } finally {
            connection.close();
        }
    }

    @Test
    public void testUpdateCredentials() throws Exception {

        addTestCredentials();

        Connection connection = getConnection();

        try {
            CommunityUser communityUser = getCommunityUser("gw1", "tom");
            CertificateCredential certificateCredential = new CertificateCredential();
            certificateCredential.setToken("tom");
            certificateCredential.setCommunityUser(communityUser);
            certificateCredential.setCertificate(x509Certificate);
            //certificateCredential.setPrivateKey(privateKey);
            certificateCredential.setPortalUserName("test2");
            certificateCredential.setLifeTime(50);
            certificateCredential.setNotBefore("15 OCT 2012 5:34:23");
            certificateCredential.setNotAfter("16 OCT 2012 5:34:23");

            credentialsDAO.updateCredentials(communityUser.getGatewayName(), certificateCredential, connection);

            certificateCredential = (CertificateCredential)credentialsDAO.getCredential("gw1", "tom", connection);

            Assert.assertEquals("CN=Airavata Project, OU=IU, O=Indiana University, L=Bloomington, ST=IN, C=US",
                    certificateCredential.getCertificate().getIssuerDN().toString());
            //Assert.assertNotNull(certificateCredential.getPrivateKey());
            Assert.assertEquals("test2", certificateCredential.getPortalUserName());

        } finally {
            connection.close();
        }

    }

    @Test
    public void testGetCredentials() throws Exception {

        addTestCredentials();

        Connection connection = getConnection();

        try {

            CertificateCredential certificateCredential = (CertificateCredential)credentialsDAO.getCredential("gw1", "tom", connection);
            Assert.assertEquals("CN=Airavata Project, OU=IU, O=Indiana University, L=Bloomington, ST=IN, C=US",
                    certificateCredential.getCertificate().getIssuerDN().toString());
           // Assert.assertNotNull(certificateCredential.getPrivateKey());

        } finally {
            connection.close();
        }
    }

    @Test
    public void testGetGatewayCredentials() throws Exception {

        addTestCredentials();

        Connection connection = getConnection();

        try {
            List<Credential> list = credentialsDAO.getCredentials("gw1", connection);

            Assert.assertEquals(1, list.size());
        } finally {
            connection.close();
        }

    }
}
