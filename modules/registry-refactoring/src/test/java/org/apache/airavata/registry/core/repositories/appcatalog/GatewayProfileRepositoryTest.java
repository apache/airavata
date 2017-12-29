package org.apache.airavata.registry.core.repositories.appcatalog;

import org.apache.airavata.model.appcatalog.gatewayprofile.GatewayResourceProfile;
import org.apache.airavata.registry.core.repositories.util.Initialize;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.junit.Assert.assertTrue;

public class GatewayProfileRepositoryTest {

    private static Initialize initialize;
    private GwyResourceProfileRepository gwyResourceProfileRepository;
    private static final Logger logger = LoggerFactory.getLogger(GatewayProfileRepositoryTest.class);

    @Before
    public void setUp() {
        try {
            initialize = new Initialize("appcatalog-derby.sql");
            initialize.initializeDB();
            gwyResourceProfileRepository = new GwyResourceProfileRepository();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @After
    public void tearDown() throws Exception {
        System.out.println("********** TEAR DOWN ************");
        initialize.stopDerbyServer();
    }

    @Test
    public void gatewayProfileRepositorytest() {
        GatewayResourceProfile gf = new GatewayResourceProfile();
        gf.setGatewayID("testGateway");
        gf.setCredentialStoreToken("testCredential");
        gf.setIdentityServerPwdCredToken("pwdCredential");
        gf.setIdentityServerTenant("testTenant");

        GatewayResourceProfile gf1 = new GatewayResourceProfile();
        gf1.setGatewayID("testGateway1");
        gf1.setCredentialStoreToken("testCredential");
        gf1.setIdentityServerPwdCredToken("pwdCredential");
        gf1.setIdentityServerTenant("testTenant");

        String gwId = gwyResourceProfileRepository.addGatewayResourceProfile(gf);
        GatewayResourceProfile retrievedProfile = null;
        if (gwyResourceProfileRepository.isExists(gwId)){
            retrievedProfile = gwyResourceProfileRepository.getGatewayProfile(gwId);
            System.out.println("************ gateway id ************** :" + retrievedProfile.getGatewayID());
            assertTrue("Retrieved gateway id matched", retrievedProfile.getGatewayID().equals("testGateway"));
            assertTrue(retrievedProfile.getCredentialStoreToken().equals("testCredential"));
            assertTrue(retrievedProfile.getIdentityServerPwdCredToken().equals("pwdCredential"));
            assertTrue(retrievedProfile.getIdentityServerTenant().equals("testTenant"));
        }

        gwyResourceProfileRepository.addGatewayResourceProfile(gf1);
        List<GatewayResourceProfile> getGatewayResourceList = gwyResourceProfileRepository.getAllGatewayProfiles();
        assertTrue(getGatewayResourceList.size() == 2);
    }

}
