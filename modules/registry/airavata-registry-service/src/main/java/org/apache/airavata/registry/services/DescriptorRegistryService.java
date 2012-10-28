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

package org.apache.airavata.registry.services;

import org.apache.airavata.registry.api.exception.RegistryException;
import org.apache.airavata.registry.api.exception.gateway.DescriptorAlreadyExistsException;
import org.apache.airavata.registry.api.exception.gateway.DescriptorDoesNotExistsException;
import org.apache.airavata.registry.api.exception.gateway.MalformedDescriptorException;

import javax.ws.rs.core.Response;

public interface DescriptorRegistryService {
    /*
     * Note Name changes of the descriptors should not be allowed
     */

    // ---------Host Descriptor data------------
    public Response isHostDescriptorExists(String descriptorName) throws RegistryException;

    public Response addHostDescriptor(String descriptor) throws DescriptorAlreadyExistsException, RegistryException;

    public Response updateHostDescriptor(String descriptor) throws DescriptorDoesNotExistsException, RegistryException;

    public Response getHostDescriptor(String hostName) throws DescriptorDoesNotExistsException,
            MalformedDescriptorException, RegistryException;

    public Response removeHostDescriptor(String hostName) throws DescriptorDoesNotExistsException, RegistryException;

    public Response getHostDescriptors() throws MalformedDescriptorException, RegistryException;

    public Response getHostDescriptorMetadata(String hostName) throws DescriptorDoesNotExistsException,
            RegistryException;

    // ---------Service Descriptor data------------
    public Response isServiceDescriptorExists(String descriptorName) throws RegistryException;

    public Response addServiceDescriptor(String descriptor) throws DescriptorAlreadyExistsException, RegistryException;

    public Response updateServiceDescriptor(String descriptor) throws DescriptorDoesNotExistsException,
            RegistryException;

    public Response getServiceDescriptor(String serviceName) throws DescriptorDoesNotExistsException,
            MalformedDescriptorException, RegistryException;

    public Response removeServiceDescriptor(String serviceName) throws DescriptorDoesNotExistsException,
            RegistryException;

    public Response getServiceDescriptors() throws MalformedDescriptorException, RegistryException;

    public Response getServiceDescriptorMetadata(String serviceName) throws DescriptorDoesNotExistsException,
            RegistryException;

    // ---------Application Descriptor data------------
    public Response isApplicationDescriptorExists(String serviceName, String hostName, String descriptorName)
            throws RegistryException;

    public Response addApplicationDescriptor(String serviceDescription, String hostDescriptor, String descriptor)
            throws DescriptorAlreadyExistsException, RegistryException;

    public Response addApplicationDesc(String serviceName, String hostName, String descriptor)
            throws DescriptorAlreadyExistsException, RegistryException;

    public Response udpateApplicationDescriptorByDescriptors(String serviceDescription, String hostDescriptor,
            String descriptor) throws DescriptorDoesNotExistsException, RegistryException;

    public Response updateApplicationDescriptor(String serviceName, String hostName, String descriptor)
            throws DescriptorDoesNotExistsException, RegistryException;

    public Response getApplicationDescriptor(String serviceName, String hostname, String applicationName)
            throws DescriptorDoesNotExistsException, MalformedDescriptorException, RegistryException;

    public Response getApplicationDescriptors(String serviceName, String hostname) throws MalformedDescriptorException,
            RegistryException;

    public Response getApplicationDescriptors(String serviceName) throws MalformedDescriptorException,
            RegistryException;

    public Response getApplicationDescriptors() throws MalformedDescriptorException, RegistryException;

    public Response removeApplicationDescriptor(String serviceName, String hostName, String applicationName)
            throws DescriptorDoesNotExistsException, RegistryException;

    public Response getApplicationDescriptorMetadata(String serviceName, String hostName, String applicationName)
            throws DescriptorDoesNotExistsException, RegistryException;

}
