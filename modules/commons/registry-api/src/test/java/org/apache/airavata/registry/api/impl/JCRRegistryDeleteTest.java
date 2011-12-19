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
package org.apache.airavata.registry.api.impl;

import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.jackrabbit.core.TransientRepository;
import org.junit.Assert;
import org.junit.Test;

import javax.jcr.Repository;

import static org.junit.Assert.fail;

public class JCRRegistryDeleteTest {
    @Test
       public void testJCRRegistryDelete() {
           try {
               /*
                * Create database
                */
               JCRRegistry jcrRegistry = new JCRRegistry(null, "org.apache.jackrabbit.core.RepositoryFactoryImpl",
                       "admin", "admin", null);

               String hostId = "localhost";
               String address = "127.0.0.1";
               /*
                * Host
                */
               HostDescription host = new HostDescription();
               host.getType().setHostName(hostId);
               host.getType().setHostAddress(address);

               jcrRegistry.saveHostDescription(host);

               jcrRegistry.deleteHostDescription(hostId);

               HostDescription hostR = jcrRegistry.getHostDescription(hostId);
               Assert.assertNull(hostR);
           } catch (Exception e) {
               e.printStackTrace();
               fail(e.getMessage());
           }
       }
}
