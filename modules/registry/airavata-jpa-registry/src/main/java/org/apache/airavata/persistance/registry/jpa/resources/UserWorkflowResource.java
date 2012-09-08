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
import org.apache.airavata.persistance.registry.jpa.model.Gateway;
import org.apache.airavata.persistance.registry.jpa.model.User_Workflow;
import org.apache.airavata.persistance.registry.jpa.model.Users;

import javax.persistence.Query;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class UserWorkflowResource extends AbstractResource {
    private GatewayResource gateway;
    private WorkerResource worker;
    private String name;
    private Date lastUpdateDate;
    private String content;

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

    public void setLastUpdateDate(Date lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    public String getContent() {
        return content;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Resource create(ResourceType type) {
        throw new UnsupportedOperationException();
    }

    public void remove(ResourceType type, Object name) {
        throw new UnsupportedOperationException();
    }

    public void removeMe(Object[] keys) {

    }

    public Resource get(ResourceType type, Object name) {
        throw new UnsupportedOperationException();
    }

    /**
     *
     * @param keys should be in the order of gateway_name,user_name and user_workflow_name
     * @return
     */
    public List<Resource> getMe(Object[] keys) {
        List<Resource> list = new ArrayList<Resource>();
        begin();
        Query q = em.createQuery("SELECT p FROM User_Workflow p WHERE p.gateway_name = :gate_name and p.user_name =:usr_name and p.user_workflow_name=:usr_wf_name");
        q.setParameter("gate_name", keys[0]);
        q.setParameter("usr_name", keys[1]);
        q.setParameter("usr_wf_name",keys[2]);
        User_Workflow userWorkflow = (User_Workflow)q.getSingleResult();
        UserWorkflowResource userWorkflowResource = new UserWorkflowResource();
//        userWorkflowResource.setUserName(userWorkflow.getUser().getUser_name());
//        userWorkflowResource.setGatewayname(userWorkflow.getGateway().getGateway_name());
        userWorkflowResource.setName(userWorkflow.getTemplate_name());
        userWorkflowResource.setContent(userWorkflow.getWorkflow_graph());
        end();
        list.add(userWorkflowResource);
        return list;
    }

    public List<Resource> get(ResourceType type) {
        throw new UnsupportedOperationException();
    }

    public void save() {
        begin();
        User_Workflow userWorkflow = new User_Workflow();
        userWorkflow.setTemplate_name(name);
        userWorkflow.setLast_updated_date(lastUpdateDate);
        userWorkflow.setWorkflow_graph(content);
        Gateway gateway = new Gateway();
//        gateway.setGateway_name(gatewayname);
        userWorkflow.setGateway(gateway);
        Users user = new Users();
//        user.setUser_name(userName);
        userWorkflow.setUser(user);
        em.persist(userWorkflow);
        end();
    }

    public void save(boolean isAppendable) {

    }

    public boolean isExists(ResourceType type, Object name) {
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
