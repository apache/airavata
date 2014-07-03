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

import org.airavata.appcatalog.cpi.AppCatalogException;
import org.airavata.appcatalog.cpi.GProfile;
import org.apache.aiaravata.application.catalog.data.resources.*;
import org.apache.aiaravata.application.catalog.data.util.AppCatalogThriftConversion;
import org.apache.aiaravata.application.catalog.data.util.AppCatalogUtils;
import org.apache.airavata.model.appcatalog.computeresource.DataMovementProtocol;
import org.apache.airavata.model.appcatalog.computeresource.JobSubmissionProtocol;
import org.apache.airavata.model.appcatalog.gatewayprofile.ComputeResourcePreference;
import org.apache.airavata.model.appcatalog.gatewayprofile.GatewayProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GatewayProfileImpl implements GProfile {
    private final static Logger logger = LoggerFactory.getLogger(GatewayProfileImpl.class);

    @Override
    public String addGatewayProfile(GatewayProfile gatewayProfile) throws AppCatalogException {
        try {
            GatewayProfileResource profileResource = new GatewayProfileResource();
            profileResource.setGatewayName(gatewayProfile.getGatewayName());
            profileResource.setGatewayID(AppCatalogUtils.getID(gatewayProfile.getGatewayName()));
            profileResource.setGatewayDesc(gatewayProfile.getGatewayDescription());
            profileResource.save();
            gatewayProfile.setGatewayID(profileResource.getGatewayID());
            List<ComputeResourcePreference> computeResourcePreferences = gatewayProfile.getComputeResourcePreferences();
            if (computeResourcePreferences != null && !computeResourcePreferences.isEmpty()){
                for (ComputeResourcePreference preference : computeResourcePreferences ){
                    ComputeHostPreferenceResource resource = new ComputeHostPreferenceResource();
                    resource.setGatewayProfile(profileResource);
                    resource.setResourceId(preference.getComputeResourceId());
                    ComputeHostResource computeHostResource = new ComputeHostResource();
                    resource.setComputeHostResource((ComputeHostResource)computeHostResource.get(preference.getComputeResourceId()));
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
    public void updateGatewayProfile(String gatewayId, GatewayProfile updatedProfile) throws AppCatalogException {
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
                    Map<String, String> ids = new HashMap<String, String>();
                    ids.put(AbstractResource.ComputeResourcePreferenceConstants.GATEWAY_ID, gatewayId);
                    ids.put(AbstractResource.ComputeResourcePreferenceConstants.RESOURCE_ID, preference.getComputeResourceId());
                    ComputeHostPreferenceResource existingPreferenceResource = (ComputeHostPreferenceResource)resource.get(ids);

                    existingPreferenceResource.setGatewayProfile(existingGP);
                    existingPreferenceResource.setResourceId(preference.getComputeResourceId());
                    ComputeHostResource computeHostResource = new ComputeHostResource();
                    existingPreferenceResource.setComputeHostResource((ComputeHostResource)computeHostResource.get(preference.getComputeResourceId()));
                    existingPreferenceResource.setGatewayId(gatewayId);
                    existingPreferenceResource.setOverrideByAiravata(preference.isOverridebyAiravata());
                    existingPreferenceResource.setPreferredJobProtocol(preference.getPreferredJobSubmissionProtocol());
                    existingPreferenceResource.setPreferedDMProtocol(preference.getPreferredDataMovementProtocol());
                    existingPreferenceResource.setBatchQueue(preference.getPreferredBatchQueue());
                    existingPreferenceResource.setProjectNumber(preference.getAllocationProjectNumber());
                    existingPreferenceResource.setScratchLocation(preference.getScratchLocation());
                    existingPreferenceResource.save();
                }
            }
        }catch (Exception e) {
            logger.error("Error while updating gateway profile...", e);
            throw new AppCatalogException(e);
        }
    }

    @Override
    public GatewayProfile getGatewayProfile(String gatewayId) throws AppCatalogException {
        try {
            GatewayProfileResource resource = new GatewayProfileResource();
            GatewayProfileResource gwresource = (GatewayProfileResource)resource.get(gatewayId);
            ComputeHostPreferenceResource prefResource = new ComputeHostPreferenceResource();
            List<Resource> computePrefList = prefResource.get(AbstractResource.ComputeResourcePreferenceConstants.GATEWAY_ID, gatewayId);
            List<ComputeResourcePreference> computeResourcePreferences = AppCatalogThriftConversion.getComputeResourcePreferences(computePrefList);
            return AppCatalogThriftConversion.getGatewayProfile(gwresource, computeResourcePreferences);
        }catch (Exception e) {
            logger.error("Error while retrieving gateway profile...", e);
            throw new AppCatalogException(e);
        }
    }

    @Override
    public JobSubmissionProtocol getPreferedJobSubmissionProtocol(String gatewayId, String hostId) throws AppCatalogException{
        try {
            ComputeHostPreferenceResource prefResource = new ComputeHostPreferenceResource();
            List<Resource> computePrefList = prefResource.get(AbstractResource.ComputeResourcePreferenceConstants.GATEWAY_ID, gatewayId);
            if (computePrefList != null && !computePrefList.isEmpty()){
                for (Resource pref : computePrefList){
                    ComputeHostPreferenceResource preferenceResource = (ComputeHostPreferenceResource)pref;
                    if (preferenceResource.getResourceId().equals(hostId)){
                        return JobSubmissionProtocol.valueOf(preferenceResource.getPreferredJobProtocol());
                    }
                }
            }
        }catch (Exception e) {
            logger.error("Error while retrieving job submission protocol for given host and gateway...", e);
            throw new AppCatalogException(e);
        }
        return null;
    }

    @Override
    public DataMovementProtocol getPreferedDMProtocol(String gatewayId, String hostId) throws AppCatalogException{
        try {
            ComputeHostPreferenceResource prefResource = new ComputeHostPreferenceResource();
            List<Resource> computePrefList = prefResource.get(AbstractResource.ComputeResourcePreferenceConstants.GATEWAY_ID, gatewayId);
            if (computePrefList != null && !computePrefList.isEmpty()){
                for (Resource pref : computePrefList){
                    ComputeHostPreferenceResource preferenceResource = (ComputeHostPreferenceResource)pref;
                    if (preferenceResource.getResourceId().equals(hostId)){
                        return DataMovementProtocol.valueOf(preferenceResource.getPreferedDMProtocol());
                    }
                }
            }
        }catch (Exception e) {
            logger.error("Error while retrieving data movement protocol for given host and gateway...", e);
            throw new AppCatalogException(e);
        }
        return null;
    }


    @Override
    public boolean removeGatewayProfile(String gatewayId) throws AppCatalogException {
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
    public boolean isGatewayProfileExists(String gatewayId) throws AppCatalogException {
        try {
            GatewayProfileResource resource = new GatewayProfileResource();
            return resource.isExists(gatewayId);
        }catch (Exception e) {
            logger.error("Error while retrieving gateway profile...", e);
            throw new AppCatalogException(e);
        }
    }
}
