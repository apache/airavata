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

import java.io.StringReader;
import java.sql.SQLException;
import java.util.List;

import org.apache.airavata.wsmg.commons.MsgBoxQNameConstants;
import org.apache.airavata.wsmg.commons.NameSpaceConstants;
import org.apache.airavata.wsmg.msgbox.Storage.MsgBoxStorage;
import org.apache.airavata.wsmg.msgbox.util.MsgBoxCommonConstants;
import org.apache.airavata.wsmg.msgbox.util.MsgBoxUtils;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.service.Lifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service class for MsgBoxService this get called by MsgBoxServiceMessageReceiverInOut with ProcessingContext
 */
public class MsgBoxServiceSkeleton implements Lifecycle {

    private static final Logger logger = LoggerFactory.getLogger(MsgBoxServiceSkeleton.class);
    private static final String TRUE = Boolean.toString(true);
    private static final String FALSE = Boolean.toString(false);
    private static final long SLEEP_TIME = 5 * 60 * 1000l; // 1 hour;
    private static OMFactory factory = OMAbstractFactory.getOMFactory();
    private MsgBoxStorage storage;
    private Thread deletingThread;
    private boolean stop;

    public void init(ServiceContext context) throws AxisFault {
        this.storage = (MsgBoxStorage) context.getConfigurationContext().getProperty(
                MsgBoxCommonConstants.MSGBOX_STORAGE);

        logger.info("Start clean up thread for messagebox");
        deletingThread = new Thread(new DeleteOldMessageRunnable());
        deletingThread.start();
    }

    public void destroy(ServiceContext context) {
        this.storage = null;

        // stop Deleting thread
        this.stop = true;
        this.deletingThread.interrupt();

        try {
            deletingThread.join();
        } catch (Exception e) {
            logger.error("Cannot shutdown cleanup thread", e);
        }
    }

    public OMElement createMsgBox() throws Exception {
        try {
            String createdMsgBoxId = storage.createMsgBox();

            logger.debug("MsgBox created:" + createdMsgBoxId);

            /*
             * Output response
             */
            OMElement dd = factory.createOMElement(MsgBoxQNameConstants.CREATE_MSGBOX_RESP_QNAME);
            OMElement url = factory.createOMElement(MsgBoxQNameConstants.MSG_BOXID_QNAME);
            url.setText(createdMsgBoxId);
            dd.addChild(url);
            return dd;
        } catch (Exception e) {
            logger.error("Error creating the message box", e);
            AxisFault f = new AxisFault("Error creating the message box", "6000", e);
            throw f;
        }

    }

    public OMElement storeMessages(String msgBoxAddr, String messageID, String soapAction, OMElement message)
            throws Exception {
        OMElement resp = factory.createOMElement(MsgBoxQNameConstants.STOREMSG_RESP_QNAME);
        OMElement status = factory.createOMElement(MsgBoxQNameConstants.MSGBOX_STATUS_QNAME);
        try {
            storage.putMessageIntoMsgBox(msgBoxAddr, messageID, soapAction, message);

            logger.debug("Put Message to MsgBox:" + msgBoxAddr + " with messageID:" + messageID);

            status.setText(TRUE);
        } catch (SQLException e) {
            logger.error("Error while storing message: " + message + " in msgbx: " + msgBoxAddr, e);
            status.setText(FALSE);

            // FIXME: Should we throw exception?? or client will read false
            // status
        }
        resp.addChild(status);
        resp.declareNamespace(NameSpaceConstants.MSG_BOX);
        return resp;
    }

    public OMElement takeMessages(String msgBoxAddr) throws Exception {
        try {
            OMElement respEl = factory.createOMElement(MsgBoxQNameConstants.TAKE_MSGBOX_RESP_QNAME);
            OMElement messageSet = factory.createOMElement(MsgBoxQNameConstants.MSGBOX_MESSAGE_QNAME);

            List<String> list = storage.takeMessagesFromMsgBox(msgBoxAddr);
            if (list != null && list.size() != 0) {
                for (String string : list) {
                    messageSet.addChild(MsgBoxUtils.reader2OMElement(new StringReader(string)));
                }
                logger.debug("Take all messages from MsgBox:" + msgBoxAddr);
            } else {
                logger.debug("  no messages..  ");
            }
            respEl.addChild(messageSet);
            respEl.declareNamespace(NameSpaceConstants.MSG_BOX);
            return respEl;
        } catch (Exception e) {
            logger.error("Error taking mesages of message box: " + msgBoxAddr, e);
            throw e;
        }
    }

    public OMElement destroyMsgBox(String msgBoxAddr) throws Exception {
        OMElement respEl = factory.createOMElement(MsgBoxQNameConstants.DESTROY_MSGBOX_RESP_QNAME);
        OMElement statusEl = factory.createOMElement(MsgBoxQNameConstants.MSGBOX_STATUS_QNAME);
        String addr = msgBoxAddr;
        try {
            storage.destroyMsgBox(addr);
            logger.debug("Destry MsgBox:" + msgBoxAddr);
            statusEl.setText(TRUE);
        } catch (Exception e) {
            logger.warn("Error while delete msgbx: " + msgBoxAddr, e);
            statusEl.setText(FALSE);

            // FIXME: Should we throw exception?? or client will read false
            // status
        }
        respEl.addChild(statusEl);
        return respEl;
    }

    class DeleteOldMessageRunnable implements Runnable {

        public void run() {
            while (!stop) {
                try {

                    // sleep
                    Thread.sleep(SLEEP_TIME);

                    // try to remove old message
                    if (storage != null) {
                        storage.removeAncientMessages();
                    }
                } catch (Exception e) {
                    logger.warn("Msgbox cleanup thread is interrupted to close");
                }
            }
        }

    }

}
