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

package org.apache.airavata.xbaya.test.service.approver;

import java.util.Random;

import org.apache.airavata.common.utils.XMLUtil;
import org.apache.airavata.xbaya.test.service.ServiceNotificationSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlNamespace;

import xsul.XmlConstants;

public class ApproverImpl implements Approver {

    private final static Logger logger = LoggerFactory.getLogger(ApproverImpl.class);

    /**
     * @see org.apache.airavata.xbaya.test.service.approver.Approver#approve(org.xmlpull.v1.builder.XmlElement)
     */
    public XmlElement approve(XmlElement inputElement) {
        logger.info(XMLUtil.xmlElementToString(inputElement));
        ServiceNotificationSender notifier = ServiceNotificationSender.invoked(inputElement);

        XmlElement amountElement = inputElement.requiredElement(null, "amount");
        String amountString = amountElement.requiredTextContent();
        int amount = Integer.parseInt(amountString);
        logger.info("amount: " + amount);

        Random random = new Random();
        int msec = random.nextInt(5000);
        logger.info("Sleep for " + msec + " msec");
        try {
            Thread.sleep(msec);
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        }

        String accept = "No";

        XmlNamespace namespace = XmlConstants.BUILDER.newNamespace("approvertypens",
                "http://www.extreme.indiana.edu/loan/approver/xsd/");
        XmlElement outputElement = XmlConstants.BUILDER.newFragment(namespace, "ApproveOutput");
        XmlElement acceptElement = outputElement.addElement("accept");
        acceptElement.addChild(accept);

        if (notifier != null) {
            notifier.sendingResult(outputElement);
        }
        logger.info(XMLUtil.xmlElementToString(outputElement));
        return outputElement;
    }
}