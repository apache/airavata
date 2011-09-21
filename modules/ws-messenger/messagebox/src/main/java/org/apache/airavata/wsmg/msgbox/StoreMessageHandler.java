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

package org.apache.airavata.wsmg.msgbox;

import java.util.List;

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

/**
 * This Dispatcher is used to validate the incoming message, this is set to Handler list in MsgBoxServiceLifeCycle.
 */
public class StoreMessageHandler extends AddressingBasedDispatcher {
    private static final Logger logger = LoggerFactory.getLogger(StoreMessageHandler.class);
    private static final String WSMG_MSGSTORE_SOAP_ACTION = "http://org.apache.airavata/xgws/msgbox/2004/storeMessages";
    private static final String ADDRESSING_VALIDATE_ACTION = "addressing.validateAction";
    
    private Phase addressingPhase;
    private AxisOperation messageBoxOperation;

    public org.apache.axis2.engine.Handler.InvocationResponse invoke(MessageContext msgContext) throws AxisFault {

        InvocationResponse response = InvocationResponse.CONTINUE;
        if (msgContext.getAxisService() == null || msgContext.getAxisOperation() == null) {

            boolean validateAction = JavaUtils.isTrue(msgContext.getProperty(ADDRESSING_VALIDATE_ACTION), true);
            msgContext.setProperty(ADDRESSING_VALIDATE_ACTION, Boolean.valueOf(false));

            response = super.invoke(msgContext);

            if (isForMessageBoxService(msgContext))
                validateMsgBoxStoreOperation(msgContext);
            if (validateAction)
                checkAction(msgContext);
            msgContext.setProperty(ADDRESSING_VALIDATE_ACTION, Boolean.valueOf(validateAction));

        }
        return response;
    }

    private void validateMsgBoxStoreOperation(MessageContext msgContext) {
        if (msgContext.getAxisOperation() == null) {
            AxisService service = msgContext.getAxisService();
            AxisOperation storeMsgOperation = getMessageBoxOperation(service);

            msgContext.setAxisOperation(storeMsgOperation);
        }
    }

    private boolean isForMessageBoxService(MessageContext msgContext) {
        return msgContext.getAxisService() != null && msgContext.getAxisService().getName().equals("MsgBoxService");
    }

    private AxisOperation getMessageBoxOperation(AxisService msgBoxService) {
        if (messageBoxOperation == null)
            messageBoxOperation = msgBoxService.getOperationBySOAPAction(WSMG_MSGSTORE_SOAP_ACTION);
        return messageBoxOperation;
    }

    private void checkAction(MessageContext msgContext) throws AxisFault {

        Phase addPhase = getAddressingPhase(msgContext);

        if (addPhase == null) {
            logger.error("unable to locate addressing phase object");
        }

        if (msgContext.getCurrentPhaseIndex() + 1 == addPhase.getHandlerCount()) {
            if (msgContext.getAxisService() == null || msgContext.getAxisOperation() == null)
                AddressingFaultsHelper.triggerActionNotSupportedFault(msgContext, msgContext.getWSAAction());
        }

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

}
