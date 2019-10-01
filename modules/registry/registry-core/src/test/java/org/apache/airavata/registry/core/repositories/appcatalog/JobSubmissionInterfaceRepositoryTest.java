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

import org.apache.airavata.model.appcatalog.computeresource.BatchQueue;
import org.apache.airavata.model.appcatalog.computeresource.ComputeResourceDescription;
import org.apache.airavata.model.appcatalog.computeresource.JobSubmissionInterface;
import org.apache.airavata.model.appcatalog.computeresource.JobSubmissionProtocol;
import org.apache.airavata.registry.core.entities.appcatalog.JobSubmissionInterfacePK;
import org.apache.airavata.registry.core.repositories.common.TestBase;
import org.apache.airavata.registry.cpi.AppCatalogException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobSubmissionInterfaceRepositoryTest extends TestBase {
    private static final Logger logger = LoggerFactory.getLogger(ComputeResourceRepositoryTest.class);

    private JobSubmissionInterfaceRepository jobSubmissionInterfaceRepository;
    private ComputeResourceRepository computeResourceRepository;
    private String computeResourceId;

    public JobSubmissionInterfaceRepositoryTest() {
        super(Database.APP_CATALOG);
        jobSubmissionInterfaceRepository = new JobSubmissionInterfaceRepository();
        computeResourceRepository = new ComputeResourceRepository();
    }

    @Before
    public void createTestComputeResource() throws AppCatalogException {

        ComputeResourceDescription description = new ComputeResourceDescription();
        description.setHostName("localhost");
        description.addToBatchQueues(new BatchQueue("queue1"));
        description.addToBatchQueues(new BatchQueue("queue2"));
        computeResourceId = computeResourceRepository.addComputeResource(description);
    }

    @After
    public void removeTestComputeResource() throws AppCatalogException {

        computeResourceRepository.removeComputeResource(computeResourceId);
    }

    @Test
    public void testAddJobSubmissionInterface() throws AppCatalogException {

        JobSubmissionInterface jobSubmissionInterface = new JobSubmissionInterface();
        jobSubmissionInterface.setJobSubmissionInterfaceId("test");
        jobSubmissionInterface.setPriorityOrder(1);
        jobSubmissionInterface.setJobSubmissionProtocol(JobSubmissionProtocol.SSH);

        String jobSubmissionInterfaceId = jobSubmissionInterfaceRepository.addJobSubmission(computeResourceId, jobSubmissionInterface);

        JobSubmissionInterfacePK pk = new JobSubmissionInterfacePK();
        pk.setComputeResourceId(computeResourceId);
        pk.setJobSubmissionInterfaceId(jobSubmissionInterfaceId);

        JobSubmissionInterface retrievedJobSubmissionInterface = jobSubmissionInterfaceRepository.get(pk);
        Assert.assertEquals("test", retrievedJobSubmissionInterface.getJobSubmissionInterfaceId());
        Assert.assertEquals(1, retrievedJobSubmissionInterface.getPriorityOrder());
        Assert.assertEquals(JobSubmissionProtocol.SSH, retrievedJobSubmissionInterface.getJobSubmissionProtocol());


        jobSubmissionInterfaceRepository.delete(pk);
    }
}
