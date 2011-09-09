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
import java.util.Iterator;
import java.util.List;

import org.apache.airavata.commons.gfac.api.Registry;
import org.apache.airavata.commons.gfac.type.ApplicationDeploymentDescription;
import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.commons.gfac.type.ServiceDescription;
import org.apache.airavata.core.gfac.context.invocation.InvocationContext;
import org.apache.airavata.core.gfac.context.invocation.impl.DefaultExecutionDescription;
import org.apache.airavata.core.gfac.exception.SchedulerException;
import org.apache.airavata.core.gfac.provider.GramProvider;
import org.apache.airavata.core.gfac.provider.LocalProvider;
import org.apache.airavata.core.gfac.provider.Provider;
import org.apache.airavata.core.gfac.scheduler.Scheduler;
import org.apache.airavata.core.gfac.utils.GfacUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class selects {@link Provider} based on information in {@link Registry}
 */
public class SchedulerImpl implements Scheduler {

    private static Logger log = LoggerFactory.getLogger(SchedulerImpl.class);

    public Provider schedule(InvocationContext context) throws SchedulerException {

        Registry registryService = context.getExecutionContext().getRegistryService();

        /*
         * Load Service
         */
        ServiceDescription serviceDesc = registryService.getServiceDescription(context.getServiceName());

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
        ApplicationDeploymentDescription app = registryService.getDeploymentDescription(context.getServiceName(),
                host.getName());

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

        /*
         * Determine provider
         */
        String hostName = host.getName();
        try {
            if (GfacUtils.isLocalHost(hostName)) {
                return new LocalProvider();
            } else {
                return new GramProvider();
            }
        } catch (UnknownHostException e) {
            throw new SchedulerException("Cannot get IP for current host", e);
        }

    }

    private HostDescription scheduleToHost(Registry regService, String serviceName) {

        log.info("Searching registry for some deployed application hosts");
        List<HostDescription> hosts = regService.getServiceLocation(serviceName);
        if (hosts != null && hosts.size() > 0) {
            HostDescription result = null;
            for (Iterator<HostDescription> iterator = hosts.iterator(); iterator.hasNext();) {
                result = iterator.next();

                log.info("Found service on: " + result.getName());
            }
            return result;
        } else {
            log.warn("Applcation  " + serviceName + " not found in registry");
            return null;
        }
    }
}
