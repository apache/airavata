/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.airavata.tools.load;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import org.apache.airavata.agents.api.AgentException;
import org.apache.airavata.api.Airavata;
import org.apache.airavata.helix.adaptor.SSHJStorageAdaptor;
import org.apache.airavata.model.appcatalog.gatewayprofile.StoragePreference;
import org.apache.airavata.model.appcatalog.storageresource.StorageResourceDescription;
import org.apache.airavata.model.data.replica.*;
import org.apache.airavata.model.security.AuthzToken;
import org.apache.thrift.TException;

public class StorageResourceManager {

    private StoragePreference gatewayStoragePreference;
    private StorageResourceDescription storageResource;
    private String storageResourceId;

    private String privateKeyFile;
    private String publicKeyFile;
    private String passPhrase;

    private SSHJStorageAdaptor storageAdaptor = new SSHJStorageAdaptor();

    public StorageResourceManager(
            StoragePreference gatewayStoragePreference,
            StorageResourceDescription storageResource,
            String privateKeyFile,
            String publicKeyFile,
            String passPhrase) {
        this.storageResourceId = storageResource.getStorageResourceId();
        this.storageResource = storageResource;
        this.gatewayStoragePreference = gatewayStoragePreference;
        this.privateKeyFile = privateKeyFile;
        this.publicKeyFile = publicKeyFile;
        this.passPhrase = passPhrase;
    }

    public void init() throws IOException, AgentException {
        storageAdaptor.init(
                gatewayStoragePreference.getLoginUserName(),
                storageResource.getHostName(),
                22,
                readFile(publicKeyFile, Charset.defaultCharset()),
                readFile(privateKeyFile, Charset.defaultCharset()),
                passPhrase);
    }

    public void destroy() {
        storageAdaptor.destroy();
    }

    public String uploadInputFile(
            Airavata.Client airavataClient,
            String filePath,
            String user,
            String project,
            String experiment,
            String gatewayId)
            throws TException, AgentException {

        String experimentDirectory = getExperimentDirectory(user, project, experiment);

        String uploadFilePath = experimentDirectory.concat(File.separator).concat(new File(filePath).getName());
        storageAdaptor.uploadFile(filePath, uploadFilePath);

        DataProductModel dataProductModel = new DataProductModel();
        dataProductModel.setGatewayId(gatewayId);
        dataProductModel.setOwnerName(user);
        dataProductModel.setDataProductType(DataProductType.FILE);

        DataReplicaLocationModel replicaLocationModel = new DataReplicaLocationModel();
        replicaLocationModel.setStorageResourceId(storageResourceId);
        replicaLocationModel.setReplicaName((new File(filePath).getName()) + " gateway data store copy");
        replicaLocationModel.setReplicaLocationCategory(ReplicaLocationCategory.GATEWAY_DATA_STORE);
        replicaLocationModel.setReplicaPersistentType(ReplicaPersistentType.TRANSIENT);
        replicaLocationModel.setFilePath("file://" + storageResource.getHostName() + ":" + uploadFilePath);

        dataProductModel.setReplicaLocations(Collections.singletonList(replicaLocationModel));
        System.out.println("Registring " + uploadFilePath);
        return airavataClient.registerDataProduct(new AuthzToken(""), dataProductModel);
    }

    public void createExperimentDirectory(String user, String project, String experiment) throws AgentException {
        String experimentDirectory = getExperimentDirectory(user, project, experiment);
        storageAdaptor.createDirectory(experimentDirectory, true);
    }

    private String getExperimentDirectory(String user, String project, String experiment) {
        return gatewayStoragePreference
                .getFileSystemRootLocation()
                .concat(File.separator)
                .concat(user)
                .concat(File.separator)
                .concat(project)
                .concat(File.separator)
                .concat(experiment);
    }

    static String readFile(String path, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }
}
