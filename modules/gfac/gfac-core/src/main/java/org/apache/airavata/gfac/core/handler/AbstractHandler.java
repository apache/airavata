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
package org.apache.airavata.gfac.core.handler;

import org.apache.airavata.common.utils.MonitorPublisher;
import org.apache.airavata.gfac.core.context.JobExecutionContext;
import org.apache.airavata.gfac.core.cpi.BetterGfacImpl;
import org.apache.airavata.persistance.registry.jpa.impl.RegistryFactory;
import org.apache.airavata.registry.cpi.Registry;
import org.apache.airavata.registry.cpi.RegistryException;

public abstract class AbstractHandler implements GFacHandler {
	protected Registry registry = null;

    protected MonitorPublisher publisher = null;

    protected AbstractHandler() {
        publisher = BetterGfacImpl.getMonitorPublisher();   // This will not be null because this will be initialize in GFacIml
    }

    public void invoke(JobExecutionContext jobExecutionContext) throws GFacHandlerException {
		registry = jobExecutionContext.getRegistry();
        if(registry == null){
            try {
                registry = RegistryFactory.getDefaultRegistry();
            } catch (RegistryException e) {
                throw new GFacHandlerException("unable to create registry instance", e);
            }
        }
	}
    public MonitorPublisher getPublisher() {
        return publisher;
    }

    public void setPublisher(MonitorPublisher publisher) {
        this.publisher = publisher;
    }

    public Registry getRegistry() {
        return registry;
    }

    public void setRegistry(Registry registry) {
        this.registry = registry;
    }
}
