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

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.jaxen.JaxenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * AMQPRoutingKey represents an AMQP routing key. A key would consist of one or more segments where
 * a segment would be an element or an attribute of an event.
 */
public class AMQPRoutingKey {
    private static final Logger log = LoggerFactory.getLogger(AMQPRoutingKey.class);

    private String eventName = "";
    private String keyName = "";
    private List<Segment> segments = new ArrayList<Segment>();
    
    public AMQPRoutingKey(String eventName, String keyName) {
        this.eventName = eventName;
        this.keyName = keyName;
    }

    /**
     * Get associated event name.
     *
     * @return Event name.
     */
    public String getEventName() {
        return eventName;
    }

    /**
     * Set associated event name.
     *
     * @param eventName Event name.
     */
    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    /**
     * Get name of key.
     *
     * @return Key name.
     */
    public String getKeyName() {
        return keyName;
    }

    /**
     * Set name of key.
     *
     * @param keyName Key name.
     */
    public void setKeyName(String keyName) {
        this.keyName = keyName;
    }

    /**
     * Check if a segment already exists.
     *
     * @param name Name of the segment.
     * @return true if exists or false otherwise.
     */
    boolean containsSegment(String name) {
        boolean found = false;

        for (Segment segment : segments) {
            if (segment.getName().equals(name)) {
                found = true;
                break;
            }
        }

        return found;
    }

    /**
     * Add a segment.
     *
     * @param name Name of the segment.
     * @param value Value of the segment.
     * @throws AMQPException on duplicate segment.
     */
    public void addSegment(String name, String value) throws AMQPException {
        segments.add(new Segment(name, value));
    }

    /**
     * Add an evaluatable element segment.
     *
     * @param name Name of the element.
     * @param expression Expression that needs evaluating to retrieve the value of element.
     * @throws AMQPException on duplicate element.
     */
    public void addEvaluatableElementSegment(String name, String expression) throws AMQPException {
        try {
            segments.add(new EvaluatableElementSegment(name, expression));
        } catch (JaxenException e) {
            throw new AMQPException(e);
        }
    }

    /**
     * Add an evaluatable attribute segment.
     *
     * @param name Name of the attribute.
     * @param expression Expression that needs evaluating to retrieve the value of attribute.
     * @throws AMQPException on duplicate attribute.
     */
    public void addEvaluatableAttributeSegment(String name, String expression) throws AMQPException {
        try {
            segments.add(new EvaluatableAttributeSegment(name, expression));
        } catch (JaxenException e) {
            throw new AMQPException(e);
        }
    }

    /**
     * Generate native AMQP key using the segments.
     *
     * @return Routing key in native(AMQP) format.
     */
    public String getNativeKey() {
        String routingKey = !eventName.isEmpty() ? eventName : "*";

        boolean segmentsGiven = false;
        for (Segment segment : segments) {

            String segmentValue = segment.getValue().trim();
            if (!segmentValue.isEmpty()) {
                routingKey += ".";
                routingKey += segment.getValue();

                segmentsGiven = true;
            }
        }

        if (!segmentsGiven) {
            routingKey += ".";
            routingKey += "#";
        }

        return routingKey;
    }

    /**
     * Evaluate the routing key for a given message.
     *
     * @param message Message for which the routing key is required.
     * @return Routing key.
     * @throws JaxenException on expression evaluation error.
     */
    public String evaluate(OMElement message) throws JaxenException {
        String routingKey = eventName;

        for (Segment segment : segments) {

            if (segment instanceof EvaluatableSegment) {
                routingKey += ".";
                routingKey += ((EvaluatableSegment)segment).evaluate(message);
            }
        }

        return routingKey;
    }

    /**
     * Segment provides a base implementation for segments. This class could be extended
     * by a particular type of segment(element/attribute) based on its specific requirements.
     */
    private class Segment {

        private String name = "";
        private String value = "";

        /**
         * Create an instance of segment.
         *
         * @param name Name of segment.
         */
        public Segment(String name) {
            this.name = name;
        }

        /**
         * Create an instance of segment.
         *
         * @param name Name of segment.
         * @param value Value of segment.
         */
        public Segment(String name, String value) {
            this.name = name;
            this.value = value;
        }

        /**
         * Get name of segment.
         *
         * @return  Name.
         */
        public String getName() {
            return name;
        }

        /**
         * Set name of segment.
         *
         * @param name Name to be set.
         */
        public void setName(String name) {
            this.name = name;
        }

        /**
         * Get value of segment.
         *
         * @return  Value.
         */
        public String getValue() {
            return value;
        }

        /**
         * Set value of segment.
         *
         * @param value Value to be set.
         */
        public void setValue(String value) {
            this.value = value;
        }
    }

    /**
     * EvaluatableSegment provides a base implementation for segments that are evaluated on the fly
     * based on an incoming event. This class could be extended by a particular type of segment(element/attribute)
     * based on its specific requirements.
     */
    private abstract class EvaluatableSegment extends Segment {

        private static final String NAMESPACE_PREFIX = "ns";
        private static final String NAMESPACE_URL = "http://airavata.apache.org/schemas/wft/2011/08";

        protected AXIOMXPath xpathProcessor = null;

        /**
         * Create an instance of EvaluatableSegment.
         *
         * @param name Name of segment.
         * @param expression Expression that needs evaluating to retrieve the value of segment.
         * @throws JaxenException on expression evalution error.
         */
        protected EvaluatableSegment(String name, String expression) throws JaxenException {
            super(name);

            xpathProcessor = new AXIOMXPath(getNormalizedExpression(expression));
            xpathProcessor.addNamespace(NAMESPACE_PREFIX, NAMESPACE_URL);
        }

        /**
         * Normalize an expression to include namespace.
         *
         * @param expression Expression to be normalized.
         * @return Normalized expression.
         */
        private String getNormalizedExpression(String expression) {
            try {
                StringBuffer normalizedExpression = new StringBuffer();
                normalizedExpression.append(NAMESPACE_PREFIX);
                normalizedExpression.append(":");

                expression = expression.trim();
                for (int i = 0; i < expression.length(); i++) {
                    char c = expression.charAt(i);

                    normalizedExpression.append(c);
                    if (((c == '/') && (expression.charAt(i + 1) != '@')) || (c == '@')) {
                        normalizedExpression.append(NAMESPACE_PREFIX);
                        normalizedExpression.append(":");
                    }
                }

                return normalizedExpression.toString();
            } catch (ArrayIndexOutOfBoundsException e) {
                return "";
            }
        }

        /**
         * Returns value of segment.
         *
         * @param message Message from which the data is extracted.
         * @return Value of segment.
         * @throws JaxenException on error.
         */
        public abstract String evaluate(OMElement message) throws JaxenException;
    }

    /**
     * EvaluatableElementSegment is the EvaluatableSegment extension for event elements.
     */
    private class EvaluatableElementSegment extends EvaluatableSegment {

        public EvaluatableElementSegment(String name, String expression) throws JaxenException {
            super(name, expression);
        }

        @Override
        public String evaluate(OMElement message) throws JaxenException {
            String value = "";
            
            OMElement element = (OMElement)xpathProcessor.selectSingleNode(message);
            if (element != null) {
                value = element.getText();
            }

            return value;
        }
    }

    /**
     * EvaluatableAttributeSegment is the EvaluatableSegment extension for event attributes.
     */
    private class EvaluatableAttributeSegment extends EvaluatableSegment {

        public EvaluatableAttributeSegment(String name, String expression) throws JaxenException {
            super(name, expression);
        }

        @Override
        public String evaluate(OMElement message) throws JaxenException {
            String value = "";
            
            OMAttribute attribute = (OMAttribute)xpathProcessor.selectSingleNode(message);
            if (attribute != null) {
                value = attribute.getAttributeValue();
            }
            
            return value;
        }
    }
}
