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

package org.apache.airavata.wsmg.broker.wsnotification;

import org.apache.airavata.wsmg.broker.AbstractBrokerMsgReceiver;
import org.apache.airavata.wsmg.broker.context.ProcessingContext;
import org.apache.airavata.wsmg.commons.WsmgCommonConstants;
import org.apache.airavata.wsmg.commons.WsmgNameSpaceConstants;
import org.apache.airavata.wsmg.config.WsmgConfigurationContext;
import org.apache.airavata.wsmg.util.WsNotificationOperations;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * BrokerServiceMessageReceiverInOut message receiver
 */

public class WSNotificationMsgReceiver extends AbstractBrokerMsgReceiver {

    private static final Logger log = LoggerFactory.getLogger(WSNotificationMsgReceiver.class);

    WSNotificationProcessingContextBuilder builder = new WSNotificationProcessingContextBuilder();

    public MessageContext process(MessageContext inMsg, String operationName) throws AxisFault {

        WsNotificationOperations msgType = WsNotificationOperations.valueFrom(operationName);

        ProcessingContext processingContext = builder.build(inMsg, msgType);

        MessageContext outputMsg = null;

        switch (msgType) {
        case NOTIFY: {
            try {

                WsmgConfigurationContext brokerConfigContext = (WsmgConfigurationContext) inMsg
                        .getConfigurationContext().getProperty(WsmgCommonConstants.BROKER_WSMGCONFIG);

                brokerConfigContext.getNotificationProcessor().processMsg(processingContext,
                        WsmgNameSpaceConstants.WSNT_NS);
            } catch (Exception e) {
                throw new AxisFault("unable to process message", e);
            }
            outputMsg = createOutputMessageContext(inMsg, processingContext);
            break;
        }
        case SUBSCRIBE: {

            WsmgConfigurationContext brokerConfigContext = (WsmgConfigurationContext) inMsg.getConfigurationContext()
                    .getProperty(WsmgCommonConstants.BROKER_WSMGCONFIG);
            brokerConfigContext.getSubscriptionManager().subscribe(processingContext);
            outputMsg = createOutputMessageContext(inMsg, processingContext);
            break;
        }
        case UNSUBSCRIBE: {

            WsmgConfigurationContext brokerConfigContext = (WsmgConfigurationContext) inMsg.getConfigurationContext()
                    .getProperty(WsmgCommonConstants.BROKER_WSMGCONFIG);

            brokerConfigContext.getSubscriptionManager().unsubscribe(processingContext);
            outputMsg = createOutputMessageContext(inMsg, processingContext);
            break;
        }
        case RESUME_SUBSCRIPTION: {

            WsmgConfigurationContext brokerConfigContext = (WsmgConfigurationContext) inMsg.getConfigurationContext()
                    .getProperty(WsmgCommonConstants.BROKER_WSMGCONFIG);

            brokerConfigContext.getSubscriptionManager().resumeSubscription(processingContext);
            outputMsg = createOutputMessageContext(inMsg, processingContext);
            break;
        }
        case PAUSE_SUBSCRIPTION: {
            WsmgConfigurationContext brokerConfigContext = (WsmgConfigurationContext) inMsg.getConfigurationContext()
                    .getProperty(WsmgCommonConstants.BROKER_WSMGCONFIG);

            brokerConfigContext.getSubscriptionManager().pauseSubscription(processingContext);
            outputMsg = createOutputMessageContext(inMsg, processingContext);
            break;
        }
        case GET_CURRENT_MSG:
        default:
            throw new AxisFault("not implemented yet");
        }

        return outputMsg;
    }

}// end of class
