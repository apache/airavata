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

package org.apache.airavata.persistance.registry.jpa.resources;


import org.apache.airavata.persistance.registry.jpa.Resource;
import org.apache.airavata.persistance.registry.jpa.ResourceType;
import org.apache.airavata.persistance.registry.jpa.ResourceUtils;
import org.apache.airavata.persistance.registry.jpa.model.Gram_Data;
import org.apache.airavata.persistance.registry.jpa.model.Gram_DataPK;
import org.apache.airavata.persistance.registry.jpa.model.Node_Data;
import org.apache.airavata.persistance.registry.jpa.model.Workflow_Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.util.List;

public class GramDataResource extends AbstractResource{
    private final static Logger logger = LoggerFactory.getLogger(GramDataResource.class);
    private WorkflowDataResource workflowDataResource;
    private String nodeID;
    private String rsl;
    private String invokedHost;
    private String localJobID;

    public String getNodeID() {
        return nodeID;
    }

    public String getRsl() {
        return rsl;
    }

    public String getInvokedHost() {
        return invokedHost;
    }

    public String getLocalJobID() {
        return localJobID;
    }

    public WorkflowDataResource getWorkflowDataResource() {
        return workflowDataResource;
    }

    public void setWorkflowDataResource(WorkflowDataResource workflowDataResource) {
        this.workflowDataResource = workflowDataResource;
    }

    public void setNodeID(String nodeID) {
        this.nodeID = nodeID;
    }

    public void setRsl(String rsl) {
        this.rsl = rsl;
    }

    public void setInvokedHost(String invokedHost) {
        this.invokedHost = invokedHost;
    }

    public void setLocalJobID(String localJobID) {
        this.localJobID = localJobID;
    }

    public Resource create(ResourceType type) {
        logger.error("Unsupported resource type for Gram data resource" ,new UnsupportedOperationException() );
        throw new UnsupportedOperationException();
    }

    public void remove(ResourceType type, Object name) {
        logger.error("Unsupported resource type for Gram data resource" ,new UnsupportedOperationException() );
        throw new UnsupportedOperationException();
    }

    public Resource get(ResourceType type, Object name) {
        logger.error("Unsupported resource type for Gram data resource" ,new UnsupportedOperationException() );
        throw new UnsupportedOperationException();
    }

    public List<Resource> get(ResourceType type) {
        logger.error("Unsupported resource type for Gram data resource" ,new UnsupportedOperationException() );
        throw new UnsupportedOperationException();
    }

    public void save() {
        EntityManager em = ResourceUtils.getEntityManager();
        Gram_Data existingGramData = em.find(Gram_Data.class, new Gram_DataPK(workflowDataResource.getWorkflowInstanceID(), nodeID));
        em.close();

        em = ResourceUtils.getEntityManager();
        em.getTransaction().begin();
        Gram_Data gramData = new Gram_Data();
        gramData.setNode_id(nodeID);
        Workflow_Data workflow_data = em.find(Workflow_Data.class, workflowDataResource.getWorkflowInstanceID());
        gramData.setWorkflow_Data(workflow_data);
        gramData.setNode_id(nodeID);
        gramData.setInvoked_host(invokedHost);
        gramData.setLocal_Job_ID(localJobID);
        byte[] bytes = rsl.getBytes();
        gramData.setRsl(bytes);
        if(existingGramData != null){
            existingGramData.setInvoked_host(invokedHost);
            existingGramData.setLocal_Job_ID(localJobID);
            existingGramData.setRsl(bytes);
            gramData = em.merge(existingGramData);
        }  else {
            em.persist(gramData);
        }
        em.getTransaction().commit();
        em.close();
    }
}
