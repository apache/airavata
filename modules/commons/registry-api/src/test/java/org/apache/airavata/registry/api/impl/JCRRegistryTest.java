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

import java.util.ArrayList;
import java.util.List;

import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.commons.gfac.type.ServiceDescription;
import org.apache.airavata.schemas.gfac.GlobusHostType;
import org.apache.airavata.schemas.gfac.Parameter;
import org.junit.Test;

public class JCRRegistryTest {

    @Test
    public void testSaveLoadHostDescription() {
        try {
            /*
             * Create database
             */
            JCRRegistry jcrRegistry = new JCRRegistry(null, "org.apache.jackrabbit.core.RepositoryFactoryImpl",
                    "admin", "admin", null);

            String hostId = "localhost";
            String address = "127.0.0.1";
            
            String hostId2 = "localhost2";

            /*
             * Host
             */
            HostDescription host = new HostDescription();
            host.getType().setName(hostId);
            host.getType().setAddress(address);

            jcrRegistry.saveHostDescription(host);

            HostDescription hostR = jcrRegistry.getHostDescription(hostId);

            if (!(hostR.getType().getName().equals(hostId) && hostR.getType().getAddress().equals(address))) {
                fail("Save and Load Host Description Fail with Different Value");
            }
            
            /*
             * Test for polymorphism
             */
            HostDescription globus = new HostDescription(GlobusHostType.type);
            globus.getType().setName(hostId2);
            globus.getType().setAddress(address);
            
            ((GlobusHostType)globus.getType()).setGridFTPEndPoint("xxxxxxx");

            jcrRegistry.saveHostDescription(globus);

            HostDescription hg = jcrRegistry.getHostDescription(hostId2);

            if (!(hg.getType().getName().equals(hostId2) && hg.getType().getAddress().equals(address))) {
                fail("Save and Load Host Description Fail with Different Value");
            }
            
            if(!(hg.getType() instanceof GlobusHostType))
                fail("Save and Load Host Type Fail with Different Type when loading");

        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    public void testSaveLoadServiceDescription() {
        try {

            JCRRegistry jcrRegistry = new JCRRegistry(null, "org.apache.jackrabbit.core.RepositoryFactoryImpl",
                    "admin", "admin", null);
            
            String serviceId = "SimpleEcho";            
            
            ServiceDescription serv = new ServiceDescription();
            serv.getType().setName(serviceId);

            Parameter input = Parameter.Factory.newInstance();
    		input.setName("echo_input");
    		input.addNewType();
    		List<Parameter> inputList = new ArrayList<Parameter>();
    		inputList.add(input);
    		Parameter[] inputParamList = inputList.toArray(new Parameter[inputList
    				.size()]);

    		Parameter output = Parameter.Factory.newInstance();
    		output.setName("echo_output");
    		output.addNewType();
    		List<Parameter> outputList = new ArrayList<Parameter>();
    		outputList.add(output);
    		Parameter[] outputParamList = outputList
    				.toArray(new Parameter[outputList.size()]);
    		serv.getType().setInputParametersArray(inputParamList);
    		serv.getType().setOutputParametersArray(outputParamList);

            /*
             * Save to registry
             */
            jcrRegistry.saveServiceDescription(serv);
                        
            /*
             * Load
             */
            ServiceDescription service = jcrRegistry.getServiceDescription(serviceId);
            
            if(service == null){
                fail("Service is null");
            }
            
            if(service.getType().getInputParametersArray() == null || service.getType().getInputParametersArray().length != 1){
                fail("Input Parameters is missing");
            }
            
            if(service.getType().getOutputParametersArray()== null || service.getType().getOutputParametersArray().length != 1){
                fail("Input Parameters is missing");
            }
            
            
            
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
}
