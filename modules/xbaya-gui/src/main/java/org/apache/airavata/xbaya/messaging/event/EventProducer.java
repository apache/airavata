/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.airavata.xbaya.messaging.event;

import org.apache.airavata.workflow.model.exceptions.WorkflowException;
import org.apache.airavata.workflow.model.exceptions.WorkflowRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

public abstract class EventProducer {

    private static final Logger logger = LoggerFactory.getLogger(EventProducer.class);

    private List<EventListener> listeners = new LinkedList<EventListener>();

    /**
     * @param listener
     */
    public void addEventListener(EventListener listener) {
        this.listeners.add(listener);
    }

    /**
     * @param listener
     */
    public void removeEventListener(EventListener listener) {
        this.listeners.remove(listener);
    }

    /**
     * @param event
     */
    public void sendSafeEvent(Event event) {
        Throwable exception = null;
        for (EventListener listener : this.listeners) {
            try {
                listener.eventReceived(event);
            } catch (Throwable e) {
                logger.error(e.getMessage(), e);
                // Just remember the first one.
                if (exception == null) {
                    exception = e;
                }
            }
        }
        if (exception != null) {
            if (exception instanceof RuntimeException) {
                throw (RuntimeException) exception;
            } else {
                throw new WorkflowRuntimeException(exception);
            }
        }
    }

    /**
     * Sends an event.
     *
     * @param event
     * @throws org.apache.airavata.workflow.model.exceptions.WorkflowException
     */
    public void sendEvent(Event event) throws WorkflowException {
        Throwable exception = null;
        for (EventListener listener : this.listeners) {
            try {
                listener.eventReceived(event);
            } catch (Throwable e) {
                logger.error(e.getMessage(), e);
                // Just remember the first one.
                if (exception == null) {
                    exception = e;
                }
            }
        }
        if (exception != null) {
            if (exception instanceof WorkflowException) {
                throw (WorkflowException) exception;
            } else {
                throw new WorkflowException(exception);
            }
        }
    }

}