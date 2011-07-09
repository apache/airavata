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

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;

public interface MessageBrokerClient {

    /**
     * May be we just use a constructor here
     * 
     * @param brokerLocation
     */
    public void init(String brokerLocation);

    /**
     * this is only a helper method
     * 
     * @param brokerURL
     * @param topic
     * @return
     */
    public EndpointReference createEndpointReference(String brokerURL, String topic);

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
}
