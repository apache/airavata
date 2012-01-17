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

import org.apache.airavata.core.gfac.notification.GFacNotifiable;
import org.apache.airavata.core.gfac.notification.GFacNotifier;
import org.apache.airavata.registry.api.AiravataRegistry;
import org.apache.axiom.om.OMElement;
import xsul.lead.LeadContextHeader;

/**
 * The Execution Context is used for passing information around the whole service. It keeps information about general
 * execution step. For example, notification service, registry service.
 * 
 */
public interface ExecutionContext {

    /**
     * Get Notifier object to used for notification.
     * 
     * @return NotificationService to be used.
     */
    GFacNotifier getNotifier();

    /**
     * add Notifiable object.
     * 
     * @param Notifiable
     *            object to used
     */
    void addNotifiable(GFacNotifiable value);

    /**
     * Get Registry object. It is used to retrieve important information about application execution.
     * 
     * @return Registry object
     */
    AiravataRegistry getRegistryService();

    /**
     * Set Registry object.
     * 
     * @param AiravataRegistry
     *            object to used.
     */
    void setRegistryService(AiravataRegistry value);

    public OMElement getSecurityContextHeader();

    public void setSecurityContextHeader(OMElement header);
}
