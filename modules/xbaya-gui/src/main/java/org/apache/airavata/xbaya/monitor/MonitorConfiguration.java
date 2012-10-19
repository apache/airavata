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
import java.util.List;

import org.apache.airavata.xbaya.event.Event;
import org.apache.airavata.xbaya.event.Event.Type;
import org.apache.airavata.xbaya.event.EventProducer;
import org.apache.airavata.common.utils.StringUtil;

public class MonitorConfiguration extends EventProducer implements Cloneable{

    // private static final Log logger = LogFactory.getLog();

    private URI brokerURL;

    private String topic;

    private boolean pullMode;

    private URI messageBoxURL;

    private List<String> interactiveNodeIDs = null;

    /**
     * Constructs a NotificationConfiguration.
     * 
     * @param brokerURL
     * @param topic
     * @param pullMode
     * @param messageBoxURL
     */
    public MonitorConfiguration(URI brokerURL, String topic, boolean pullMode, URI messageBoxURL) {
        set(brokerURL, topic, pullMode, messageBoxURL);
    }

    /**
     * @param brokerURL
     * @param topic
     * @param pullMode
     * @param messageBoxURL
     */
    public void set(URI brokerURL, String topic, boolean pullMode, URI messageBoxURL) {
        this.brokerURL = brokerURL;
        this.topic = topic;
        this.pullMode = pullMode;
        this.messageBoxURL = messageBoxURL;
        sendSafeEvent(new Event(Type.MONITOR_CONFIGURATION_CHANGED));
    }

    /**
     * @param brokerURL
     *            The brokerLocation to set.
     */
    public void setBrokerURL(URI brokerURL) {
        this.brokerURL = brokerURL;
        sendSafeEvent(new Event(Type.MONITOR_CONFIGURATION_CHANGED));
    }

    /**
     * Returns the URL of the notification broker.
     * 
     * @return The URL of the notification broker
     */
    public URI getBrokerURL() {
        return this.brokerURL;
    }

    /**
     * @param topic
     *            The topic to set
     */
    public void setTopic(String topic) {
        this.topic = StringUtil.trimAndNullify(topic);
        sendSafeEvent(new Event(Type.MONITOR_CONFIGURATION_CHANGED));
    }

    /**
     * @return The userId.
     */
    public String getTopic() {
        return this.topic;
    }

    /**
     * Returns the messageBoxUrl.
     * 
     * @return The messageBoxUrl
     */
    public URI getMessageBoxURL() {
        return this.messageBoxURL;
    }

    /**
     * Sets messageBoxUrl.
     * 
     * @param messageBoxURL
     *            The messageBoxUrl to set.
     */
    public void setMessageBoxURL(URI messageBoxURL) {
        this.messageBoxURL = messageBoxURL;
        sendSafeEvent(new Event(Type.MONITOR_CONFIGURATION_CHANGED));
    }

    /**
     * Returns the pullMode.
     * 
     * @return The pullMode
     */
    public boolean isPullMode() {
        return this.pullMode;
    }

    /**
     * Sets pullMode.
     * 
     * @param pullMode
     *            The pullMode to set.
     */
    public void setPullMode(boolean pullMode) {
        this.pullMode = pullMode;
        sendSafeEvent(new Event(Type.MONITOR_CONFIGURATION_CHANGED));
    }

    /**
     * @return true if the configuration is valid; false otherwise.
     */
    public boolean isValid() {
        if (this.brokerURL == null) {
            return false;
        }
        if (this.topic == null || this.topic.length() == 0) {
            return false;
        }
        if (this.pullMode == true) {
            if (this.messageBoxURL == null) {
                return false;
            }
        }
        return true;
    }

    /**
     * @see java.lang.Object#clone()
     */
    @Override
    public MonitorConfiguration clone() {
        return new MonitorConfiguration(this.brokerURL, this.topic, this.pullMode, this.messageBoxURL);
    }

    /**
     * Returns the interactiveNodeIDs.
     * 
     * @return The interactiveNodeIDs
     */
    public List<String> getInteractiveNodeIDs() {
        return this.interactiveNodeIDs;
    }

    /**
     * Sets interactiveNodeIDs.
     * 
     * @param interactiveNodeIDs
     *            The interactiveNodeIDs to set.
     */
    public void setInteractiveNodeIDs(List<String> interactiveNodeIDs) {
        this.interactiveNodeIDs = interactiveNodeIDs;
    }

}