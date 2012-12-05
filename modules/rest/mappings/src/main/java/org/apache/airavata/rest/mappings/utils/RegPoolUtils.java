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

package org.apache.airavata.rest.mappings.utils;

import org.apache.airavata.common.context.WorkflowContext;
import org.apache.airavata.common.exception.AiravataConfigurationException;
import org.apache.airavata.registry.api.AiravataRegistry2;
import org.apache.airavata.registry.api.AiravataRegistryFactory;
import org.apache.airavata.registry.api.AiravataUser;
import org.apache.airavata.registry.api.Gateway;
import org.apache.airavata.registry.api.exception.RegistryAccessorInstantiateException;
import org.apache.airavata.registry.api.exception.RegistryAccessorInvalidException;
import org.apache.airavata.registry.api.exception.RegistryAccessorNotFoundException;
import org.apache.airavata.registry.api.exception.RegistryAccessorUndefinedException;

import javax.servlet.ServletContext;
import java.util.Map;


public class RegPoolUtils {

    public static AiravataRegistry2 acquireRegistry(ServletContext context) {
        AiravataRegistry2 airavataRegistry=null;
        String user =  WorkflowContext.getRequestUser();
        String gatewayId = WorkflowContext.getGatewayId();
        Gateway gateway = new Gateway(gatewayId);
        AiravataUser airavataUser = new AiravataUser(user);

        RegistryInstancesPool registryInstancesPool =
                (RegistryInstancesPool) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY_POOL);
        Map<RegIdentifier,AiravataRegistry2> registryMap = registryInstancesPool.getRegistryInstancesList();
        boolean foundReg=false;
        try{
            while(!foundReg){
                synchronized (registryMap){
                    RegIdentifier identifier = new RegIdentifier(user, gateway.getGatewayName());
                    if (registryMap.size()==0){
                        registryMap.put(identifier,
                                AiravataRegistryFactory.getRegistry(gateway, airavataUser));
                    }else {
                        airavataRegistry = registryMap.get(identifier);
                        if (airavataRegistry == null){
                            registryMap.put(identifier,
                                    AiravataRegistryFactory.getRegistry(gateway, airavataUser));
                        }
                    }
                    airavataRegistry=registryMap.get(identifier);
                    registryMap.remove(identifier);
                    foundReg=true;
                }
            }
        }catch (RegistryAccessorInvalidException e) {
            e.printStackTrace();
        } catch (RegistryAccessorInstantiateException e) {
            e.printStackTrace();
        } catch (RegistryAccessorUndefinedException e) {
            e.printStackTrace();
        } catch (RegistryAccessorNotFoundException e) {
            e.printStackTrace();
        } catch (AiravataConfigurationException e) {
            e.printStackTrace();
        }
        return airavataRegistry;
    }

    public static void releaseRegistry(ServletContext context, AiravataRegistry2 airavataRegistry) {
        RegistryInstancesPool registryInstancesPool =
                (RegistryInstancesPool)context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY_POOL);
        Map<RegIdentifier, AiravataRegistry2> registryInstancesList =
                registryInstancesPool.getRegistryInstancesList();
        synchronized (registryInstancesList){
            RegIdentifier regIdentifier =
                    new RegIdentifier(airavataRegistry.getUser().getUserName(),
                            airavataRegistry.getGateway().getGatewayName());
            registryInstancesList.put(regIdentifier, airavataRegistry);
        }
    }

}
