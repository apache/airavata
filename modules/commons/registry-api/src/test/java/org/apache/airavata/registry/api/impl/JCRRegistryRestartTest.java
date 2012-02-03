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

import org.apache.airavata.common.utils.IOUtil;
import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.schemas.gfac.GlobusHostType;
import org.apache.jackrabbit.core.RepositoryFactoryImpl;
import org.apache.jackrabbit.core.TransientRepository;
import org.apache.xmlbeans.XmlString;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import javax.jcr.Repository;
import javax.jcr.RepositoryFactory;
import javax.jcr.Session;

import java.io.File;

import static org.junit.Assert.fail;

public class JCRRegistryRestartTest {
    @Test
       public void testJCRRegistryRestart() {
           try {
               /*
                * Create database
                */
               AiravataJCRRegistry jcrRegistry = new AiravataJCRRegistry(null, "org.apache.jackrabbit.core.RepositoryFactoryImpl",
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

               Repository repository = jcrRegistry.getRepository();
               ((TransientRepository)repository).shutdown();

               Session session = jcrRegistry.getSession();
               if (session!=null){
	               HostDescription hostR = jcrRegistry.getHostDescription(hostId);
	               Assert.assertNotNull(hostR);
               }else{
            	   Assert.assertTrue(true);
               }
           } catch (Exception e) {
               e.printStackTrace();
               fail(e.getMessage());
           }
       }
      @After
    public void cleanup(){
        File jackrabbit = new File(".");
           String s = jackrabbit.getAbsolutePath() + File.separator +
                   "modules" + File.separator + "registry-api" + File.separator +"jackrabbit";
           IOUtil.deleteDirectory(new File(s));
    }

}
