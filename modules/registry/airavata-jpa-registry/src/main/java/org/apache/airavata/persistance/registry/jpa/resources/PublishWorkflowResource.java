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
import org.apache.airavata.persistance.registry.jpa.model.Published_Workflow;

import javax.persistence.Query;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class PublishWorkflowResource extends AbstractResource {
    private String name;
    private String version;
    private Date publishedDate;
    private String content;
    private GatewayResource gateway;
    private String createdUser;
    private String path;

    public PublishWorkflowResource() {
    }

    public PublishWorkflowResource(GatewayResource gateway) {
        this.gateway = gateway;
    }

    public String getCreatedUser() {
        return createdUser;
    }

    public String getPath() {
        return path;
    }

    public void setCreatedUser(String createdUser) {
        this.createdUser = createdUser;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public Date getPublishedDate() {
        return publishedDate;
    }

    public String getContent() {
        return content;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setPublishedDate(Date publishedDate) {
        this.publishedDate = publishedDate;
    }

    public void setContent(String content) {
        this.content = content;
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

    public List<Resource> getMe(Object[] keys) {
        List<Resource> list = new ArrayList<Resource>();
        begin();
        Query q = em.createQuery("SELECT p FROM Published_Workflow p WHERE p.gateway_name = :gate_name and p.publish_workflow_name =:pub_wf_name");
        q.setParameter("gate_name", keys[0]);
        q.setParameter("pub_wf_name", keys[1]);
        Published_Workflow publishedWorkflow = (Published_Workflow)q.getSingleResult();
        PublishWorkflowResource publishWorkflowResource = new PublishWorkflowResource();
        publishWorkflowResource.setGateway(new GatewayResource(publishedWorkflow.getGateway().getGateway_name()));
        publishWorkflowResource.setContent(publishedWorkflow.getWorkflow_content());
        publishWorkflowResource.setPublishedDate(publishedWorkflow.getPublished_date());
        publishWorkflowResource.setVersion(publishedWorkflow.getVersion());
        end();
        list.add(publishWorkflowResource);
        return list;
    }

    public List<Resource> get(ResourceType type) {
        throw new UnsupportedOperationException();
    }

    public void save() {
        begin();
        Published_Workflow publishedWorkflow = new Published_Workflow();
        publishedWorkflow.setPublish_workflow_name(getName());
        publishedWorkflow.setPublished_date(publishedDate);
        publishedWorkflow.setVersion(version);
        publishedWorkflow.setWorkflow_content(content);
        Gateway gateway = new Gateway();
        gateway.setGateway_name(this.gateway.getGatewayName());
        publishedWorkflow.setGateway(gateway);
        em.persist(gateway);
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

	public void setName(String name) {
		this.name = name;
	}
}
