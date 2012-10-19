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

package org.apache.airavata.workflow.tracking;

import java.io.IOException;
import java.net.URI;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.airavata.workflow.tracking.common.InvocationEntity;
import org.apache.airavata.workflow.tracking.common.NotifierVersion;
import org.apache.airavata.workflow.tracking.common.WorkflowTrackingContext;
import org.apache.airavata.workflow.tracking.impl.publish.NotificationPublisher;
import org.apache.airavata.workflow.tracking.impl.publish.WSMPublisher;
import org.apache.airavata.workflow.tracking.impl.state.InvocationEntityImpl;
import org.apache.airavata.workflow.tracking.types.BaseNotificationType;
import org.apache.airavata.workflow.tracking.util.XmlBeanUtils;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class AbstractNotifier {
    private static final Log log = LogFactory.getLog(AbstractNotifier.class);
    private Map<EndpointReference, NotificationPublisher> publishermap = new ConcurrentHashMap<EndpointReference, NotificationPublisher>();

    protected Date activityTimestamp = null;

    public AbstractNotifier() {

    }

    public WorkflowTrackingContext createTrackingContext(Properties golbalProperties, String epr, URI workflowID,
            URI serviceID, String workflowNodeID, Integer workflowTimestep) {
        WorkflowTrackingContext workflowTrackingContext = new WorkflowTrackingContext();
        workflowTrackingContext.setGlobalAnnotations(golbalProperties);
        workflowTrackingContext.setBrokerEpr(new EndpointReference(epr));
        workflowTrackingContext.setMyself(createEntity(workflowID, serviceID, workflowNodeID, workflowTimestep));
        return workflowTrackingContext;
    }

    public InvocationEntity createEntity(URI workflowID, URI serviceID, String workflowNodeID, Integer workflowTimestep) {
        InvocationEntityImpl invocationEntityImpl = new InvocationEntityImpl(workflowID, serviceID, workflowNodeID,
                workflowTimestep);
        return invocationEntityImpl;
    }

    public void setActivityTimestamp(Date timestamp) {
        activityTimestamp = timestamp;
    }

    protected void setIDAndTimestamp(WorkflowTrackingContext context, BaseNotificationType msg,
            InvocationEntity entity, Date timestamp) {
        // add version for information model
        msg.setInfoModelVersion(NotifierVersion.getTypesVersion());

        msg.addNewNotificationSource().set(entity.toBaseIDType());
        final Calendar cal = new GregorianCalendar();
        cal.setTime(timestamp);
        msg.setTimestamp(cal);
    }

    /**
     * Method setDescAndAnno
     * 
     * @param msg
     *            a BaseNotificationType
     * @param descriptionAndAnnotationa
     *            first string is the description. subsequent strings refer to annotations. each should be a valid xml
     *            fragment that are concatenated into one xml document, which is the annotation.
     * @param defaultDescription
     *            this description used if the user does not pass a message
     * 
     */
    protected void setDescAndAnno(WorkflowTrackingContext context, BaseNotificationType msg,
            String[] descriptionAndAnnotation, String defaultDescription) throws WorkflowTrackingException {
        try {
            String description;
            if (descriptionAndAnnotation != null && descriptionAndAnnotation.length > 0) {
                description = descriptionAndAnnotation[0];
            } else {
                description = defaultDescription;
            }

            final StringBuffer anno = new StringBuffer("<xml-fragment>"); // fixme?
            if (descriptionAndAnnotation != null) {
                for (int i = 1; i < descriptionAndAnnotation.length; i++) {
                    anno.append(descriptionAndAnnotation[i]);
                }
            }

            if (context.getGlobalAnnotations() != null) {
                anno.append(context.getGlobalAnnotations());
            }
            anno.append("</xml-fragment>"); // fixme?
            XmlObject annotations = XmlObject.Factory.parse(anno.toString());

            msg.setDescription(description);
            msg.setAnnotation(annotations);
        } catch (XmlException e) {
            throw new WorkflowTrackingException(e);
        }
    }

    protected void sendNotification(WorkflowTrackingContext context, XmlObject xmldata,
            String[] descriptionAndAnnotation, String defaultDesc) {
        BaseNotificationType xmlMessage = XmlBeanUtils.extractBaseNotificationType(xmldata);
        NotificationPublisher publisher = publishermap.get(context.getBrokerEpr());
        try {
            if (publisher == null) {
                // if a publisher class name has been defined to override the default WSM publisher, use it
                if (context.getPublisherImpl() != null) {
                    publisher = PublisherFactory.createSomePublisher(context.getPublisherImpl(), context);
                } else {
                    if (context.getTopic() == null) {
                        publisher = new WSMPublisher(100, context.isEnableAsyncPublishing(), context.getBrokerEpr()
                                .getAddress(), false);
                    } else {
                        publisher = new WSMPublisher(100, context.isEnableAsyncPublishing(), context.getBrokerEpr()
                                .getAddress(), context.getTopic());
                    }
                }
                publishermap.put(context.getBrokerEpr(), publisher);
            }

            setIDAndTimestamp(context, xmlMessage, context.getMyself(), activityTimestamp != null ? activityTimestamp
                    : new Date());
            setDescAndAnno(context, xmlMessage, descriptionAndAnnotation, defaultDesc);
            xmlMessage.getNotificationSource().setExperimentID(context.getTopic());
            // System.out.println(xmldata);
            if (log.isDebugEnabled()) {
                log.debug(xmldata.toString());
            }
            publisher.publish(xmldata);
        } catch (RuntimeException e) {
            throw new WorkflowTrackingException(e);
        } catch (IOException e) {
            throw new WorkflowTrackingException(e);
        }
    }

    public void flush() {
        // TODO do we need to only plush specific publisher? For now left as it is.
        for (NotificationPublisher publisher : publishermap.values()) {
            publisher.flush();
        }
    }

    /**
     * {@inheritDoc}
     * 
     */
    public void delete() {
        for (NotificationPublisher publisher : publishermap.values()) {
            publisher.flush();
            publisher.delete();
        }
    }

}
