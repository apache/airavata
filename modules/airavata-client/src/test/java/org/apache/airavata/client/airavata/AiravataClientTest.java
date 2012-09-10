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
package org.apache.airavata.client.airavata;

import org.apache.airavata.common.registry.api.exception.RegistryException;
import org.apache.airavata.common.registry.api.impl.JCRRegistry;
import org.apache.airavata.xbaya.interpretor.NameValue;
import org.apache.jackrabbit.core.RepositoryCopier;
import org.apache.jackrabbit.core.config.RepositoryConfig;
import org.junit.Test;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class AiravataClientTest {

    @Test
	public void testInvokeWorkflowString() {
//        try {
//            AiravataClient airavataClient = new AiravataClient("xbaya.properties");
//            List<String> workflowTemplateIds = airavataClient.getWorkflowTemplateIds();
//            for(String eachId:workflowTemplateIds){
//                List<WorkflowInput> workflowInputs = airavataClient.getWorkflowInputs(eachId);
//                for(WorkflowInput input:workflowInputs){
//                    input.setValue("testing");
//                }
//                System.out.println(airavataClient.runWorkflow(eachId,workflowInputs));
//            }
//        } catch (RegistryException e1) {
//            e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//        } catch (IOException e1) {
//            e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//        } catch (Exception e) {
//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//        }
    }

    @Test
    public void testExperimentDeletion() {
        URI uri1 = null,uri2 = null;
        try {
            uri1 = new URI("http://gw56.quarry.iu.teragrid.org:8090/jackrabbit-webapp-2.4.0/rmi");
            uri2 = new URI("http://gf7.ucs.indiana.edu:8030/jackrabbit/rmi");
        } catch (URISyntaxException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("org.apache.jackrabbit.repository.uri", "http://gf7.ucs.indiana.edu:8030/jackrabbit/rmi");
        try {
            JCRRegistry jcrRegistry1 = new JCRRegistry(
                    uri1,
                    "org.apache.jackrabbit.rmi.repository.RmiRepositoryFactory",
                    "admin",
                    "admin", map);
            Session session = jcrRegistry1.getSession();
//            session.importXML("/SERVICE_HOST",export, ImportUUIDBehavior.IMPORT_UUID_COLLISION_REMOVE_EXISTING);
            javax.jcr.Node serviceHost = jcrRegistry1.getOrAddNode(jcrRegistry1.getRootNode(session), "experiments");
            serviceHost.remove();
            session.save();
//            javax.jcr.Node appHost = jcrRegistry1.getOrAddNode(jcrRegistry1.getRootNode(session), "APP_HOST");
//            javax.jcr.Node workflows = jcrRegistry1.getOrAddNode(jcrRegistry1.getRootNode(session), "WORKFLOWS");
//
//            HashMap<String, String> map2 = new HashMap<String, String>();
//            map2.put("org.apache.jackrabbit.repository.uri", "http://gf7.ucs.indiana.edu:8030/jackrabbit/rmi");
//            JCRRegistry jcrRegistry2 = new JCRRegistry(
//                    uri2,
//                    "org.apache.jackrabbit.rmi.repository.RmiRepositoryFactory",
//                    "admin",
//                    "admin", map2);
//            Session session2 = jcrRegistry2.getSession();
//
//            RepositoryCopier.copy(session.getRepository(),jcrRegistry2.getRepository());
//            Node service_host = jcrRegistry2.getOrAddNode(jcrRegistry2.getRootNode(session2), "SERVICE_HOST");
//            service_host = serviceHost;
//            session2.save();
//

        } catch (RepositoryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }  catch (RegistryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }



}
