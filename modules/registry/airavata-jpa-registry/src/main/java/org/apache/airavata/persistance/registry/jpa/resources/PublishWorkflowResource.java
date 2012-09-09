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
import org.apache.airavata.persistance.registry.jpa.model.Users;
import org.apache.airavata.persistance.registry.jpa.utils.QueryGenerator;

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

    public Resource get(ResourceType type, Object name) {
        throw new UnsupportedOperationException();
    }

    public List<Resource> populate(Object[] keys) {
        List<Resource> list = new ArrayList<Resource>();
        begin();
        QueryGenerator generator = new QueryGenerator(PUBLISHED_WORKFLOW);
        generator.setParameter(PublishedWorkflowConstants.GATEWAY_NAME, keys[0]);
        generator.setParameter(PublishedWorkflowConstants.PUBLISH_WORKFLOW_NAME, keys[1]);
        Query q = generator.selectQuery(em);
        Published_Workflow publishedWorkflow = (Published_Workflow)q.getSingleResult();
        PublishWorkflowResource publishWorkflowResource = (PublishWorkflowResource)Utils.getResource(ResourceType.PUBLISHED_WORKFLOW, publishedWorkflow);
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
        Users user = new Users();
        user.setUser_name(createdUser);
        publishedWorkflow.setUser(user);
        em.persist(gateway);
        end();
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
