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

import java.util.Iterator;
import java.util.List;

import org.apache.airavata.core.gfac.api.Registry;
import org.apache.airavata.core.gfac.context.GFACContext;
import org.apache.airavata.core.gfac.context.InvocationContext;
import org.apache.airavata.core.gfac.exception.GfacException;
import org.apache.airavata.core.gfac.exception.GfacException.FaultCode;
import org.apache.airavata.core.gfac.provider.GramProvider;
import org.apache.airavata.core.gfac.provider.LocalProvider;
import org.apache.airavata.core.gfac.provider.Provider;
import org.apache.airavata.core.gfac.scheduler.Scheduler;
import org.apache.airavata.core.gfac.type.ApplicationDeploymentDescription;
import org.apache.airavata.core.gfac.type.HostDescription;
import org.apache.airavata.core.gfac.type.ServiceDescription;
import org.apache.airavata.core.gfac.utils.GfacUtils;

public class SchedulerImpl implements Scheduler {

	public Provider schedule(InvocationContext context) throws GfacException {

		Registry registryService = context.getExecutionContext()
				.getRegistryService();

		/*
		 * Load Service
		 */
		ServiceDescription serviceDesc = registryService
				.getServiceDescription(context.getServiceName());

		if (serviceDesc == null)
			throw new GfacException("Service Desciption for "
					+ context.getServiceName()
					+ " does not found on resource Catalog " + registryService,
					FaultCode.InvalidRequest);

		/*
		 * Load host
		 */
		HostDescription host = scheduleToHost(registryService,
				context.getServiceName());

		if (host == null)
			throw new GfacException("Host Desciption for "
					+ context.getServiceName()
					+ " does not found on resource Catalog " + registryService,
					FaultCode.InvalidRequest);

		/*
		 * Load app
		 */
		ApplicationDeploymentDescription app = registryService
				.getDeploymentDescription(context.getServiceName(),
						host.getName());

		if (app == null)
			throw new GfacException("App Desciption for "
					+ context.getServiceName()
					+ " does not found on resource Catalog " + registryService,
					FaultCode.InvalidRequest);

		/*
		 * Binding
		 */

		if (context.getGfacContext() == null) {
			context.setGfacContext(new GFACContext());
		}
		context.getGfacContext().setHost(host);
		context.getGfacContext().setService(serviceDesc);
		context.getGfacContext().setApp(app);

		/*
		 * Determine provider
		 */
		String hostName = host.getName();
		if (GfacUtils.isLocalHost(hostName)) {
			return new LocalProvider();
		} else {
			return new GramProvider();
		}

	}

	private HostDescription scheduleToHost(Registry regService,
			String serviceName) {

		System.out
				.println("Searching registry for some deployed application hosts\n");
		List<HostDescription> hosts = regService
				.getServiceLocation(serviceName);
		if (hosts != null && hosts.size() > 0) {
			HostDescription result = null;
			for (Iterator iterator = hosts.iterator(); iterator.hasNext();) {
				result = (HostDescription) iterator.next();

				System.out.println("Found service on: " + result.getName());
			}
			return result;
		} else {
			System.out.println("Applcation  " + serviceName
					+ " not found in registry");
			return null;
		}
	}
}
