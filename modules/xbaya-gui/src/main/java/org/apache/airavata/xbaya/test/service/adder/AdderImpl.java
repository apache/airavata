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

package org.apache.airavata.xbaya.test.service.adder;

import java.util.Random;

import org.apache.airavata.common.utils.XMLUtil;
import org.apache.airavata.xbaya.test.service.ServiceNotificationSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlNamespace;

import xsul.XmlConstants;

public class AdderImpl implements Adder {

    private final static Logger logger = LoggerFactory.getLogger(AdderImpl.class);

    /**
     * @see org.apache.airavata.xbaya.test.service.adder.Adder#add(org.xmlpull.v1.builder.XmlElement)
     */
    @Override
    public XmlElement add(XmlElement inputElement) {
        logger.info(XMLUtil.xmlElementToString(inputElement));
        ServiceNotificationSender notifier = ServiceNotificationSender.invoked(inputElement);

        XmlElement xElement = inputElement.requiredElement(null, "x");
        XmlElement yElement = inputElement.requiredElement(null, "y");
        String xString = xElement.requiredTextContent();
        String yString = yElement.requiredTextContent();

        int x = Integer.parseInt(xString);
        int y = Integer.parseInt(yString);

        int z = x + y;

        Random random = new Random();
        int msec = random.nextInt(5000);
        logger.info("Sleep for " + msec + " msec");
        try {
            Thread.sleep(msec);
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        }

        String zString = "" + z;

        XmlNamespace namespace = XmlConstants.BUILDER.newNamespace("addertypens",
                "http://www.extreme.indiana.edu/math/adder/xsd/");
        XmlElement outputElement = XmlConstants.BUILDER.newFragment(namespace, "AddOutput");
        XmlElement zElement = outputElement.addElement("z");
        zElement.addChild(zString);

        if (notifier != null) {
            notifier.sendingResult(outputElement);
        }
        logger.info(XMLUtil.xmlElementToString(outputElement));
        return outputElement;
    }
}