package org.apache.airavata.registry.core.repositories.expcatalog;

import org.apache.airavata.model.workspace.Notification;
import org.apache.airavata.model.workspace.NotificationPriority;
import org.apache.airavata.registry.core.repositories.expcatalog.util.Initialize;
import org.apache.airavata.registry.cpi.RegistryException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class NotificationRepositoryTest {

    private static Initialize initialize;
    private String testGateway = "testGateway";
    NotificationRepository notificationRepository;
    private static final Logger logger = LoggerFactory.getLogger(NotificationRepositoryTest.class);

    @Before
    public void setUp() {
        try {
            initialize = new Initialize("expcatalog-derby.sql");
            initialize.initializeDB();
            notificationRepository = new NotificationRepository();
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
    public void NotificationRepositoryTest() throws RegistryException {
        Notification notification = new Notification();
        notification.setNotificationId("notificationId");
        notification.setGatewayId(testGateway);
        notification.setTitle("notificationTitle");
        notification.setNotificationMessage("notificationMessage");

        String notificationId = notificationRepository.createNotification(notification);
        assertEquals(notification.getNotificationId(), notificationId);

        notification.setPriority(NotificationPriority.NORMAL);
        notificationRepository.updateNotification(notification);

        Notification retrievedNotification = notificationRepository.getNotification(notificationId);
        assertEquals(NotificationPriority.NORMAL, retrievedNotification.getPriority());

        assertTrue(notificationRepository.getAllGatewayNotifications(testGateway).size() == 1);

        notificationRepository.deleteNotification(notificationId);
    }

}
