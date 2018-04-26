package org.apache.airavata.registry.core.repositories.expcatalog;

import org.apache.airavata.model.status.QueueStatusModel;
import org.apache.airavata.registry.core.repositories.expcatalog.util.Initialize;
import org.apache.airavata.registry.cpi.RegistryException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class QueueStatusRepositoryTest {

    private static Initialize initialize;
    QueueStatusRepository queueStatusRepository;
    private static final Logger logger = LoggerFactory.getLogger(QueueStatusRepositoryTest.class);

    @Before
    public void setUp() {
        try {
            initialize = new Initialize("expcatalog-derby.sql");
            initialize.initializeDB();
            queueStatusRepository = new QueueStatusRepository();
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
    public void QueueStatusRepositoryTest() throws RegistryException {
        QueueStatusModel queueStatusModel = new QueueStatusModel();
        queueStatusModel.setHostName("host");
        queueStatusModel.setQueueName("queue");
        queueStatusModel.setQueueUp(true);
        queueStatusModel.setRunningJobs(1);
        queueStatusModel.setQueuedJobs(2);
        queueStatusModel.setTime(System.currentTimeMillis());

        boolean returnValue = queueStatusRepository.createQueueStatuses(Arrays.asList(queueStatusModel));
        assertTrue(returnValue);

        List<QueueStatusModel> queueStatusModelList = queueStatusRepository.getLatestQueueStatuses();
        assertTrue(queueStatusModelList.size() == 1);
        assertEquals(queueStatusModel.getHostName(), queueStatusModelList.get(0).getHostName());
    }

}
