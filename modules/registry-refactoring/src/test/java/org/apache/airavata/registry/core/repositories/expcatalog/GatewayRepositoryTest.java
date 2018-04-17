package org.apache.airavata.registry.core.repositories.expcatalog;

import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.registry.core.repositories.expcatalog.util.Initialize;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.registry.cpi.RegistryException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class GatewayRepositoryTest {

    private static Initialize initialize;
    private String testGatewayId = "testGateway";
    GatewayRepository gatewayRepository;
    private static final Logger logger = LoggerFactory.getLogger(GatewayRepositoryTest.class);

    @Before
    public void setUp() {
        try {
            initialize = new Initialize("expcatalog-derby.sql");
            initialize.initializeDB();
            gatewayRepository = new GatewayRepository();
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
    public void GatewayRepositoryTest() throws ApplicationSettingsException, RegistryException {
        Gateway gateway = new Gateway();
        gateway.setGatewayId(testGatewayId);
        gateway.setDomain("SEAGRID");
        gateway.setEmailAddress("abc@d.com");

        String gatewayId = gatewayRepository.addGateway(gateway);
        assertEquals(testGatewayId, gatewayId);

        gateway.setGatewayAdminFirstName("ABC");
        gatewayRepository.updateGateway(testGatewayId, gateway);

        Gateway retrievedGateway = gatewayRepository.getGateway(gatewayId);
        assertEquals(gateway.getGatewayAdminFirstName(), retrievedGateway.getGatewayAdminFirstName());

        assertTrue(gatewayRepository.getAllGateways().size() == 1);

        gatewayRepository.removeGateway(gatewayId);
        assertFalse(gatewayRepository.isGatewayExist(gatewayId));
    }

}
