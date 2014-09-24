package org.apache.airavata.messaging.core;

import org.apache.airavata.model.messaging.event.*;

public interface Publisher {
    public void publish(Message message);
}
