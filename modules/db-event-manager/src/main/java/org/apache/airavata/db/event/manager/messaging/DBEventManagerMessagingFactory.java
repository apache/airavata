package org.apache.airavata.db.event.manager.messaging;

import org.apache.airavata.common.utils.listener.DBEventManagerConstant;
import org.apache.airavata.messaging.core.MessagingFactory;
import org.apache.airavata.messaging.core.Publisher;
import org.apache.airavata.messaging.core.Subscriber;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ajinkya on 3/1/17.
 */
public class DBEventManagerMessagingFactory {

    private static final Subscriber dbEventMessagingSubscriber;

    static{
        //FIXME: create subscriber of db.event.manager.queue
        dbEventMessagingSubscriber = null;
    }

    public static Subscriber getDBEventMessagingSubscriber(){
        //just to add reference
        //FIXME: remove
        String queueName = DBEventManagerConstant.DB_EVENT_MANAGER_QUEUE;
        return dbEventMessagingSubscriber;
    }
    private Publisher getPublisher(String entityName){
        Publisher publisher = null;
        return publisher;
    }


    public static List<Publisher> getDBEventPublisher(String entityName){
        List<Publisher> publishers = new ArrayList<>();
        //TODO: get corresponding subscribers
        /*
        -get publishers from map
        -create publishers using private method getPublisher(entityName) if not exist
         */

        return publishers;
    }

}
