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

package org.apache.airavata.experiment.catalog;

import junit.framework.Assert;
import org.apache.airavata.registry.core.experiment.catalog.ExperimentCatResource;
import org.apache.airavata.registry.core.experiment.catalog.ResourceType;
import org.apache.airavata.registry.core.experiment.catalog.resources.QueueStatusResource;
import org.apache.airavata.registry.cpi.RegistryException;
import org.junit.Test;

import java.util.List;

public class QueueStatusResourceTest extends AbstractResourceTest {

    @Test
    public void test(){
        QueueStatusResource queueStatusResource1 = new QueueStatusResource();
        queueStatusResource1.setHostName("bigred2.uits.iu.edu");
        queueStatusResource1.setQueueName("cpu");
        queueStatusResource1.setTime((long) 1 + System.currentTimeMillis());
        queueStatusResource1.setQueueUp(true);
        queueStatusResource1.setRunningJobs(3);
        queueStatusResource1.setQueuedJobs(4);
        try {
            queueStatusResource1.save();
        } catch (RegistryException e) {
            e.printStackTrace();
            Assert.fail();
        }

        QueueStatusResource queueStatusResource2 = new QueueStatusResource();
        queueStatusResource2.setHostName("bigred2.uits.iu.edu");
        queueStatusResource2.setQueueName("cpu");
        queueStatusResource2.setTime((long) 2 + System.currentTimeMillis());
        queueStatusResource2.setQueueUp(true);
        queueStatusResource2.setRunningJobs(33);
        queueStatusResource2.setQueuedJobs(44);
        try {
            queueStatusResource2.save();
        } catch (RegistryException e) {
            e.printStackTrace();
            Assert.fail();
        }

        try {
            List<ExperimentCatResource> experimentCatResources = queueStatusResource1.get(ResourceType.QUEUE_STATUS);
            Assert.assertTrue(experimentCatResources.size()==1);
            QueueStatusResource queueStatusResource = (QueueStatusResource) experimentCatResources.get(0);
            Assert.assertEquals(queueStatusResource2.getTime(), queueStatusResource.getTime());
        } catch (RegistryException e) {
            e.printStackTrace();
            Assert.fail();
        }

    }
}
