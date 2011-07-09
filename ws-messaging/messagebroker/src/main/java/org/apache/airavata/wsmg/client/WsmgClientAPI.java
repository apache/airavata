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

package org.apache.airavata.wsmg.client;

import java.rmi.RemoteException;

import org.apache.airavata.wsmg.client.msgbox.MessagePuller;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;

public interface WsmgClientAPI {

    public String[] startConsumerService(int port, ConsumerNotificationHandler handler) throws AxisFault;

    public void shutdownConsumerService();

    public String subscribe(String eventSourceLoc, String eventSinkLoc, String topic) throws AxisFault;

    public String subscribe(String brokerService, String eventSinkLocIn, String topic,
            String eventSinkEndpointReferenceNS, String eventSinkEndpointReference) throws AxisFault;

    public String subscribe(String brokerService, String eventSinkLoc, String subscriptionExpression,
            String xpathExpression) throws AxisFault;

    public String subscribe(String brokerService, String eventSinkLocIn, String subscriptionExpression,
            String xpathExpression, String eventSinkEndpointReferenceNS, String eventSinkEndpointReference)
            throws AxisFault;

    public String subscribeMsgBox(String brokerService, EndpointReference msgBoxEpr, String topic, String xpath)
            throws AxisFault;

    public String subscribeMsgBox(String brokerService, EndpointReference msgBoxEpr, String topic,
            String xpathExpression, String eventSinkEndpointReferenceNS, String eventSinkEndpointReference)
            throws AxisFault;

    public int unSubscribe(String subscriptionManagerLocIn, String subId, OMElement message, String replyTo)
            throws AxisFault;

    public int publishTopic(String eventSinkLoc, String publisherLoc, String topicString, String message)
            throws AxisFault;

    public int publish(String eventSinkLocIn, String topicString, String message) throws AxisFault;

    public MessagePuller startPullingFromExistingMsgBox(EndpointReference msgBoxAddr, NotificationHandler handler,
            long backoff, long timeout) throws AxisFault;

    public MessagePuller startPullingEventsFromMsgBox(EndpointReference msgBoxAddr, NotificationHandler handler,
            long backoff, long timeout) throws RemoteException;

    // public EndpointReference createPullMsgBox(String msgBoxServerLoc, String userAgent) throws RemoteException ;

    public void stopPullingEventsFromMsgBox(org.apache.airavata.wsmg.client.msgbox.MessagePuller msgPuller);

}
