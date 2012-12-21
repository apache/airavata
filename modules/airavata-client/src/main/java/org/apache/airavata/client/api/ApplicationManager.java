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

package org.apache.airavata.client.api;

import java.util.List;
import java.util.Map;

import org.apache.airavata.commons.gfac.type.ApplicationDeploymentDescription;
import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.commons.gfac.type.ServiceDescription;

public interface ApplicationManager {
	//Service descriptors

    /**
     * Retrieve registered service description of the given service name 
     * @param serviceName
     * @return
     * @throws AiravataAPIInvocationException
     */
	public ServiceDescription getServiceDescription(String serviceName) throws AiravataAPIInvocationException;

    /**
     * Retrieve all registered service descriptions
     * @return
     * @throws AiravataAPIInvocationException
     */
    public List<ServiceDescription> getAllServiceDescriptions() throws AiravataAPIInvocationException;

    /**
     * Save service description on registry
     * @param service Service description information to update.
     * @return The service descriptor name.
     * @deprecated Deprecated since 0.6 release. User {@see #addServiceDescription} and {@see #updateServiceDescription}
     *             methods instead.
     * @throws AiravataAPIInvocationException If an error occurred while updating service descriptor.
     */
    @Deprecated
    public String saveServiceDescription(ServiceDescription service)throws AiravataAPIInvocationException;

    /**
     * Adds a new Service descriptor to the system. If service descriptor already exists in the system
     * this will throw {@see DescriptorRecordAlreadyExistsException}. If you want to update an existing
     * service descriptor use {@see #updateServiceDescription}.
     * @param serviceDescription The service descriptor.
     * @throws AiravataAPIInvocationException If an error occurred while adding service description.
     * @throws DescriptorRecordAlreadyExistsException If service descriptor already exists in the system.
     */
    public void addServiceDescription(ServiceDescription serviceDescription)throws AiravataAPIInvocationException,
            DescriptorRecordAlreadyExistsException;

    /**
     * Updates the service descriptor.
     * @param serviceDescription Service description information to update.
     * @throws AiravataAPIInvocationException If an error occurred while updating service description.
     */
    public void updateServiceDescription(ServiceDescription serviceDescription)throws AiravataAPIInvocationException;



    /**
     * Delete service description from the registry
     * @param serviceName
     * @throws AiravataAPIInvocationException
     */
    public void deleteServiceDescription(String serviceName) throws AiravataAPIInvocationException;

    /**
     * Retrieve a list of registered service descriptions of the given regex service name
     * @param nameRegEx
     * @return
     * @throws AiravataAPIInvocationException
     */
    public List<ServiceDescription> searchServiceDescription(String nameRegEx) throws AiravataAPIInvocationException;

    //Application descriptors

    /**
     * Retrieve registered application description of the given service name & hostName
     * @param serviceName
     * @param hostName
     * @return
     * @throws AiravataAPIInvocationException
     */
    public ApplicationDeploymentDescription getDeploymentDescription(String serviceName, String hostName)throws AiravataAPIInvocationException;

    /**
     * Save deployment description on registry for a given service for a host
     * @param serviceId The service descriptor id.
     * @param hostId The host descriptor id.
     * @param app The application deployment descriptor.
     * @return The application deployment descriptor name.
     * @deprecated Deprecated since 0.6 release. Please use {@see #addDeploymentDescription} and
     *              {@see #updateDeploymentDescription}.
     * @throws AiravataAPIInvocationException If an error occurred while adding application deployment descriptor.
     */
    @Deprecated
    public String saveDeploymentDescription(String serviceId, String hostId, ApplicationDeploymentDescription app)throws AiravataAPIInvocationException;


    /**
     * Adds a new deployment description associating with given service description and given host description. If
     * an association already exists this will throw {@see DescriptorRecordAlreadyExistsException} exception. If you
     * want to update an existing deployment descriptor use {@see #updateDeploymentDescription}.
     * @param serviceDescription The service description to associate. Should be saved before passing to this method.
     * @param hostDescription The host description to associate, should have been saved before calling this method.
     * @param applicationDeploymentDescription The application descriptor to save.
     * @throws AiravataAPIInvocationException If an error occurred while saving application descriptor.
     * @throws DescriptorRecordAlreadyExistsException If deployment descriptor already exists in the system.
     */
    public void addDeploymentDescription(ServiceDescription serviceDescription, HostDescription hostDescription,
                                         ApplicationDeploymentDescription applicationDeploymentDescription)
        throws AiravataAPIInvocationException, DescriptorRecordAlreadyExistsException;

    /**
     * Adds a new deployment description associating with given service description and given host description. If
     * an association already exists this will throw {@see DescriptorRecordAlreadyExistsException} exception.
     * @param serviceDescription The service description to associate. Should be saved before passing to this method.
     * @param hostDescription The host description to associate, should have been saved before calling this method.
     * @param applicationDeploymentDescription The application descriptor to save.
     * @throws AiravataAPIInvocationException If an error occurred while saving application descriptor.
     */
    public void updateDeploymentDescription(ServiceDescription serviceDescription, HostDescription hostDescription,
                                         ApplicationDeploymentDescription applicationDeploymentDescription)
            throws AiravataAPIInvocationException;

    /**
     * Retrieve list of registered deployment descriptions of the given regex service name & regex host name
     * @param serviceName
     * @param hostName
     * @return
     * @throws AiravataAPIInvocationException
     */
    public List<ApplicationDeploymentDescription> searchDeploymentDescription(String serviceName, String hostName)throws AiravataAPIInvocationException;

    /**
     * Retrieve all registered deployment descriptions, The key represents the service name & host name in string array
     * @return
     * @throws AiravataAPIInvocationException
     */
    public Map<String[], ApplicationDeploymentDescription> getAllDeploymentDescriptions() throws AiravataAPIInvocationException;

    /**
     * Retrieve list of registered deployment descriptions of the given regex service name, regex host name & regex application name 
     * @param serviceName
     * @param hostName
     * @param applicationName
     * @return
     * @throws AiravataAPIInvocationException
     */
    public List<ApplicationDeploymentDescription> searchDeploymentDescription(String serviceName, String hostName,String applicationName) throws AiravataAPIInvocationException;

    /**
     * Retrieve registered map of deployment descriptions for their host description of the given service name
     * @param serviceName
     * @return
     * @throws AiravataAPIInvocationException
     */
    public Map<HostDescription, List<ApplicationDeploymentDescription>> searchDeploymentDescription(String serviceName)throws AiravataAPIInvocationException;

    /**
     * Delete deployment description from the registry which is exposed as the service name in the host name 
     * @param serviceName
     * @param hostName
     * @param applicationName
     * @throws AiravataAPIInvocationException
     */
    public void deleteDeploymentDescription(String serviceName, String hostName, String applicationName)throws AiravataAPIInvocationException;
    
    //Host descriptors

    /**
     * Retrieve registered host description of the given host name
     * @param hostName
     * @return
     * @throws AiravataAPIInvocationException
     */
    public HostDescription getHostDescription(String hostName) throws AiravataAPIInvocationException;

    /**
     * Retrieve all registered host descriptions
     * @return
     * @throws AiravataAPIInvocationException
     */
    public List<HostDescription> getAllHostDescriptions() throws AiravataAPIInvocationException;

    /**
     * Save host description on registry
     * @param host The host descriptor object to update in the database.
     * @deprecated Deprecated since 0.6 release. Please use {@see #addHostDescription} and {@see #updateHostDescription}
     * @throws AiravataAPIInvocationException If an error occurred while saving the host description.
     */
    @Deprecated
    public String saveHostDescription(HostDescription host)throws AiravataAPIInvocationException;


    /**
     * Adds a new host descriptor object. If adding host descriptor already exists in the system this will throw
     * DescriptorRecordAlreadyExistsException. If user wants to update an existing host descriptor use
     * {@see #updateHostDescription(HostDescription host)} method.
     * @param host The host descriptor object to save in the database.
     * @throws AiravataAPIInvocationException If an error occurred while saving the host description.
     * @throws DescriptorRecordAlreadyExistsException If host descriptor object already exists in the system.
     */
    public void addHostDescription (HostDescription host) throws AiravataAPIInvocationException,
            DescriptorRecordAlreadyExistsException;


    /**
     * Updates an existing host descriptor. If you are not sure whether descriptor already exists try using
     * {@see #addHostDescription} and catch {@see DescriptorRecordAlreadyExistsException}. If caught use this method
     * to update the record.
     * @param host The host descriptor object to update in the database.
     * @throws AiravataAPIInvocationException If an error occurred while saving the host description.
     */
    public void updateHostDescription(HostDescription host)throws AiravataAPIInvocationException;

    /**
     * Retrieve a list of registered hsot descriptions of the given regex host name
     * @param regExName
     * @return
     * @throws AiravataAPIInvocationException
     */
    public List<HostDescription> searchHostDescription(String regExName) throws AiravataAPIInvocationException;

    /**
     * Delete host description from the registry
     * @param hostId
     * @throws AiravataAPIInvocationException
     */
    public void deleteHostDescription(String hostId) throws AiravataAPIInvocationException;

    /**
     * Map services to possible hosts 
     * @param serviceName
     * @param hostName
     * @return
     * @throws AiravataAPIInvocationException
     */
    public boolean deployServiceOnHost(String serviceName, String hostName)throws AiravataAPIInvocationException;

    public Map<String,ApplicationDeploymentDescription> getApplicationDescriptors (String serviceName) throws AiravataAPIInvocationException;

    public boolean isHostDescriptorExists(String descriptorName) throws AiravataAPIInvocationException;

    public void removeHostDescriptor(String hostName) throws AiravataAPIInvocationException;

    public boolean isServiceDescriptorExists(String descriptorName) throws AiravataAPIInvocationException;
    
    public boolean isDeploymentDescriptorExists(String serviceName, String hostName, String descriptorName)throws AiravataAPIInvocationException;

    public void removeServiceDescriptor(String serviceName) throws AiravataAPIInvocationException;

    public void removeApplicationDescriptor(String serviceName, String hostName, String applicationName) throws AiravataAPIInvocationException;

    public void updateHostDescriptor(HostDescription descriptor) throws AiravataAPIInvocationException;

    public void updateServiceDescriptor(ServiceDescription descriptor) throws AiravataAPIInvocationException;

    public void updateApplicationDescriptor(String serviceName, String hostName, ApplicationDeploymentDescription descriptor) throws AiravataAPIInvocationException;

    public ApplicationDeploymentDescription getApplicationDescriptor(String serviceName, String hostname, String applicationName) throws AiravataAPIInvocationException;
}
