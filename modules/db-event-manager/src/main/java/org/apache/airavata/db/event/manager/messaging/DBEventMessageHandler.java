package org.apache.airavata.db.event.manager.messaging;

import org.apache.airavata.messaging.core.MessageContext;
import org.apache.airavata.messaging.core.MessageHandler;

/**
 * Created by Ajinkya on 3/1/17.
 */
public class DBEventMessageHandler implements MessageHandler{
    @Override
    public void onMessage(MessageContext messageContext) {
        //TODO:
        /*
        -identify message type db_sync/handshake
        -if db_sync
          -get all corresponding publishers from factory for entity in messageContext
          -publish message using those publishers
        -else if handshake
          -update DB_EVENT_MAP
         */
    }
}
