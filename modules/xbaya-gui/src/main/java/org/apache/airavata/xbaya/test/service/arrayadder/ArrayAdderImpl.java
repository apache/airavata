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

package org.apache.airavata.xbaya.test.service.arrayadder;

import java.util.Iterator;
import java.util.Random;

import org.apache.airavata.common.utils.XMLUtil;
import org.apache.airavata.xbaya.test.service.ServiceNotificationSender;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlNamespace;

import xsul.XmlConstants;

public class ArrayAdderImpl implements ArrayAdder {

    private static final Log logger = LogFactory.getLog(ArrayAdderImpl.class);

    /**
     * @see org.apache.airavata.xbaya.test.service.arrayadder.ArrayAdder#add(org.xmlpull.v1.builder.XmlElement)
     */
    public XmlElement add(XmlElement inputElement) {
        logger.info(XMLUtil.xmlElementToString(inputElement));

        ServiceNotificationSender notifier = ServiceNotificationSender.invoked(inputElement);

        XmlElement arrayElement = inputElement.requiredElement(null, "input");

        int sum = 0;
        @SuppressWarnings("rawtypes")
		Iterator valueIt = arrayElement.elements(null, "value").iterator();
        while (valueIt.hasNext()) {
            XmlElement valueElement = (XmlElement) valueIt.next();
            String valueString = valueElement.requiredTextContent();
            int value = Integer.parseInt(valueString);
            sum += value;
        }

        Random random = new Random();
        int msec = random.nextInt(5000);
        logger.info("Sleep for " + msec + " msec");
        try {
            Thread.sleep(msec);
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        }

        String sumString = "" + sum;

        XmlNamespace namespace = XmlConstants.BUILDER.newNamespace("typens",
                "http://www.extreme.indiana.edu/math/arrayadder/xsd/");
        XmlElement outputElement = XmlConstants.BUILDER.newFragment(namespace, "ArrayAdderOutput");
        XmlElement sumElement = outputElement.addElement("sum");
        sumElement.addChild(sumString);

        if (notifier != null) {
            notifier.sendingResult(outputElement);
        }
        logger.info(XMLUtil.xmlElementToString(outputElement));
        return outputElement;
    }
}