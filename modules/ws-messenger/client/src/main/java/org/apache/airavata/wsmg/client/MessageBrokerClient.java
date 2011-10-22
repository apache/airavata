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

import org.apache.airavata.wsmg.client.msgbox.MessagePuller;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;

import java.rmi.RemoteException;

public interface MessageBrokerClient {

    /**
     * May be we just use a constructor here
     * 
     * @param brokerLocation
     */
    public void init(String brokerLocation);

    /**
     * 
     * @param subscriptionId
     * @return
     */
    public boolean unSubscribe(String subscriptionId) throws MsgBrokerClientException;

    /**
     * 
     * @param eventSinkLocation
     * @param topicExpression
     * @param xpathExpression
     * @return
     * @throws AxisFault
     */
    public String subscribe(String eventSinkLocation, String topicExpression, String xpathExpression)
            throws MsgBrokerClientException;

    public String subscribe(EndpointReference eventSinkLocation, String topicExpression, String xpathExpression)
            throws MsgBrokerClientException;

    /**
     * 
     * @param eventSinkLocation
     * @param topicExpression
     * @param xpathExpression
     * @param expireTime
     *            with -1, it never expires
     * @return
     * @throws AxisFault
     */
    public String subscribe(EndpointReference eventSinkLocation, String topicExpression, String xpathExpression,
            long expireTime) throws MsgBrokerClientException;

    public void publish(String topic, String plainText) throws MsgBrokerClientException;

    public void publish(String topic, OMElement message) throws MsgBrokerClientException;

    /**
     * 
     * @param brokerLocation
     * @param msgBoxEpr
     * @param topicExpression
     * @param xpathExpression
     * @param expireTime
     *            with -1, it never expires
     * @return
     * @throws MsgBrokerClientException
     */
    public String subscribeMsgBox(EndpointReference msgBoxEpr, String topicExpression, String xpathExpression,
            long expireTime) throws MsgBrokerClientException;

    /**
     * This method can be used to shutdown the consumer server started
     */
    public void shutdownConsumerService();

    /**
     * 
     * @param port
     * @param handler
     * @return
     * @throws MsgBrokerClientException
     */
    public String[] startConsumerService(int port, ConsumerNotificationHandler handler) throws MsgBrokerClientException;

    /**
     * 
     * @param msgBoxAddr
     * @param handler
     * @param backoff
     * @param timeout
     * @return
     * @throws AxisFault
     */
    public MessagePuller startPullingFromExistingMsgBox(EndpointReference msgBoxAddr, NotificationHandler handler,
            long backoff, long timeout) throws MsgBrokerClientException;

    /**
     * 
     * @param msgBoxAddr
     * @param handler
     * @param backoff
     * @param timeout
     * @return
     * @throws RemoteException
     */
    public MessagePuller startPullingEventsFromMsgBox(EndpointReference msgBoxAddr, NotificationHandler handler,
            long backoff, long timeout) throws MsgBrokerClientException;

    // public EndpointReference createPullMsgBox(String msgBoxServerLoc, String userAgent) throws RemoteException ;

    /**
     * 
     * @param msgPuller
     */
    public void stopPullingEventsFromMsgBox(org.apache.airavata.wsmg.client.msgbox.MessagePuller msgPuller)
            throws MsgBrokerClientException;

    /**
     * 
     * @return
     */
    public String[] getConsumerServiceEndpointReference();

    /**
     * 
     * @param brokerService
     * @param msgBoxEpr
     * @param topic
     * @param xpath
     * @return
     */
    public String subscribeMsgBox(String brokerService, EndpointReference msgBoxEpr, String topic, String xpath)
            throws MsgBrokerClientException;

    /**
     * 
     * @param msgBoxLocation
     * @param timeout
     * @return
     * @throws MsgBrokerClientException
     */
    public EndpointReference createPullMsgBox(String msgBoxLocation, long timeout) throws MsgBrokerClientException;

    /**
     * 
     * @param msgBoxServerLoc
     * @return
     * @throws MsgBrokerClientException
     */
    public EndpointReference createPullMsgBox(String msgBoxServerLoc) throws MsgBrokerClientException;

}
