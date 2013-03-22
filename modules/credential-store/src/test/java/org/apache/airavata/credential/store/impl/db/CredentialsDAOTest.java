package org.apache.airavata.credential.store.impl.db;

import junit.framework.Assert;
import org.apache.airavata.common.utils.DBUtil;
import org.apache.airavata.common.utils.DatabaseTestCases;
import org.apache.airavata.common.utils.DerbyUtil;
import org.apache.airavata.credential.store.CertificateCredential;
import org.apache.airavata.credential.store.CommunityUser;
import org.bouncycastle.jce.X509Principal;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Test class for credential class
 */
public class CredentialsDAOTest extends DatabaseTestCases {

    private CredentialsDAO credentialsDAO;

    private X509Certificate x509Certificate;
    private PrivateKey privateKey;

    @BeforeClass
    public static void setUpDatabase() throws Exception{
        DerbyUtil.startDerbyInServerMode(getHostAddress(), getPort(), getUserName(), getPassword());

        waitTillServerStarts();

        String createTable = "CREATE TABLE CREDENTIALS\n" +
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
                ")";
        executeSQL(createTable);

    }

    @AfterClass
    public static void shutDownDatabase() throws Exception {
        DerbyUtil.stopDerbyServer();
    }

    @Before
    public void setUp() throws Exception {

        credentialsDAO = new CredentialsDAO(getDbUtil());

        // Cleanup tables;
        Connection connection = getDbUtil().getConnection();
        DBUtil.truncate("credentials", connection);
        DBUtil.truncate("community_user", connection);

        connection.close();

        initializeKeys();
    }

    private void initializeKeys() throws Exception {
        KeyStore ks = KeyStore.getInstance("JKS");
        char[] password = "password".toCharArray();

        String baseDirectory = System.getProperty("credential.module.directory");

        String keyStorePath = "src" + File.separator + "test"  + File.separator + "resources" + File.separator
                + "keystore.jks";

        if (baseDirectory != null) {
            keyStorePath = baseDirectory + File.separator + keyStorePath;
        }

        java.io.FileInputStream fis = null;
        try {
            fis =
                    new java.io.FileInputStream(keyStorePath);
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

        CertificateCredential certificateCredential = new CertificateCredential();
        certificateCredential.setCertificate(x509Certificate);
        certificateCredential.setPrivateKey(privateKey);
        certificateCredential.setCommunityUser(getCommunityUser("gw1", "tom"));
        certificateCredential.setLifeTime(1000);
        certificateCredential.setPortalUserName("jerry");
        certificateCredential.setNotBefore("13 OCT 2012 5:34:23");
        certificateCredential.setNotAfter("14 OCT 2012 5:34:23");

        credentialsDAO.addCredentials(certificateCredential);
    }

    @Test
    public void testSerialization() throws IOException, ClassNotFoundException {
        byte[] array = CredentialsDAO.convertObjectToByteArray(privateKey);
        PrivateKey newKey = (PrivateKey) CredentialsDAO.convertByteArrayToObject(array);

        Assert.assertNotNull(newKey);
        Assert.assertEquals(privateKey.getClass(), newKey.getClass());

        Assert.assertEquals(privateKey.getFormat(), newKey.getFormat());
        Assert.assertEquals(privateKey.getAlgorithm(), newKey.getAlgorithm());
        Assert.assertTrue(Arrays.equals(privateKey.getEncoded(), newKey.getEncoded()));
    }

    @Test
    public void testAddCredentials() throws Exception {

        addTestCredentials();

        CertificateCredential certificateCredential
                = credentialsDAO.getCredential("gw1", "tom");
        Assert.assertNotNull(certificateCredential);
        Assert.assertEquals("jerry", certificateCredential.getPortalUserName());
        Assert.assertEquals(x509Certificate, certificateCredential.getCertificate());
        Assert.assertEquals(privateKey, certificateCredential.getPrivateKey());

    }

    @Test
    public void testDeleteCredentials() throws Exception {

        addTestCredentials();

        CertificateCredential certificateCredential
                = credentialsDAO.getCredential("gw1", "tom");
        Assert.assertNotNull(certificateCredential);

        credentialsDAO.deleteCredentials("gw1", "tom");

        certificateCredential = credentialsDAO.getCredential("gw1", "tom");
        Assert.assertNull(certificateCredential);
    }

    @Test
    public void testUpdateCredentials() throws Exception {

        addTestCredentials();

        CertificateCredential certificateCredential = new CertificateCredential();
        certificateCredential.setCommunityUser(getCommunityUser("gw1", "tom"));
        certificateCredential.setCertificate(x509Certificate);
        certificateCredential.setPrivateKey(privateKey);
        certificateCredential.setPortalUserName("test2");
        certificateCredential.setLifeTime(50);
        certificateCredential.setNotBefore("15 OCT 2012 5:34:23");
        certificateCredential.setNotAfter("16 OCT 2012 5:34:23");

        credentialsDAO.updateCredentials(certificateCredential);

        certificateCredential = credentialsDAO.getCredential("gw1", "tom");

        Assert.assertEquals("CN=Airavata Project, OU=IU, O=Indiana University, L=Bloomington, ST=IN, C=US",
                certificateCredential.getCertificate().getIssuerDN().toString());
        Assert.assertNotNull(certificateCredential.getPrivateKey());
        Assert.assertEquals("test2", certificateCredential.getPortalUserName());

    }

    @Test
    public void testGetCredentials() throws Exception {

        addTestCredentials();

        CertificateCredential certificateCredential = credentialsDAO.getCredential("gw1", "tom");

        Assert.assertEquals("CN=Airavata Project, OU=IU, O=Indiana University, L=Bloomington, ST=IN, C=US",
                certificateCredential.getCertificate().getIssuerDN().toString());
        Assert.assertNotNull(certificateCredential.getPrivateKey());
    }

    @Test
    public void testGetGatewayCredentials() throws Exception {

        addTestCredentials();

        List<CertificateCredential> list = credentialsDAO.getCredentials("gw1");

        Assert.assertEquals(1, list.size());
    }
}
