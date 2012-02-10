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

import org.apache.airavata.commons.gfac.type.ApplicationDeploymentDescription;
import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.commons.gfac.type.ServiceDescription;

/**
 * ExecutionDescription represents an application description which is used to determine how to invoke an application.
 * For example, host where an application is deployed, service name, service parameters, etc.
 * 
 */
public interface ExecutionDescription {

    /**
     * Get HostDescription
     * 
     * @return HostDescription
     */
    HostDescription getHost();

    /**
     * Set HostDescription
     * 
     * @param host
     */
    <T extends HostDescription> void setHost(T host);

    /**
     * Get ApplicationDeploymentDescription
     * 
     * @return ApplicationDeploymentDescription
     */
    ApplicationDeploymentDescription getApp();

    /**
     * Set ApplicationDeploymentDescription
     * 
     * @param app
     */
    <T extends ApplicationDeploymentDescription> void setApp(T app);

    /**
     * Get ServiceDescription
     * 
     * @return service
     */
    ServiceDescription getService();

    /**
     * Set ServiceDescription
     * 
     * @param service
     */
    <T extends ServiceDescription> void setService(T service);
}