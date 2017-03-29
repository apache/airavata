package org.apache.airavata.sharing.registry.messaging;

import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.common.utils.DBEventManagerConstants;
import org.apache.airavata.common.utils.DBEventService;
import org.apache.airavata.messaging.core.MessageContext;
import org.apache.airavata.messaging.core.MessagingFactory;
import org.apache.airavata.messaging.core.Publisher;
import org.apache.airavata.model.dbevent.DBEventMessage;
import org.apache.airavata.model.dbevent.DBEventMessageContext;
import org.apache.airavata.model.dbevent.DBEventSubscriber;
import org.apache.airavata.model.dbevent.DBEventType;
import org.apache.airavata.model.messaging.event.MessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by Ajinkya on 3/28/17.
 */
public class SharingServiceDBEventMessagingFactory {

    private final static Logger log = LoggerFactory.getLogger(SharingServiceDBEventMessagingFactory.class);

    private static Publisher dbEventPublisher;

    /**
     * Get publisher for DB Event queue
     * Change access specifier as required
     * @return
     * @throws AiravataException
     */
    private static Publisher getDBEventPublisher() throws AiravataException {
        if(null != dbEventPublisher){
            synchronized (SharingServiceDBEventMessagingFactory.class){
                if(null != dbEventPublisher){
                    log.info("Creating DB Event publisher.....");
                    dbEventPublisher = MessagingFactory.getDBEventPublisher();
                    log.info("DB Event publisher created");
                }
            }
        }
        return dbEventPublisher;
    }

    /**
     * Register sharing service with stated publishers
     * @param publishers
     * @return
     * @throws AiravataException
     */
    public static boolean registerSharingServiceWithPublishers(List<String> publishers) throws AiravataException {

        for(String publisher : publishers){

            log.info("Sending service discovery message. Publisher : " + publisher + ", Subscriber : " + DBEventService.REGISTRY.toString());

            DBEventSubscriber dbEventSubscriber = new DBEventSubscriber(DBEventService.REGISTRY.toString());
            DBEventMessageContext dbEventMessageContext = new DBEventMessageContext();
            dbEventMessageContext.setSubscriber(dbEventSubscriber);

            DBEventMessage dbEventMessage = new DBEventMessage(DBEventType.SUBSCRIBER, dbEventMessageContext, publisher);

            MessageContext messageContext = new MessageContext(dbEventMessage, MessageType.DB_EVENT, "", "");

            getDBEventPublisher().publish(messageContext, DBEventManagerConstants.getRoutingKey(DBEventService.DB_EVENT.toString()));

        }

        return true;
    }

}
