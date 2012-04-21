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

package org.apache.airavata.core.gfac.context.invocation.impl;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.airavata.core.gfac.context.invocation.ExecutionContext;
import org.apache.airavata.core.gfac.context.invocation.ExecutionDescription;
import org.apache.airavata.core.gfac.context.invocation.InvocationContext;
import org.apache.airavata.core.gfac.context.message.MessageContext;
import org.apache.airavata.core.gfac.context.security.SecurityContext;

/**
 * Main context that is used throughout the service
 */
public class DefaultInvocationContext implements InvocationContext {

    protected final String MESSAGE_CONTEXT_INPUT = "input";
    protected final String MESSAGE_CONTEXT_OUTPUT = "output";

    private String serviceName;
    private ExecutionContext executionContext;
    private ExecutionDescription gfacContext;
    private Map<String, MessageContext<?>> messageContextMap = new LinkedHashMap<String, MessageContext<?>>();
    private Map<String, SecurityContext> securityContextMap = new LinkedHashMap<String, SecurityContext>();

    public void setServiceName(String name) {
        this.serviceName = name;
    }

    public String getServiceName() {
        return this.serviceName;
    }

    public ExecutionDescription getExecutionDescription() {
        return this.gfacContext;
    }

    public void setExecutionDescription(ExecutionDescription value) {
        this.gfacContext = value;
    }

    public ExecutionContext getExecutionContext() {
        return this.executionContext;
    }

    public void setExecutionContext(ExecutionContext value) {
        this.executionContext = value;
    }

    public <T> MessageContext<T> getMessageContext(String name) {
        return (MessageContext<T>) this.messageContextMap.get(name);
    }

    public SecurityContext getSecurityContext(String name) {
        return this.securityContextMap.get(name);
    }

    public void addMessageContext(String name, MessageContext<?> value) {
        this.messageContextMap.put(name, value);
    }

    public void addSecurityContext(String name, SecurityContext value) {
        this.securityContextMap.put(name, value);
    }

    public <T> MessageContext<T> getInput() {
        return getMessageContext(MESSAGE_CONTEXT_INPUT);
    }

    public void setInput(MessageContext<?> value) {
        this.messageContextMap.put(MESSAGE_CONTEXT_INPUT, value);
    }

    public <T> MessageContext<T> getOutput() {
        return getMessageContext(MESSAGE_CONTEXT_OUTPUT);
    }

    public void setOutput(MessageContext<?> value) {
        this.messageContextMap.put(MESSAGE_CONTEXT_OUTPUT, value);

    };
}
