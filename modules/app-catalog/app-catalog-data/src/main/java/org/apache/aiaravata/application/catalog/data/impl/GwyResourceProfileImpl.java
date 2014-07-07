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

package org.apache.aiaravata.application.catalog.data.impl;

import java.util.List;

import org.airavata.appcatalog.cpi.AppCatalogException;
import org.airavata.appcatalog.cpi.GwyResourceProfile;
import org.apache.aiaravata.application.catalog.data.resources.AbstractResource;
import org.apache.aiaravata.application.catalog.data.resources.ComputeHostPreferenceResource;
import org.apache.aiaravata.application.catalog.data.resources.ComputeResourceResource;
import org.apache.aiaravata.application.catalog.data.resources.GatewayProfileResource;
import org.apache.aiaravata.application.catalog.data.resources.Resource;
import org.apache.aiaravata.application.catalog.data.util.AppCatalogThriftConversion;
import org.apache.airavata.model.appcatalog.gatewayprofile.ComputeResourcePreference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GwyResourceProfileImpl implements GwyResourceProfile {
    private final static Logger logger = LoggerFactory.getLogger(GwyResourceProfileImpl.class);

    @Override
    public String addGatewayResourceProfile(org.apache.airavata.model.appcatalog.gatewayprofile.GatewayResourceProfile gatewayProfile) throws AppCatalogException {
        try {
            GatewayProfileResource profileResource = new GatewayProfileResource();
            profileResource.setGatewayName(gatewayProfile.getGatewayName());
            profileResource.setGatewayID(gatewayProfile.getGatewayName());
            profileResource.setGatewayDesc(gatewayProfile.getGatewayDescription());
            profileResource.save();
            gatewayProfile.setGatewayID(profileResource.getGatewayID());
            List<ComputeResourcePreference> computeResourcePreferences = gatewayProfile.getComputeResourcePreferences();
            if (computeResourcePreferences != null && !computeResourcePreferences.isEmpty()){
                for (ComputeResourcePreference preference : computeResourcePreferences ){
                    ComputeHostPreferenceResource resource = new ComputeHostPreferenceResource();
                    resource.setGatewayProfile(profileResource);
                    resource.setResourceId(preference.getComputeResourceId());
                    ComputeResourceResource computeHostResource = new ComputeResourceResource();
                    resource.setComputeHostResource((ComputeResourceResource)computeHostResource.get(preference.getComputeResourceId()));
                    resource.setGatewayId(profileResource.getGatewayID());
                    resource.setOverrideByAiravata(preference.isOverridebyAiravata());
                    resource.setPreferredJobProtocol(preference.getPreferredJobSubmissionProtocol());
                    resource.setPreferedDMProtocol(preference.getPreferredDataMovementProtocol());
                    resource.setBatchQueue(preference.getPreferredBatchQueue());
                    resource.setProjectNumber(preference.getAllocationProjectNumber());
                    resource.setScratchLocation(preference.getScratchLocation());
                    resource.save();
                }
            }
            return profileResource.getGatewayID();
        }catch (Exception e) {
            logger.error("Error while saving gateway profile...", e);
            throw new AppCatalogException(e);
        }
    }

    @Override
    public void updateGatewayResourceProfile(String gatewayId, org.apache.airavata.model.appcatalog.gatewayprofile.GatewayResourceProfile updatedProfile) throws AppCatalogException {
        try {
            GatewayProfileResource profileResource = new GatewayProfileResource();
            GatewayProfileResource existingGP = (GatewayProfileResource)profileResource.get(gatewayId);
            existingGP.setGatewayName(updatedProfile.getGatewayName());
            existingGP.setGatewayDesc(updatedProfile.getGatewayDescription());
            existingGP.save();

            List<ComputeResourcePreference> computeResourcePreferences = updatedProfile.getComputeResourcePreferences();
            if (computeResourcePreferences != null && !computeResourcePreferences.isEmpty()){
                for (ComputeResourcePreference preference : computeResourcePreferences ){
                    ComputeHostPreferenceResource resource = new ComputeHostPreferenceResource();
                    resource.setGatewayProfile(existingGP);
                    resource.setResourceId(preference.getComputeResourceId());
                    ComputeResourceResource computeHostResource = new ComputeResourceResource();
                    resource.setComputeHostResource((ComputeResourceResource)computeHostResource.get(preference.getComputeResourceId()));
                    resource.setGatewayId(gatewayId);
                    resource.setOverrideByAiravata(preference.isOverridebyAiravata());
                    resource.setPreferredJobProtocol(preference.getPreferredJobSubmissionProtocol());
                    resource.setPreferedDMProtocol(preference.getPreferredDataMovementProtocol());
                    resource.setBatchQueue(preference.getPreferredBatchQueue());
                    resource.setProjectNumber(preference.getAllocationProjectNumber());
                    resource.setScratchLocation(preference.getScratchLocation());
                    resource.save();
                }
            }
        }catch (Exception e) {
            logger.error("Error while updating gateway profile...", e);
            throw new AppCatalogException(e);
        }
    }

    @Override
    public org.apache.airavata.model.appcatalog.gatewayprofile.GatewayResourceProfile getGatewayProfile(String gatewayId) throws AppCatalogException {
        try {
            GatewayProfileResource resource = new GatewayProfileResource();
            GatewayProfileResource gwresource = (GatewayProfileResource)resource.get(gatewayId);
            ComputeHostPreferenceResource prefResource = new ComputeHostPreferenceResource();
            List<Resource> computePrefList = prefResource.get(AbstractResource.ComputeResourcePreferenceConstants.GATEWAY_ID, gatewayId);
            List<ComputeResourcePreference> computeResourcePreferences = AppCatalogThriftConversion.getComputeResourcePreferences(computePrefList);
            return AppCatalogThriftConversion.getGatewayResourceProfile(gwresource, computeResourcePreferences);
        }catch (Exception e) {
            logger.error("Error while retrieving gateway profile...", e);
            throw new AppCatalogException(e);
        }
    }

    @Override
    public boolean removeGatewayResourceProfile(String gatewayId) throws AppCatalogException {
       try {
           GatewayProfileResource resource = new GatewayProfileResource();
           resource.remove(gatewayId);
           return true;
       }catch (Exception e) {
           logger.error("Error while deleting gateway profile...", e);
           throw new AppCatalogException(e);
       }
    }

    @Override
    public boolean isGatewayResourceProfileExists(String gatewayId) throws AppCatalogException {
        try {
            GatewayProfileResource resource = new GatewayProfileResource();
            return resource.isExists(gatewayId);
        }catch (Exception e) {
            logger.error("Error while retrieving gateway profile...", e);
            throw new AppCatalogException(e);
        }
    }

    /**
     * @param gatewayId
     * @param hostId
     * @return ComputeResourcePreference
     */
    @Override
    public ComputeResourcePreference getComputeResourcePreference(String gatewayId, String hostId) throws AppCatalogException {
        try {
            ComputeHostPreferenceResource prefResource = new ComputeHostPreferenceResource();
            List<Resource> computePrefList = prefResource.get(AbstractResource.ComputeResourcePreferenceConstants.GATEWAY_ID, gatewayId);
            for (Resource resource : computePrefList){
                ComputeHostPreferenceResource cmP = (ComputeHostPreferenceResource) resource;
                if (cmP.getResourceId() != null && !cmP.getResourceId().equals("")){
                    if (cmP.getResourceId().equals(hostId)){
                        return AppCatalogThriftConversion.getComputeResourcePreference(cmP);
                    }
                }
            }
        }catch (Exception e) {
            logger.error("Error while retrieving compute resource preference...", e);
            throw new AppCatalogException(e);
        }
        return null;
    }

    /**
     * @param gatewayId
     * @return
     */
    @Override
    public List<ComputeResourcePreference> getAllComputeResourcePreferences(String gatewayId) throws AppCatalogException {
        try {
            ComputeHostPreferenceResource prefResource = new ComputeHostPreferenceResource();
            List<Resource> computePrefList = prefResource.get(AbstractResource.ComputeResourcePreferenceConstants.GATEWAY_ID, gatewayId);
            return AppCatalogThriftConversion.getComputeResourcePreferences(computePrefList);
        }catch (Exception e) {
            logger.error("Error while retrieving compute resource preference...", e);
            throw new AppCatalogException(e);
        }
    }
}
