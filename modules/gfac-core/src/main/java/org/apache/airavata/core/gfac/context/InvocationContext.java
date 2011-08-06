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

package org.apache.airavata.core.gfac.context;

import java.util.HashMap;
import java.util.Map;

/**
 * Main context that is used throughout the service
 * 
 * 
 */
public class InvocationContext {

    private String serviceName;
    private ExecutionContext executionContext;
    private GFACContext gfacContext;
   	private Map<String, MessageContext> messageContextMap = new HashMap<String, MessageContext>();
    private Map<String, SecurityContext> securityContextMap = new HashMap<String, SecurityContext>();;

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public GFACContext getGfacContext() {
		return gfacContext;
	}

	public void setGfacContext(GFACContext gfacContext) {
		this.gfacContext = gfacContext;
	}
    
    public ExecutionContext getExecutionContext() {
        return executionContext;
    }

    public void setExecutionContext(ExecutionContext executionContext) {
        this.executionContext = executionContext;
    }

    public MessageContext getMessageContext(String name) {
        return this.messageContextMap.get(name);
    }

    public void addMessageContext(String name, MessageContext messageContext) {
        this.messageContextMap.put(name, messageContext);
    }

    public SecurityContext getSecurityContext(String name) {
        return this.securityContextMap.get(name);
    }

    public void addSecurityContext(String name, SecurityContext securityContext) {
        this.securityContextMap.put(name, securityContext);
    }
}
