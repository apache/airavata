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

import java.util.Iterator;

import org.apache.airavata.wsmg.commons.WsmgCommonConstants;
import org.apache.airavata.wsmg.util.WsmgUtil;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axis2.context.MessageContext;
import org.apache.log4j.Logger;

public class ProcessingContextFactory implements ContextFactory {

    // TODO : change this class to search for only required parameters of each
    // operation. and after finding out all parameter stop immediately with out
    // traversing entire xml

    Logger log = Logger.getLogger(ProcessingContextFactory.class);

    public ProcessingContext create(OMElement element) {

        ProcessingContext ctx = new ProcessingContext();
        /*
         * OMDescendantsIterator ite = new OMDescendantsIterator(element); Object next = null; while (ite.hasNext()) {
         * next = ite.next(); OMNode node = (OMNode) next; if (node instanceof OMElement) { OMElement omElement =
         * (OMElement) node; String nameSpaceURI = omElement.getQName().getNamespaceURI(); String localPart =
         * omElement.getQName().getLocalPart();
         * 
         * // in this if else ladder input in the order else upper level // wont execute
         * 
         * if (nameSpaceURI .equalsIgnoreCase(org.apache.axiom.soap.SOAP11Version .getSingleton().getEnvelopeURI()) &&
         * localPart.equalsIgnoreCase("Envelope")) {
         * 
         * log.debug("setting envelope"); ctx.setEnvelope((SOAPEnvelope) omElement);
         * 
         * continue; }
         * 
         * if (nameSpaceURI .equalsIgnoreCase(org.apache.axiom.soap.SOAP12Version .getSingleton().getEnvelopeURI()) &&
         * localPart.equalsIgnoreCase("Envelope")) {
         * 
         * log.debug("setting envelope"); ctx.setEnvelope((SOAPEnvelope) omElement); continue; }
         * 
         * if (localPart.equalsIgnoreCase("Subscribe") || localPart.equalsIgnoreCase("SubscribeRequest")) {
         * log.debug("setting subscribe"); ctx.setSubscribeElement(omElement); continue; }
         * 
         * if (nameSpaceURI.equalsIgnoreCase(WsmgNameSpaceConstants.WSE .getNamespaceURI()) &&
         * localPart.equalsIgnoreCase("Expires")) { log.debug("setting expires");
         * ctx.setSubscriberExpTime(omElement.getText()); continue; }
         * 
         * if (localPart.equalsIgnoreCase("ReplyTo")) { log.debug("setting reply to"); ctx .setReplyToEPR(new
         * EndpointReference(omElement .getText())); continue; }
         * 
         * if (nameSpaceURI.equalsIgnoreCase(WsmgNameSpaceConstants.WSE .getNamespaceURI()) &&
         * localPart.equalsIgnoreCase("Filter")) { log.debug("setting filter"); ctx.setFilterElement(omElement); }
         * 
         * if (nameSpaceURI .equalsIgnoreCase(WsmgNameSpaceConstants.WSNT_NS .getNamespaceURI()) &&
         * localPart.equalsIgnoreCase("UseNotify")) { log.debug("setting useNotify"); ctx.setUseNotifyEl(omElement);
         * continue; }
         * 
         * if (nameSpaceURI .equalsIgnoreCase(WsmgNameSpaceConstants.WSNT_NS .getNamespaceURI()) &&
         * localPart.equalsIgnoreCase("SubscriptionPolicy")) { log.debug("setting subscriptionPolicy");
         * ctx.setSubPolicy(omElement); continue; }
         * 
         * if (nameSpaceURI .equalsIgnoreCase(WsmgNameSpaceConstants.WSNT_NS .getNamespaceURI()) &&
         * localPart.equalsIgnoreCase("TopicExpression")) { log.debug("setting TopicExpression");
         * ctx.setTopicExpressionEl(omElement); continue; }
         * 
         * if (nameSpaceURI .equalsIgnoreCase(WsmgNameSpaceConstants.WSNT_NS .getNamespaceURI()) &&
         * localPart.equalsIgnoreCase("Selector")) { log.debug("setting Selector"); ctx.setXpathEl(omElement); continue;
         * }
         * 
         * if (localPart.equalsIgnoreCase("NotifyTo")) { log.debug("setting consumer epr"); ctx.setNotifyTo(omElement);
         * try { ctx.setNotifyToEPR(EndpointReferenceHelper .fromOM(omElement)); } catch (AxisFault e) { // TODO Throw
         * this log.fatal("invalid epr", e);
         * 
         * } continue; }
         * 
         * if (localPart.equalsIgnoreCase("ConsumerReference")) { log.debug("setting consumer epr");
         * ctx.setNotifyTo(omElement); try { ctx.setNotifyToEPR(EndpointReferenceHelper .fromOM(omElement)); } catch
         * (AxisFault e) { // TODO Auto-generated catch block log.fatal("invalid epr", e); } continue; }
         * 
         * } }
         */
        return ctx;

    }

    private void extractInfoFromHeader(ProcessingContext context, SOAPHeader header) {

        // exteact Identifier

        Iterator ite = header.getChildrenWithLocalName(WsmgCommonConstants.SUBSCRIPTION_ID);
        if (ite.hasNext()) {
            OMElement identifier = (OMElement) ite.next();
            log.debug("extracted identifier " + identifier.getText());

            context.setContextParameter(ContextParameters.SUB_ID, identifier.getText());

        }

    }

    public ProcessingContext createProcessingContext(org.apache.axiom.soap.SOAPEnvelope elem) {

        ProcessingContext context = create(elem);
        if (elem.getHeader() != null)
            extractInfoFromHeader(context, elem.getHeader());
        // else
        // extractInfoFromBody(context, elem.getBody());
        return context;
    }

    public ProcessingContext createProcessingContext(OMElement elem) {

        return create(elem);
    }

    public ProcessingContext createProcessingContext(MessageContext msgContext) {
        ProcessingContext context = createProcessingContext(msgContext.getEnvelope());
        context.setMessageConext(msgContext);

        String topicFromUrl = WsmgUtil.getTopicFromRequestPath(msgContext.getTo().getAddress());
        context.setContextParameter(ContextParameters.TOPIC_FROM_URL, topicFromUrl);

        return context;
    }
}
