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

import org.apache.airavata.registry.core.repositories.util.Initialize;
import org.apache.airavata.registry.cpi.ReplicaCatalogException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class DataReplicaLocationRepositoryTest {

    private static Initialize initialize;
    private DataReplicaLocationRepository dataReplicaLocationRepository;
    private String gatewayId = "testGateway";
    private static final Logger logger = LoggerFactory.getLogger(DataReplicaLocationRepositoryTest.class);

    @Before
    public void setUp() {
        try {
            initialize = new Initialize("replicacatalog-derby.sql");
            initialize.initializeDB();
            dataReplicaLocationRepository = new DataReplicaLocationRepository();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @After
    public void tearDown() throws Exception {
        System.out.println("********** TEAR DOWN ************");
        initialize.stopDerbyServer();
    }

    @Test
    public void DataProductRepositoryTest() throws ReplicaCatalogException {

    }

}
