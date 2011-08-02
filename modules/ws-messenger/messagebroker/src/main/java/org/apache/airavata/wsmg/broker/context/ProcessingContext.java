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

package org.apache.airavata.wsmg.broker.context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.airavata.wsmg.broker.subscription.SubscriptionState;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.context.MessageContext;

public class ProcessingContext {

    private Map<ContextParameterInfo<? extends Object>, Object> contextInfo = new HashMap<ContextParameterInfo<? extends Object>, Object>();

    private List<OMNamespace> responseMsgNameSpaces;

    private MessageContext messageContext = null;

    private SOAPEnvelope envelope; // Used for WSe notification messages.topics
    // are
    // in header.

    private OMElement respMessage;

    private SubscriptionState subscription = null;

    public SOAPEnvelope getEnvelope() {
        return envelope;
    }

    public void setEnvelope(SOAPEnvelope envelope) {
        this.envelope = envelope;
    }

    public SOAPBody getSoapBody() {

        return envelope.getBody();
    }

    public OMElement getRespMessage() {
        return respMessage;
    }

    public void setRespMessage(OMElement respMessage) {
        this.respMessage = respMessage;
    }

    public SubscriptionState getSubscription() {
        return subscription;
    }

    public void setSubscription(SubscriptionState subscription) {
        this.subscription = subscription;
    }

    public void setMessageConext(MessageContext msgContext) {
        this.messageContext = msgContext;
    }

    public MessageContext getMessageContext() {
        return messageContext;
    }

    public void addResponseMsgNameSpaces(OMNamespace ns) {

        if (responseMsgNameSpaces == null) {
            responseMsgNameSpaces = new ArrayList<OMNamespace>();
        }

        if (!responseMsgNameSpaces.contains(ns)) {
            responseMsgNameSpaces.add(ns);
        }
    }

    public List<OMNamespace> getResponseMsgNamespaces() {
        return responseMsgNameSpaces;
    }

    public void setContextParameter(ContextParameterInfo<?> name, Object value) {
        contextInfo.put(name, value);
    }

    public <T> T getContextParameter(ContextParameterInfo<T> name) {

        Object o = contextInfo.get(name);
        return name.cast(o);

    }
}
