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

package org.apache.airavata.core.gfac.context.invocation;

import org.apache.airavata.core.gfac.context.message.MessageContext;
import org.apache.airavata.core.gfac.context.security.SecurityContext;

/**
 * InvocationContext is the main context contains other contexts. It is used (per invocation) as
 * a parameter to pass to all modules in the Gfac service.
 * 
 */
public interface InvocationContext {
    
    /**
     * Get ServiceName for the invocation
     * 
     * @return
     */
    public String getServiceName();

    /**
     * Get ExecutionDescription
     * 
     * @return ExecutionDescription
     */
    public ExecutionDescription getExecutionDescription();

    /**
     * Set ExecutionDescription
     * 
     * @param value
     */
    public <T extends ExecutionDescription> void setExecutionDescription(T value);

    /**
     * Get ExecutionContext
     * 
     * @return ExecutionContext
     */
    public ExecutionContext getExecutionContext();

    /**
     * Set ExecutionContext
     * 
     * @param value
     */
    public <T extends ExecutionContext> void setExecutionContext(T value);

    /**
     * Get MessageContext
     * 
     * @param name
     * @return MessageContext
     */
    public <T> MessageContext<T> getMessageContext(String name);

    /**
     * Add MessageContext to the invocation with specific name.
     * 
     * @param name
     * @param value
     */
    public <T extends MessageContext<?>> void addMessageContext(String name, T value);

    /**
     * Get SecurityContext
     * 
     * @param name
     * @return
     */
    public SecurityContext getSecurityContext(String name);
    
    /**
     * Add SecurityContext to the invocation with specific name.
     * 
     * @param name
     * @param value
     */
    public <T extends SecurityContext> void addSecurityContext(String name, T value);

}
