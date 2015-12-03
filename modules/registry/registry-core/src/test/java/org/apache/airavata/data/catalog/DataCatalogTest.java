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
package org.apache.airavata.data.catalog;

import org.apache.airavata.data.catalog.util.Initialize;
import org.apache.airavata.model.data.resource.DataReplicaLocationModel;
import org.apache.airavata.model.data.resource.DataResourceModel;
import org.apache.airavata.registry.core.experiment.catalog.impl.RegistryFactory;
import org.apache.airavata.registry.cpi.DataCatalog;
import org.apache.airavata.registry.cpi.DataCatalogException;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DataCatalogTest {
    private final static Logger logger = LoggerFactory.getLogger(DataCatalogTest.class);
    private static Initialize initialize;
    private static DataCatalog datacatalog;
    private static DataResourceModel dataResourceModel;

    @BeforeClass
    public static void setUp() {
        try {
            System.out.println("********** SET UP ************");
            initialize = new Initialize("datacatalog-derby.sql");
            initialize.initializeDB();
            datacatalog = RegistryFactory.getDataCatalog();
            dataResourceModel = new DataResourceModel();
            dataResourceModel.setResourceName("test-file.txt");
            List<DataReplicaLocationModel> replicaLocationModelList = new ArrayList<>();
            DataReplicaLocationModel dataReplicaLocationModel = new DataReplicaLocationModel();
            dataReplicaLocationModel.setReplicaName("1-st-replica");
            ArrayList<String> dataLocations = new ArrayList<>();
            dataLocations.add("scp://g75.iu.xsede.org:/var/www/portal/experimentData/test-file.txt");
            dataReplicaLocationModel.setDataLocations(dataLocations);
            replicaLocationModelList.add(dataReplicaLocationModel);
            dataResourceModel.setReplicaLocations(replicaLocationModelList);
        } catch (DataCatalogException e) {
            logger.error(e.getMessage(), e);
        }
    }

    @AfterClass
    public static void tearDown() throws Exception {
        System.out.println("********** TEAR DOWN ************");
        initialize.stopDerbyServer();
    }

    @Test
    public void testPublishDataResource(){
        try {
            String resourceId = datacatalog.publishResource(dataResourceModel);
            org.junit.Assert.assertNotNull(resourceId);
        } catch (DataCatalogException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testRemoveDataResource(){
        try {
            boolean result = datacatalog.removeResource("234234234");
            Assert.assertFalse(result);
            String resourceId = datacatalog.publishResource(dataResourceModel);
            Assert.assertNotNull(resourceId);
            result = datacatalog.removeResource(resourceId);
            Assert.assertTrue(result);
            result = datacatalog.removeResource(resourceId);
            Assert.assertFalse(result);
        } catch (DataCatalogException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGetDataResource(){
        try {
            String resourceId = datacatalog.publishResource(dataResourceModel);
            Assert.assertNotNull(resourceId);
            DataResourceModel persistedCopy = datacatalog.getResource(resourceId);
            Assert.assertNotNull(persistedCopy);
            Assert.assertTrue(persistedCopy.getReplicaLocations().size()==1);
        } catch (DataCatalogException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testUpdateDataResource(){
        try {
            dataResourceModel.setResourceId(UUID.randomUUID().toString());
            boolean result = datacatalog.updateResource(dataResourceModel);
            Assert.assertFalse(result);
            datacatalog.publishResource(dataResourceModel);
            DataReplicaLocationModel dataReplicaLocationModel = new DataReplicaLocationModel();
            dataReplicaLocationModel.setReplicaName("2-nd-replica");
            ArrayList<String> dataLocations = new ArrayList<>();
            dataLocations.add("scp://g175.iu.xsede.org:/var/www/portal/experimentData/test-file.txt");
            dataResourceModel.getReplicaLocations().add(dataReplicaLocationModel);
            datacatalog.updateResource(dataResourceModel);
            dataResourceModel = datacatalog.getResource(dataResourceModel.getResourceId());
            Assert.assertTrue(dataResourceModel.getReplicaLocations().size()==2);
            dataResourceModel.getReplicaLocations().remove(1);
            datacatalog.updateResource(dataResourceModel);
            dataResourceModel = datacatalog.getResource(dataResourceModel.getResourceId());
            Assert.assertTrue(dataResourceModel.getReplicaLocations().size()==1);
        } catch (DataCatalogException e) {
            e.printStackTrace();
        }
    }
}