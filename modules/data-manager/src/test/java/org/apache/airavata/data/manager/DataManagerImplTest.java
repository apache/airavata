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
package org.apache.airavata.data.manager;

import org.apache.airavata.data.manager.utils.DataCatInit;
import org.apache.airavata.data.manager.utils.ssh.SSHKeyAuthentication;
import org.apache.airavata.model.data.resource.DataReplicaLocationModel;
import org.apache.airavata.model.data.resource.DataResourceModel;
import org.apache.airavata.registry.core.experiment.catalog.impl.RegistryFactory;
import org.apache.airavata.registry.cpi.DataCatalog;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DataManagerImplTest {
    private final static Logger logger = LoggerFactory.getLogger(DataManagerImplTest.class);
    private static DataCatInit dataCatInit;
    private static DataManager dataManager;
    private static DataResourceModel dataResourceModel;
    private static DataReplicaLocationModel dataReplicaLocationModel;

    @BeforeClass
    public static void setUp() {
        try {
            System.out.println("********** SET UP ************");
            dataCatInit = new DataCatInit("datacatalog-derby.sql");
            dataCatInit.initializeDB();
            DataCatalog dataCatalog = RegistryFactory.getDataCatalog();
            SSHKeyAuthentication sshKeyAuthentication = new SSHKeyAuthentication();
            ClassLoader classLoader = DataManagerImplTest.class.getClassLoader();
            File privateKey = new File(classLoader.getResource("id_rsa").getFile());
            File publicKey = new File(classLoader.getResource("id_rsa.pub").getFile());
            File knownHosts = new File(classLoader.getResource("known_hosts").getFile());
            sshKeyAuthentication.setUserName("airavata");
            sshKeyAuthentication.setPrivateKeyFilePath(privateKey.getAbsolutePath());
            sshKeyAuthentication.setPublicKeyFilePath(publicKey.getAbsolutePath());
            sshKeyAuthentication.setKnownHostsFilePath(knownHosts.getAbsolutePath());
            sshKeyAuthentication.setPassphrase("airavata_2015");
            sshKeyAuthentication.setStrictHostKeyChecking("no");

//            sshKeyAuthentication.setUserName("pga");
//            sshKeyAuthentication.setKnownHostsFilePath("/Users/supun/.ssh/known_hosts");
//            sshKeyAuthentication.setPublicKeyFilePath("/Users/supun/.ssh/id_rsa.pub");
//            sshKeyAuthentication.setPrivateKeyFilePath("/Users/supun/.ssh/id_rsa");
//            sshKeyAuthentication.setPassphrase("");

            dataManager = new DataManagerImpl(dataCatalog, sshKeyAuthentication);
            dataResourceModel = new DataResourceModel();
            dataResourceModel.setResourceName("test-file.txt");
            dataReplicaLocationModel = new DataReplicaLocationModel();
            dataReplicaLocationModel.setReplicaName("1-st-replica");
            ArrayList<String> dataLocations = new ArrayList<>();
            dataLocations.add("scp://g75.iu.xsede.org:/var/www/portal/experimentData/test-file.txt");
            dataReplicaLocationModel.setDataLocations(dataLocations);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @AfterClass
    public static void tearDown() throws Exception {
        System.out.println("********** TEAR DOWN ************");
        dataCatInit.stopDerbyServer();
    }

    @Test
    public void testPublishDataResource(){
        try {
            String resourceId = dataManager.publishResource(dataResourceModel);
            org.junit.Assert.assertNotNull(resourceId);
        } catch (DataManagerException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testRemoveDataResource(){
        try {
            boolean result = dataManager.removeResource("234234234");
            Assert.assertFalse(result);
            String resourceId = dataManager.publishResource(dataResourceModel);
            Assert.assertNotNull(resourceId);
            result = dataManager.removeResource(resourceId);
            Assert.assertTrue(result);
            result = dataManager.removeResource(resourceId);
            Assert.assertFalse(result);
        } catch (DataManagerException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testGetDataResource(){
        try {
            String resourceId = dataManager.publishResource(dataResourceModel);
            Assert.assertNotNull(resourceId);
            DataResourceModel persistedCopy = dataManager.getResource(resourceId);
            Assert.assertNotNull(persistedCopy);
        } catch (DataManagerException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testUpdateDataResource(){
        try {
            dataResourceModel.setResourceId(UUID.randomUUID().toString());
            boolean result = dataManager.updateResource(dataResourceModel);
            Assert.assertFalse(result);
            dataManager.publishResource(dataResourceModel);
            dataResourceModel.setResourceName("updated-name");
            dataManager.updateResource(dataResourceModel);
            dataResourceModel = dataManager.getResource(dataResourceModel.getResourceId());
            Assert.assertTrue(dataResourceModel.getResourceName().equals("updated-name"));
        } catch (DataManagerException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testPublishReplicaLocation(){
        try {
            String resourceId = dataManager.publishResource(dataResourceModel);
            dataReplicaLocationModel.setResourceId(resourceId);
            String replicaId = dataManager.publishReplicaLocation(dataReplicaLocationModel);
            org.junit.Assert.assertNotNull(replicaId);
        } catch (DataManagerException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testRemoveReplicaLocation(){
        try {
            String resourceId = dataManager.publishResource(dataResourceModel);
            dataReplicaLocationModel.setResourceId(resourceId);
            String replicaId = dataManager.publishReplicaLocation(dataReplicaLocationModel);
            boolean result = dataManager.removeReplicaLocation(replicaId);
            Assert.assertTrue(result);
            result = dataManager.removeReplicaLocation(dataReplicaLocationModel.getReplicaId());
            Assert.assertFalse(result);
        } catch (DataManagerException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testGetReplicaLocation(){
        try {
            String resourceId = dataManager.publishResource(dataResourceModel);
            dataReplicaLocationModel.setResourceId(resourceId);
            String replicaId = dataManager.publishReplicaLocation(dataReplicaLocationModel);
            DataReplicaLocationModel persistedCopy = dataManager.getReplicaLocation(replicaId);
            Assert.assertNotNull(persistedCopy);
        } catch (DataManagerException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testUpdateReplicaLocation(){
        try {
            String resourceId = dataManager.publishResource(dataResourceModel);
            dataReplicaLocationModel.setResourceId(resourceId);
            String replicaId = dataManager.publishReplicaLocation(dataReplicaLocationModel);
            DataReplicaLocationModel persistedCopy = dataManager.getReplicaLocation(replicaId);
            persistedCopy.setReplicaDescription("updated-description");
            dataManager.updateReplicaLocation(persistedCopy);
            persistedCopy = dataManager.getReplicaLocation(replicaId);
            Assert.assertTrue(persistedCopy.getReplicaDescription().equals("updated-description"));
        } catch (DataManagerException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testGetAllReplicaLocations(){
        try {
            String resourceId = dataManager.publishResource(dataResourceModel);
            dataReplicaLocationModel.setResourceId(resourceId);
            String replicaId = dataManager.publishReplicaLocation(dataReplicaLocationModel);
            List<DataReplicaLocationModel> replicaLocationModelList = dataManager.getAllReplicaLocations(resourceId);
            Assert.assertTrue(replicaLocationModelList.get(0).getReplicaId().equals(replicaId));
        } catch (DataManagerException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testCopyResource(){
        try {
            String resourceId = dataManager.publishResource(dataResourceModel);
            File temp = File.createTempFile("temp-file-1", ".tmp");
            dataReplicaLocationModel.setResourceId(resourceId);
            ArrayList<String> dataLocations = new ArrayList<>();
            dataLocations.add(DataManagerConstants.LOCAL_URI_SCHEME+"://"+temp.getAbsolutePath());
            dataReplicaLocationModel.setDataLocations(dataLocations);
            String replicaId = dataManager.publishReplicaLocation(dataReplicaLocationModel);
            String destPath = DataManagerConstants.LOCAL_URI_SCHEME+"://" + System.getProperty("java.io.tmpdir")
                    + File.separator + "temp-file-2";
            dataManager.copyResource(resourceId,replicaId, destPath);
            File newFile = new File((new URI(destPath)).getPath());
            Assert.assertTrue(newFile.exists());
            DataReplicaLocationModel newReplicaLocation = new DataReplicaLocationModel();
            newReplicaLocation.setReplicaName("new replica location");
            ArrayList<String> newDataLocations = new ArrayList<>();
            newDataLocations.add(DataManagerConstants.LOCAL_URI_SCHEME+"://"+newFile.getAbsolutePath());
            newReplicaLocation.setDataLocations(newDataLocations);
            newReplicaLocation.setResourceId(resourceId);
            dataManager.publishReplicaLocation(newReplicaLocation);
            List<DataReplicaLocationModel> replicaLocationModelList = dataManager.getAllReplicaLocations(resourceId);
            Assert.assertTrue(replicaLocationModelList.size()==2);

//            String scpDestLocation = DataManagerConstants.SCP_URI_SCHEME+"://gw75.iu.xsede.org:/var/www/portal" +
//                    "/experimentData/scnakandala/temp-file";
//            boolean result = dataManager.copyResource(resourceId, replicaId, scpDestLocation);
//            Assert.assertTrue(result);
//            newDataLocations = new ArrayList<>();
//            newDataLocations.add(scpDestLocation);
//            newReplicaLocation.setDataLocations(newDataLocations);
//            newReplicaLocation.setResourceId(resourceId);
//            replicaId = dataManager.publishReplicaLocation(newReplicaLocation);
//            String scpDestLocationNew = DataManagerConstants.SCP_URI_SCHEME+"://gw75.iu.xsede.org:22/var/www/portal" +
//                    "/experimentData/scnakandala/temp-file-new";
//            result = dataManager.copyResource(resourceId, replicaId, scpDestLocationNew);
//            Assert.assertTrue(result);
//            String localDestLocation = DataManagerConstants.LOCAL_URI_SCHEME+"://" + System.getProperty("java.io.tmpdir")
//                    + File.separator + "temp-file-sup" + System.currentTimeMillis();
//            result = dataManager.copyResource(resourceId, replicaId, localDestLocation);
//            Assert.assertTrue(result);
//            Assert.assertTrue((new File((new URI(localDestLocation)).getPath())).exists());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }
}