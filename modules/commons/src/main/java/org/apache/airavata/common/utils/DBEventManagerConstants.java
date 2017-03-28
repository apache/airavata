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
     * Return routing key which is capable of consuming any message published with queueName in it.
     * For example: let' say queueName is 'hello' function will return #.hello.#
     * This queue can consume message with any of these routing keys 'q1.12.hello.q3', 'q1.hello', 'hello.q2' or just 'hello'
     * It just need to have 'hello' in it.
     * @param queueName
     * @return
     */
    public static String getRoutingKey(String queueName) {
        return "#." + queueName + ".#";
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
