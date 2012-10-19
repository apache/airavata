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

package org.apache.airavata.wsmg.gui;

import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import org.apache.airavata.wsmg.client.ConsumerNotificationHandler;
import org.apache.airavata.wsmg.commons.NameSpaceConstants;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class WsntViewerConsumerNotificationHandler implements ConsumerNotificationHandler {
    private static final Log logger = LogFactory.getLog(WsntViewerConsumerNotificationHandler.class);

    public NotificationViewerFrame frame = null;

    public WsntViewerConsumerNotificationHandler() {
    }

    public WsntViewerConsumerNotificationHandler(NotificationViewerFrame f) {
        this.frame = f;
    }

    public void handleNotification(SOAPEnvelope msgEnvelope) {
        logger.debug("*******lead message handler Received message********");
        logger.debug(msgEnvelope.toString());
        try {
            addBriefMsg(msgEnvelope);
            addWholeMsg(msgEnvelope);
        } catch (XMLStreamException e) {
            logger.error("invalid message received", e);
            throw new RuntimeException("invalid message recieved", e);
        }
    }

    private void addBriefMsg(SOAPEnvelope env) throws XMLStreamException {

        QName notify = new QName(NameSpaceConstants.WSNT_NS.getNamespaceURI(), "Notify",
                NameSpaceConstants.WSNT_NS.getPrefix());

        QName notifyMsg = new QName(NameSpaceConstants.WSNT_NS.getNamespaceURI(), "NotificationMessage",
                NameSpaceConstants.WSNT_NS.getPrefix());

        QName msg = new QName(NameSpaceConstants.WSNT_NS.getNamespaceURI(), "Message",
                NameSpaceConstants.WSNT_NS.getPrefix());

        if (env.getBody() == null) {
            throw new RuntimeException("invalid soap envelope - no soap body");
        }

        Iterator ite = env.getBody().getChildrenWithName(notify);

        if (!ite.hasNext()) {
            throw new RuntimeException(notify.getLocalPart() + " tag is not found");
        }

        OMElement ele = (OMElement) ite.next();
        ite = ele.getChildrenWithName(notifyMsg);

        if (!ite.hasNext()) {
            throw new RuntimeException(notifyMsg.getLocalPart() + " tag is not found");
        }

        ele = (OMElement) ite.next();

        ite = ele.getChildrenWithName(msg);

        if (!ite.hasNext()) {
            throw new RuntimeException(msg.getLocalPart() + " tags are not found");
        }

        while (ite.hasNext()) {
            ele = (OMElement) ite.next();

            if (ele.getFirstElement() != null) {
                frame.addBriefMessage(ele.getFirstElement().toString());
            } else {
                throw new RuntimeException("raw message is not found");
            }

        }

    }

    private void addWholeMsg(SOAPEnvelope envelope) throws XMLStreamException {
        frame.addWholeMessage(envelope.toStringWithConsume());

    }

}
