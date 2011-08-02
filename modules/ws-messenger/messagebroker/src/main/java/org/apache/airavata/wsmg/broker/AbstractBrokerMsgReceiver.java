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

package org.apache.airavata.wsmg.broker;

import java.util.List;

import org.apache.airavata.wsmg.broker.context.ProcessingContext;
import org.apache.airavata.wsmg.commons.WsmgNameSpaceConstants;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.receivers.AbstractMessageReceiver;
import org.apache.axis2.util.MessageContextBuilder;

public abstract class AbstractBrokerMsgReceiver extends AbstractMessageReceiver {

    protected abstract MessageContext process(MessageContext inMsgContext, String operationName) throws AxisFault;

    @Override
    protected void invokeBusinessLogic(MessageContext inMsgContext) throws AxisFault {

        String operationName = getOperationName(inMsgContext);
        MessageContext outMsgContext = process(inMsgContext, operationName);

        if (outMsgContext != null) {
            outMsgContext.setTo(null);
            super.replicateState(inMsgContext);
            AxisEngine.send(outMsgContext);

        }

    }

    protected String getOperationName(MessageContext inMsg) throws AxisFault {

        org.apache.axis2.description.AxisOperation op = inMsg.getOperationContext().getAxisOperation();
        if (op == null) {
            throw new AxisFault(
                    "Operation is not located, if this is doclit style the SOAP-ACTION should specified via the SOAP Action to use the RawXMLProvider");
        }

        java.lang.String operationName = null;
        if ((op.getName() == null)
                || ((operationName = org.apache.axis2.util.JavaUtils.xmlNameToJava(op.getName().getLocalPart())) == null)) {
            throw new AxisFault("invalid operation found");
        }

        return operationName;
    }

    protected MessageContext createOutputMessageContext(MessageContext inMsg, ProcessingContext processingContext)
            throws AxisFault {

        MessageContext outMsgContext = MessageContextBuilder.createOutMessageContext(inMsg);
        outMsgContext.getOperationContext().addMessageContext(outMsgContext);

        SOAPEnvelope outputEnvelope = getSOAPFactory(inMsg).getDefaultEnvelope();

        if (processingContext.getRespMessage() != null) {

            outputEnvelope.getBody().addChild(processingContext.getRespMessage());

            if (processingContext.getResponseMsgNamespaces() != null) {
                declareResponseMsgNamespace(outputEnvelope, processingContext.getResponseMsgNamespaces());
            }
        }

        outMsgContext.setEnvelope(outputEnvelope);
        return outMsgContext;
    }

    private void declareResponseMsgNamespace(SOAPEnvelope outputEnvelope, List<OMNamespace> namespaces) {

        if (!namespaces.contains(WsmgNameSpaceConstants.WSA_NS)) {
            namespaces.add(WsmgNameSpaceConstants.WSA_NS);// declare WSA by default
        }

        for (OMNamespace ns : namespaces) {
            outputEnvelope.declareNamespace(ns);
        }

    }

}
