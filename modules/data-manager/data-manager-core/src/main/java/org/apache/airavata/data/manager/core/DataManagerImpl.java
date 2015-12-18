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
package org.apache.airavata.data.manager.core;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.credential.store.client.CredentialStoreClientFactory;
import org.apache.airavata.credential.store.cpi.CredentialStoreService;
import org.apache.airavata.credential.store.datamodel.SSHCredential;
import org.apache.airavata.credential.store.exception.CredentialStoreException;
import org.apache.airavata.data.manager.core.ssh.SSHUtils;
import org.apache.airavata.model.appcatalog.gatewayprofile.GatewayResourceProfile;
import org.apache.airavata.model.appcatalog.gatewayprofile.StoragePreference;
import org.apache.airavata.model.appcatalog.storageresource.StorageResourceDescription;
import org.apache.airavata.model.data.movement.DataMovementInterface;
import org.apache.airavata.model.data.movement.DataMovementProtocol;
import org.apache.airavata.model.data.movement.SCPDataMovement;
import org.apache.airavata.model.data.resource.DataReplicaLocationModel;
import org.apache.airavata.model.data.resource.DataResourceModel;
import org.apache.airavata.model.data.resource.DataResourceType;
import org.apache.airavata.registry.core.experiment.catalog.impl.RegistryFactory;
import org.apache.airavata.registry.cpi.AppCatalog;
import org.apache.airavata.registry.cpi.AppCatalogException;
import org.apache.airavata.registry.cpi.DataCatalog;
import org.apache.airavata.registry.cpi.DataCatalogException;

import org.apache.airavata.data.manager.cpi.DataManager;
import org.apache.airavata.data.manager.cpi.DataManagerException;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;

public class DataManagerImpl implements DataManager {
    private final static Logger logger = LoggerFactory.getLogger(DataManagerImpl.class);

    private final AppCatalog appCatalog;
    private final DataCatalog dataCatalog;

    public DataManagerImpl() throws DataManagerException {
        try {
            this.appCatalog = RegistryFactory.getAppCatalog();
            this.dataCatalog = RegistryFactory.getDataCatalog();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new DataManagerException(e);
        }
    }

    public DataManagerImpl(AppCatalog appCatalog, DataCatalog dataCatalog){
        this.appCatalog = appCatalog;
        this.dataCatalog = dataCatalog;
    }

    /**
     * To create a replica entry for an already existing file(s). This is how the system comes to know about already
     * existing resources
     * @param dataResourceModel
     * @return
     */
    @Override
    public String registerResource(DataResourceModel dataResourceModel) throws DataManagerException {
        try {
            String resourceId = dataCatalog.registerResource(dataResourceModel);
            return resourceId;
        } catch (DataCatalogException e) {
            logger.error(e.getMessage(), e);
            throw new DataManagerException(e);
        }
    }

    /**
     * To remove a resource entry from the replica catalog
     * @param resourceId
     * @return
     */
    @Override
    public boolean removeResource(String resourceId) throws DataManagerException {
        try {
            boolean result = dataCatalog.removeResource(resourceId);
            return result;
        } catch (DataCatalogException e) {
            logger.error(e.getMessage(), e);
            throw new DataManagerException(e);
        }
    }

    /**
     * To update an existing data resource model
     * @param dataResourceModel
     * @return
     * @throws DataCatalogException
     */
    @Override
    public boolean updateResource(DataResourceModel dataResourceModel) throws DataManagerException {
        try {
            boolean result = dataCatalog.updateResource(dataResourceModel);
            return result;
        } catch (DataCatalogException e) {
            logger.error(e.getMessage(), e);
            throw new DataManagerException(e);
        }
    }

    /**
     * To retrieve a resource object providing the resourceId
     * @param resourceId
     * @return
     */
    @Override
    public DataResourceModel getResource(String resourceId) throws DataManagerException {
        try {
            DataResourceModel dataResource = dataCatalog.getResource(resourceId);
            return dataResource;
        } catch (DataCatalogException e) {
            logger.error(e.getMessage(), e);
            throw new DataManagerException(e);
        }
    }

    /**
     * To create a new data replica location. This is how the system comes to know about already
     * existing resources
     *
     * @param dataReplicaLocationModel
     * @return
     */
    @Override
    public String registerReplicaLocation(DataReplicaLocationModel dataReplicaLocationModel) throws DataManagerException {
        try {
            String replicaId = dataCatalog.registerReplicaLocation(dataReplicaLocationModel);
            return replicaId;
        } catch (DataCatalogException e) {
            logger.error(e.getMessage(), e);
            throw new DataManagerException(e);
        }
    }

    /**
     * To remove a replica entry from the replica catalog
     *
     * @param replicaId
     * @return
     */
    @Override
    public boolean removeReplicaLocation(String replicaId) throws DataManagerException {
        try {
            boolean result = dataCatalog.removeReplicaLocation(replicaId);
            return result;
        } catch (DataCatalogException e) {
            logger.error(e.getMessage(), e);
            throw new DataManagerException(e);
        }
    }

    /**
     * To update an existing data replica model
     *
     * @param dataReplicaLocationModel
     * @return
     * @throws DataCatalogException
     */
    @Override
    public boolean updateReplicaLocation(DataReplicaLocationModel dataReplicaLocationModel) throws DataManagerException {
        try {
            boolean result = dataCatalog.updateReplicaLocation(dataReplicaLocationModel);
            return result;
        } catch (DataCatalogException e) {
            logger.error(e.getMessage(), e);
            throw new DataManagerException(e);
        }
    }

    /**
     * To retrieve a replica object providing the replicaId
     *
     * @param replicaId
     * @return
     */
    @Override
    public DataReplicaLocationModel getReplicaLocation(String replicaId) throws DataManagerException {
        try {
            DataReplicaLocationModel dataReplicaLocationModel = dataCatalog.getReplicaLocation(replicaId);
            return dataReplicaLocationModel;
        } catch (DataCatalogException e) {
            logger.error(e.getMessage(), e);
            throw new DataManagerException(e);
        }
    }

    /**
     * To retrieve all the replica entries for a given resource id
     *
     * @param resourceId
     * @return
     * @throws DataCatalogException
     */
    @Override
    public List<DataReplicaLocationModel> getAllReplicaLocations(String resourceId) throws DataManagerException {
        try {
            List<DataReplicaLocationModel> dataReplicaLocationModelList = dataCatalog.getAllReplicaLocations(resourceId);
            return dataReplicaLocationModelList;
        } catch (DataCatalogException e) {
            logger.error(e.getMessage(), e);
            throw new DataManagerException(e);
        }
    }

    /**
     * API method to copy a resource to the provided destination storage resource. Only resources of type FILE can be
     * copied using this API method.
     *
     * @param dataResourceId
     * @param destStorageResourceId
     * @param destinationParentPath
     * @return
     */
    @Override
    public String copyResource(String dataResourceId, String destStorageResourceId, String destinationParentPath) throws DataManagerException {
        try {
            return copyReplica(dataResourceId, null, destStorageResourceId, destinationParentPath);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new DataManagerException(e);
        }
    }

    /**
     * API method to copy the specified replica to the provided destination storage resource. Only resources of type FILE
     * can be copied using this API method. Method returns the new replicaId
     *
     * @param dataResourceId
     * @param replicaId
     * @param destStorageResourceId
     * @param destinationParentPath
     * @return
     * @throws DataManagerException
     */
    @Override
    public String copyReplica(String dataResourceId, String replicaId, String destStorageResourceId, String destinationParentPath) throws DataManagerException {
        try{
            DataResourceModel dataResourceModel = dataCatalog.getResource(dataResourceId);
            if(dataResourceModel.getDataResourceType() != DataResourceType.FILE)
                throw new DataCatalogException("Only resources of type FILE can be transferred using this method");

            StorageResourceDescription destinationStorageResource = appCatalog.getStorageResource()
                    .getStorageResource(destStorageResourceId);
            if(destinationStorageResource == null)
                throw new DataCatalogException("Invalid destination storage resource id");

            List<DataReplicaLocationModel> replicaLocationModels = dataResourceModel.getReplicaLocations();
            if(replicaLocationModels == null || replicaLocationModels.size() == 0)
                throw new DataCatalogException("No replicas available for the given data resource");

            DataReplicaLocationModel sourceReplica = null;
            if(replicaId == null || replicaId.isEmpty()) {
                //FIXME This should be an intelligent selection
                sourceReplica = replicaLocationModels.get(0);
            }else{
                for(DataReplicaLocationModel rp : replicaLocationModels){
                    if(rp.getReplicaId().equals(replicaId)){
                        sourceReplica = rp;
                    }
                }
            }
            if(sourceReplica == null)
                throw new DataManagerException("No matching source replica found");

            StorageResourceDescription sourceStorageResource = appCatalog.getStorageResource()
                    .getStorageResource(sourceReplica.getStorageResourceId());
            if(sourceStorageResource == null)
                throw new DataCatalogException("Cannot find storage resource of the source replica");

            //FIXME Currently we support only SCP data movement protocol
            List<DataMovementInterface> sourceDataMovementInterfaces = sourceStorageResource.getDataMovementInterfaces();
            Optional<DataMovementInterface> sourceDataMovementInterface = sourceDataMovementInterfaces.stream()
                    .filter(dmi -> dmi.getDataMovementProtocol() == DataMovementProtocol.SCP).findFirst();
            if(!sourceDataMovementInterface.isPresent())
                throw new DataCatalogException("No matching DMI found for source storage resource");
            List<DataMovementInterface> destDataMovementInterfaces = destinationStorageResource.getDataMovementInterfaces();
            Optional<DataMovementInterface> destDataMovementInterface = destDataMovementInterfaces.stream()
                    .filter(dmi -> dmi.getDataMovementProtocol() == DataMovementProtocol.SCP).findFirst();
            if(!destDataMovementInterface.isPresent())
                throw new DataCatalogException("No matching DMI found for destination storage resource");

            //Finding the gateway specific storage preferences for resources
            GatewayResourceProfile gatewayProfile = appCatalog.getGatewayProfile().getGatewayProfile(dataResourceModel.getGatewayId());
            List<StoragePreference> storagePreferences = gatewayProfile.getStoragePreferences();
            StoragePreference sourceResourcePreference = null;
            for(StoragePreference sp : storagePreferences) {
                if (sp.getStorageResourceId().equals(sourceStorageResource.getStorageResourceId())) {
                    sourceResourcePreference = sp;
                    break;
                }
            }
            if(sourceResourcePreference == null)
                throw new DataCatalogException("Could not find storage preference for storage resource id:"
                        + sourceStorageResource.getStorageResourceId());
            StoragePreference destResourcePreference = null;
            for(StoragePreference sp : storagePreferences) {
                if (sp.getStorageResourceId().equals(destStorageResourceId)) {
                    destResourcePreference = sp;
                    break;
                }
            }
            if(destResourcePreference == null)
                throw new DataCatalogException("Could not find storage preference for storage resource id:"
                        + destinationStorageResource.getStorageResourceId());

            String destFilePath = copyUsingScp(gatewayProfile, sourceStorageResource, sourceDataMovementInterface.get(),
                    sourceResourcePreference, sourceReplica, destinationStorageResource, destDataMovementInterface.get(),
                    destResourcePreference, destinationParentPath);

            DataReplicaLocationModel dataReplicaLocationModel = new DataReplicaLocationModel();
            dataReplicaLocationModel.setResourceId(dataResourceId);
            dataReplicaLocationModel.setFileAbsolutePath(destFilePath);
            String newReplicaId = this.registerReplicaLocation(dataReplicaLocationModel);
            return newReplicaId;
        }catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new DataManagerException(e);
        }
    }

    /**
     * This method copies the provided source replica to the destination storage resource and returns the absolute file path
     * of the destination file. This method uses the credential store service to fetch required credentials for talking to
     * storage resources
     *
     * @param gatewayProfile
     * @param sourceStorageResource
     * @param sourceDataMovementInterface
     * @param sourceResourcePreference
     * @param sourceReplica
     * @param destStorageResource
     * @param destDataMovementInterface
     * @param destResourcePreference
     * @param destinationParentPath
     * @return
     * @throws TException
     * @throws ApplicationSettingsException
     * @throws AppCatalogException
     * @throws JSchException
     * @throws IOException
     */
    private String copyUsingScp(GatewayResourceProfile gatewayProfile, StorageResourceDescription sourceStorageResource,
                              DataMovementInterface sourceDataMovementInterface, StoragePreference sourceResourcePreference,
                              DataReplicaLocationModel sourceReplica, StorageResourceDescription destStorageResource,
                              DataMovementInterface destDataMovementInterface, StoragePreference destResourcePreference,
                              String destinationParentPath)
            throws Exception {
        //Creating JSch sessions
        //Source session
        Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", "no");

        CredentialStoreService.Client credentialStoreServiceClient = getCredentialStoreServiceClient();
        String sourceHostName =  sourceStorageResource.getHostName();
        SCPDataMovement sourceSCPDMI = appCatalog.getComputeResource().getSCPDataMovement(sourceDataMovementInterface.getDataMovementInterfaceId());
        int sourcePort = sourceSCPDMI.getSshPort();
        String sourceLoginUserName = sourceResourcePreference.getLoginUserName();
        JSch sourceJSch = new JSch();
        String sourceCredentialStoreToken;
        if(sourceResourcePreference.getResourceSpecificCredentialStoreToken() != null
                && !sourceResourcePreference.getResourceSpecificCredentialStoreToken().isEmpty()){
            sourceCredentialStoreToken = sourceResourcePreference.getResourceSpecificCredentialStoreToken();
        }else{
            sourceCredentialStoreToken = gatewayProfile.getCredentialStoreToken();
        }
        SSHCredential sourceSshCredential = credentialStoreServiceClient.getSSHCredential(sourceCredentialStoreToken,
                gatewayProfile.getGatewayID());
        sourceJSch.addIdentity(UUID.randomUUID().toString(), sourceSshCredential.getPrivateKey().getBytes(),
                sourceSshCredential.getPublicKey().getBytes(), sourceSshCredential.getPassphrase().getBytes());
        Session sourceSession = sourceJSch.getSession(sourceLoginUserName, sourceHostName, sourcePort);
        sourceSession.setConfig(config);
        sourceSession.connect();
        String sourceFilePath = sourceReplica.getFileAbsolutePath();

        //Destination session
        String destHostName =  destStorageResource.getHostName();
        SCPDataMovement destSCPDMI = appCatalog.getComputeResource().getSCPDataMovement(destDataMovementInterface
                .getDataMovementInterfaceId());
        int destPort = destSCPDMI.getSshPort();
        String destLoginUserName = sourceResourcePreference.getLoginUserName();
        JSch destJSch = new JSch();
        String destCredentialStoreToken;
        if(destResourcePreference.getResourceSpecificCredentialStoreToken() != null
                && !destResourcePreference.getResourceSpecificCredentialStoreToken().isEmpty()){
            destCredentialStoreToken = destResourcePreference.getResourceSpecificCredentialStoreToken();
        }else{
            destCredentialStoreToken = gatewayProfile.getCredentialStoreToken();
        }
        SSHCredential destSshCredential = credentialStoreServiceClient.getSSHCredential(destCredentialStoreToken,
                gatewayProfile.getGatewayID());
        destJSch.addIdentity(UUID.randomUUID().toString(), destSshCredential.getPrivateKey().getBytes(),
                destSshCredential.getPublicKey().getBytes(), destSshCredential.getPassphrase().getBytes());
        Session destSession = destJSch.getSession(destLoginUserName, destHostName, destPort);
        destSession.setConfig(config);
        destSession.connect();

        SSHUtils.scpThirdParty(sourceFilePath, sourceSession, destinationParentPath, destSession);
        if(!destinationParentPath.endsWith(File.separator))
            destinationParentPath += File.separator;
        String destFilePath = destinationParentPath + (new File(sourceFilePath).getName());
        return destFilePath;
    }

    private CredentialStoreService.Client getCredentialStoreServiceClient() throws TException, ApplicationSettingsException {
        final int serverPort = Integer.parseInt(ServerSettings.getCredentialStoreServerPort());
        final String serverHost = ServerSettings.getCredentialStoreServerHost();
        try {
            return CredentialStoreClientFactory.createAiravataCSClient(serverHost, serverPort);
        } catch (CredentialStoreException e) {
            throw new TException("Unable to create credential store client...", e);
        }
    }
}