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
package org.apache.airavata.client.samples;

import org.apache.airavata.api.Airavata;
import org.apache.airavata.api.client.AiravataClientFactory;
import org.apache.airavata.model.data.replica.*;
import org.apache.airavata.model.security.AuthzToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReplicaCatalogSample {
    private final static Logger logger = LoggerFactory.getLogger(ReplicaCatalogSample.class);

    public static final String THRIFT_SERVER_HOST = "gw56.iu.xsede.org";
    public static final int THRIFT_SERVER_PORT = 8930;
    private static final String USER_NAME = "master";
    private static final String DEFAULT_GATEWAY = "default";
    private static final String STORAGE_RESOURCE_ID = "gw75.iu.xsede.org_3e40e62b-be11-4590-bf24-b1b6796c3572";
    private static final AuthzToken authzToken = new AuthzToken("empty-token");
    private static Airavata.Client client;

    public static void main(String[] args) {
        try {
            client = AiravataClientFactory.createAiravataClient(THRIFT_SERVER_HOST, THRIFT_SERVER_PORT);
            System.out.println(client.getAPIVersion());

            DataProductModel dataProductModel = new DataProductModel();
            dataProductModel.setGatewayId(DEFAULT_GATEWAY);
            dataProductModel.setOwnerName(USER_NAME);
            dataProductModel.setProductName("test-1");
            dataProductModel.setDataProductType(DataProductType.FILE);

            DataReplicaLocationModel replicaLocationModel = new DataReplicaLocationModel();
            replicaLocationModel.setStorageResourceId(STORAGE_RESOURCE_ID);
            replicaLocationModel.setReplicaName("test-1-replica-1");
            replicaLocationModel.setReplicaLocationCategory(ReplicaLocationCategory.GATEWAY_DATA_STORE);
            replicaLocationModel.setReplicaPersistentType(ReplicaPersistentType.PERSISTENT);
            replicaLocationModel.setFilePath("/var/www/portals/gateway-user-data/testdrive/test.txt");

            dataProductModel.addToReplicaLocations(replicaLocationModel);

            String productUri = client.registerDataProduct(authzToken, dataProductModel);
            System.out.println(productUri);


            dataProductModel = client.getDataProduct(authzToken, "airavata-dp://Eroma2016@seagrid:/");
            System.out.println(dataProductModel.getReplicaLocations().size());
        } catch (Exception e) {
            logger.error("Error while connecting with server", e.getMessage());
            e.printStackTrace();
        }
    }
}
