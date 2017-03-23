package org.apache.airavata.common.utils;

/**
 * Created by Ajinkya on 3/22/17.
 */
public class DBEventManagerConstants {

    public static final String DB_EVENT_SERVICE_DISCOVERY_QUEUE = "db.event.service.discovery.queue";
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

    public static String getQueueName(DBEventService dBEventService){
        return dBEventService.toString() + QUEUE_SUFFIX;
    }

}
