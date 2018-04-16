package org.apache.airavata.registry.core.repositories.expcatalog;

import org.apache.airavata.registry.core.repositories.expcatalog.util.Initialize;
import org.apache.airavata.registry.core.utils.DBConstants;
import org.apache.airavata.registry.cpi.RegistryException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class ExperimentSummaryRepositoryTest {

    private static Initialize initialize;
    ExperimentSummaryRepository experimentSummaryRepository;
    private static final Logger logger = LoggerFactory.getLogger(ExperimentSummaryRepositoryTest.class);

    @Before
    public void setUp() {
        try {
            initialize = new Initialize("expcatalog-derby.sql");
            initialize.initializeDB();
            experimentSummaryRepository = new ExperimentSummaryRepository();
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
    public void ExperimentSummaryRepositoryTest() throws RegistryException {

    }

}
