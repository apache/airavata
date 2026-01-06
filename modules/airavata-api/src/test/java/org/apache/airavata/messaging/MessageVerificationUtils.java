/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.airavata.messaging;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.apache.airavata.common.model.ExperimentState;
import org.apache.airavata.common.model.ExperimentStatusChangeEvent;
import org.apache.airavata.messaging.MessageContext;
import org.apache.airavata.common.model.MessageType;
import org.apache.airavata.common.model.ProcessState;
import org.apache.airavata.common.model.ProcessStatusChangeEvent;
import org.apache.airavata.messaging.MessageHandler;
import org.apache.airavata.messaging.Subscriber;
import org.apache.airavata.messaging.Type;
import org.apache.airavata.messaging.rabbitmq.MessagingFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for verifying messages in integration tests.
 * Provides helper methods to capture and verify published messages.
 */
public class MessageVerificationUtils {

    private static final Logger logger = LoggerFactory.getLogger(MessageVerificationUtils.class);

    /**
     * Captures messages published during a test execution.
     * Creates a subscriber that listens for messages and collects them.
     *
     * @param messagingFactory The messaging factory to create subscribers
     * @param messageType The type of messages to capture
     * @param routingKeys Routing keys to subscribe to
     * @param timeout Maximum time to wait for messages
     * @return List of captured message contexts
     */
    public static List<MessageContext> capturePublishedMessages(
            MessagingFactory messagingFactory,
            Type type,
            List<String> routingKeys,
            Duration timeout) {
        List<MessageContext> capturedMessages = new ArrayList<>();
        CountDownLatch messageReceived = new CountDownLatch(1);

        MessageHandler handler = message -> {
            capturedMessages.add(message);
            messageReceived.countDown();
        };

        Subscriber subscriber = null;
        String subscriberId = null;
        try {
            subscriber = messagingFactory.getSubscriber(handler, routingKeys, type);
            // Note: getSubscriber() internally calls listen() but doesn't return the id
            // For cleanup, we would need the id, but since we can't get it, we'll skip cleanup
            // In a real scenario, the subscriber would be managed by the test framework
            messageReceived.await(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            logger.warn("Error capturing messages: {}", e.getMessage());
        } finally {
            // Note: Subscriber interface requires stopListen(id) but we don't have the id
            // The subscriber will be cleaned up when the connection is closed
        }

        return capturedMessages;
    }

    /**
     * Verifies that a specific message was published.
     *
     * @param expectedMessage The message to look for
     * @param capturedMessages List of captured messages
     * @return true if the message was found
     */
    public static boolean verifyMessagePublished(MessageContext expectedMessage, List<MessageContext> capturedMessages) {
        return capturedMessages.stream().anyMatch(msg -> {
            if (!msg.getMessageId().equals(expectedMessage.getMessageId())) {
                return false;
            }
            if (!msg.getType().equals(expectedMessage.getType())) {
                return false;
            }
            return true;
        });
    }

    /**
     * Verifies that state transition messages were published in the correct order.
     *
     * @param capturedMessages List of captured messages
     * @param expectedStates List of expected states in order
     * @param messageType The type of messages to verify (EXPERIMENT or PROCESS)
     * @return true if all states were found in the correct order
     */
    public static boolean verifyStateTransitionMessages(
            List<MessageContext> capturedMessages, List<? extends Enum<?>> expectedStates, MessageType messageType) {
        List<Enum<?>> actualStates = new ArrayList<>();

        for (MessageContext message : capturedMessages) {
            if (message.getType().equals(messageType)) {
                if (messageType == MessageType.EXPERIMENT) {
                    ExperimentStatusChangeEvent event = (ExperimentStatusChangeEvent) message.getEvent();
                    actualStates.add(event.getState());
                } else if (messageType == MessageType.PROCESS) {
                    ProcessStatusChangeEvent event = (ProcessStatusChangeEvent) message.getEvent();
                    actualStates.add(event.getState());
                }
            }
        }

        // Verify all expected states are present
        if (actualStates.size() < expectedStates.size()) {
            logger.warn("Expected {} states but found {}", expectedStates.size(), actualStates.size());
            return false;
        }

        // Verify states match (allowing for additional states)
        int expectedIndex = 0;
        for (Enum<?> actualState : actualStates) {
            if (expectedIndex < expectedStates.size() && actualState.equals(expectedStates.get(expectedIndex))) {
                expectedIndex++;
            }
        }

        return expectedIndex == expectedStates.size();
    }

    /**
     * Verifies that an experiment state change message was published with the correct state.
     *
     * @param capturedMessages List of captured messages
     * @param experimentId Expected experiment ID
     * @param expectedState Expected experiment state
     * @return true if the message was found
     */
    public static boolean verifyExperimentStateMessage(
            List<MessageContext> capturedMessages, String experimentId, ExperimentState expectedState) {
        return capturedMessages.stream().anyMatch(msg -> {
            if (msg.getType() != MessageType.EXPERIMENT) {
                return false;
            }
            ExperimentStatusChangeEvent event = (ExperimentStatusChangeEvent) msg.getEvent();
            return event.getExperimentId().equals(experimentId) && event.getState() == expectedState;
        });
    }

    /**
     * Verifies that a process state change message was published with the correct state.
     *
     * @param capturedMessages List of captured messages
     * @param processId Expected process ID
     * @param expectedState Expected process state
     * @return true if the message was found
     */
    public static boolean verifyProcessStateMessage(
            List<MessageContext> capturedMessages, String processId, ProcessState expectedState) {
        return capturedMessages.stream().anyMatch(msg -> {
            if (msg.getType() != MessageType.PROCESS) {
                return false;
            }
            ProcessStatusChangeEvent event = (ProcessStatusChangeEvent) msg.getEvent();
            return event.getProcessIdentity().getProcessId().equals(processId)
                    && event.getState() == expectedState;
        });
    }

    /**
     * Creates a message handler that captures messages into a list.
     *
     * @param capturedMessages List to store captured messages
     * @return MessageHandler that captures messages
     */
    public static MessageHandler createCapturingHandler(List<MessageContext> capturedMessages) {
        return message -> {
            capturedMessages.add(message);
            logger.debug("Captured message: type={}, id={}", message.getType(), message.getMessageId());
        };
    }

    /**
     * Creates a message handler that captures messages and counts down a latch.
     *
     * @param capturedMessages List to store captured messages
     * @param latch CountDownLatch to count down when messages are received
     * @param expectedCount Number of messages expected before counting down
     * @return MessageHandler that captures messages and counts down
     */
    public static MessageHandler createCapturingHandlerWithLatch(
            List<MessageContext> capturedMessages, CountDownLatch latch, int expectedCount) {
        return message -> {
            capturedMessages.add(message);
            logger.debug("Captured message: type={}, id={}", message.getType(), message.getMessageId());
            // Count down when we've captured the expected number of messages
            if (capturedMessages.size() >= expectedCount) {
                latch.countDown();
            }
        };
    }
}

