/**
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
 */
package org.apache.airavata.testsuite.multitenantedairavata;

import org.apache.airavata.api.Airavata;
import org.apache.airavata.model.appcatalog.gatewayprofile.GatewayResourceProfile;
import org.apache.airavata.model.appcatalog.gatewayprofile.StoragePreference;
import org.apache.airavata.model.appcatalog.storageresource.StorageResourceDescription;
import org.apache.airavata.model.data.movement.DMType;
import org.apache.airavata.model.data.movement.LOCALDataMovement;
import org.apache.airavata.model.security.AuthzToken;
import org.apache.airavata.testsuite.multitenantedairavata.utils.TestFrameworkConstants;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.airavata.testsuite.multitenantedairavata.utils.TestFrameworkConstants.LocalEchoProperties.*;

public class StorageResourceRegister {

    private final static Logger logger = LoggerFactory.getLogger(StorageResourceRegister.class);

    private Airavata.Client airavata;

    private TestFrameworkProps properties;
    private AuthzToken authzToken;

    private Map<String, String> loginNamesWithResourceMap;
    private Map<String, String> loginNamesWithResourceIds;

    public Map<String, String> getLoginNamesMap() throws Exception {
        loginNamesWithResourceMap = new HashMap<String, String>();
        TestFrameworkProps.Resource[] resourcesWithloginName = properties.getResources();
        if (resourcesWithloginName != null){
            for (TestFrameworkProps.Resource resource : resourcesWithloginName){
                loginNamesWithResourceMap.put(resource.getName(), resource.getLoginUser());
            }
        }
        return loginNamesWithResourceMap;
    }

    public Map<String, String> getLoginNamesWithResourceIDs() throws Exception {
        loginNamesWithResourceIds = new HashMap<String, String>();
        Map<String, String> allStorageResourceNames = airavata.getAllStorageResourceNames(authzToken);
        for (String resourceId : allStorageResourceNames.keySet()) {
            String resourceName = allStorageResourceNames.get(resourceId);
            loginNamesWithResourceIds.put(resourceId, loginNamesWithResourceMap.get(resourceName));
        }

        return loginNamesWithResourceIds;
    }

    public StorageResourceRegister(Airavata.Client airavata, TestFrameworkProps props) throws Exception{
        this.airavata = airavata;
        this.properties = props;
        loginNamesWithResourceMap = getLoginNamesMap();
        authzToken = new AuthzToken("emptyToken");
    }

    public String addStorageResourceResource() throws Exception {

        String localStorageResource = null;

        try{
            /*
            check if local storage is already there
             */
            Map<String, String> storageResources = airavata.getAllStorageResourceNames(authzToken);

            for(Map.Entry<String, String> storageResource: storageResources.entrySet()){
                if(storageResource.getValue().contains("localhost")){
                    localStorageResource =  storageResource.getKey();
                    System.out.println("Existing Local Storage Resource Id " + localStorageResource);
                    return localStorageResource;
                }
            }

            /*
            Create local storage
             */
            for (String resourceName : loginNamesWithResourceMap.keySet()) {
                if (resourceName.contains(RESOURCE_NAME)) {
                    localStorageResource = registerStorageResource(HOST_NAME, HOST_DESC);
                    System.out.println("Local Storage Resource Id " + localStorageResource);
                }
            }

        } catch (TException e) {
            logger.error("Error occured while creating storage resource preference", e);
            throw new Exception("Error occured while creating storage resource preference", e);
        }
        return localStorageResource;
    }

    public StorageResourceDescription getStorageResourceDescription(String storageResourceId) throws Exception {
        return airavata.getStorageResource(authzToken, storageResourceId);

    }

    public String registerStorageResource(String hostName, String hostDesc) throws TException{
        StorageResourceDescription storageResourceDescription = createStorageResourceDescription(hostName, hostDesc);
        String storageResourceId = airavata.registerStorageResource(authzToken, storageResourceDescription);
        airavata.addLocalDataMovementDetails(authzToken, storageResourceId, DMType.STORAGE_RESOURCE, 0, new LOCALDataMovement());
        return storageResourceId;
    }

    public StorageResourceDescription createStorageResourceDescription(
            String hostName, String hostDesc) {
        StorageResourceDescription host = new StorageResourceDescription();
        host.setHostName(hostName);
        host.setStorageResourceDescription(hostDesc);
        host.setEnabled(true);
        return host;
    }

    public void registerGatewayStorageProfile(String storageResourceId) throws Exception{
        StoragePreference localResourcePreference = null;
        try{
            loginNamesWithResourceIds = getLoginNamesWithResourceIDs();
            List<GatewayResourceProfile> allGatewayComputeResources = airavata.getAllGatewayResourceProfiles(authzToken);
            for (GatewayResourceProfile gatewayResourceProfile : allGatewayComputeResources) {
                for (String resourceId : loginNamesWithResourceIds.keySet()) {
                    String loginUserName = loginNamesWithResourceIds.get(resourceId);
                    if (resourceId.equals(storageResourceId) && loginUserName.equals(LOGIN_USER)){
                        localResourcePreference = createStoragePreferenceResource(resourceId, loginUserName, TestFrameworkConstants.STORAGE_LOCATION);
                        airavata.addGatewayStoragePreference(authzToken, gatewayResourceProfile.getGatewayID(), resourceId, localResourcePreference);
                    }
                }
            }

        } catch (TException e) {
            logger.error("Error occured while registering gateway storage profiles", e);
            throw new Exception("Error occured while registering gateway storage profiles", e);
        }
    }

    public StoragePreference getStoragePreference(String gatewayId, String storageResourceId) throws Exception {
        return airavata.getGatewayStoragePreference(authzToken, gatewayId, storageResourceId);

    }

    public StoragePreference createStoragePreferenceResource(String storageResourceId, String loginUser, String fsRootLocation){
        StoragePreference storagePreference = new StoragePreference();
        storagePreference.setStorageResourceId(storageResourceId);
        storagePreference.setLoginUserName(loginUser);
        storagePreference.setFileSystemRootLocation(fsRootLocation);
        return storagePreference;
    }
}
