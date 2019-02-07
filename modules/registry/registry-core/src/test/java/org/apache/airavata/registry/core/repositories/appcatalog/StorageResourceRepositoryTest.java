/*
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
package org.apache.airavata.registry.core.repositories.appcatalog;

import org.apache.airavata.model.appcatalog.storageresource.StorageResourceDescription;
import org.apache.airavata.model.data.movement.DataMovementInterface;
import org.apache.airavata.model.data.movement.DataMovementProtocol;
import org.apache.airavata.model.data.movement.GridFTPDataMovement;
import org.apache.airavata.model.data.movement.SCPDataMovement;
import org.apache.airavata.model.data.movement.SecurityProtocol;
import org.apache.airavata.registry.core.repositories.common.TestBase;
import org.apache.airavata.registry.cpi.AppCatalogException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by skariyat on 3/13/18.
 */
public class StorageResourceRepositoryTest extends TestBase {

    private static final Logger logger = LoggerFactory.getLogger(StorageResourceRepository.class);

    private StorageResourceRepository storageResourceRepository;

    public StorageResourceRepositoryTest() {
        super(Database.APP_CATALOG);
        storageResourceRepository = new StorageResourceRepository();
    }

    @Test
    public void StorageResourceRepositoryTest() throws AppCatalogException {

        StorageResourceDescription description = new StorageResourceDescription();

        description.setHostName("localhost");
        description.setEnabled(true);
        description.setStorageResourceDescription("testDescription");


        String scpDataMoveId = addSCPDataMovement();
        System.out.println("**** SCP DataMoveId****** :" + scpDataMoveId);
        String gridFTPDataMoveId = addGridFTPDataMovement();
        System.out.println("**** grid FTP DataMoveId****** :" + gridFTPDataMoveId);

        List<DataMovementInterface> dataMovementInterfaces = new ArrayList<DataMovementInterface>();
        DataMovementInterface scpInterface = new DataMovementInterface();
        scpInterface.setDataMovementInterfaceId(scpDataMoveId);
        scpInterface.setDataMovementProtocol(DataMovementProtocol.SCP);
        scpInterface.setPriorityOrder(1);

        DataMovementInterface gridFTPMv = new DataMovementInterface();
        gridFTPMv.setDataMovementInterfaceId(gridFTPDataMoveId);
        gridFTPMv.setDataMovementProtocol(DataMovementProtocol.GridFTP);
        gridFTPMv.setPriorityOrder(2);

        dataMovementInterfaces.add(scpInterface);
        dataMovementInterfaces.add(gridFTPMv);
        description.setDataMovementInterfaces(dataMovementInterfaces);

        String resourceId = storageResourceRepository.addStorageResource(description);
        StorageResourceDescription storageResourceDescription = null;

        if (storageResourceRepository.isExists(resourceId)) {
            storageResourceDescription = storageResourceRepository.getStorageResource(resourceId);
            assertTrue(storageResourceDescription.getHostName().equals("localhost"));
            assertTrue(storageResourceDescription.getStorageResourceDescription().equals("testDescription"));
            List<DataMovementInterface> movementInterfaces = storageResourceDescription.getDataMovementInterfaces();
            if (movementInterfaces != null && !movementInterfaces.isEmpty()){
                for (DataMovementInterface dataMovementInterface : movementInterfaces){
                    System.out.println("Data Movement Interface Id :" + dataMovementInterface.getDataMovementInterfaceId());
                    System.out.println("Data Movement Protocol :" + dataMovementInterface.getDataMovementProtocol().toString());
                }
            }
        } else {
            fail("Created Storage Resource not found");
        }

        description.setHostName("localhost2");
        storageResourceRepository.updateStorageResource(resourceId, description);
        if (storageResourceRepository.isStorageResourceExists(resourceId)) {
            storageResourceDescription = storageResourceRepository.getStorageResource(resourceId);
            System.out.println("**********Updated Resource name ************* : " + storageResourceDescription.getHostName());
            assertTrue(storageResourceDescription.getHostName().equals("localhost2"));
        }
        assertTrue("Storage resource save successfully", storageResourceDescription != null);


    }

    public String addSCPDataMovement (){
        try {
            SCPDataMovement dataMovement = new SCPDataMovement();
            dataMovement.setSshPort(22);
            dataMovement.setSecurityProtocol(SecurityProtocol.SSH_KEYS);
            return new ComputeResourceRepository().addScpDataMovement(dataMovement);
        } catch (AppCatalogException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    public String addGridFTPDataMovement (){
        try {
            GridFTPDataMovement dataMovement = new GridFTPDataMovement();
            dataMovement.setSecurityProtocol(SecurityProtocol.SSH_KEYS);
            List<String> endPoints = new ArrayList<String>();
            endPoints.add("222.33.43.444");
            endPoints.add("23.344.44.454");
            dataMovement.setGridFTPEndPoints(endPoints);
            return new ComputeResourceRepository().addGridFTPDataMovement(dataMovement);
        } catch (AppCatalogException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

}
