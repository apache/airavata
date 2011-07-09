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

import org.apache.airavata.wsmg.broker.subscription.SubscriptionState;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.addressing.EndpointReference;

public class ContextParameters {

    private static <V> ContextParameterInfo<V> createParam(Class<V> c, String name) {
        ContextParameterInfo<V> info = new ContextParameterInfo<V>(c, name);

        return info;
    }

    public static ContextParameterInfo<String> RESOURCE_ID = createParam(String.class, "resourceID");

    public static ContextParameterInfo<String> SUB_ID = createParam(String.class, "subID");

    public static ContextParameterInfo<String> TOPIC_FROM_URL = createParam(String.class, "topicFromUrl");

    public static ContextParameterInfo<String> SOAP_ACTION = createParam(String.class, "soapAction");

    public static ContextParameterInfo<SubscriptionState> SUBSCRIPTION = createParam(SubscriptionState.class,
            "subscription");

    public static ContextParameterInfo<String> SUBSCRIBER_EXPIRES = createParam(String.class, "subscriberExpires");

    public ContextParameterInfo<String> USE_NOTIFY_TEXT = createParam(String.class, "useNotifyText");

    public static ContextParameterInfo<OMElement> USE_NOTIFY_ELEMENT = createParam(OMElement.class, "useNotifyEl");

    public static ContextParameterInfo<OMElement> NOTIFY_TO_ELEMENT = createParam(OMElement.class, "NotifyTo");

    public static ContextParameterInfo<EndpointReference> NOTIFY_TO_EPR = createParam(EndpointReference.class,
            "NotifyToEPR");

    public static ContextParameterInfo<OMElement> SUB_POLICY = createParam(OMElement.class, "subPolicy");

    public static ContextParameterInfo<OMElement> FILTER_ELEMENT = createParam(OMElement.class, "filterElement");

    public static ContextParameterInfo<OMElement> TOPIC_EXPRESSION_ELEMENT = createParam(OMElement.class,
            "topicExpressionEl");

    public static ContextParameterInfo<OMElement> XPATH_ELEMENT = createParam(OMElement.class, "xpathEl");

    public static ContextParameterInfo<OMElement> SUBSCRIBE_ELEMENT = createParam(OMElement.class, "subscribeElement");

    public static ContextParameterInfo<EndpointReference> SUBSCRIBE_ELEMENT_EPR = createParam(EndpointReference.class,
            "subscribeElement");

}
