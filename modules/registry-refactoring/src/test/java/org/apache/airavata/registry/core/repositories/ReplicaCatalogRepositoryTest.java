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
package org.apache.airavata.registry.core.repositories;

//import org.apache.airavata.registry.core.entities.workspacecatalog.GatewayEntity;
//import org.apache.airavata.registry.core.entities.workspacecatalog.UserProfileEntity;
//import org.apache.airavata.registry.core.repositories.replicacatalog.DataProductRepository;
//import org.apache.airavata.registry.core.repositories.workspacecatalog.GatewayRepository;
//import org.apache.airavata.registry.core.repositories.workspacecatalog.UserProfileRepository;

public class ReplicaCatalogRepositoryTest {

//    private GatewayRepository gatewayRepository;
//    private UserProfileRepository userProfileRepository;
//    private String gatewayId;
//    private String userId;
//    private String dataProductUri;
//
//    private final String GATEWAY_DOMAIN = "test1.com";
//    private final String DATA_PRODUCT_DESCRIPTION = "testDesc";
//
//
//    @Before
//    public void setupRepository()   {
//
//        gatewayRepository = new GatewayRepository(Gateway.class, GatewayEntity.class);
//        userProfileRepository = new UserProfileRepository(UserProfile.class, UserProfileEntity.class);
//
//
//        gatewayId = "test.com" + System.currentTimeMillis();
//        userId = "testuser" + System.currentTimeMillis();
//        dataProductUri = "uri" + System.currentTimeMillis();
//
//    }
//    @Test
//    public void dataProductRepositoryTest() {
//
//        DataProductRepository dataProductRepository = new DataProductRepository(DataProductModel.class, DataProductEntity.class);
//
//        /*
//         * Creating Gateway required for UserProfile & Project creation
//		 */
//        Gateway gateway = new Gateway();
//        gateway.setGatewayApprovalStatus(GatewayApprovalStatus.ACTIVE);
//        gateway.setGatewayId(gatewayId);
//        gateway.setDomain(GATEWAY_DOMAIN);
//        gateway = gatewayRepository.create(gateway);
//        Assert.assertTrue(!gateway.getGatewayId().isEmpty());
//
//		/*
//         * UserProfile Instance creation required for Project Creation
//		 */
//        UserProfile userProfile = new UserProfile();
//        userProfile.setAiravataInternalUserId(userId);
//        userProfile.setGatewayId(gateway.getGatewayId());
//        userProfile = userProfileRepository.create(userProfile);
//        Assert.assertTrue(!userProfile.getAiravataInternalUserId().isEmpty());
//
//        /*
//         * DataProduct Instance creation
//         */
//        DataProductModel dataProduct = new DataProductModel();
//        dataProduct.setProductUri(dataProductUri);
//        dataProduct.setGatewayId(gatewayId);
//        dataProduct.setOwnerName(gatewayId);
//        dataProduct.setProductName("Product1234");
//
//
//        /*
//         * Data Product Repository Insert Operation Test
//		 */
//        dataProduct = dataProductRepository.create(dataProduct);
//        Assert.assertTrue(!dataProduct.getProductUri().isEmpty());
//
//
//
//        /*
//         * DataProduct Repository Update Operation Test
//		 */
//        dataProduct.setProductDescription(DATA_PRODUCT_DESCRIPTION);
//        dataProductRepository.update(dataProduct);
//        dataProduct = dataProductRepository.get(dataProduct.getProductUri());
//        Assert.assertEquals(dataProduct.getProductDescription(), DATA_PRODUCT_DESCRIPTION);
//
//		/*
//         * Data Product Repository Select Operation Test
//		 */
//        dataProduct = dataProductRepository.get(dataProductUri);
//        Assert.assertNotNull(dataProduct);
//
//		/*
//         * Workspace Project Repository Delete Operation
//		 */
//        boolean deleteResult = dataProductRepository.delete(dataProductUri);
//        Assert.assertTrue(deleteResult);
//
//        deleteResult = userProfileRepository.delete(userId);
//        Assert.assertTrue(deleteResult);
//
//        deleteResult = gatewayRepository.delete(gatewayId);
//        Assert.assertTrue(deleteResult);

//    }
}
