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

package org.apache.airavata.wsmg.broker.wseventing;

import org.apache.airavata.wsmg.broker.AbstractBrokerMsgReceiver;
import org.apache.airavata.wsmg.broker.context.ProcessingContext;
import org.apache.airavata.wsmg.commons.WsmgCommonConstants;
import org.apache.airavata.wsmg.commons.NameSpaceConstants;
import org.apache.airavata.wsmg.config.WsmgConfigurationContext;
import org.apache.airavata.wsmg.util.WsEventingOperations;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;

public class WSEventingPublishMsgReceiver extends AbstractBrokerMsgReceiver {

    WSEProcessingContextBuilder builder = new WSEProcessingContextBuilder();

    @Override
    protected MessageContext process(MessageContext inMsgContext, String operationName) throws AxisFault {

        ProcessingContext processingContext = builder.build(inMsgContext, WsEventingOperations.PUBLISH);

        try {

            WsmgConfigurationContext brokerConfigContext = (WsmgConfigurationContext) inMsgContext
                    .getConfigurationContext().getProperty(WsmgCommonConstants.BROKER_WSMGCONFIG);

            brokerConfigContext.getNotificationProcessor().processMsg(processingContext, NameSpaceConstants.WSE_NS);
        } catch (Exception e) {
            throw new AxisFault("unable to process message", e);
        }
        return createOutputMessageContext(inMsgContext, processingContext);
    }

    @Override
    protected MessageContext createOutputMessageContext(MessageContext inMsg, ProcessingContext processingContext)
            throws AxisFault {

        MessageContext outputContext = null;

        OMElement responseMessage = processingContext.getRespMessage();
        if (responseMessage != null) {

            outputContext = super.createOutputMessageContext(inMsg, processingContext);

            String responseAction = String.format("%s/%s", NameSpaceConstants.WSE_NS.getNamespaceURI(),
                    responseMessage.getLocalName());

            outputContext.setSoapAction(responseAction);
        }

        return outputContext;
    }
}
