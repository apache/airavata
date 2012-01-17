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

import org.apache.airavata.core.gfac.context.invocation.ExecutionContext;
import org.apache.airavata.core.gfac.notification.GFacNotifiable;
import org.apache.airavata.core.gfac.notification.GFacNotifier;
import org.apache.airavata.core.gfac.notification.impl.DefaultNotifier;
import org.apache.airavata.registry.api.AiravataRegistry;
import org.apache.axiom.om.OMElement;

/**
 * DefaultExecutionContext is a simple implementation of ExecutionContext. It uses DefaultNotifier as its base notifier.
 * 
 */
public class DefaultExecutionContext implements ExecutionContext {

    private GFacNotifier notificationService = new DefaultNotifier();
    private AiravataRegistry registryService;
    private OMElement header;

    public GFacNotifier getNotifier() {
        return this.notificationService;
    }

    public void addNotifiable(GFacNotifiable service) {
        this.notificationService.addNotifiable(service);
    }

    public AiravataRegistry getRegistryService() {
        return this.registryService;
    }

    public void setRegistryService(AiravataRegistry registryService) {
        this.registryService = registryService;
    }

    public OMElement getSecurityContextHeader() {
        return header;
    }

    public void setSecurityContextHeader(OMElement header) {
        this.header = header;
    }
}
