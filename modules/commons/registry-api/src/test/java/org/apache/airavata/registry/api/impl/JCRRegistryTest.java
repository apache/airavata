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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.airavata.common.utils.IOUtil;
import org.apache.airavata.commons.gfac.type.ApplicationDeploymentDescription;
import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.commons.gfac.type.ServiceDescription;
import org.apache.airavata.schemas.gfac.GlobusHostType;
import org.apache.airavata.schemas.gfac.InputParameterType;
import org.apache.airavata.schemas.gfac.OutputParameterType;
import org.apache.xmlbeans.XmlString;
import org.junit.After;
import org.junit.Test;

public class JCRRegistryTest {

	public static void main(String[] args) {
		new JCRRegistryTest().testSaveLoadHostDescription();
	}
	
    @Test
    public void testSaveLoadHostDescription() {
        try {
         Map<String,String> config = new HashMap<String,String>();
            config.put("org.apache.jackrabbit.repository.home","target" + File.separator + "jackrabbit4");
            AiravataJCRRegistry jcrRegistry = new AiravataJCRRegistry(null, "org.apache.jackrabbit.core.RepositoryFactoryImpl",
                    "admin", "admin", config);

            String hostId = "localhost";
            String address = "127.0.0.1";
            
            String hostId2 = "localhost2";

            /*
             * Host
             */
            HostDescription host = new HostDescription();
            host.getType().setHostName(hostId);
            host.getType().setHostAddress(address);

            jcrRegistry.saveHostDescription(host);

            HostDescription hostR = jcrRegistry.getHostDescription(hostId);

//            if (!(hostR.getType().getHostName().equals(hostId) && hostR.getType().getHostAddress().equals(address))) {
//                fail("Save and Load Host Description Fail with Different Value");
//            }
//            
            /*
             * Test for polymorphism
             */
            HostDescription globus = new HostDescription(GlobusHostType.type);
            globus.getType().setHostName(hostId2);
            globus.getType().setHostAddress(address);
            
            XmlString point = ((GlobusHostType)globus.getType()).addNewGridFTPEndPoint();
            point.setStringValue("xxxxxxx");           

            jcrRegistry.saveHostDescription(globus);

            HostDescription hg = jcrRegistry.getHostDescription(hostId2);

//            if (!(hg.getType().getHostName().equals(hostId2) && hg.getType().getHostAddress().equals(address))) {
//                fail("Save and Load Host Description Fail with Different Value");
//            }
            
//            if(!(hg.getType() instanceof GlobusHostType))
//                fail("Save and Load Host Type Fail with Different Type when loading");
            jcrRegistry.closeConnection();
            System.out.println((new File((new File(".")).getAbsolutePath() + File.separator + "target" + File.separator + "jackrabbit4")).getAbsolutePath());
            IOUtil.deleteDirectory(new File((new File(".")).getAbsolutePath() + File.separator + "target" + File.separator + "jackrabbit4"));

        } catch (Exception e) {
            e.printStackTrace();
//            fail(e.getMessage());
        }
    }

    @Test
    public void testSaveLoadServiceDescription() {
        try {
            Map<String,String> config = new HashMap<String,String>();
            config.put("org.apache.jackrabbit.repository.home","target" + File.separator + "jackrabbit4");
            AiravataJCRRegistry jcrRegistry = new AiravataJCRRegistry(null, "org.apache.jackrabbit.core.RepositoryFactoryImpl",
                    "admin", "admin", config);
            
            String serviceId = "SimpleEcho";            
            
            ServiceDescription serv = new ServiceDescription();
            serv.getType().setName(serviceId);

            InputParameterType input = InputParameterType.Factory.newInstance();
    		input.setParameterName("echo_input");
    		List<InputParameterType> inputList = new ArrayList<InputParameterType>();
    		inputList.add(input);
    		InputParameterType[] inputParamList = inputList.toArray(new InputParameterType[inputList
    				.size()]);

    		OutputParameterType output = OutputParameterType.Factory.newInstance();
    		output.setParameterName("echo_output");
    		List<OutputParameterType> outputList = new ArrayList<OutputParameterType>();
    		outputList.add(output);
    		OutputParameterType[] outputParamList = outputList
    				.toArray(new OutputParameterType[outputList.size()]);
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


            jcrRegistry.closeConnection();
            IOUtil.deleteDirectory(new File((new File(".")).getAbsolutePath() + File.separator + "target" + File.separator + "jackrabbit4"));
            
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    
    @Test
    public void testSaveLoadApplicationDescription() {
        try {
            Map<String,String> config = new HashMap<String,String>();
            config.put("org.apache.jackrabbit.repository.home","target" + File.separator + "jackrabbit5");
            AiravataJCRRegistry jcrRegistry = new AiravataJCRRegistry(null, "org.apache.jackrabbit.core.RepositoryFactoryImpl",
                    "admin", "admin", config);
            
            String hostId = "localhost";
            String address = "127.0.0.1";
            String serviceId = "SimpleEcho";            
            
            ServiceDescription serv = new ServiceDescription();
            serv.getType().setName(serviceId);

            InputParameterType input = InputParameterType.Factory.newInstance();
            input.setParameterName("echo_input");
            List<InputParameterType> inputList = new ArrayList<InputParameterType>();
            inputList.add(input);
            InputParameterType[] inputParamList = inputList.toArray(new InputParameterType[inputList
                    .size()]);

            OutputParameterType output = OutputParameterType.Factory.newInstance();
            output.setParameterName("echo_output");
            List<OutputParameterType> outputList = new ArrayList<OutputParameterType>();
            outputList.add(output);
            OutputParameterType[] outputParamList = outputList
                    .toArray(new OutputParameterType[outputList.size()]);
            serv.getType().setInputParametersArray(inputParamList);
            serv.getType().setOutputParametersArray(outputParamList);            
            
            
            /*
             * Host
             */
            HostDescription host = new HostDescription();
            host.getType().setHostName(hostId);
            host.getType().setHostAddress(address);

            /*
             * Save
             */
            jcrRegistry.saveHostDescription(host);
            jcrRegistry.saveServiceDescription(serv);
            
            
            ApplicationDeploymentDescription app = new ApplicationDeploymentDescription();
            app.getType().addNewApplicationName().setStringValue("ECHOLOCAL");
            app.getType().setExecutableLocation("/bin/echo");
            app.getType().setScratchWorkingDirectory("/tmp");
            
            jcrRegistry.deployServiceOnHost(serviceId, hostId);
            jcrRegistry.saveDeploymentDescription(serviceId, hostId, app);            
                        
            /*
             * Load
             */
            ApplicationDeploymentDescription appR = jcrRegistry.getDeploymentDescription(serviceId, hostId);
            
            if(appR == null){
                fail("Deployment is null");
            }
            
            if(appR.getType().getApplicationName() == null || !appR.getType().getApplicationName().getStringValue().equals("ECHOLOCAL")){
                fail("Wrong deployment name");
            }
            
            if(!appR.getType().getExecutableLocation().equals("/bin/echo") || !appR.getType().getScratchWorkingDirectory().equals("/tmp")){
                fail("Setting and Loading value fail");
            }                        
            
            jcrRegistry.closeConnection();
            IOUtil.deleteDirectory(new File((new File(".")).getAbsolutePath() + File.separator + "target" + File.separator + "jackrabbit5"));
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

}
