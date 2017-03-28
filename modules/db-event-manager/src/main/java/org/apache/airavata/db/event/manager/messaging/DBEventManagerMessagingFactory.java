package org.apache.airavata.db.event.manager.messaging;

import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.common.utils.DBEventManagerConstants;
import org.apache.airavata.db.event.manager.messaging.impl.DBEventMessageHandler;
import org.apache.airavata.messaging.core.MessagingFactory;
import org.apache.airavata.messaging.core.Publisher;
import org.apache.airavata.messaging.core.Subscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Ajinkya on 3/1/17.
 */
public class DBEventManagerMessagingFactory {

    private final static Logger log = LoggerFactory.getLogger(DBEventManagerMessagingFactory.class);

    private static Subscriber dbEventSubscriber;

    /**
     * Get DB Event subscriber
     * @return
     * @throws AiravataException
     */
    public static Subscriber getDBEventSubscriber() throws AiravataException {
        if(null != dbEventSubscriber){
            synchronized (dbEventSubscriber){
                if(null != dbEventSubscriber){
                    log.info("Creating DB Event subscriber.....");
                    dbEventSubscriber = MessagingFactory.getDBEventSubscriber(new DBEventMessageHandler(), DBEventManagerConstants.DB_EVENT_QUEUE);
                    log.info("DB Event Service created");
                }
            }
        }
        return dbEventSubscriber;
    }

    /**
     * Get DB Event publisher based on routing key
     * @param routingKey
     * @return
     * @throws AiravataException
     */
    public static Publisher getDBEventPublisher(String routingKey) throws AiravataException {
        log.info("Creating DB Event Publisher for routing key : " + routingKey);
        return MessagingFactory.getDBEventPublisher(routingKey);
    }

}
