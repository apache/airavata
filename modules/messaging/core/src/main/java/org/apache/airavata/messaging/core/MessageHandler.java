package org.apache.airavata.messaging.core;

import org.apache.airavata.model.messaging.event.Message;

public interface MessageHandler {
    void onMessage(MessageContext message);
}
