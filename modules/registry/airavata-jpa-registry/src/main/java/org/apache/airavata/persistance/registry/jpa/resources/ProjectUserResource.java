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
import org.apache.airavata.persistance.registry.jpa.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.util.List;

public class ProjectUserResource extends AbstractResource {
    private String projectName;
    private String userName;

    private static final Logger logger = LoggerFactory.getLogger(ProjectUserResource.class);

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public Resource create(ResourceType type) {
        logger.error("Unsupported resource type for project resource data resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    @Override
    public void remove(ResourceType type, Object name) {
        logger.error("Unsupported resource type for project resource data resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    @Override
    public Resource get(ResourceType type, Object name) {
        logger.error("Unsupported resource type for project resource data resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Resource> get(ResourceType type) {
        logger.error("Unsupported resource type for project resource data resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    @Override
    public void save() {
        EntityManager em = ResourceUtils.getEntityManager();
        ProjectUser existingPrUser = em.find(ProjectUser.class, new ProjectUser_PK(projectName, userName));
        em.close();

        em = ResourceUtils.getEntityManager();
        em.getTransaction().begin();
        ProjectUser prUser = new ProjectUser();
        prUser.setProjectName(projectName);
        prUser.setUserName(userName);
        Users user = em.find(Users.class, userName);
        prUser.setUser(user);
        Project project = em.find(Project.class, projectName);
        prUser.setProject(project);

        if(existingPrUser != null){
            existingPrUser.setProjectName(projectName);
            existingPrUser.setUserName(userName);
            existingPrUser.setUser(user);
            existingPrUser.setProject(project);
            prUser = em.merge(existingPrUser);
        }else {
            em.persist(prUser);
        }

        em.getTransaction().commit();
        em.close();
    }
}
