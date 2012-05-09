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
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.airavata.xbaya.monitor.MonitorUtil.EventType;
import org.xmlpull.infoset.XmlElement;

public class MonitorEvent {

    private String timeText;
    
    private Date timestamp;

    private String idText;

    private String statusText;

    private String message;

    private XmlElement event;

    private EventType type;

    private URI workflowID;

    private String nodeID;

    /**
     * Constructs a MonitorEvent.
     * 
     * @param event
     */
    public MonitorEvent(XmlElement event) {
        this.event = event;
        parse();
    }

    /**
     * Returns the event.
     * 
     * @return The event
     */
    public XmlElement getEvent() {
        return this.event;
    }

    /**
     * Returns the idText.
     * 
     * @return The idText
     */
    public String getIDText() {
        return this.idText;
    }

    /**
     * Returns the message.
     * 
     * @return The message
     */
    public String getMessage() {
        return this.message;
    }

    /**
     * Returns the statusText.
     * 
     * @return The statusText
     */
    public String getStatusText() {
        return this.statusText;
    }

    /**
     * Returns the timeText.
     * 
     * @return The timeText
     */
    public String getTimeText() {
        return this.timeText;
    }

    /**
     * Returns the type.
     * 
     * @return The type
     */
    public EventType getType() {
        return this.type;
    }

    /**
     * Returns the workflowID.
     * 
     * @return The workflowID
     */
    public URI getWorkflowID() {
        return this.workflowID;
    }

    /**
     * Returns the nodeID.
     * 
     * @return The nodeID
     */
    public String getNodeID() {
        return this.nodeID;
    }

    private void parse() {
        this.type = MonitorUtil.getType(this.event);
        this.workflowID = MonitorUtil.getWorkflowID(this.event);
        this.nodeID = MonitorUtil.getNodeID(this.event);

        timestamp = MonitorUtil.getTimestamp(this.event);
        if (timestamp != null) {
            SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss.S MM/dd/yy ");
            this.timeText = format.format(timestamp);
        } else {
            this.timeText = "";
        }

        this.idText = this.nodeID;

        this.statusText = MonitorUtil.getStatus(this.event);

        this.message = MonitorUtil.getMessage(this.event);
        if (this.type == MonitorUtil.EventType.PUBLISH_URL) {
            String location = MonitorUtil.getLocation(this.event);
            // should be looked into
            // String url = PREFIX + location + SUFFIX;
            String url = location;
            this.message = "<html>" + this.message + ": " + "<a href=\"" + url + "\">" + url + " </a></html>";
        }
    }

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

}