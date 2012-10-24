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
import org.apache.airavata.persistance.registry.jpa.model.Gateway;
import org.apache.airavata.persistance.registry.jpa.model.User_Workflow;
import org.apache.airavata.persistance.registry.jpa.model.User_Workflow_PK;
import org.apache.airavata.persistance.registry.jpa.model.Users;
import org.apache.airavata.persistance.registry.jpa.utils.QueryGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class UserWorkflowResource extends AbstractResource {
    private final static Logger logger = LoggerFactory.getLogger(UserWorkflowResource.class);
    private GatewayResource gateway;
    private WorkerResource worker;
    private String name;
    private Timestamp lastUpdateDate;
    private String content;
    private String path;

    public UserWorkflowResource() {
    }

    public UserWorkflowResource(GatewayResource gateway, WorkerResource worker, String name) {
        this.setGateway(gateway);
        this.setWorker(worker);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setLastUpdateDate(Timestamp lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Timestamp getLastUpdateDate() {
        return lastUpdateDate;
    }

    public String getContent() {
        return content;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Resource create(ResourceType type) {
        logger.error("Unsupported resource type for user workflow resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    public void remove(ResourceType type, Object name) {
        logger.error("Unsupported resource type for user workflow resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    public Resource get(ResourceType type, Object name) {
        logger.error("Unsupported resource type for user workflow resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    /**
     *
     * @param keys should be in the order of gateway_name,user_name and user_workflow_name
     * @return resource list
     */
    public List<Resource> populate(Object[] keys) {
        List<Resource> list = new ArrayList<Resource>();
        EntityManager em = ResourceUtils.getEntityManager();
        em.getTransaction().begin();
        QueryGenerator queryGenerator = new QueryGenerator(USER_WORKFLOW);
        queryGenerator.setParameter(UserWorkflowConstants.GATEWAY_NAME, keys[0]);
        queryGenerator.setParameter(UserWorkflowConstants.OWNER, keys[1]);
        queryGenerator.setParameter(UserWorkflowConstants.TEMPLATE_NAME, keys[2]);
        Query q = queryGenerator.selectQuery(em);
        User_Workflow userWorkflow = (User_Workflow)q.getSingleResult();
        UserWorkflowResource userWorkflowResource = (UserWorkflowResource)Utils.getResource(
                ResourceType.USER_WORKFLOW, userWorkflow);
        em.getTransaction().commit();
        em.close();
        list.add(userWorkflowResource);
        return list;
    }

    public List<Resource> get(ResourceType type) {
        logger.error("Unsupported resource type for user workflow resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    public void save() {
        EntityManager em = ResourceUtils.getEntityManager();
        User_Workflow existingWF = em.find(User_Workflow.class, new User_Workflow_PK(name, worker.getUser(), gateway.getGatewayName()));
        em.close();

        em = ResourceUtils.getEntityManager();
        em.getTransaction().begin();
        User_Workflow userWorkflow = new User_Workflow();
        userWorkflow.setTemplate_name(name);
        if(lastUpdateDate == null){
            java.util.Date date= new java.util.Date();
            lastUpdateDate = new Timestamp(date.getTime());
        }
        userWorkflow.setLast_updated_date(lastUpdateDate);
        byte[] bytes = content.getBytes();
        userWorkflow.setWorkflow_graph(bytes);
        userWorkflow.setGateway_name(this.gateway.getGatewayName());
        userWorkflow.setOwner(this.getWorker().getUser());
        userWorkflow.setPath(path);
        if(existingWF != null){
            existingWF.setGateway_name(this.gateway.getGatewayName());
            existingWF.setOwner(this.getWorker().getUser());
            existingWF.setTemplate_name(name);
            existingWF.setLast_updated_date(lastUpdateDate);
            existingWF.setPath(path);
            existingWF.setWorkflow_graph(bytes);
            userWorkflow = em.merge(existingWF);
        } else {
            em.persist(userWorkflow);
        }
        em.getTransaction().commit();
        em.close();
    }

    public boolean isExists(ResourceType type, Object name) {
        logger.error("Unsupported resource type for user workflow resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

	public GatewayResource getGateway() {
		return gateway;
	}

	public void setGateway(GatewayResource gateway) {
		this.gateway = gateway;
	}

	public WorkerResource getWorker() {
		return worker;
	}

	public void setWorker(WorkerResource worker) {
		this.worker = worker;
	}
}
