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

package org.apache.airavata.registry.core.app.catalog.impl;

import org.apache.airavata.model.appcatalog.gatewayprofile.ComputeResourcePreference;
import org.apache.airavata.model.appcatalog.gatewayprofile.DataStoragePreference;
import org.apache.airavata.model.appcatalog.gatewayprofile.GatewayResourceProfile;
import org.apache.airavata.registry.core.app.catalog.resources.*;
import org.apache.airavata.registry.core.app.catalog.util.AppCatalogThriftConversion;
import org.apache.airavata.registry.cpi.AppCatalogException;
import org.apache.airavata.registry.cpi.GwyResourceProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GwyResourceProfileImpl implements GwyResourceProfile {
    private final static Logger logger = LoggerFactory.getLogger(GwyResourceProfileImpl.class);

    @Override
    public String addGatewayResourceProfile(org.apache.airavata.model.appcatalog.gatewayprofile.GatewayResourceProfile gatewayProfile) throws AppCatalogException {
        try {
            GatewayProfileResource profileResource = new GatewayProfileResource();
            if (!gatewayProfile.getGatewayID().equals("")){
                profileResource.setGatewayID(gatewayProfile.getGatewayID());
            }
//            profileResource.setGatewayID(gatewayProfile.getGatewayID());
            profileResource.save();
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
                    resource.setLoginUserName(preference.getLoginUserName());
                    resource.setResourceCSToken(preference.getResourceSpecificCredentialStoreToken());
                    if (preference.getPreferredJobSubmissionProtocol() != null){
                        resource.setPreferredJobProtocol(preference.getPreferredJobSubmissionProtocol().toString());
                    }

                    if (preference.getPreferredDataMovementProtocol() != null){
                        resource.setPreferedDMProtocol(preference.getPreferredDataMovementProtocol().toString());
                    }

                    resource.setBatchQueue(preference.getPreferredBatchQueue());
                    resource.setProjectNumber(preference.getAllocationProjectNumber());
                    resource.setScratchLocation(preference.getScratchLocation());
                    resource.save();
                }
            }
            List<DataStoragePreference> dataStoragePreferences = gatewayProfile.getDataStoragePreferences();
            if (dataStoragePreferences != null && !dataStoragePreferences.isEmpty()){
                for (DataStoragePreference storagePreference : dataStoragePreferences){
                    DataStoragePreferenceResource resource = new DataStoragePreferenceResource();
                    resource.setDataMoveId(storagePreference.getDataMovememtResourceId());
                    resource.setGatewayId(profileResource.getGatewayID());
                    resource.setFsRootLocation(storagePreference.getFileSystemRootLocation());
                    resource.setLoginUserName(storagePreference.getLoginUserName());
                    resource.setResourceCSToken(storagePreference.getResourceSpecificCredentialStoreToken());
                    resource.setGatewayProfile(profileResource);
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
                    resource.setLoginUserName(preference.getLoginUserName());
                    resource.setOverrideByAiravata(preference.isOverridebyAiravata());
                    if (preference.getPreferredJobSubmissionProtocol() != null){
                        resource.setPreferredJobProtocol(preference.getPreferredJobSubmissionProtocol().toString());
                    }

                    if (preference.getPreferredDataMovementProtocol() != null){
                        resource.setPreferedDMProtocol(preference.getPreferredDataMovementProtocol().toString());
                    }
                    resource.setBatchQueue(preference.getPreferredBatchQueue());
                    resource.setProjectNumber(preference.getAllocationProjectNumber());
                    resource.setScratchLocation(preference.getScratchLocation());
                    resource.save();
                }
            }
            List<DataStoragePreference> dataStoragePreferences = updatedProfile.getDataStoragePreferences();
            if (dataStoragePreferences != null && !dataStoragePreferences.isEmpty()){
                for (DataStoragePreference storagePreference : dataStoragePreferences){
                    DataStoragePreferenceResource resource = new DataStoragePreferenceResource();
                    resource.setDataMoveId(storagePreference.getDataMovememtResourceId());
                    resource.setGatewayId(profileResource.getGatewayID());
                    resource.setFsRootLocation(storagePreference.getFileSystemRootLocation());
                    resource.setLoginUserName(storagePreference.getLoginUserName());
                    resource.setResourceCSToken(storagePreference.getResourceSpecificCredentialStoreToken());
                    resource.setGatewayProfile(profileResource);
                    resource.save();
                }
            }
        }catch (Exception e) {
            logger.error("Error while updating gateway profile...", e);
            throw new AppCatalogException(e);
        }
    }

    @Override
    public GatewayResourceProfile getGatewayProfile(String gatewayId) throws AppCatalogException {
        try {
            GatewayProfileResource resource = new GatewayProfileResource();
            GatewayProfileResource gwresource = (GatewayProfileResource)resource.get(gatewayId);
            ComputeHostPreferenceResource prefResource = new ComputeHostPreferenceResource();
            List<AppCatalogResource> computePrefList = prefResource.get(AppCatAbstractResource.ComputeResourcePreferenceConstants.GATEWAY_ID, gatewayId);
            List<ComputeResourcePreference> computeResourcePreferences = AppCatalogThriftConversion.getComputeResourcePreferences(computePrefList);
            List<DataStoragePreference> dataStoragePreferences = getAllDataStoragePreferences(gatewayId);
            return AppCatalogThriftConversion.getGatewayResourceProfile(gwresource, computeResourcePreferences, dataStoragePreferences);
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
    public boolean removeComputeResourcePreferenceFromGateway(String gatewayId, String preferenceId) throws AppCatalogException {
        try {
            ComputeHostPreferenceResource resource = new ComputeHostPreferenceResource();
            Map<String, String> ids = new HashMap<String, String>();
            ids.put(AppCatAbstractResource.ComputeResourcePreferenceConstants.GATEWAY_ID, gatewayId);
            ids.put(AppCatAbstractResource.ComputeResourcePreferenceConstants.RESOURCE_ID, preferenceId);
            resource.remove(ids);
            return true;
        }catch (Exception e) {
            logger.error("Error while deleting gateway profile...", e);
            throw new AppCatalogException(e);
        }
    }

    @Override
    public boolean removeDataStoragePreferenceFromGateway(String gatewayId, String preferenceId) throws AppCatalogException {
        try {
            DataStoragePreferenceResource resource = new DataStoragePreferenceResource();
            Map<String, String> ids = new HashMap<String, String>();
            ids.put(AppCatAbstractResource.DataStoragePreferenceConstants.GATEWAY_ID, gatewayId);
            ids.put(AppCatAbstractResource.DataStoragePreferenceConstants.DATA_MOVEMENT_ID, preferenceId);
            resource.remove(ids);
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
            List<AppCatalogResource> computePrefList = prefResource.get(AppCatAbstractResource.ComputeResourcePreferenceConstants.GATEWAY_ID, gatewayId);
            for (AppCatalogResource resource : computePrefList){
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

    @Override
    public DataStoragePreference getDataStoragePreference(String gatewayId, String dataMoveId) throws AppCatalogException {
        try {
            DataStoragePreferenceResource prefResource = new DataStoragePreferenceResource();
            List<AppCatalogResource> computePrefList = prefResource.get(AppCatAbstractResource.ComputeResourcePreferenceConstants.GATEWAY_ID, gatewayId);
            for (AppCatalogResource resource : computePrefList){
                DataStoragePreferenceResource dsP = (DataStoragePreferenceResource) resource;
                if (dsP.getDataMoveId() != null && !dsP.getDataMoveId().equals("")){
                    if (dsP.getDataMoveId().equals(dataMoveId)){
                        return AppCatalogThriftConversion.getDataStoragePreference(dsP);
                    }
                }
            }
        }catch (Exception e) {
            logger.error("Error while retrieving data storage preference...", e);
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
            List<AppCatalogResource> computePrefList = prefResource.get(AppCatAbstractResource.ComputeResourcePreferenceConstants.GATEWAY_ID, gatewayId);
            return AppCatalogThriftConversion.getComputeResourcePreferences(computePrefList);
        }catch (Exception e) {
            logger.error("Error while retrieving compute resource preference...", e);
            throw new AppCatalogException(e);
        }
    }

    @Override
    public List<DataStoragePreference> getAllDataStoragePreferences(String gatewayId) throws AppCatalogException {
        try {
            DataStoragePreferenceResource prefResource = new DataStoragePreferenceResource();
            List<AppCatalogResource> dataStoragePrefList = prefResource.get(AppCatAbstractResource.DataStoragePreferenceConstants.GATEWAY_ID, gatewayId);
            return AppCatalogThriftConversion.getDataStoragePreferences(dataStoragePrefList);
        }catch (Exception e) {
            logger.error("Error while retrieving data storage preference...", e);
            throw new AppCatalogException(e);
        }
    }

    @Override
    public List<String> getGatewayProfileIds(String gatewayName) throws AppCatalogException {
        try {
            GatewayProfileResource profileResource = new GatewayProfileResource();
            List<AppCatalogResource> resourceList = profileResource.get(AppCatAbstractResource.GatewayProfileConstants.GATEWAY_ID, gatewayName);
            List<String> gatewayIds = new ArrayList<String>();
            if (resourceList != null && !resourceList.isEmpty()){
                for (AppCatalogResource resource : resourceList){
                    gatewayIds.add(((GatewayProfileResource)resource).getGatewayID());
                }
            }
            return gatewayIds;
        }catch (Exception e) {
            logger.error("Error while retrieving gateway ids...", e);
            throw new AppCatalogException(e);
        }
    }

    @Override
    public List<GatewayResourceProfile> getAllGatewayProfiles() throws AppCatalogException {
        try {
            List<GatewayResourceProfile> gatewayResourceProfileList = new ArrayList<GatewayResourceProfile>();
            GatewayProfileResource profileResource = new GatewayProfileResource();
            List<AppCatalogResource> resourceList = profileResource.getAll();
            if (resourceList != null && !resourceList.isEmpty()){
                for (AppCatalogResource resource : resourceList){
                    GatewayProfileResource gatewayProfileResource = (GatewayProfileResource)resource;
                    List<ComputeResourcePreference> computeResourcePreferences = getAllComputeResourcePreferences(gatewayProfileResource.getGatewayID());
                    List<DataStoragePreference> dataStoragePreferences = getAllDataStoragePreferences(gatewayProfileResource.getGatewayID());
                    GatewayResourceProfile gatewayResourceProfile = AppCatalogThriftConversion.getGatewayResourceProfile(gatewayProfileResource, computeResourcePreferences, dataStoragePreferences);
                    gatewayResourceProfileList.add(gatewayResourceProfile);
                }
            }
            return gatewayResourceProfileList;
        }catch (Exception e) {
            logger.error("Error while retrieving gateway ids...", e);
            throw new AppCatalogException(e);
        }
    }
}
