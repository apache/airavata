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
import java.net.URISyntaxException;
import java.util.Date;

import org.apache.airavata.common.utils.XMLUtil;
import org.apache.airavata.workflow.model.exceptions.WorkflowRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlpull.infoset.XmlElement;
import org.xmlpull.infoset.XmlNamespace;

import atomixmiser.DcDate;

/**
 * Manipulate XML Events
 */
public class MonitorUtil {

    private static final Logger logger = LoggerFactory.getLogger(MonitorUtil.class);

    /**
     * Workflow tracking namespace
     */
    public static final XmlNamespace WOR_NS = XMLUtil.BUILDER.newNamespace("wor",
            "http://airavata.apache.org/schemas/workflow_tracking_types");

    /**
     * XBaya events namespace
     */
    public static final XmlNamespace XBAYA_EVENTS_NS = XMLUtil.BUILDER.newNamespace("xbaya",
            "http://www.extreme.indiana.edu/xgws/xbaya/ns/2006/");

    /**
     * gotResult
     */
    public static final String GOT_RESULT_EVENT_TAG = "receivedResult";

    /**
     * description
     */
    public static final String DESCRIPTION_TAG = "description";

    /**
     * timestamp
     */
    public static final String TIMESTAMP_TAG = "timestamp";

    /**
     * request
     * 
     * In invokingService
     */
    public static final String REQUEST = "request";

    /**
     * result
     * 
     * In sendingResult
     */
    public static final String RESULT = "result";

    /**
     * body
     * 
     * In sendingResult
     */
    public static final String BODY = "body";

    /**
     * workflowID
     * 
     * workflow instance ID.
     */
    private static final String WORKFLOW_ID_ATTRIBUTE = "workflowID";

    /**
     * serviceID
     * 
     * In workflowInitialized notification, this is the worklfow instance ID.
     */
    private static final String SERVICE_ID_ATTRIBUTE = "serviceID";

    /**
     * workflowNodeID
     * 
     * Node ID.
     */
    private static final String WORKFLOW_NODE_ID_ATTRIBUTE = "workflowNodeID";

    /**
     * receiver
     * 
     * Extract a node ID from here when a notification is sent by a workflow when the workflow is sending a message to a
     * service.
     */
    private static final String RECEIVER_TAG = "receiver";

    /**
     * responder
     * 
     * Extract a node ID from here when a notification is sent by a workflow when the workflow is receiving a message
     * from a service.
     */
    private static final String RESPONDER_TAG = "responder";

    /**
     * notificationSource
     * 
     * Extract a node ID from here when a service sends notification.
     */
    private static final String NOTIFICATION_SOURCE_TAG = "notificationSource";

    // Followings are specific to some event types.

    /**
     * location
     * 
     * In publishURL
     */
    private static final String LOCATION_TAG = "location";

    /**
     * retryStatusCount
     * 
     * In computationDuration
     */
    private static final String RETRY_STATUS_COUNT_TAG = "retryStatusCount";

    /**
     * mappedResource
     * 
     * In resourceMapping
     */
    private static final String MAPPED_RESOURCE_TAG = "mappedResource";

    /**
     * dataProduct
     * 
     * In a couple of data-related notifications.
     */
    private static final String DATA_PRODUCT_TAG = "dataProduct";

    /**
     * durationInMillis
     * 
     * In computationDuration
     */
    @SuppressWarnings("unused")
    private static final String DURATION_IN_MILLS_TAG = "durationInMillis";

    /**
     * Type of the notification event.
     */
    public enum EventType {
        /**
         * unknown
         */
        UNKNOWN("unknown"),

        // Notification from a workflow

        /**
         * workflowInitialized
         */
        WORKFLOW_INITIALIZED("workflowInitialized"),

        /**
         * workflowInvoked
         */
        WORKFLOW_INVOKED("workflowInvoked"),

        /**
         * workflowTerminated
         */
        WORKFLOW_TERMINATED("workflowTerminated"),

        /**
         * invokingService
         */
        INVOKING_SERVICE("invokingService"),

        /**
         * invokingServiceSucceeded
         */
        INVOKING_SERVICE_SUCCEEDED("invokingServiceSucceeded"),

        /**
         * invokingServiceFailed
         */
        INVOKING_SERVICE_FAILED("invokingServiceFailed"),

        /**
         * receivedResult
         */
        RECEIVED_RESULT("receivedResult"),

        /**
         * receivedFault
         */
        RECEIVED_FAULT("receivedFault"),

        // Notification from a service

        /**
         * serviceInvoked
         */
        SERVICE_INVOKED("serviceInvoked"),

        /**
         * sendingResult
         */
        SENDING_RESULT("sendingResult"),

        /**
         * sendingResponseFailed
         */
        SENDING_RESPONSE_FAILED("sendingResponseFailed"),

        /**
         * sendingFault
         */
        SENDING_FAULT("sendingFault"),

        // Other types of notification from a service.
        /**
         * logInfo
         */
        LOG_INFO("logInfo"),

        /**
         * logException
         */
        LOG_EXCEPTION("logException"),

        /**
         * logWarning
         */
        LOG_WARNING("logWarning"),

        /**
         * logDebug
         */
        LOG_DEBUG("logDebug"),

        /**
         * dataConsumed
         */
        DATA_CONSUMED("dataConsumed"),

        /**
         * dataProduced
         */
        DATA_PRODUCED("dataProduced"),

        /**
         * dataReceiveDuration
         */
        DATA_RECEIVE_DURATION("dataReceiveDuration"),

        /**
         * applicationAudit
         */
        APPLICATION_AUDIT("applicationAudit"),

        /**
         * computationDuration
         */
        COMPUTATION_DURATION("computationDuration"),

        /**
         * publishURL
         */
        PUBLISH_URL("publishURL"),

        /**
         * resourceMapping
         */
        RESOURCE_MAPPING("resourceMapping");

        String name;

        EventType(String name) {
            this.name = name;
        }
    }

    /**
     * @param event
     * @return The type of the event.
     */
    public static EventType getType(XmlElement event) {
        if (event == null) {
            throw new IllegalArgumentException("null");
        }
        XmlNamespace ns = event.getNamespace();
        String name = event.getName();
        if (XBAYA_EVENTS_NS.equals(ns)) {
            if (GOT_RESULT_EVENT_TAG.equals(name)) {
                return EventType.WORKFLOW_TERMINATED;
            } else {
                return EventType.UNKNOWN;
            }
        } else if (WOR_NS.equals(ns)) {
            for (EventType type : EventType.values()) {
                if (type.name.equals(name)) {
                    return type;
                }
            }
        }
        return EventType.UNKNOWN;
    }

    /**
     * @param event
     * @return The timestamp.
     */
    public static Date getTimestamp(XmlElement event) {
        if (event == null) {
            throw new IllegalArgumentException("null");
        }
        XmlElement timestampEl = event.element(WOR_NS, TIMESTAMP_TAG);
        if (timestampEl == null)
            return null;
        String timestamp = timestampEl.requiredText();
        DcDate date = DcDate.create(timestamp);
        return new Date(date.getTimeInMillis());
    }

    /**
     * @param event
     * @return The node ID if the message contains it; "", otherwise
     */
    public static String getNodeID(XmlElement event) {
        if (event == null) {
            throw new IllegalArgumentException("null");
        }
        XmlElement idElement = getIDElement(event);
        String nodeID = null;
        if (idElement != null) {
            nodeID = idElement.attributeValue(WOR_NS, WORKFLOW_NODE_ID_ATTRIBUTE);
        }
        if (nodeID == null) {
            nodeID = "";
        }
        return nodeID;
    }

    /**
     * @param event
     * @return The workflow instance ID. null if there is no workflow instance ID.
     */
    public static URI getWorkflowID(XmlElement event) {
        if (event == null) {
            throw new IllegalArgumentException("null");
        }
        EventType type = getType(event);
        XmlElement idElement = getIDElement(event);
        if (idElement == null) {
            return null;
        }

        String workflowID;
        switch (type) {
        case WORKFLOW_INITIALIZED:
        case WORKFLOW_TERMINATED:
            // Special cases
            workflowID = idElement.attributeValue(WOR_NS, SERVICE_ID_ATTRIBUTE);
            break;
        default:
            // Default
            workflowID = idElement.attributeValue(WOR_NS, WORKFLOW_ID_ATTRIBUTE);
            break;
        }
        if (workflowID == null || workflowID.length() == 0) {
            return null;
        }
        try {
            return new URI(workflowID);
        } catch (URISyntaxException e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * @param event
     * @return The type of the event to display
     */
    public static String getStatus(XmlElement event) {
        if (event == null) {
            throw new IllegalArgumentException("null");
        }
        EventType type = getType(event);
        String status;
        switch (type) {
        case LOG_INFO:
            status = "INFO";
            break;
        case LOG_EXCEPTION:
            status = "EXCEPTION";
            break;
        case LOG_WARNING:
            status = "WARNING";
            break;
        case LOG_DEBUG:
            status = "DEBUG";
            break;
        default:
            status = event.getName();
        }
        return status;
    }

    /**
     * @param event
     * @return The message to display.
     */
    public static String getMessage(XmlElement event) {
        if (event == null) {
            throw new IllegalArgumentException("null");
        }
        String description = null;
        XmlElement descElement = event.element(WOR_NS, DESCRIPTION_TAG);
        if (descElement != null) {
            description = descElement.requiredText();
        }

        if (description == null || description.length() == 0) {
            // It might be a data-related notification
            XmlElement dataProduct = event.element(WOR_NS, DATA_PRODUCT_TAG);
            if (dataProduct != null) {
                descElement = dataProduct.element(WOR_NS, DESCRIPTION_TAG);
                if (descElement != null) {
                    description = descElement.requiredText();
                }
            }
        }

        if (description == null) {
            description = "";
        }
        return description;
    }

    /**
     * @param event
     * @return The location.
     */
    public static String getLocation(XmlElement event) {
        if (event == null) {
            throw new IllegalArgumentException("null");
        }
        XmlElement locationEl = event.element(WOR_NS, LOCATION_TAG);
        if (locationEl != null) {
            String location = locationEl.requiredText();
            return location;
        } else {
            return null;
        }
    }

    /**
     * Gets the mapped resource from the event.
     * 
     * @param event
     *            The event, the type of which has to be resourceMapping.
     * @return The resource
     */
    public static String getMappedResource(XmlElement event) {
        if (event == null) {
            throw new IllegalArgumentException("null");
        }
        EventType type = getType(event);
        if (type != EventType.RESOURCE_MAPPING) {
            throw new IllegalArgumentException("Event must have resourceMapping type instead of " + type);
        }
        XmlElement mappedResource = event.element(MAPPED_RESOURCE_TAG);
        String resource = mappedResource.requiredText();
        return resource;
    }

    /**
     * Gets the retry count from the event.
     * 
     * @param event
     *            The event, the type of which has to be resourceMapping.
     * @return The resource
     */
    public static String getRetryCount(XmlElement event) {
        if (event == null) {
            throw new IllegalArgumentException("null");
        }
        EventType type = getType(event);
        if (type != EventType.RESOURCE_MAPPING) {
            throw new IllegalArgumentException("Event must have resourceMapping type instead of " + type);
        }
        XmlElement retryCountElement = event.element(RETRY_STATUS_COUNT_TAG);
        String retryCount = retryCountElement.requiredText();
        return retryCount;
    }

    /**
     * Gets the workflow instance ID.
     * 
     * @param event
     *            The event, the type of which has to be workflowInitialized.
     * @return The workflowInstanceID
     */
    public static URI getWorkflowInstanceID(XmlElement event) {
        EventType type = getType(event);
        if (!(type == EventType.WORKFLOW_INITIALIZED || type == EventType.WORKFLOW_TERMINATED)) {
            throw new IllegalArgumentException(
                    "Event must be an workflowInitialized type or an workflowTerminated type instead of " + type);
        }
        XmlElement notificationSource = event.element(WOR_NS, NOTIFICATION_SOURCE_TAG);
        if (notificationSource == null) {
            throw new WorkflowRuntimeException("The notification should have " + NOTIFICATION_SOURCE_TAG + " element.");
        }
        String workflowInstanceID = notificationSource.attributeValue(WOR_NS, SERVICE_ID_ATTRIBUTE);
        if (workflowInstanceID == null) {
            throw new WorkflowRuntimeException("The notification should have " + SERVICE_ID_ATTRIBUTE + " attribute.");
        }
        try {
            return new URI(workflowInstanceID);
        } catch (URISyntaxException e) {
            throw new WorkflowRuntimeException(e);
        }
    }

    /**
     * @param event
     * @return The element that has workflow ID, node ID, etc.
     */
    private static XmlElement getIDElement(XmlElement event) {
        EventType type = getType(event);
        switch (type) {
        // The following three don't include node ID, but includes workflow ID.
        case WORKFLOW_INITIALIZED:
        case WORKFLOW_INVOKED:
        case WORKFLOW_TERMINATED:
            // The followings include both workflow ID and node ID.
            // TODO they might be used by a workflow too.
        case SERVICE_INVOKED:
        case SENDING_RESULT:
        case SENDING_FAULT:
        case SENDING_RESPONSE_FAILED: // TODO make sure
            // The followings are used only in services.
        case LOG_INFO:
        case LOG_WARNING:
        case LOG_EXCEPTION:
        case LOG_DEBUG:
        case DATA_CONSUMED:
        case DATA_PRODUCED:
        case DATA_RECEIVE_DURATION:
        case APPLICATION_AUDIT:
        case COMPUTATION_DURATION:
        case RESOURCE_MAPPING:
        case PUBLISH_URL:
            return event.element(NOTIFICATION_SOURCE_TAG);
        case INVOKING_SERVICE:
        case INVOKING_SERVICE_SUCCEEDED: // TODO make sure
        case INVOKING_SERVICE_FAILED: // TODO make sure
            return event.element(RECEIVER_TAG);
        case RECEIVED_RESULT:
        case RECEIVED_FAULT:
            return event.element(RESPONDER_TAG);
        case UNKNOWN:
            // Most of unknown types are from service.
            return event.element(NOTIFICATION_SOURCE_TAG);
        default:
            // Most of unknown types are from service.
            return event.element(NOTIFICATION_SOURCE_TAG);
        }
    }
}