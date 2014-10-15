package org.apache.airavata.messaging.core;

import java.util.Map;

public interface MessageHandler {
    Map<String, String> getProperties();

    void onMessage(MessageContext message);
}
