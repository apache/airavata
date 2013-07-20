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

package org.apache.airavata.wsmg.client.amqp;

import org.apache.axiom.om.OMElement;
import org.jaxen.JaxenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.HashMap;
import java.util.List;
import java.util.Properties;

/**
 * AMQPRoutingAwareClient class takes care of handling routing keys so that a derived class
 * can only have the logic for sending/receiving messages based on its intended message flow pattern.
 */
public class AMQPRoutingAwareClient extends AMQPClient {
    private static final Logger log = LoggerFactory.getLogger(AMQPClient.class);
    
    private static final String ELEMENT_EVENT = "event";
    private static final String ELEMENT_KEY = "key";
    private static final String ELEMENT_SEGMENT = "segment";
    private static final String ATTRIBUTE_NAME = "name";

    private HashMap<String, HashMap<String, AMQPRoutingKey>> eventRoutingKeys = new HashMap<String, HashMap<String, AMQPRoutingKey>>();

    /**
     * Create an instance of client.
     *
     * @param properties Connection properties.
     */
    public AMQPRoutingAwareClient(Properties properties) {
        super(properties);
    }

    /**
     * Initialize the client.
     *
     * @param routingKeys Routing key configuration.
     * @throws AMQPException on error.
     */
    public void init(Element routingKeys) throws AMQPException {
        if (routingKeys != null) {
            NodeList events = routingKeys.getElementsByTagName(ELEMENT_EVENT);
            for (int i = 0; i < events.getLength(); i++) {
                // event
                Element event = (Element)(events.item(i));
                String eventName = event.getAttribute(ATTRIBUTE_NAME).trim();
                if ((eventName == null) || (eventName.isEmpty()) || eventRoutingKeys.containsKey(eventName)) {
                    continue;
                }

                HashMap<String, AMQPRoutingKey> eventKeys = new HashMap<String, AMQPRoutingKey>();
                eventRoutingKeys.put(eventName, eventKeys);

                // keys
                NodeList keys = event.getElementsByTagName(ELEMENT_KEY);
                for (int j = 0; j < keys.getLength(); j++) {
                    Element key = (Element)(keys.item(j));
                    String keyName = key.getAttribute(ATTRIBUTE_NAME).trim();
                    if ((keyName == null) || (keyName.isEmpty()) || eventKeys.containsKey(keyName)) {
                        continue;
                    }

                    AMQPRoutingKey routingKey = new AMQPRoutingKey(eventName, keyName);
                    eventKeys.put(keyName, routingKey);

                    // segments
                    NodeList segments = key.getElementsByTagName(ELEMENT_SEGMENT);
                    for (int k = 0; k < segments.getLength(); k++) {
                        Element segment = (Element)(segments.item(k));
                        String segmentName = segment.getAttribute(ATTRIBUTE_NAME).trim();
                        if ((segmentName == null) || (segmentName.isEmpty()) || routingKey.containsSegment(segmentName)) {
                            continue;
                        }

                        String segmentExpression = segment.getTextContent().trim();
                        if (-1 != segmentExpression.indexOf('@')) {
                            // Attribute
                            routingKey.addEvaluatableAttributeSegment(segmentName, segmentExpression);
                        } else {
                            // Element
                            routingKey.addEvaluatableElementSegment(segmentName, segmentExpression);
                        }
                    }
                }
            }
        }
    }

    /**
     * Initialize client. Load routing configuration on its own.
     *
     * @throws AMQPException on error.
     */
    public void init() throws AMQPException {
        init(AMQPUtil.loadRoutingKeys());
    }

    /**
     * Check if a given message is routable as per routing configuration.
     *
     * @param message Message to be routed.
     * @return true if routable or false otherwise.
     */
    protected boolean isRoutable(OMElement message) {
        return eventRoutingKeys.containsKey(message.getLocalName());
    }

    /**
     * Evaluate the set of native routing keys for a given message.
     *
     * @param message Message for which the routing keys are required.
     * @param routingKeys Possible set of routing keys.
     */
    protected void getRoutingKeys(OMElement message, List<String> routingKeys) {
        HashMap<String, AMQPRoutingKey> eventKeys = eventRoutingKeys.get(message.getLocalName());
        if (eventKeys != null) {

            for (AMQPRoutingKey eventKey : eventKeys.values()) {
                try {
                    String routingKey = eventKey.evaluate(message);
                    if (!routingKey.isEmpty()) {
                        routingKeys.add(routingKey);
                    }
                } catch (JaxenException e) {
                    // Do nothing. The erroneous key will be ignored.
                }
            }
        }
    }
}
