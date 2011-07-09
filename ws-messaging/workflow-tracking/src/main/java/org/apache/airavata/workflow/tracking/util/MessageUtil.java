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

package org.apache.airavata.workflow.tracking.util;

import static org.apache.airavata.workflow.tracking.client.NotificationType.ApplicationAudit;
import static org.apache.airavata.workflow.tracking.client.NotificationType.ComputationDuration;
import static org.apache.airavata.workflow.tracking.client.NotificationType.DataConsumed;
import static org.apache.airavata.workflow.tracking.client.NotificationType.DataProduced;
import static org.apache.airavata.workflow.tracking.client.NotificationType.DataReceiveDuration;
import static org.apache.airavata.workflow.tracking.client.NotificationType.DataSendDuration;
import static org.apache.airavata.workflow.tracking.client.NotificationType.InvokingService;
import static org.apache.airavata.workflow.tracking.client.NotificationType.InvokingServiceFailed;
import static org.apache.airavata.workflow.tracking.client.NotificationType.InvokingServiceSucceeded;
import static org.apache.airavata.workflow.tracking.client.NotificationType.JobStatus;
import static org.apache.airavata.workflow.tracking.client.NotificationType.LogDebug;
import static org.apache.airavata.workflow.tracking.client.NotificationType.LogException;
import static org.apache.airavata.workflow.tracking.client.NotificationType.LogInfo;
import static org.apache.airavata.workflow.tracking.client.NotificationType.LogWarning;
import static org.apache.airavata.workflow.tracking.client.NotificationType.PublishURL;
import static org.apache.airavata.workflow.tracking.client.NotificationType.ReceivedFault;
import static org.apache.airavata.workflow.tracking.client.NotificationType.ReceivedResult;
import static org.apache.airavata.workflow.tracking.client.NotificationType.ResourceMapping;
import static org.apache.airavata.workflow.tracking.client.NotificationType.SendingFault;
import static org.apache.airavata.workflow.tracking.client.NotificationType.SendingResponseFailed;
import static org.apache.airavata.workflow.tracking.client.NotificationType.SendingResponseSucceeded;
import static org.apache.airavata.workflow.tracking.client.NotificationType.SendingResult;
import static org.apache.airavata.workflow.tracking.client.NotificationType.ServiceInitialized;
import static org.apache.airavata.workflow.tracking.client.NotificationType.ServiceInvoked;
import static org.apache.airavata.workflow.tracking.client.NotificationType.ServiceTerminated;
import static org.apache.airavata.workflow.tracking.client.NotificationType.WorkflowInitialized;
import static org.apache.airavata.workflow.tracking.client.NotificationType.WorkflowInvoked;
import static org.apache.airavata.workflow.tracking.client.NotificationType.WorkflowPaused;
import static org.apache.airavata.workflow.tracking.client.NotificationType.WorkflowResumed;
import static org.apache.airavata.workflow.tracking.client.NotificationType.WorkflowTerminated;

import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.airavata.commons.LeadContextHeader;
import org.apache.airavata.workflow.tracking.client.NotificationType;
import org.apache.airavata.workflow.tracking.common.AnnotationConsts;
import org.apache.airavata.workflow.tracking.common.AnnotationProps;
import org.apache.airavata.workflow.tracking.common.ConstructorConsts;
import org.apache.airavata.workflow.tracking.common.ConstructorProps;
import org.apache.airavata.workflow.tracking.types.*;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.xmlbeans.XmlAnySimpleType;
import org.apache.xmlbeans.XmlCalendar;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;

/**
 * Utility library to extract standard fields from LEAD message
 */
public class MessageUtil {

    public static final String WFT_NS = "http://lead.extreme.indiana.edu/namespaces/2006/06/workflow_tracking";
    public static final QName ANNO_QNAME = new QName(WFT_NS, "annotation");

    /**
     * Returns the type of the LEAD Message as a Enum type. This byte ID can be used to quickly check the type of lead
     * message using an if..then statement or a switch statement
     * 
     * @param message
     *            a LEAD Message Xml Document
     * 
     * @return An Enum of type <code> org.apache.airavata.workflow.tracking.util.NOtificationType</code>
     * 
     */
    public static final NotificationType getType(XmlObject message) {

        if (message instanceof ServiceInitializedDocument)
            return ServiceInitialized;
        if (message instanceof WorkflowInitializedDocument)
            return WorkflowInitialized;
        if (message instanceof ServiceTerminatedDocument)
            return ServiceTerminated;
        if (message instanceof WorkflowTerminatedDocument)
            return WorkflowTerminated;
        if (message instanceof InvokingServiceDocument)
            return InvokingService;
        if (message instanceof WorkflowInvokedDocument)
            return WorkflowInvoked;
        if (message instanceof ServiceInvokedDocument)
            return ServiceInvoked;
        if (message instanceof WorkflowPausedDocument)
            return WorkflowPaused;
        if (message instanceof WorkflowResumedDocument)
            return WorkflowResumed;
        if (message instanceof InvokingServiceSucceededDocument)
            return InvokingServiceSucceeded;
        if (message instanceof InvokingServiceFailedDocument)
            return InvokingServiceFailed;
        if (message instanceof SendingResultDocument)
            return SendingResult;
        if (message instanceof SendingFaultDocument)
            return SendingFault;
        if (message instanceof ReceivedResultDocument)
            return ReceivedResult;
        if (message instanceof ReceivedFaultDocument)
            return ReceivedFault;
        if (message instanceof SendingResponseSucceededDocument)
            return SendingResponseSucceeded;
        if (message instanceof SendingResponseFailedDocument)
            return SendingResponseFailed;
        if (message instanceof DataConsumedDocument)
            return DataConsumed;
        if (message instanceof DataProducedDocument)
            return DataProduced;
        if (message instanceof ApplicationAuditDocument)
            return ApplicationAudit;
        if (message instanceof ComputationDurationDocument)
            return ComputationDuration;
        if (message instanceof DataSendDurationDocument)
            return DataSendDuration;
        if (message instanceof DataReceiveDurationDocument)
            return DataReceiveDuration;
        if (message instanceof PublishURLDocument)
            return PublishURL;
        if (message instanceof LogInfoDocument)
            return LogInfo;
        if (message instanceof LogExceptionDocument)
            return LogException;
        if (message instanceof LogWarningDocument)
            return LogWarning;
        if (message instanceof LogDebugDocument)
            return LogDebug;
        if (message instanceof ResourceMappingDocument)
            return ResourceMapping;
        if (message instanceof JobStatusDocument)
            return JobStatus;

        // default
        return NotificationType.Unknown;
    }

    public static ActivityTime getActivityTime(XmlObject activity) throws ParseException {
        Date clockTime = getActivityTimestamp(activity);
        int logicalTime = getActivityWorkflowTimestep(activity);
        return new ActivityTime(logicalTime, clockTime);
    }

    public static final QName TIMESTAMP_QNAME = new QName(WFT_NS, "timestamp");
    public static final QName NOTIFICATION_SRC_QNAME = new QName(WFT_NS, "notificationSource");
    public static final QName WF_TIMESTEP_QNAME = new QName(WFT_NS, "workflowTimestep");

    public static Date getActivityTimestamp(XmlObject activity) throws ParseException {
        // $ACTIVITY_XML/*/timestamp
        XmlCursor xc = activity.newCursor();
        // ./
        // xc.toStartDoc();
        // ./*
        xc.toNextToken();
        // ./*/timestamp
        xc.toChild(TIMESTAMP_QNAME);
        System.out.println(xc.xmlText());
        XmlCalendar calendar = new XmlCalendar(xc.getTextValue());
        // return getDateFormat().parse(xc.getTextValue()); // fixme: this
        // supports only one date format
        return calendar.getTime();
    }

    static SimpleDateFormat getDateFormat() {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    }

    public static int getActivityWorkflowTimestep(XmlObject activity) {
        // $ACTIVITY_XML/*/notificationSource/@workflowTimestep
        XmlCursor xc = activity.newCursor();
        // ./
        // xc.toStartDoc();
        // ./*
        xc.toNextToken();
        // ./*/notificationSource
        xc.toChild(NOTIFICATION_SRC_QNAME);
        // ./*/notificationSource/@*
        boolean hasAttr = xc.toFirstAttribute();
        while (hasAttr && !WF_TIMESTEP_QNAME.equals(xc.getName())) {
            hasAttr = xc.toNextAttribute();
        }
        if (hasAttr) {
            // ./*/notificationSource/@workflowTimestep
            return Integer.parseInt(xc.getTextValue());
        } else {
            return -1;
        }
    }

    public static List<XmlObject> getAnnotations(XmlObject message, AnnotationConsts annoElementQName) {
        // locate the wft:annotation element
        final String ANNO_XPATH = "declare namespace wft='" + ANNO_QNAME.getNamespaceURI() + "' .//wft:"
                + ANNO_QNAME.getLocalPart();
        XmlObject[] annoObj = message.selectPath(ANNO_XPATH);
        // for(XmlObject obj : annoObj){ System.out.println(obj); }
        // if no result, return nothing
        if (annoObj == null || annoObj.length == 0)
            return new ArrayList<XmlObject>(0);
        // if more than one 'annotation' element, select only first. FIXME?
        // Throw exception?
        final String ANNO_ELEMENT_XPATH = "declare namespace ann='" + annoElementQName.getQName().getNamespaceURI()
                + "' .//ann:" + annoElementQName.getQName().getLocalPart();
        XmlObject[] annoElements = annoObj[0].selectPath(ANNO_ELEMENT_XPATH);
        // for(XmlObject obj : annoElements){ System.out.println(obj); }
        return Arrays.asList(annoElements);
    }

    public static List<String> getSimpleAnnotations(XmlObject message, AnnotationConsts annoElementQName)
            throws XmlException {
        List<XmlObject> simpleXmlElements = getAnnotations(message, annoElementQName);
        List<String> simpleElements = new ArrayList<String>(simpleXmlElements.size());
        for (XmlObject obj : simpleXmlElements) {
            simpleElements.add((XmlAnySimpleType.Factory.parse(obj.xmlText())).stringValue());
        }
        return simpleElements;
    }

    public static Map<QName, XmlObject> getAllAnnotations(XmlObject message) throws XmlException {
        // locate the wft:annotation element
        final String ANNO_XPATH = "declare namespace wft='" + ANNO_QNAME.getNamespaceURI() + "' .//wft:"
                + ANNO_QNAME.getLocalPart();
        XmlObject[] annoObj = message.selectPath(ANNO_XPATH);
        // for(XmlObject obj : annoObj){ System.out.println(obj); }
        // if no result, return nothing
        if (annoObj == null || annoObj.length == 0)
            return new HashMap<QName, XmlObject>();
        // if more than one 'annotation' element, select only first. FIXME?
        // Throw exception?
        final String ANNO_ELEMENT_XPATH = "*";
        XmlObject[] annoElements = annoObj[0].selectPath(ANNO_ELEMENT_XPATH);
        // for(XmlObject obj : annoElements){ System.out.println(obj); }
        Map<QName, XmlObject> annoMap = new HashMap<QName, XmlObject>();
        for (XmlObject annoFrag : annoElements) {
            XmlObject annoElt = XmlObject.Factory.parse(annoFrag.xmlText(new XmlOptions().setSaveOuter()));
            // System.out.println(annoElt);
            XmlCursor xc = annoElt.newCursor();
            xc.toNextToken();
            // System.out.println(xc.getName());
            // System.out.println(xc.getObject());
            annoMap.put(xc.getName(), xc.getObject());
        }
        return annoMap;
    }

    public static Map<QName, String> getSimpleAnnotations(XmlObject message) throws XmlException {
        Map<QName, XmlObject> simpleXmlElements = getAllAnnotations(message);
        Map<QName, String> simpleElements = new HashMap<QName, String>(simpleXmlElements.size());
        for (Map.Entry<QName, XmlObject> obj : simpleXmlElements.entrySet()) {
            simpleElements.put(obj.getKey(), (XmlAnySimpleType.Factory.parse(obj.getValue().xmlText())).stringValue());
        }
        return simpleElements;
    }

    /**
     * Constructs a conttructorProps from LeadContextHeader.
     * 
     * This methods sets annotations available in LeadContextHeader. You still need to set additional annotations if any
     * before calling NotifierFactory.createNotifier().
     * 
     * @param leadContext
     * @return The constructor props created.
     */
    public static ConstructorProps createConstructorPropsFromLeadContext(LeadContextHeader leadContext) {
        EndpointReference sinkEpr = leadContext.getEventSink();
        ConstructorProps props = ConstructorProps.newProps(ConstructorConsts.BROKER_EPR, sinkEpr.getAddress());

        AnnotationProps annotationProps = AnnotationProps.newProps();
        String experimentId = leadContext.getExperimentId();
        if (experimentId != null) {
            annotationProps.set(AnnotationConsts.ExperimentID, experimentId);
        }
        URI workflowTemplateId = leadContext.getWorkflowTemplateId();
        if (workflowTemplateId != null) {
            annotationProps.set(AnnotationConsts.AbstractWorkflowID, workflowTemplateId.toString());
        }
        String userDn = leadContext.getUserDn();
        if (userDn != null) {
            annotationProps.set(AnnotationConsts.UserDN, userDn);
        }
        props.set(ConstructorConsts.ANNOTATIONS, annotationProps);
        return props;
    }

    public static void testAnno(String[] args) throws XmlException {
        String test1 = "<wor:dataProduced xmlns:wor='http://lead.extreme.indiana.edu/namespaces/2006/06/workflow_tracking'>"
                + "<wor:notificationSource wor:serviceID='urn:qname:http://www.extreme.indiana.edu/lead:Terrain' wor:workflowID='tag:gpel.leadproject.org,2006:728/ADASInitializedWRFForecasting/instance46' wor:workflowTimestep='6' wor:workflowNodeID='Terrain_Preprocessor'/>"
                + "<wor:timestamp>2007-02-14T15:44:46.509-05:00</wor:timestamp>"
                + "<wor:dataProduct>"
                + "<wor:id>gsiftp://bigred.iu.teragrid.org//N/gpfsbr/tg-drlead/workDirs/Terrain_Wed_Feb_14_15_44_42_EST_2007_b017e36f-146e-4852-89de-e26b00d82d77/inputData/arpstrn.input</wor:id>"
                + "<wor:location>gsiftp://bigred.iu.teragrid.org//N/gpfsbr/tg-drlead/workDirs/Terrain_Wed_Feb_14_15_44_42_EST_2007_b017e36f-146e-4852-89de-e26b00d82d77/inputData/arpstrn.input</wor:location>"
                + "<wor:sizeInBytes>-1</wor:sizeInBytes>"
                + "<wor:timestamp>2007-02-14T15:44:46.508-05:00</wor:timestamp>"
                + "<wor:description>gsiftp://bigred.iu.teragrid.org//N/gpfsbr/tg-drlead/workDirs/Terrain_Wed_Feb_14_15_44_42_EST_2007_b017e36f-146e-4852-89de-e26b00d82d77/inputData/arpstrn.input</wor:description>"
                + "<wor:annotation>"
                + "<dataProductType xmlns='http://lead.extreme.indiana.edu/namespaces/2006/06/workflow_tracking'>LEADNameListFile</dataProductType>"
                + "</wor:annotation>" + "</wor:dataProduct>" + "</wor:dataProduced>";
        String test2 = "<wor:serviceInvoked xmlns:wor='http://lead.extreme.indiana.edu/namespaces/2006/06/workflow_tracking'>"
                + "  <wor:notificationSource wor:serviceID='urn:qname:http://www.extreme.indiana.edu/lead:Terrain' wor:workflowID='tag:gpel.leadproject.org,2006:728/ADASInitializedWRFForecasting/instance46' wor:workflowTimestep='6' wor:workflowNodeID='Terrain_Preprocessor'/>"
                + "  <wor:timestamp>2007-02-14T15:44:42.784-05:00</wor:timestamp>"
                + "  <wor:description>Service Invoked</wor:description>"
                + "  <wor:annotation>"
                + " <typedSOAPRequest xmlns='http://lead.extreme.indiana.edu/namespaces/2006/06/workflow_tracking'>"
                + "    <S:Envelope xmlns:wsa='http://www.w3.org/2005/08/addressing' xmlns:wsp='http://schemas.xmlsoap.org/ws/2002/12/policy' xmlns:S='http://schemas.xmlsoap.org/soap/envelope/'>"
                + "      <S:Header>"
                + "        <lh:context xmlns:lh='http://lead.extreme.indiana.edu/namespaces/2005/10/lead-context-header'>"
                + "          <lh:experiment-id>urn:uuid:3f422b0b-912f-49c1-8bb6-0c5612d160bf</lh:experiment-id>"
                + "          <lh:event-sink-epr>"
                + "            <wsa:Address>http://tyr11.cs.indiana.edu:12346/topic/3f422b0b-912f-49c1-8bb6-0c5612d160bf</wsa:Address>"
                + "          </lh:event-sink-epr>"
                + "          <lh:user-dn>/O=LEAD Project/OU=portal.leadproject.org/OU=cs.indiana.edu/CN=marcus/EMAIL=machrist@cs.indiana.edu</lh:user-dn>"
                + "          <lh:resource-catalog-url>https://everest.extreme.indiana.edu:22443/resource_catalog?wsdl</lh:resource-catalog-url>"
                + "          <lh:gfac-url>https://tyr09.cs.indiana.edu:23443?wsdl</lh:gfac-url>"
                + "          <lh:mylead-agent-url>https://bitternut.cs.indiana.edu:10243/myleadagent?wsdl</lh:mylead-agent-url>"
                + "          <lh:OUTPUT_DATA_DIRECTORY>gsiftp://chinkapin.cs.indiana.edu//data/data-output/3f422b0b-912f-49c1-8bb6-0c5612d160bf</lh:OUTPUT_DATA_DIRECTORY>"
                + "          <lh:OPENDAP_DIRECTORY>opendap://chinkapin.cs.indiana.edu:8080/thredds/dodsC/data/data-output/3f422b0b-912f-49c1-8bb6-0c5612d160bf</lh:OPENDAP_DIRECTORY>"
                + "          <lh:OUTPUT_DATA_FILES_SUFFIX>.nc</lh:OUTPUT_DATA_FILES_SUFFIX>"
                + "          <lh:workflow-instance-id>tag:gpel.leadproject.org,2006:728/ADASInitializedWRFForecasting/instance46</lh:workflow-instance-id>"
                + "          <lh:resource-broker-url>http://152.54.1.30:3333/resourcebroker?wsdl</lh:resource-broker-url>"
                + "          <lh:workflow-time-step>6</lh:workflow-time-step>"
                + "          <lh:workflow-node-id>Terrain_Preprocessor</lh:workflow-node-id>"
                + "          <lh:service-instance-id>http://tempuri.org/no-service-id</lh:service-instance-id>"
                + "        </lh:context>"
                + "        <wsa:MessageID>tag:gpel.leadproject.org,2006:728/ADASInitializedWRFForecasting/instance46/outgoing/Terrain_PreprocessorPartner/1</wsa:MessageID>"
                + "        <wsa:ReplyTo>"
                + "          <wsa:Address>http://tyr10.cs.indiana.edu:7080/gpel/728/ADASInitializedWRFForecasting/instance46/incoming/Terrain_PreprocessorPartner.atom</wsa:Address>"
                + "        </wsa:ReplyTo>"
                + "        <wsa:Action/>"
                + "        <wsa:To>https://tyr11.cs.indiana.edu:12554/</wsa:To>"
                + "        <wsa:FaultTo>"
                + "          <wsa:Address>http://tyr10.cs.indiana.edu:7080/gpel/728/ADASInitializedWRFForecasting/instance46/incoming/Terrain_PreprocessorPartner.atom</wsa:Address>"
                + "        </wsa:FaultTo>"
                + "        <wsa:RelatesTo>tag:gpel.leadproject.org,2006:728/ADASInitializedWRFForecasting/instance46/outgoing/Terrain_PreprocessorPartner/1</wsa:RelatesTo>"
                + "      </S:Header>"
                + "      <S:Body>"
                + "        <terrain_preprocessortypens:Preprocessor_InputParams xmlns:terrain_preprocessortypens='http://www.extreme.indiana.edu/lead/Terrain/xsd'>"
                + "          <CrossCuttingConfigurations n1:leadType='LeadCrosscutParameters' xmlns:n1='http://www.extreme.indiana.edu/namespaces/2004/01/gFac'>"
                + "            <lcp:nx xmlns:lcp='http://lead.extreme.indiana.edu/namespaces/2006/lead-crosscut-parameters/'>53</lcp:nx>"
                + "            <lcp:ny xmlns:lcp='http://lead.extreme.indiana.edu/namespaces/2006/lead-crosscut-parameters/'>53</lcp:ny>"
                + "            <lcp:dx xmlns:lcp='http://lead.extreme.indiana.edu/namespaces/2006/lead-crosscut-parameters/'>20000</lcp:dx>"
                + "            <lcp:dy xmlns:lcp='http://lead.extreme.indiana.edu/namespaces/2006/lead-crosscut-parameters/'>20000</lcp:dy>"
                + "            <lcp:fcst_time xmlns:lcp='http://lead.extreme.indiana.edu/namespaces/2006/lead-crosscut-parameters/'>12.0</lcp:fcst_time>"
                + "            <lcp:start_date xmlns:lcp='http://lead.extreme.indiana.edu/namespaces/2006/lead-crosscut-parameters/'>2007/02/13</lcp:start_date>"
                + "            <lcp:start_hour xmlns:lcp='http://lead.extreme.indiana.edu/namespaces/2006/lead-crosscut-parameters/'>18</lcp:start_hour>"
                + "            <lcp:ctrlat xmlns:lcp='http://lead.extreme.indiana.edu/namespaces/2006/lead-crosscut-parameters/'>39.97712</lcp:ctrlat>"
                + "            <lcp:ctrlon xmlns:lcp='http://lead.extreme.indiana.edu/namespaces/2006/lead-crosscut-parameters/'>-86.484375</lcp:ctrlon>"
                + "            <lcp:westbc xmlns:lcp='http://lead.extreme.indiana.edu/namespaces/2006/lead-crosscut-parameters/'>-90.97595</lcp:westbc>"
                + "            <lcp:eastbc xmlns:lcp='http://lead.extreme.indiana.edu/namespaces/2006/lead-crosscut-parameters/'>-81.9928</lcp:eastbc>"
                + "            <lcp:northbc xmlns:lcp='http://lead.extreme.indiana.edu/namespaces/2006/lead-crosscut-parameters/'>43.34412</lcp:northbc>"
                + "            <lcp:southbc xmlns:lcp='http://lead.extreme.indiana.edu/namespaces/2006/lead-crosscut-parameters/'>36.434242</lcp:southbc>"
                + "          </CrossCuttingConfigurations>"
                + "        </terrain_preprocessortypens:Preprocessor_InputParams>"
                + "      </S:Body>"
                + "    </S:Envelope>"
                + " </typedSOAPRequest>"
                + "  </wor:annotation>"
                + "  <wor:request>"
                + "    <wor:header>"
                + "      <S:Header xmlns:S='http://schemas.xmlsoap.org/soap/envelope/'>"
                + "        <lh:context xmlns:lh='http://lead.extreme.indiana.edu/namespaces/2005/10/lead-context-header'>"
                + "          <lh:experiment-id>urn:uuid:3f422b0b-912f-49c1-8bb6-0c5612d160bf</lh:experiment-id>"
                + "          <lh:event-sink-epr>"
                + "            <wsa:Address xmlns:wsa='http://www.w3.org/2005/08/addressing'>http://tyr11.cs.indiana.edu:12346/topic/3f422b0b-912f-49c1-8bb6-0c5612d160bf</wsa:Address>"
                + "          </lh:event-sink-epr>"
                + "          <lh:user-dn>/O=LEAD Project/OU=portal.leadproject.org/OU=cs.indiana.edu/CN=marcus/EMAIL=machrist@cs.indiana.edu</lh:user-dn>"
                + "          <lh:resource-catalog-url>https://everest.extreme.indiana.edu:22443/resource_catalog?wsdl</lh:resource-catalog-url>"
                + "          <lh:gfac-url>https://tyr09.cs.indiana.edu:23443?wsdl</lh:gfac-url>"
                + "          <lh:mylead-agent-url>https://bitternut.cs.indiana.edu:10243/myleadagent?wsdl</lh:mylead-agent-url>"
                + "          <lh:OUTPUT_DATA_DIRECTORY>gsiftp://chinkapin.cs.indiana.edu//data/data-output/3f422b0b-912f-49c1-8bb6-0c5612d160bf</lh:OUTPUT_DATA_DIRECTORY>"
                + "          <lh:OPENDAP_DIRECTORY>opendap://chinkapin.cs.indiana.edu:8080/thredds/dodsC/data/data-output/3f422b0b-912f-49c1-8bb6-0c5612d160bf</lh:OPENDAP_DIRECTORY>"
                + "          <lh:OUTPUT_DATA_FILES_SUFFIX>.nc</lh:OUTPUT_DATA_FILES_SUFFIX>"
                + "          <lh:workflow-instance-id>tag:gpel.leadproject.org,2006:728/ADASInitializedWRFForecasting/instance46</lh:workflow-instance-id>"
                + "          <lh:resource-broker-url>http://152.54.1.30:3333/resourcebroker?wsdl</lh:resource-broker-url>"
                + "          <lh:workflow-time-step>6</lh:workflow-time-step>"
                + "          <lh:workflow-node-id>Terrain_Preprocessor</lh:workflow-node-id>"
                + "          <lh:service-instance-id>http://tempuri.org/no-service-id</lh:service-instance-id>"
                + "        </lh:context>"
                + "        <wsa:MessageID xmlns:wsa='http://www.w3.org/2005/08/addressing'>tag:gpel.leadproject.org,2006:728/ADASInitializedWRFForecasting/instance46/outgoing/Terrain_PreprocessorPartner/1</wsa:MessageID>"
                + "        <wsa:ReplyTo xmlns:wsa='http://www.w3.org/2005/08/addressing'>"
                + "          <wsa:Address>http://tyr10.cs.indiana.edu:7080/gpel/728/ADASInitializedWRFForecasting/instance46/incoming/Terrain_PreprocessorPartner.atom</wsa:Address>"
                + "        </wsa:ReplyTo>"
                + "        <wsa:Action xmlns:wsa='http://www.w3.org/2005/08/addressing'/>"
                + "        <wsa:To xmlns:wsa='http://www.w3.org/2005/08/addressing'>https://tyr11.cs.indiana.edu:12554/</wsa:To>"
                + "        <wsa:FaultTo xmlns:wsa='http://www.w3.org/2005/08/addressing'>"
                + "          <wsa:Address>http://tyr10.cs.indiana.edu:7080/gpel/728/ADASInitializedWRFForecasting/instance46/incoming/Terrain_PreprocessorPartner.atom</wsa:Address>"
                + "        </wsa:FaultTo>"
                + "        <wsa:RelatesTo xmlns:wsa='http://www.w3.org/2005/08/addressing'>tag:gpel.leadproject.org,2006:728/ADASInitializedWRFForecasting/instance46/outgoing/Terrain_PreprocessorPartner/1</wsa:RelatesTo>"
                + "      </S:Header>"
                + "    </wor:header>"
                + "    <wor:body>"
                + "      <S:Body xmlns:S='http://schemas.xmlsoap.org/soap/envelope/'>"
                + "        <terrain_preprocessortypens:Preprocessor_InputParams xmlns:terrain_preprocessortypens='http://www.extreme.indiana.edu/lead/Terrain/xsd'>"
                + "          <CrossCuttingConfigurations>"
                + "            <lcp:nx xmlns:lcp='http://lead.extreme.indiana.edu/namespaces/2006/lead-crosscut-parameters/'>53</lcp:nx>"
                + "            <lcp:ny xmlns:lcp='http://lead.extreme.indiana.edu/namespaces/2006/lead-crosscut-parameters/'>53</lcp:ny>"
                + "            <lcp:dx xmlns:lcp='http://lead.extreme.indiana.edu/namespaces/2006/lead-crosscut-parameters/'>20000</lcp:dx>"
                + "            <lcp:dy xmlns:lcp='http://lead.extreme.indiana.edu/namespaces/2006/lead-crosscut-parameters/'>20000</lcp:dy>"
                + "            <lcp:fcst_time xmlns:lcp='http://lead.extreme.indiana.edu/namespaces/2006/lead-crosscut-parameters/'>12.0</lcp:fcst_time>"
                + "            <lcp:start_date xmlns:lcp='http://lead.extreme.indiana.edu/namespaces/2006/lead-crosscut-parameters/'>2007/02/13</lcp:start_date>"
                + "            <lcp:start_hour xmlns:lcp='http://lead.extreme.indiana.edu/namespaces/2006/lead-crosscut-parameters/'>18</lcp:start_hour>"
                + "            <lcp:ctrlat xmlns:lcp='http://lead.extreme.indiana.edu/namespaces/2006/lead-crosscut-parameters/'>39.97712</lcp:ctrlat>"
                + "            <lcp:ctrlon xmlns:lcp='http://lead.extreme.indiana.edu/namespaces/2006/lead-crosscut-parameters/'>-86.484375</lcp:ctrlon>"
                + "            <lcp:westbc xmlns:lcp='http://lead.extreme.indiana.edu/namespaces/2006/lead-crosscut-parameters/'>-90.97595</lcp:westbc>"
                + "            <lcp:eastbc xmlns:lcp='http://lead.extreme.indiana.edu/namespaces/2006/lead-crosscut-parameters/'>-81.9928</lcp:eastbc>"
                + "            <lcp:northbc xmlns:lcp='http://lead.extreme.indiana.edu/namespaces/2006/lead-crosscut-parameters/'>43.34412</lcp:northbc>"
                + "            <lcp:southbc xmlns:lcp='http://lead.extreme.indiana.edu/namespaces/2006/lead-crosscut-parameters/'>36.434242</lcp:southbc>"
                + "          </CrossCuttingConfigurations>"
                + "        </terrain_preprocessortypens:Preprocessor_InputParams>"
                + "      </S:Body>"
                + "    </wor:body>"
                + "  </wor:request>"
                + "  <wor:initiator wor:serviceID='tag:gpel.leadproject.org,2006:728/ADASInitializedWRFForecasting/instance46' wor:workflowTimestep='-1'/>"
                + "</wor:serviceInvoked>";

        XmlObject test1Obj = XmlObject.Factory.parse(test1);
        List<String> result1 = getSimpleAnnotations(test1Obj, AnnotationConsts.DataProductType);
        for (String obj : result1) {
            System.out.println(obj);
        }

        XmlObject test2Obj = XmlObject.Factory.parse(test2);
        List<XmlObject> result2 = getAnnotations(test2Obj, AnnotationConsts.TypedSOAPRequest);
        for (XmlObject obj : result2) {
            System.out.println(obj);
            final String ANNO_XPATH = "declare namespace S='http://schemas.xmlsoap.org/soap/envelope/'"
                    + " ./S:Envelope/S:Body/*";
            XmlObject[] out = obj.selectPath(ANNO_XPATH);
            for (XmlObject xo : out) {
                System.out.println("====");
                System.out.println(xo);
                System.out.println("----");
                class Param {
                    String paramName, paramType, paramValue;

                    public Param(String paramName_, String paramType_, String paramValue_) {
                        paramName = paramName_;
                        paramType = paramType_;
                        paramValue = paramValue_;
                    }

                    public String toString() {
                        return paramName + "<" + paramType + ">=[" + paramValue + "]";
                    }
                }
                List<Param> paramList = new ArrayList<Param>();

                XmlCursor xc = xo.newCursor();
                boolean exists = xc.toFirstChild();
                while (exists) {
                    String paramName = xc.getName().getLocalPart();
                    String paramType = xc.getAttributeText(new QName(
                            "http://www.extreme.indiana.edu/namespaces/2004/01/gFac", "leadType"));
                    String paramValue;
                    if ("LeadCrosscutParameters".equals(paramType)) {
                        XmlObject paramObj = xc.getObject();
                        XmlCursor xc2 = paramObj.newCursor();
                        boolean exists2 = xc2.toFirstChild();
                        while (exists2) {
                            String paramName2 = xc2.getName().getLocalPart();
                            String paramValue2 = xc2.getTextValue();
                            // get type from param name
                            String paramType2; // default
                            if ("nx".equals(paramName2) || "ny".equals(paramName2) || "dx".equals(paramName2)
                                    || "dx".equals(paramName2) || "ctrlat".equals(paramName2)
                                    || "ctrlon".equals(paramName2) || "westbc".equals(paramName2)
                                    || "eastbc".equals(paramName2) || "northbc".equals(paramName2)
                                    || "southbc".equals(paramName2)
                            // TODO: add more
                            ) {

                                paramType2 = "Numeric";
                            } else {
                                // default string
                                paramType2 = "String";
                            }

                            paramList.add(new Param(paramName + ":" + paramName2, paramType2, paramValue2));
                            exists2 = xc2.toNextSibling();
                        }
                    } else if ("Integer".equals(paramType)) {
                        paramValue = xc.getTextValue();
                        paramType = "Numeric";
                        paramList.add(new Param(paramName, paramType, paramValue));
                    } else if ("Nominal".equals(paramType)) {
                        paramValue = xc.getTextValue();
                        paramType = "Nominal";
                        paramList.add(new Param(paramName, paramType, paramValue));
                    } else {
                        // default
                        paramValue = xc.getTextValue();
                        paramType = "String";
                        paramList.add(new Param(paramName, paramType, paramValue));
                    }
                    exists = xc.toNextSibling();
                }
                // return paramList;

                for (Param p : paramList) {
                    System.out.println(p);
                }
            }
        }
    }

    public static void testTimestamp(String[] args) throws Exception {
        final String ACTIVITY = "<wor:serviceInvoked infoModelVersion='2.6' xmlns:wor='http://lead.extreme.indiana.edu/namespaces/2006/06/workflow_tracking'><wor:notificationSource wor:serviceID='urn:qname:http://www.extreme.indiana.edu/lead:FactoryService_Fri_Jun_22_21_21_24_EDT_2007_311971' wor:workflowID='tag:gpel.leadproject.org,2006:76M/Challenge2WorkflowPartI/instance5' wor:workflowTimestep='6' wor:workflowNodeID='GFac:AlignWarpService_Run'/><wor:timestamp>2007-06-23T11:16:35.404-04:00</wor:timestamp><wor:description>Service Invoked</wor:description><wor:annotation><n1:typedSOAPRequest xmlns:n1='http://lead.extreme.indiana.edu/namespaces/2006/06/workflow_tracking'><S:Envelope xmlns:xsd='http://www.w3.org/2001/XMLSchema' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xmlns:wsa='http://www.w3.org/2005/08/addressing' xmlns:wsp='http://schemas.xmlsoap.org/ws/2002/12/policy' xmlns:S='http://schemas.xmlsoap.org/soap/envelope/'><S:Header><lh:context xmlns:lh='http://lead.extreme.indiana.edu/namespaces/2005/10/lead-context-header'><lh:experiment-id>xbaya-experiment</lh:experiment-id><lh:event-sink-epr><wsa:Address>http://tyr10.cs.indiana.edu:12346/topic/second-provenance-challenge-20070623T111715</wsa:Address></lh:event-sink-epr><lh:user-dn>/O=LEAD Project/OU=Indiana University Extreme Lab/OU=linbox1.extreme.indiana.edu/OU=extreme.indiana.edu/CN=ysimmhan/EMAIL=ysimmhan@cs.indiana.edu</lh:user-dn><lh:resource-catalog-url>https://everest.extreme.indiana.edu:20443/resource_catalog?wsdl</lh:resource-catalog-url><lh:gfac-url>https://tyr12.cs.indiana.edu:23443/?wsdl</lh:gfac-url><lh:mylead-agent-url>https://tyr03.cs.indiana.edu:20243/myleadagent?wsdl</lh:mylead-agent-url><lh:workflow-template-id>tag:gpel.leadproject.org,2006:76M/Challenge2WorkflowPartI</lh:workflow-template-id><lh:workflow-instance-id>tag:gpel.leadproject.org,2006:76M/Challenge2WorkflowPartI/instance5</lh:workflow-instance-id><lh:workflow-time-step>6</lh:workflow-time-step><lh:workflow-node-id>GFac:AlignWarpService_Run</lh:workflow-node-id><lh:service-instance-id>http://tempuri.org/no-service-id</lh:service-instance-id></lh:context><wsa:To>https://tyr12.cs.indiana.edu:23443/</wsa:To><wsa:Action>http://www.extreme.indiana.edu/lead/FactoryService/CreateService</wsa:Action></S:Header><S:Body><n1:CreateService_InputParams xmlns:n1='http://www.extreme.indiana.edu/lead/FactoryService/xsd'><serviceQName n2:leadType='String' xmlns:n2='http://www.extreme.indiana.edu/namespaces/2004/01/gFac'>{http://www.extreme.indiana.edu/karma/challenge2}AlignWarpService</serviceQName><security n3:leadType='String' xmlns:n3='http://www.extreme.indiana.edu/namespaces/2004/01/gFac'>None</security><registryUrl n4:leadType='String' xmlns:n4='http://www.extreme.indiana.edu/namespaces/2004/01/gFac'/><host n5:leadType='String' xmlns:n5='http://www.extreme.indiana.edu/namespaces/2004/01/gFac'/><appHost n6:leadType='String' xmlns:n6='http://www.extreme.indiana.edu/namespaces/2004/01/gFac'/></n1:CreateService_InputParams></S:Body></S:Envelope></n1:typedSOAPRequest><n1:experimentID xmlns:n1='http://lead.extreme.indiana.edu/namespaces/2006/06/workflow_tracking'>xbaya-experiment</n1:experimentID><n1:userDN xmlns:n1='http://lead.extreme.indiana.edu/namespaces/2006/06/workflow_tracking'>/O=LEAD Project/OU=Indiana University Extreme Lab/OU=linbox1.extreme.indiana.edu/OU=extreme.indiana.edu/CN=ysimmhan/EMAIL=ysimmhan@cs.indiana.edu</n1:userDN><experimentID xmlns='http://lead.extreme.indiana.edu/namespaces/2006/06/workflow_tracking'>xbaya-experiment</experimentID></wor:annotation><wor:request><wor:header><S:Header xmlns:S='http://schemas.xmlsoap.org/soap/envelope/'><lh:context xmlns:lh='http://lead.extreme.indiana.edu/namespaces/2005/10/lead-context-header'><lh:experiment-id>xbaya-experiment</lh:experiment-id><lh:event-sink-epr><wsa:Address xmlns:wsa='http://www.w3.org/2005/08/addressing'>http://tyr10.cs.indiana.edu:12346/topic/second-provenance-challenge-20070623T111715</wsa:Address></lh:event-sink-epr><lh:user-dn>/O=LEAD Project/OU=Indiana University Extreme Lab/OU=linbox1.extreme.indiana.edu/OU=extreme.indiana.edu/CN=ysimmhan/EMAIL=ysimmhan@cs.indiana.edu</lh:user-dn><lh:resource-catalog-url>https://everest.extreme.indiana.edu:20443/resource_catalog?wsdl</lh:resource-catalog-url><lh:gfac-url>https://tyr12.cs.indiana.edu:23443/?wsdl</lh:gfac-url><lh:mylead-agent-url>https://tyr03.cs.indiana.edu:20243/myleadagent?wsdl</lh:mylead-agent-url><lh:workflow-template-id>tag:gpel.leadproject.org,2006:76M/Challenge2WorkflowPartI</lh:workflow-template-id><lh:workflow-instance-id>tag:gpel.leadproject.org,2006:76M/Challenge2WorkflowPartI/instance5</lh:workflow-instance-id><lh:workflow-time-step>6</lh:workflow-time-step><lh:workflow-node-id>GFac:AlignWarpService_Run</lh:workflow-node-id><lh:service-instance-id>http://tempuri.org/no-service-id</lh:service-instance-id></lh:context><wsa:To xmlns:wsa='http://www.w3.org/2005/08/addressing'>https://tyr12.cs.indiana.edu:23443/</wsa:To><wsa:Action xmlns:wsa='http://www.w3.org/2005/08/addressing'>http://www.extreme.indiana.edu/lead/FactoryService/CreateService</wsa:Action></S:Header></wor:header><wor:body><S:Body xmlns:S='http://schemas.xmlsoap.org/soap/envelope/'><n1:CreateService_InputParams xmlns:n1='http://www.extreme.indiana.edu/lead/FactoryService/xsd'><serviceQName>{http://www.extreme.indiana.edu/karma/challenge2}AlignWarpService</serviceQName><security>None</security><registryUrl/><host/><appHost/></n1:CreateService_InputParams></S:Body></wor:body></wor:request><wor:initiator wor:serviceID='tag:gpel.leadproject.org,2006:76M/Challenge2WorkflowPartI/instance5' wor:workflowTimestep='-1'/></wor:serviceInvoked>";
        XmlObject activity = XmlObject.Factory.parse(ACTIVITY);
        System.out.println(getActivityTimestamp(activity));
        System.out.println(getActivityWorkflowTimestep(activity));
    }

    public static void main(String[] args) throws XmlException, ParseException {
        final String ACTIVITY = "<wor:dataConsumed xmlns:wor='http://lead.extreme.indiana.edu/namespaces/2006/06/workflow_tracking'>"
                + "<wor:notificationSource wor:serviceID='urn:qname:http://www.extreme.indiana.edu/karma/challenge2:AlignWarpService_Wed_Feb_21_12_09_49_EST_2007_67130' wor:workflowID='tag:gpel.leadproject.org,2006:72L/Challenge2WorkflowPartI/instance2' wor:workflowTimestep='6' wor:workflowNodeID='AlignWarpService_Run'/>"
                + "<wor:timestamp>2007-02-21T12:10:33.772-05:00</wor:timestamp>"
                + "<wor:dataProduct>"
                + "<wor:id>urn:leadproject-org:data:3d847d61-696e-4742-b98b-51f39aa2c679</wor:id>"
                + "<wor:location>gsiftp://tyr15.cs.indiana.edu//san/extreme/tmp/service_logs/development/AlignWarp_Wed_Feb_21_12_09_57_EST_2007_17/inputData/anatomy1.img</wor:location>"
                + "<wor:sizeInBytes>-1</wor:sizeInBytes>"
                + "<wor:timestamp>2007-02-21T12:10:33.771-05:00</wor:timestamp>"
                + "<wor:description>gsiftp://tyr15.cs.indiana.edu//san/extreme/tmp/service_logs/development/AlignWarp_Wed_Feb_21_12_09_57_EST_2007_17/inputData/anatomy1.img</wor:description>"
                + "<wor:annotation>"
                + "<type xmlns='http://www.extreme.indiana.edu/namespaces/2004/01/gFac'>DataID</type>"
                + "<center xmlns='http://twiki.ipaw.info/bin/view/Challenge/SecondProvenanceChallenge'>UChicago</center>"
                + "</wor:annotation>" + "</wor:dataProduct>" + "</wor:dataConsumed>";

        XmlObject activity = XmlObject.Factory.parse(ACTIVITY);
        Map<QName, String> annos = getSimpleAnnotations(activity);
        for (Map.Entry<QName, String> anno : annos.entrySet()) {
            System.out.println(anno);
        }

    }
}
