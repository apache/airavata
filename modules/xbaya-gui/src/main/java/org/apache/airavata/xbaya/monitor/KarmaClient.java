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

package org.apache.airavata.xbaya.monitor;

import java.net.URI;

import org.apache.airavata.xbaya.XBayaRuntimeException;
import org.apache.airavata.xbaya.util.XMLUtil;
import org.xmlpull.infoset.XmlBuilderException;
import org.xmlpull.infoset.XmlElement;

import wsmg.NotificationHandler;
import xsul5.MLogger;
import edu.indiana.extreme.karma.client.ProvenanceActivityReplay;
import edu.indiana.extreme.karma.client.ProvenanceActivityReplay.ActivityRate;

public class KarmaClient implements NotificationHandler {

    /**
     */
    public enum Rate {
        /**
         * ORIGINAL
         */
        ORIGINAL(ActivityRate.Original),
        /**
         * PERIODIC
         */
        PERIODIC(ActivityRate.Periodic),
        /**
         * BATCH
         */
        BATCH(ActivityRate.Batch);

        private Rate(ActivityRate activityRate) {
            this.activityRate = activityRate;
        }

        private ActivityRate activityRate;

        /**
         * @return ActivityRate
         */
        public ProvenanceActivityReplay.ActivityRate activityRate() {
            return this.activityRate;
        }
    }

    private static final MLogger logger = MLogger.getLogger();

    private Monitor monitor;

    private URI kermaURL;

    private URI workflowInstanceID;

    private Rate rate;

    /**
     * Constructs a KermaClient.
     * 
     * @param monitor
     * @param kermaURL
     * @param workflowInstanceID
     * @param rate
     */
    public KarmaClient(Monitor monitor, URI kermaURL, URI workflowInstanceID, Rate rate) {
        this.monitor = monitor;
        this.kermaURL = kermaURL;
        this.workflowInstanceID = workflowInstanceID;
        this.rate = rate;
    }

    /**
     * This method blocks until we get the first notification message.
     */
    public void start() {
        boolean succeed = ProvenanceActivityReplay.replayActivity(this.kermaURL.toString(),
                this.workflowInstanceID.toString(), this.rate.activityRate(), this);
        if (!succeed) {
            String message = "Something went wrong inside of the karma library.";
            throw new XBayaRuntimeException(message);
        }
    }

    /**
     * @see wsmg.NotificationHandler#handleNotification(java.lang.String)
     */
    public void handleNotification(String message) {
        try {
            // No SOAP header is included.
            // String soapBody = WsmgUtil.getSoapBodyContent(message);
            // XmlElement event = XMLUtil.stringToXmlElement(soapBody);
            XmlElement event = XMLUtil.stringToXmlElement(message);
            this.monitor.handleNotification(event);
        } catch (XmlBuilderException e) {
            // Just log them because they can be unrelated messages sent to
            // this topic by accident.
            logger.warning("Could not parse received notification: " + message, e);
        } catch (RuntimeException e) {
            logger.warning("Failed to process notification: " + message, e);
        }
    }
}