/**
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
 */
package org.apache.airavata.gfac.core.context;

import java.util.HashMap;
import java.util.Map;

/**
 * This is a singleton class, which store all required details of running processes.
 */
public class GFacContext {

	private Map<String,ProcessContext> processes;
	private static GFacContext gfacContext;

	private GFacContext() {
		processes = new HashMap<>();
	}

	public static GFacContext getInstance() {
		if (gfacContext == null) {
			synchronized (GFacContext.class) {
				if (gfacContext == null) {
					gfacContext = new GFacContext();
				}
			}
		}
		return gfacContext;
	}

	public void addProcess(ProcessContext processContext) {
		processes.put(processContext.getProcessId(), processContext);
	}

	public ProcessContext getProcess(String processId) {
		return processes.get(processId);
	}

	public void removeProcess(String processId) {
		processes.remove(processId);
	}
}
