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
package org.apache.airavata.gfac.core.provider;

import org.apache.airavata.common.utils.MonitorPublisher;
import org.apache.airavata.gfac.GFacException;
import org.apache.airavata.gfac.core.context.JobExecutionContext;
import org.apache.airavata.gfac.core.cpi.BetterGfacImpl;
import org.apache.airavata.model.workspace.experiment.JobDetails;
import org.apache.airavata.model.workspace.experiment.JobStatus;
import org.apache.airavata.persistance.registry.jpa.impl.RegistryFactory;
import org.apache.airavata.registry.cpi.Registry;
import org.apache.airavata.registry.cpi.RegistryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractRecoverableProvider implements GFacRecoverableProvider {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    protected Registry registry = null;
    protected JobDetails details;     //todo we need to remove this and add methods to fill Job details, this is not a property of a provider
    protected JobStatus status;   //todo we need to remove this and add methods to fill Job details, this is not a property of a provider
    protected JobExecutionContext jobExecutionContext;

    protected MonitorPublisher monitorPublisher;

    protected AbstractRecoverableProvider() {                                            //todo this has to be fixed
        this.monitorPublisher = BetterGfacImpl.getMonitorPublisher();
        if (this.monitorPublisher == null) {
            this.monitorPublisher = BetterGfacImpl.getMonitorPublisher();
        }
    }

    public void initialize(JobExecutionContext jobExecutionContext) throws GFacProviderException, GFacException {
        log.debug("Initializing " + this.getClass().getName());
        if (jobExecutionContext.getRegistry() == null) {
            try {
                registry = RegistryFactory.getDefaultRegistry();
            } catch (RegistryException e) {
                throw new GFacException("Unable to create registry instance", e);
            }
        } else {
            registry = jobExecutionContext.getRegistry();
        }
        details = new JobDetails();
        status = new JobStatus();
        this.jobExecutionContext = jobExecutionContext;
    }

    public MonitorPublisher getMonitorPublisher() {
        return monitorPublisher;
    }

    public void setMonitorPublisher(MonitorPublisher monitorPublisher) {
        this.monitorPublisher = monitorPublisher;
    }
}
