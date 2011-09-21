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

import org.apache.airavata.wsmg.msgbox.util.MsgBoxOperations;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.receivers.AbstractInOutMessageReceiver;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.util.JavaUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MsgBoxServiceMessageReceiverInOut message receiver, this is the actual
 * location where the service operations get invoked.
 */

public class MsgBoxServiceMessageReceiverInOut extends AbstractInOutMessageReceiver {

    private static Logger logger = LoggerFactory.getLogger(MsgBoxServiceMessageReceiverInOut.class);

    public void invokeBusinessLogic(MessageContext inMsgContext, MessageContext outMsgContext) throws AxisFault {

        // get the implementation class for the Web Service
        MsgBoxServiceSkeleton skel = (MsgBoxServiceSkeleton)getTheImplementationObject(inMsgContext);        
        
        OMElement response = null;
        
        try {
            
            String operationName = getOperationName(inMsgContext);
            MsgBoxOperations msgType = MsgBoxOperations.valueFrom(operationName);
            
            switch (msgType) {

            case STORE_MSGS: {
                SOAPEnvelope enlp = inMsgContext.getEnvelope();
                OMElement message = enlp.getBody().getFirstElement();
                String msgBoxId = getClientId(inMsgContext);
                String messageId = inMsgContext.getMessageID();
                String soapAction = inMsgContext.getSoapAction();
                response = skel.storeMessages(msgBoxId, messageId, soapAction, message);
                break;
            }

            case DESTROY_MSGBOX: {
                String msgBoxId = getClientId(inMsgContext);
                response = skel.destroyMsgBox(msgBoxId);
                break;
            }

            case TAKE_MSGS: {
                String msgBoxId = getClientId(inMsgContext);
                response = skel.takeMessages(msgBoxId);
                break;
            }

            case CREATE_MSGBOX: {
                response = skel.createMsgBox();
                break;
            }
            default:
                throw new AxisFault("unsupported operation" + msgType.toString());
            }

        } catch (AxisFault afe) {
            throw afe;
        } catch (Exception e) {
            logger.error("Exception", e);
            throw new AxisFault("Exception in Message Box ", e);
        }

        /*
         * Output
         */
        SOAPFactory soapFactory = getSOAPFactory(inMsgContext);
        SOAPEnvelope envelope = toEnvelope(soapFactory, response);
        outMsgContext.setEnvelope(envelope);
        outMsgContext.getOptions().setProperty(HTTPConstants.CHUNKED, Boolean.FALSE);
    }

    private String getClientId(MessageContext inMsg) throws AxisFault{        
        String toAddress = inMsg.getTo().getAddress();
        int biginIndex = toAddress.indexOf("clientid");
        if (biginIndex == -1) {
            throw new AxisFault("clientid cannot be found");
        }
        String clientId = toAddress.substring(biginIndex + "clientid".length() + 1);
        return clientId;
    }
    
    private SOAPEnvelope toEnvelope(SOAPFactory factory, OMElement response) {
        SOAPEnvelope envelop = factory.getDefaultEnvelope();
        envelop.getBody().addChild(response);
        return envelop;
    }

    protected String getOperationName(MessageContext inMsg) throws AxisFault {

        AxisOperation op = inMsg.getOperationContext().getAxisOperation();
        if (op == null) {
            throw new AxisFault(
                    "Operation is not located, if this is doclit style the SOAP-ACTION should specified via the SOAP Action to use the RawXMLProvider");
        }

        String operationName = null;
        if ((op.getName() == null) || ((operationName = JavaUtils.xmlNameToJava(op.getName().getLocalPart())) == null)) {
            throw new AxisFault("invalid operation found");
        }

        return operationName;
    }

}
