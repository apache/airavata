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

import static org.junit.Assert.fail;

import org.apache.airavata.registry.api.impl.JCRRegistry;
import org.apache.airavata.schemas.gfac.HostDescriptionDocument;
import org.apache.airavata.schemas.gfac.HostDescriptionType;
import org.junit.Test;

public class JCRRegistryTest {

    @Test
    public void testSaveLoadHostDescription() {
        try {
        	/*
             * Create database
             */
            JCRRegistry jcrRegistry = new JCRRegistry(null, "org.apache.jackrabbit.core.RepositoryFactoryImpl", "admin",
                    "admin", null);

            String hostId = "localhost";
            String address = "127.0.0.1";
            
            /*
             * Host
             */
            HostDescriptionDocument doc = HostDescriptionDocument.Factory.newInstance();
            HostDescriptionType host = doc.addNewHostDescription();
            host.setName(hostId);
            host.setAddress(address);
            
            jcrRegistry.saveHostDescription(doc);
            
            
            HostDescriptionDocument docR = jcrRegistry.getHostDescription(hostId);
            
            if(!(docR.getHostDescription().getName().equals(hostId) && docR.getHostDescription().getAddress().equals(address))){
            	fail("Save and Load Host Description Fail with Different Value");	
            }
          
        }catch(Exception e){
        	fail(e.getMessage());
        }
    }
}
