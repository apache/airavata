package org.apache.airavata.registry.core.repositories.expcatalog;

import org.apache.airavata.registry.core.repositories.expcatalog.util.Initialize;
import org.apache.airavata.registry.cpi.RegistryException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessResourceScheduleRepositoryTest {

    private static Initialize initialize;
    ProcessResourceScheduleRepository processResourceScheduleRepository;
    private static final Logger logger = LoggerFactory.getLogger(ProcessResourceScheduleRepositoryTest.class);

    @Before
    public void setUp() {
        try {
            initialize = new Initialize("expcatalog-derby.sql");
            initialize.initializeDB();
            processResourceScheduleRepository = new ProcessResourceScheduleRepository();
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
    public void ProcessResourceScheduleRepositoryTest() throws RegistryException {

    }

}
