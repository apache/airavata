package org.apache.airavata.common.utils;

/**
 * Created by Ajinkya on 3/22/17.
 */
public class DBEventManagerConstants {

    public static final String DB_EVENT_QUEUE = "db.event.queue";
    private final static String QUEUE_SUFFIX = ".queue";
    public final static String DB_EVENT_EXCHANGE_NAME = "db.event.exchange";

    enum DBEventService{

        USER_PROFILE("user.profile"),
        SHARING("sharing"),
        REGISTRY("registry");

        private final String name;
        DBEventService(String name) {
            this.name = name;
        }
        public String toString() {
            return this.name;
        }
    }

    /**
     * Get the queue-name of the service, given service-name as enum
     * @param dBEventService
     * @return
     */
    public static String getQueueName(DBEventService dBEventService) {
        return dBEventService.toString() + QUEUE_SUFFIX;
    }

    /**
     * Get the queue-name of the service, given service-name as string
     * @param dbEventService
     * @return
     */
    public static String getQueueName(String dbEventService) {
        return getQueueName(getDBEventService(dbEventService));
    }

    /**
     * Get the service as enum, given the service-name as string
     * @param dbEventService
     * @return
     */
    private static DBEventService getDBEventService(String dbEventService) {
        for (DBEventService service : DBEventService.values()) {
            if (service.toString().equals(dbEventService)) {
                return service;
            }
        }
        return null;
    }
}
