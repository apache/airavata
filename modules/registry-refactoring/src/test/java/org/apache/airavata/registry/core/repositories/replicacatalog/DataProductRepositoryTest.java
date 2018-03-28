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
package org.apache.airavata.registry.core.repositories.replicacatalog;

import org.apache.airavata.model.data.replica.DataProductModel;
import org.apache.airavata.model.data.replica.DataProductType;
import org.apache.airavata.registry.core.repositories.util.Initialize;
import org.apache.airavata.registry.cpi.ReplicaCatalogException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class DataProductRepositoryTest {

    private static Initialize initialize;
    private DataProductRepository dataProductRepository;
    private String gatewayId = "testGateway";
    private String userId = "testUser";
    private String productName = "testProduct";
    private static final Logger logger = LoggerFactory.getLogger(DataProductRepositoryTest.class);

    @Before
    public void setUp() {
        try {
            initialize = new Initialize("replicacatalog-derby.sql");
            initialize.initializeDB();
            dataProductRepository = new DataProductRepository();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw e;
        }
    }

    @After
    public void tearDown() throws Exception {
        System.out.println("********** TEAR DOWN ************");
        initialize.stopDerbyServer();
    }

    @Test
    public void DataProductRepositoryTest() throws ReplicaCatalogException {
        DataProductModel testDataProductModel1 = new DataProductModel();
        testDataProductModel1.setGatewayId(gatewayId);
        testDataProductModel1.setOwnerName(userId);
        testDataProductModel1.setDataProductType(DataProductType.COLLECTION);
        testDataProductModel1.setProductName(productName);

        String productUri1 = dataProductRepository.registerDataProduct(testDataProductModel1);
        assertTrue(dataProductRepository.isDataProductExists(productUri1));

        DataProductModel retrievedDataProductModel = dataProductRepository.getDataProduct(productUri1);
        assertEquals(retrievedDataProductModel.getProductUri(), productUri1);

        DataProductModel testDataProductModel2 = new DataProductModel();
        testDataProductModel2.setGatewayId(gatewayId);
        testDataProductModel2.setOwnerName(userId);
        testDataProductModel2.setDataProductType(DataProductType.FILE);
        testDataProductModel2.setProductName(productName);

        String productUri2 = dataProductRepository.registerDataProduct(testDataProductModel2);
        assertTrue(dataProductRepository.isDataProductExists(productUri2));

        testDataProductModel2.setParentProductUri(productUri1);
        dataProductRepository.updateDataProduct(testDataProductModel2);

        DataProductModel retrievedParentDataProductModel = dataProductRepository.getParentDataProduct(productUri2);
        assertEquals(retrievedParentDataProductModel.getProductUri(), productUri1);

        List<DataProductModel> childDataProductList = dataProductRepository.getChildDataProducts(productUri1);
        assertTrue(childDataProductList.size() == 1);

        List<DataProductModel> dataProductModelList = dataProductRepository.searchDataProductsByName(gatewayId, userId, productName, -1, 0);
        assertTrue(dataProductModelList.size() == 2);

        dataProductRepository.removeDataProduct(productUri1);
        assertFalse(dataProductRepository.isDataProductExists(productUri1));

        dataProductRepository.removeDataProduct(productUri2);
    }

}
