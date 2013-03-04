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

package org.apache.airavata.core.gfac.scheduler.impl;

import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import org.apache.airavata.client.api.AiravataAPI;
import org.apache.airavata.client.api.AiravataAPIInvocationException;
import org.apache.airavata.commons.gfac.type.ApplicationDescription;
import org.apache.airavata.common.workflow.execution.context.WorkflowContextHeaderBuilder;
import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.commons.gfac.type.ServiceDescription;
import org.apache.airavata.core.gfac.context.invocation.InvocationContext;
import org.apache.airavata.core.gfac.context.invocation.impl.DefaultExecutionDescription;
import org.apache.airavata.core.gfac.exception.ProviderException;
import org.apache.airavata.core.gfac.exception.SchedulerException;
import org.apache.airavata.core.gfac.provider.Provider;
import org.apache.airavata.gfac.provider.impl.EC2Provider;
import org.apache.airavata.core.gfac.provider.impl.GramProvider;
import org.apache.airavata.core.gfac.provider.impl.LocalProvider;
import org.apache.airavata.core.gfac.scheduler.Scheduler;
import org.apache.airavata.core.gfac.utils.GfacUtils;
import org.apache.airavata.registry.api.AiravataRegistry2;
import org.apache.airavata.schemas.wec.ApplicationSchedulingContextDocument;
import org.apache.airavata.schemas.wec.ContextHeaderDocument;
import org.apache.airavata.schemas.wec.SecurityContextDocument;
import org.apache.axiom.om.OMElement;
import org.apache.xmlbeans.XmlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class selects {@link Provider} based on information in {@link AiravataRegistry2}
 */
public class SchedulerImpl implements Scheduler {

    private static Logger log = LoggerFactory.getLogger(SchedulerImpl.class+
            "." + WorkflowContextHeaderBuilder.getCurrentContextHeader().getWorkflowMonitoringContext().getExperimentId());

    public Provider schedule(InvocationContext context) throws SchedulerException {

        AiravataAPI registryService = context.getExecutionContext().getRegistryService();

        /*
         * Load Service
         */
        ServiceDescription serviceDesc = null;
        try {
            serviceDesc = registryService.getApplicationManager().getServiceDescription(context.getServiceName());
        } catch (AiravataAPIInvocationException e2) {
            e2.printStackTrace();
        }

        if (serviceDesc == null)
            throw new SchedulerException("Service Desciption for " + context.getServiceName()
                    + " does not found on resource Catalog " + registryService);

        /*
         * Load host
         */
        HostDescription host = scheduleToHost(registryService, context.getServiceName());

        if (host == null)
            throw new SchedulerException("Host Desciption for " + context.getServiceName()
                    + " does not found on resource Catalog " + registryService);

        /*
         * Load app
         */
        ApplicationDescription app = null;
        try {
            app = registryService.getApplicationManager().getApplicationDescription(context.getServiceName(),
                    getRegisteredHost(registryService, context.getServiceName()).getType().getHostName());
        } catch (AiravataAPIInvocationException e2) {
            e2.printStackTrace();
        }

        if (app == null)
            throw new SchedulerException("App Desciption for " + context.getServiceName()
                    + " does not found on resource Catalog " + registryService);

        /*
         * Check class and binding
         */

        if (context.getExecutionDescription() == null) {
            context.setExecutionDescription(new DefaultExecutionDescription());
        }
        context.getExecutionDescription().setHost(host);
        context.getExecutionDescription().setService(serviceDesc);
        context.getExecutionDescription().setApp(app);

        OMElement omSecurityContextHeader = context.getExecutionContext().getSecurityContextHeader();

        ContextHeaderDocument document = null;
        try {
            if (omSecurityContextHeader != null) {
                document = ContextHeaderDocument.Factory.parse(omSecurityContextHeader.toStringWithConsume());
            }
        } catch (XMLStreamException e) {
            e.printStackTrace();
        } catch (XmlException e) {
            e.printStackTrace();
        }

        SecurityContextDocument.SecurityContext.AmazonWebservices amazonWebservices = null;
        if (document != null) {
            amazonWebservices = document.getContextHeader().getSecurityContext().getAmazonWebservices();
        }

        /*
         * Determine provider
         */
        String hostName = host.getType().getHostAddress();
        try {
            if (GfacUtils.isLocalHost(hostName)) {
                return new LocalProvider();
            } else if (amazonWebservices != null && hostName != null) {
                log.info("host name: " + hostName);

                // Amazon Provider
                if (hostName.equalsIgnoreCase("AMAZON")){
                    log.info("EC2 Provider Selected");
                    try {
                        return new EC2Provider(context);
                    } catch (ProviderException e) {
                        throw new SchedulerException("Unable to select the EC2Provider", e);
                    }
                }
            } else {
                return new GramProvider();
            }
        } catch (UnknownHostException e) {
            throw new SchedulerException("Cannot get IP for current host", e);
        }

        return null;
    }

    private HostDescription scheduleToHost(AiravataAPI regService, String serviceName) {
        // Since xbaya removes the other scheduling configuration here we only have pick the 0th element of the array
        String hostName = null;
        ContextHeaderDocument.ContextHeader currentContextHeader = WorkflowContextHeaderBuilder.getCurrentContextHeader();
        if (currentContextHeader != null && currentContextHeader.getWorkflowSchedulingContext() != null) {
            ApplicationSchedulingContextDocument.ApplicationSchedulingContext[] applicationSchedulingContextArray = currentContextHeader.getWorkflowSchedulingContext().getApplicationSchedulingContextArray();
            if (applicationSchedulingContextArray != null && applicationSchedulingContextArray.length > 0) {
                hostName = applicationSchedulingContextArray[0].getHostName();
            }
        }
        log.info("Searching registry for some deployed application hosts");
        HostDescription result = null;
        Map<HostDescription, List<ApplicationDescription>> deploymentDescription = null;
        result = getRegisteredHost(regService, serviceName);
        // if user specify the host in the workflowcontext header we pick that host instead of picking the last hostName
        if(hostName != null){
            HostDescription hostDescription = null;
            try {
                hostDescription = regService.getApplicationManager().getHostDescription(hostName);
            } catch (AiravataAPIInvocationException e) {
                e.printStackTrace();
                log.warn("Wrong host Name provided in WorkflowContext Header");
            }
            if(hostDescription != null){
                result = hostDescription;
            }
        }
        if (result==null){
        	log.warn("Application  " + serviceName + " not found in registry");
        }
        return result;
//        List<HostDescription> hosts = regService.getServiceLocation(serviceName);
//        if (hosts != null && hosts.size() > 0) {
//            HostDescription result = null;
//            for (Iterator<HostDescription> iterator = hosts.iterator(); iterator.hasNext();) {
//                result = iterator.next();
//
//                log.info("Found service on: " + result.getType().getHostAddress());
//            }
//            return result;
//        } else {
//            log.warn("Applcation  " + serviceName + " not found in registry");
//            return null;
//        }
    }

    private HostDescription getRegisteredHost(AiravataAPI regService, String serviceName) {
        HostDescription result = null;
        try {
            Map<String, ApplicationDescription> applicationDescriptors = regService.getApplicationManager().getApplicationDescriptors(serviceName);
            for (String hostDescName : applicationDescriptors.keySet()) {
                HostDescription hostDescriptor = regService.getApplicationManager().getHostDescription(hostDescName);
                result = hostDescriptor;
                log.info("Found service on: " + result.getType().getHostAddress());
            }
        } catch (AiravataAPIInvocationException e) {
            e.printStackTrace();
        }
        return result;
    }
}
