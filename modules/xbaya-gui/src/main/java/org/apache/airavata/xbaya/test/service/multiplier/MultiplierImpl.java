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

package org.apache.airavata.xbaya.test.service.multiplier;

import java.util.Random;

import org.apache.airavata.common.utils.XMLUtil;
import org.apache.airavata.xbaya.test.service.ServiceNotificationSender;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlNamespace;

import xsul.XmlConstants;
import xsul5.MLogger;

public class MultiplierImpl implements Multiplier {

    private final static MLogger logger = MLogger.getLogger();

    /**
     * @see org.apache.airavata.xbaya.test.service.multiplier.Multiplier#multiply(org.xmlpull.v1.builder.XmlElement)
     */
    public XmlElement multiply(XmlElement inputElement) {
        logger.finest(XMLUtil.xmlElementToString(inputElement));

        ServiceNotificationSender notifier = ServiceNotificationSender.invoked(inputElement);

        XmlElement xElement = inputElement.requiredElement(null, "x");
        XmlElement yElement = inputElement.requiredElement(null, "y");
        String xString = xElement.requiredTextContent();
        String yString = yElement.requiredTextContent();

        int x = Integer.parseInt(xString);
        int y = Integer.parseInt(yString);

        Random random = new Random();
        int msec = random.nextInt(10000);
        logger.info("Sleep for " + msec + " msec");
        try {
            Thread.sleep(msec);
        } catch (InterruptedException e) {
            logger.caught(e);
        }

        int z = x * y;

        String zString = "" + z;

        XmlNamespace namespace = XmlConstants.BUILDER.newNamespace("multipliertypens",
                "http://www.extreme.indiana.edu/math/multiplier/xsd/");
        XmlElement outputElement = XmlConstants.BUILDER.newFragment(namespace, "MultiplyOutput");
        XmlElement zElement = outputElement.addElement("z");
        zElement.addChild(zString);

        if (notifier != null) {
            notifier.sendingResult(outputElement);
        }
        logger.finest(XMLUtil.xmlElementToString(outputElement));
        return outputElement;
    }
}