/*
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
 *
 */

package org.apache.airavata.workflow.tracking.impl.publish;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.airavata.workflow.tracking.util.LinkedMessageQueue;
import org.apache.airavata.workflow.tracking.util.Timer;
import org.apache.xmlbeans.XmlObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract method to publish messages in sync or async mode. In async mode, the messages are kept in an in-memory queue
 * and published. Calling flush() blocks till all messages are sent and the queue is empty. In sync mode, the call
 * blocks till the message is transmitted.
 */
public abstract class AbstractPublisher implements Runnable, NotificationPublisher {

    protected static final Logger logger = LoggerFactory.getLogger(AbstractPublisher.class);
    protected static final boolean IS_LOG_FINEST = logger.isDebugEnabled();
    private final LinkedMessageQueue<BrokerEntry> messageQueue;
    protected static final boolean IS_TIMER = Boolean.getBoolean("ENABLE_TIMER");
    protected static final Timer notifTP = Timer.init("PubNotif");

    private boolean finished = false;
    private final Lock LOCK = new ReentrantLock();
    private final Condition CONDITION = LOCK.newCondition();
    private static int PUB_ID = 0;

    private final boolean IS_DEFAULT_MODE_ASYNC;

    private boolean deleted = false;

    private final Thread pubThread;

    protected AbstractPublisher(int capacity, boolean defaultAsync) {

        messageQueue = new LinkedMessageQueue<BrokerEntry>(capacity);
        IS_DEFAULT_MODE_ASYNC = defaultAsync;
        deleted = false;
        pubThread = new Thread(this, "PUBLISHER #" + PUB_ID++);
        pubThread.setDaemon(true);
        pubThread.start();
    }

    // public abstract void publishSync(String leadMessage);
    public final void delete() {
        deleted = true;
        pubThread.interrupt();
    }

    public final boolean isDeleted() {
        return deleted;
    }

    public final void publish(String leadMessage) {

        if (IS_DEFAULT_MODE_ASYNC) {
            publishAsync(leadMessage);
        } else {
            publishSync(leadMessage);
        }
    }

    public final void publish(XmlObject xmlMessage) {

        if (IS_DEFAULT_MODE_ASYNC) {
            publishAsync(xmlMessage);
        } else {
            publishSync(xmlMessage.xmlText());
        }
    }

    public final void publishAsync(String leadMessage) {

        if (IS_LOG_FINEST) {
            logger.debug("ASYNC: adding to queue, notification: " + leadMessage);
        }
        final BrokerEntry brokerEntry = new BrokerEntry(leadMessage);
        try {
            messageQueue.put(brokerEntry);
        } catch (InterruptedException e) {
            throw new RuntimeException("Publisher interrupted. Is it being deleted!?");
        }
    }

    public final void publishAsync(XmlObject xmlMessage) {

        if (IS_LOG_FINEST) {
            logger.debug("ASYNC: adding to queue, notification: " + xmlMessage);
        }

        final BrokerEntry brokerEntry = new BrokerEntry(xmlMessage);
        try {
            messageQueue.put(brokerEntry);
        } catch (InterruptedException e) {
            throw new RuntimeException("Publisher interrupted. Is it being deleted!?");
        }
    }

    public final void publishSync(XmlObject xmlMessage) {

        if (IS_LOG_FINEST) {
            logger.debug("SYNC: sending notification: " + xmlMessage);
        }
        publishSync(xmlMessage.xmlText());
    }

    public final void flush() {

        finished = true;
        LOCK.lock();
        while (messageQueue.size() > 0) {
            try {
                // wait to be signalled that all messages were sent...
                CONDITION.await();
            } catch (InterruptedException e) {
                throw new RuntimeException("Publisher interrupted. Is it being deleted!?");
            }
        }
        finished = false;
        CONDITION.signal(); // send ack...
        LOCK.unlock();
        return;
    }

    public final void run() {

        BrokerEntry brokerEntry = null;
        while (true) {

            try {
                // get the head from queue, but dont remove it yet
                // block for message to arrive only if not finished;
                // if finished, dont block...just quit
                brokerEntry = finished ? messageQueue.peek() : messageQueue.get();
                if (brokerEntry == null) {

                    // the queue has been flushed
                    if (finished) {
                        LOCK.lock();
                        CONDITION.signal(); // signal flushed queue...
                        try {
                            CONDITION.await(); // and wait for ack.
                        } catch (InterruptedException e) {
                            throw e;
                        }
                        LOCK.unlock();
                    } else { /* ignore...this should not happen */
                    }

                    // go back to to start and wait for new message in flushed queue...
                    continue;

                } else {

                    if (IS_LOG_FINEST) {
                        logger.debug("ASYNC: sending notification: " + brokerEntry.getMessage());
                    }

                    // publish message
                    publishSync(brokerEntry.getMessage());

                    // remove the published head from queue
                    messageQueue.poll();
                }

            } catch (InterruptedException e) {
                if (deleted)
                    break;
                else
                    logger.error("Interrupted when queue size: " + messageQueue.size() + ". deleted == false", e);
            } catch (RuntimeException e) {

                logger.error("Runtime Error: " + e.getMessage());
                if (logger.isDebugEnabled()) {
                    logger.debug("Runtime Error at message: "
                            + (brokerEntry != null ? brokerEntry.getMessage() : "NULL") + "; queue size: "
                            + messageQueue.size(), e);
                }
                // fixme: we should remove the entry from queue if it cannot be sent...
                // otherwise, if broker is down, this may cause an infinite loop!!!
            }
        }
    }
}
