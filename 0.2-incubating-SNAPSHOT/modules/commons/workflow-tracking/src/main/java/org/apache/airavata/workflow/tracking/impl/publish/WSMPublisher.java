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

import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;

import javax.xml.stream.XMLStreamException;

import org.apache.airavata.commons.WorkFlowUtils;
import org.apache.airavata.workflow.tracking.WorkflowTrackingException;
import org.apache.airavata.workflow.tracking.common.WorkflowTrackingContext;
import org.apache.airavata.wsmg.client.MsgBrokerClientException;
import org.apache.airavata.wsmg.client.WseMsgBrokerClient;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.addressing.EndpointReferenceHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Publish WS-Eventing messages using WS-Messenger client API
 * 
 */
public class WSMPublisher extends AbstractPublisher implements NotificationPublisher {

    protected final WseMsgBrokerClient broker;
    protected final EndpointReference brokerEpr;
    protected Properties configs = new Properties();

    private Logger log = LoggerFactory.getLogger(WSMPublisher.class);

    public WSMPublisher(WorkflowTrackingContext context) {
        this(10, context.isEnableAsyncPublishing(), context.getBrokerEpr());
    }

    public WSMPublisher(int capacity, boolean defaultAsync, String brokerLoc, String topic) throws IOException {
        super(capacity, defaultAsync);
        broker = new WseMsgBrokerClient();
        brokerEpr = broker.createEndpointReference(brokerLoc, topic);
        broker.init(brokerEpr.getAddress());
    }

    public WSMPublisher(int capacity, boolean defaultAsync, EndpointReference brokerEpr_)
            throws WorkflowTrackingException {
        super(capacity, defaultAsync);
        try {
            brokerEpr = brokerEpr_;
            broker = new WseMsgBrokerClient();
            broker.init(brokerEpr_.getAddress());
        } catch (Exception e) {
            throw new WorkflowTrackingException(e);
        }
    }

    public WSMPublisher(int capacity, boolean defaultAsync, String brokerEpr_) throws IOException {

        this(capacity, defaultAsync, brokerEpr_, false);

    }

    public WSMPublisher(int capacity, boolean defaultAsync, String brokerEpr_, boolean isXmlEpr) throws IOException {
        super(capacity, defaultAsync);
        if (!isXmlEpr) {
            brokerEpr = new EndpointReference(brokerEpr_);// EndpointReferenceHelper.fro(brokerEpr_);

        } else {
            brokerEpr = EndpointReferenceHelper.fromString(brokerEpr_);
        }

        broker = new WseMsgBrokerClient();
        broker.init(brokerEpr.getAddress());
    }

    /**
     * Method publishSync
     * 
     * @param leadMessage
     *            a String
     * 
     */
    public void publishSync(String leadMessage) {
        if (isDeleted())
            throw new RuntimeException("Publisher has been deleted!");
        if (IS_LOG_FINEST) {
            logger.debug("publishing notification to messenger broker: " + leadMessage);
        }
        try {
            OMElement msg = WorkFlowUtils.reader2OMElement(new StringReader(leadMessage));
            broker.publish(null, msg);

        } catch (MsgBrokerClientException e) {
            log.error("unablet to publish the lead message", e);
        } catch (XMLStreamException e) {
            log.error("unable to parse the load message - " + leadMessage, e);
        }
    }

}
