package org.apache.airavata.messaging.core;

import java.util.Map;

public interface MessageHandler {
    Map<String, Object> getProperties();

    void onMessage(MessageContext message);
}
