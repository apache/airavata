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

package org.apache.airavata.wsmg.broker.handler;

import java.util.List;

import org.apache.airavata.wsmg.commons.WsmgCommonConstants;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.AddressingFaultsHelper;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.dispatchers.AddressingBasedDispatcher;
import org.apache.axis2.engine.Phase;
import org.apache.axis2.util.JavaUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PublishedMessageHandler extends AddressingBasedDispatcher {

    private static final Logger logger = LoggerFactory.getLogger(PublishedMessageHandler.class);

    private static final String ADDRESSING_VALIDATE_ACTION = "addressing.validateAction";

    private AxisOperation publishOperation = null;

    private Phase addressingPhase = null;

    public InvocationResponse invoke(MessageContext msgContext) throws AxisFault {

        InvocationResponse response = InvocationResponse.CONTINUE;

        if (msgContext.getAxisService() == null || msgContext.getAxisOperation() == null) {
            boolean validateAction = JavaUtils.isTrue(msgContext.getProperty(ADDRESSING_VALIDATE_ACTION), true);
            msgContext.setProperty(ADDRESSING_VALIDATE_ACTION, Boolean.valueOf(false));
            response = super.invoke(msgContext);
            if (isForBrokerEventingService(msgContext))
                validateBrokerWSEventingOperation(msgContext);
            if (validateAction)
                checkAction(msgContext);
            msgContext.setProperty(ADDRESSING_VALIDATE_ACTION, Boolean.valueOf(validateAction));

        }

        return response;
    }

    private void validateBrokerWSEventingOperation(MessageContext msgContext) {
        if (msgContext.getAxisOperation() == null) {
            AxisService service = msgContext.getAxisService();
            AxisOperation pubOperation = getPublishOperation(service);
            msgContext.setAxisOperation(pubOperation);
        }
    }

    private boolean isForBrokerEventingService(MessageContext msgContext) {
        return msgContext.getAxisService() != null && msgContext.getAxisService().getName().equals("EventingService");
    }

    private AxisOperation getPublishOperation(AxisService publisherService) {
        if (publishOperation == null)
            publishOperation = publisherService.getOperationBySOAPAction(WsmgCommonConstants.WSMG_PUBLISH_SOAP_ACTION);
        return publishOperation;
    }

    private Phase getAddressingPhase(MessageContext context) {

        if (addressingPhase == null) {

            List<Phase> inFlowPhases = context.getConfigurationContext().getAxisConfiguration().getPhasesInfo()
                    .getINPhases();

            for (Phase p : inFlowPhases) {
                if (p.getName().equalsIgnoreCase("Addressing")) {
                    addressingPhase = p;
                }
            }

        }

        return addressingPhase;

    }

    private void checkAction(MessageContext msgContext) throws AxisFault {

        Phase addPhase = getAddressingPhase(msgContext);

        if (addPhase == null) {
            logger.error("unable to locate addressing phase object");
        }
        if (msgContext != null) {
            if (msgContext.getCurrentPhaseIndex() + 1 == addPhase.getHandlerCount()) {
                if (msgContext.getAxisService() == null || msgContext.getAxisOperation() == null)
                    AddressingFaultsHelper.triggerActionNotSupportedFault(msgContext, msgContext.getWSAAction());
            }
        }

    }

}
