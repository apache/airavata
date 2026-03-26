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
package org.apache.airavata.service.dataproduct;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;
import org.apache.airavata.model.data.replica.DataProductModel;
import org.apache.airavata.model.data.replica.DataReplicaLocationModel;
import org.apache.airavata.registry.api.service.handler.RegistryServerHandler;
import org.apache.airavata.service.context.RequestContext;
import org.apache.airavata.service.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DataProductServiceTest {

    @Mock
    RegistryServerHandler registryHandler;

    DataProductService dataProductService;
    RequestContext ctx;

    @BeforeEach
    void setUp() {
        dataProductService = new DataProductService(registryHandler);
        ctx = new RequestContext(
                "testUser", "testGateway", "token123", Map.of("userName", "testUser", "gatewayId", "testGateway"));
    }

    @Test
    void registerDataProduct_returnsProductId() throws Exception {
        DataProductModel model = new DataProductModel();
        model.setProductName("test-product");
        when(registryHandler.registerDataProduct(model)).thenReturn("product-123");

        String result = dataProductService.registerDataProduct(ctx, model);

        assertEquals("product-123", result);
        verify(registryHandler).registerDataProduct(model);
    }

    @Test
    void getDataProduct_returnsModel() throws Exception {
        DataProductModel model = new DataProductModel();
        model.setProductName("test-product");
        when(registryHandler.getDataProduct("uri-123")).thenReturn(model);

        DataProductModel result = dataProductService.getDataProduct(ctx, "uri-123");

        assertNotNull(result);
        assertEquals("test-product", result.getProductName());
    }

    @Test
    void registerReplicaLocation_returnsReplicaId() throws Exception {
        DataReplicaLocationModel replicaModel = new DataReplicaLocationModel();
        replicaModel.setReplicaName("replica-1");
        when(registryHandler.registerReplicaLocation(replicaModel)).thenReturn("replica-123");

        String result = dataProductService.registerReplicaLocation(ctx, replicaModel);

        assertEquals("replica-123", result);
        verify(registryHandler).registerReplicaLocation(replicaModel);
    }

    @Test
    void getParentDataProduct_returnsParent() throws Exception {
        DataProductModel parent = new DataProductModel();
        parent.setProductName("parent-product");
        when(registryHandler.getParentDataProduct("child-uri")).thenReturn(parent);

        DataProductModel result = dataProductService.getParentDataProduct(ctx, "child-uri");

        assertNotNull(result);
        assertEquals("parent-product", result.getProductName());
    }

    @Test
    void getChildDataProducts_returnsList() throws Exception {
        DataProductModel child1 = new DataProductModel();
        DataProductModel child2 = new DataProductModel();
        when(registryHandler.getChildDataProducts("parent-uri")).thenReturn(List.of(child1, child2));

        List<DataProductModel> result = dataProductService.getChildDataProducts(ctx, "parent-uri");

        assertEquals(2, result.size());
        verify(registryHandler).getChildDataProducts("parent-uri");
    }

    @Test
    void registerDataProduct_wrapsRegistryException() throws Exception {
        DataProductModel model = new DataProductModel();
        model.setProductName("bad-product");
        when(registryHandler.registerDataProduct(model)).thenThrow(new RuntimeException("DB error"));

        assertThrows(ServiceException.class, () -> dataProductService.registerDataProduct(ctx, model));
    }
}
