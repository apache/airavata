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

package org.apache.airavata.core.gfac.services.impl;

import org.apache.airavata.commons.gfac.type.ApplicationDeploymentDescription;
import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.commons.gfac.type.ServiceDescription;
import org.apache.airavata.core.gfac.context.invocation.ExecutionDescription;
import org.apache.airavata.core.gfac.context.invocation.InvocationContext;
import org.apache.airavata.core.gfac.context.invocation.impl.DefaultExecutionDescription;
import org.apache.airavata.core.gfac.exception.GfacException;
import org.apache.airavata.core.gfac.exception.ServiceException;
import org.apache.airavata.core.gfac.extension.DataServiceChain;
import org.apache.airavata.core.gfac.extension.PostExecuteChain;
import org.apache.airavata.core.gfac.extension.PreExecuteChain;
import org.apache.airavata.core.gfac.scheduler.Scheduler;
import org.apache.airavata.core.gfac.scheduler.impl.POJOSchedulerImpl;

public class POJOServiceImpl extends AbstractSimpleService {
    
    private ExecutionDescription context; 

    public POJOServiceImpl(HostDescription host, ApplicationDeploymentDescription app, ServiceDescription service){
        this.context = new DefaultExecutionDescription();
        context.setHost(host);
        context.setApp(app);
        context.setService(service);
    }
    
    public void init() throws GfacException {
    }

    public void dispose() throws GfacException {
    }

    public void preProcess(InvocationContext context) throws ServiceException {
        context.setExecutionDescription(this.context);
    }

    public void postProcess(InvocationContext context) throws ServiceException {
    }

    public Scheduler getScheduler(InvocationContext context) throws ServiceException {
        return new POJOSchedulerImpl();
    }

    public PreExecuteChain[] getPreExecutionSteps(InvocationContext context) throws ServiceException {
        return null;
    }

    public PostExecuteChain[] getPostExecuteSteps(InvocationContext context) throws ServiceException {
        return null;
    }

    public DataServiceChain[] getDataChains(InvocationContext context) throws ServiceException {
        return null;
    }   
}
