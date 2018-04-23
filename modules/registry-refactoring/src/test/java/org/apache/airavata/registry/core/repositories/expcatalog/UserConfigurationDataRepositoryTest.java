package org.apache.airavata.registry.core.repositories.expcatalog;

import org.apache.airavata.registry.core.repositories.expcatalog.util.Initialize;
import org.apache.airavata.registry.cpi.RegistryException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserConfigurationDataRepositoryTest {

    private static Initialize initialize;
    UserConfigurationDataRepository userConfigurationDataRepository;
    private static final Logger logger = LoggerFactory.getLogger(UserConfigurationDataRepositoryTest.class);

    @Before
    public void setUp() {
        try {
            initialize = new Initialize("expcatalog-derby.sql");
            initialize.initializeDB();
            userConfigurationDataRepository = new UserConfigurationDataRepository();
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
    public void UserConfigurationDataRepositoryTest() throws RegistryException {

    }

}
