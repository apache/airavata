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

package org.apache.airavata.commons;

import java.net.URI;
import java.util.Iterator;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.addressing.EndpointReferenceHelper;
import org.apache.log4j.Logger;

public class LeadContextHeader {

    OMElement target;

    public LeadContextHeader(String experimentId, String userDn) {
        this.target = factory.createOMElement(new QName(NS.getNamespaceURI(), TYPE.getLocalPart()));
        setExperimentId(experimentId);
        setUserDn(userDn);
    }

    public LeadContextHeader(OMElement target) {
        this.target = target;
    }

    public void setExperimentId(String experimentId) {
        setStringValue(NS, "experiment-id", experimentId);
    }

    public EndpointReference getEventSink() {
        try {
            return lookupEpr(NS, "event-sink-epr");
        } catch (AxisFault e) {
            e.printStackTrace();
            return null;
        }
    }

    private EndpointReference lookupEpr(OMNamespace ns2, String localPart) throws AxisFault {
        OMElement element = target.getFirstChildWithName(new QName(ns2.getNamespaceURI(), localPart));
        return EndpointReferenceHelper.fromOM(element);
    }

    private void setStringValue(OMNamespace ns2, String name, String value) {

        QName childQName = new QName(ns2.getNamespaceURI(), name);

        Iterator iterator = target.getChildrenWithName(childQName);

        boolean haschildren = false;

        while (iterator.hasNext()) {
            haschildren = true;

            OMElement currentChild = (OMElement) iterator.next();
            currentChild.setText(value);
        }

        if (!haschildren) {

            OMElement child = factory.createOMElement(childQName, target);
            child.setText(value);
        }

    }

    public String getExperimentId() {
        return getString(NS, "experiment-id");
    }

    private String getString(OMNamespace ns2, String localpart) {

        String ret = null;
        OMElement child = target.getFirstChildWithName(new QName(ns2.getNamespaceURI(), localpart));

        if (child != null) {
            ret = child.getText();
        }

        return ret;
    }

    public void setWorkflowId(URI workflowId) {
        setWorkflowInstanceId(workflowId);
    }

    public URI getWorkflowId() {
        return getWorkflowInstanceId();
    }

    public void setWorkflowInstanceId(URI workflowId) {
        setUriValue(NS, "workflow-instance-id", workflowId);
    }

    private void setUriValue(OMNamespace ns2, String localpart, URI value) {
        String s = value.toASCIIString();
        setStringValue(NS, localpart, s);
    }

    public URI getWorkflowInstanceId() {
        return lookupUriValue(NS, "workflow-instance-id");
    }

    private URI lookupUriValue(OMNamespace ns2, String localpart) {

        String svalue = getString(NS, localpart);
        if (svalue == null) {
            return null;
        } else {
            URI uri = URI.create(svalue);
            return uri;
        }

    }

    public void setWorkflowTemplateId(URI workflowId) {
        setUriValue(NS, "workflow-template-id", workflowId);
    }

    public URI getWorkflowTemplateId() {
        return lookupUriValue(NS, "workflow-template-id");
    }

    public void setNodeId(String nodeId) {
        setStringValue(NS, "workflow-node-id", nodeId);
    }

    public String getNodeId() {
        return getString(NS, "workflow-node-id");
    }

    public void setTimeStep(String timeStep) {
        setStringValue(NS, "workflow-time-step", timeStep);
    }

    public String getTimeStep() {
        return getString(NS, "workflow-time-step");
    }

    public void setServiceId(String serviceId) {
        setStringValue(NS, "service-instance-id", serviceId);
    }

    public String getServiceId() {
        return getString(NS, "service-instance-id");
    }

    public void setServiceInstanceId(URI serviceId) {
        setUriValue(NS, "service-instance-id", serviceId);
    }

    public URI getServiceInstanceId() {
        return lookupUriValue(NS, "service-instance-id");
    }

    public void setGfacUrl(URI url) {
        setUriValue(NS, "gfac-url", url);
    }

    public void setEventSinkEpr(EndpointReference epr) {
        setUriValue(NS, "event-sink-epr", URI.create(epr.getAddress()));
    }

    public URI getGfacUrl() {
        return lookupUriValue(NS, "gfac-url");
    }

    public void setDscUrl(URI url) {
        setUriValue(NS, "dsc-url", url);
    }

    public URI getDscUrl() {
        return lookupUriValue(NS, "dsc-url");
    }

    public void setMyleadAgentUrl(URI url) {
        setUriValue(NS, "mylead-agent-url", url);
    }

    public URI getMyleadAgentUrl() {
        return lookupUriValue(NS, "mylead-agent-url");
    }

    public void setResourceCatalogUrl(URI value) {
        setUriValue(NS, "resource-catalog-url", value);
    }

    public URI getResourceCatalogUrl() {
        return lookupUriValue(NS, "resource-catalog-url");
    }

    public void setResourceBrokerUrl(URI value) {
        setUriValue(NS, "resource-broker-url", value);
    }

    public URI getResourceBrokerUrl() {
        return lookupUriValue(NS, "resource-broker-url");
    }

    public void setResourceScheduler(String value) {
        setStringValue(NS, "resource-scheduler", value);
    }

    public String getResourceScheduler() {
        return getString(NS, "resource-scheduler");
    }

    public void setUserDn(String userDn) {
        setStringValue(NS, "user-dn", userDn);
    }

    public String getUserDn() {
        return getString(NS, "user-dn");
    }

    public void setUrgency(String urgency) {
        setStringValue(NS, "URGENCY", urgency);
    }

    public String getUrgency() {
        return lookupStringValue(NS, "URGENCY");
    }

    private String lookupStringValue(OMNamespace ns2, String localpart) {

        return getString(ns2, localpart);

    }

    public void setOutPutDataDir(String outPutDataDir) {
        setStringValue(NS, "OUTPUT_DATA_DIRECTORY", outPutDataDir);
    }

    public String getOutPutDataDir() {
        return lookupStringValue(NS, "OUTPUT_DATA_DIRECTORY");
    }

    public void setOpenDapPrfix(String opendapPrefix) {
        setStringValue(NS, "OPENDAP_DIRECTORY", opendapPrefix);
    }

    public String getOpenDapPrfix() {
        return lookupStringValue(NS, "OPENDAP_DIRECTORY");
    }

    public void setForceFileStagingToWorkDir(String forceFileStagingToWorkDir) {
        setStringValue(NS, "ForceFileStagingToWorkDir", forceFileStagingToWorkDir);
    }

    public String getForceFileStagingToWorkDir() {
        return lookupStringValue(NS, "ForceFileStagingToWorkDir");
    }

    public void setOutputDataFilesSuffix(String outputDataFilesSuffix) {
        setStringValue(NS, "OUTPUT_DATA_FILES_SUFFIX", outputDataFilesSuffix);
    }

    public String getOutputDataFilesSuffix() {
        return lookupStringValue(NS, "OUTPUT_DATA_FILES_SUFFIX");
    }

    private static final org.apache.log4j.Logger logger = Logger.getLogger(LeadContextHeader.class);
    private static final OMFactory factory;
    public static final String GFAC_NAMESPACE = "http://org.apache.airavata/namespaces/2004/01/gFac";
    public static final QName TYPE;
    public static final QName MAPPINGLISTTYPE;
    public static final OMNamespace NS;
    public static final OMNamespace MAPPINGLISTNS;
    public static final String EXPERIMENT_ID = "experiment-id";
    public static final String WORKFLOW_INSTANCE_ID = "workflow-instance-id";
    public static final String WORKFLOW_TEMPLATE_ID = "workflow-template-id";
    public static final String NODE_ID = "workflow-node-id";
    public static final String TIME_STEP = "workflow-time-step";
    public static final String SERVICE_INSTANCE_ID = "service-instance-id";
    public static final String GFAC_URL = "gfac-url";
    public static final String DSC_URL = "dsc-url";
    public static final String MYLEAD_AGENT_URL = "mylead-agent-url";
    public static final String RESOURCE_CATALOG_URL = "resource-catalog-url";
    public static final String RESOURCE_BROKER_URL = "resource-broker-url";
    public static final String RESOURCE_SCHEDULER = "resource-scheduler";
    public static final String LEAD_RESOURCE_SCHEDULER_ENUM = "LEAD";
    public static final String VGRADS_RESOURCE_SCHEDULER_ENUM = "VGRADS";
    public static final String SPRUCE_RESOURCE_SCHEDULER_ENUM = "SPRUCE";
    public static final String EVENT_SINK_EPR = "event-sink-epr";
    public static final String ERROR_SINK_EPR = "error-sink-epr";
    public static final String USER_DN = "user-dn";
    public static final String URGENCY = "URGENCY";
    public static final String OUTPUT_DATA_DIRECTORY = "OUTPUT_DATA_DIRECTORY";
    public static final String OPENDAP_DIRECTORY = "OPENDAP_DIRECTORY";
    public static final String FORCE_FILESTAGING_TO_WORKING_DIR = "ForceFileStagingToWorkDir";
    public static final String OUTPUT_DATA_FILES_SUFFIX = "OUTPUT_DATA_FILES_SUFFIX";

    static {
        factory = OMAbstractFactory.getOMFactory();
        TYPE = new QName("http://lead.extreme.indiana.edu/namespaces/2005/10/lead-context-header", "context");
        NS = factory.createOMNamespace(TYPE.getNamespaceURI(), "lh");

        MAPPINGLISTTYPE = new QName("http://lead.extreme.indiana.edu/namespaces/2006/lead-resource-mapping/",
                "resource-mappings");
        MAPPINGLISTNS = factory.createOMNamespace(MAPPINGLISTTYPE.getNamespaceURI(), "lrm");
    }
}
