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

import java.sql.Date;
import java.util.List;

public class PublishWorkflowResource extends AbstractResource {
    private String name;
    private String version;
    private Date publishedDate;
    private String content;
    private String gatewayName;

    public PublishWorkflowResource() {
    }

    public PublishWorkflowResource(String name) {
        this.name = name;
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

    public String getGatewayName() {
        return gatewayName;
    }

    public void setGatewayName(String gatewayName) {
        this.gatewayName = gatewayName;
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

    public List<Resource> get(ResourceType type) {
        throw new UnsupportedOperationException();
    }

    public void save() {
        begin();
        Published_Workflow publishedWorkflow = new Published_Workflow();
        publishedWorkflow.setPublish_workflow_name(name);
        publishedWorkflow.setPublished_date(publishedDate);
        publishedWorkflow.setVersion(version);
        publishedWorkflow.setWorkflow_content(content);
        Gateway gateway = new Gateway();
        gateway.setGateway_name(gatewayName);
        publishedWorkflow.setGateway_name(gatewayName);
        em.persist(gateway);
        end();
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isExists(ResourceType type, Object name) {
        throw new UnsupportedOperationException();
    }
}
