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

import javax.xml.namespace.QName;

import org.apache.airavata.core.gfac.context.InvocationContext;
import org.apache.airavata.core.gfac.exception.GfacException;
import org.apache.airavata.core.gfac.exception.GfacException.FaultCode;
import org.apache.airavata.core.gfac.provider.GramProvider;
import org.apache.airavata.core.gfac.provider.LocalProvider;
import org.apache.airavata.core.gfac.provider.Provider;
import org.apache.airavata.core.gfac.registry.RegistryService;
import org.apache.airavata.core.gfac.scheduler.Scheduler;
import org.apache.airavata.core.gfac.utils.GfacUtils;
import org.apache.xmlbeans.XmlException;
import org.ogce.schemas.gfac.documents.ServiceMapDocument;
import org.ogce.schemas.gfac.documents.ServiceMapType;

public class SchedulerImpl implements Scheduler {

    public Provider schedule(InvocationContext context) throws GfacException {

        String hostName = null;

        /*
         * Load host and app description from registry
         */
        RegistryService registryService = context.getExecutionContext().getRegistryService();
        String serviceMapStr = registryService.getServiceMap(context.getServiceName());

        if (serviceMapStr != null) {
            try {

                ServiceMapType serviceMap = ServiceMapDocument.Factory.parse(serviceMapStr).getServiceMap();
                QName appName = GfacUtils.findApplcationName(serviceMap);

                // host name
                hostName = findHostFromServiceMap(registryService, appName);

            } catch (XmlException e) {
                throw new GfacException(e, FaultCode.InitalizationError);
            }
        } else {
            throw new GfacException("Service Map for " + context.getServiceName()
                    + " does not found on resource Catalog " + registryService, FaultCode.InvalidRequest);
        }

        /*
         * Determine provider
         */
        if (GfacUtils.isLocalHost(hostName)) {
            return new LocalProvider();
        } else {
            // set Security context for executionContext
            if (context.getSecurityContext(GramProvider.MYPROXY_SECURITY_CONTEXT) != null) {
                context.getExecutionContext().setSecurityContext(
                        context.getSecurityContext(GramProvider.MYPROXY_SECURITY_CONTEXT));
            } else {
                throw new GfacException("Cannot get security context to run on Gram", FaultCode.InvalidRequest);
            }

            return new GramProvider();
        }
    }

    private String findHostFromServiceMap(RegistryService regService, QName appName) throws GfacException {

        System.out.println("Searching registry for some deployed application hosts\n");
        String[] hosts = regService.app2Hosts(appName.toString());
        if (hosts.length > 1) {
            String hostNames = "";
            for (int i = 0; i < hosts.length; i++) {
                hostNames = hostNames + hosts[i];
            }
            System.out.println("Application deployed on more than one machine. The full Host list is " + hostNames
                    + "\n");
        }
        if (hosts.length >= 1) {
            System.out.println("Found Host = " + hosts[0]);
            return hosts[0];
        } else {
            System.out.println("Applcation  " + appName.getLocalPart() + " not found in registry");
            return null;
        }
    }
}
