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

import java.util.List;

import org.apache.airavata.persistance.registry.jpa.Resource;
import org.apache.airavata.persistance.registry.jpa.ResourceType;
import org.apache.airavata.persistance.registry.jpa.ResourceUtils;
import org.apache.airavata.persistance.registry.jpa.model.Users;

import javax.persistence.EntityManager;

public class UserResource extends AbstractResource {
    private String userName;
    private String password;
    private String gatewayName;
    private ProjectResource projectResource;

    /**
     *
     */
    public UserResource() {
    }

    /**
     *
     * @param userName user name
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     *
     * @return user name
     */
    public String getUserName() {
        return userName;
    }

    /**
     *
     * @return gateway name
     */
    public String getGatewayName() {
        return gatewayName;
    }

    /**
     *
     * @param gatewayName gateway name
     */
    public void setGatewayName(String gatewayName) {
        this.gatewayName = gatewayName;
    }

    /**
     * User is a hypothical data structure.
     * @param type child resource type
     * @return child resource
     */
    public Resource create(ResourceType type) {
        throw new UnsupportedOperationException();
    }

    /**
     *
     * @param type child resource type
     * @param name child resource name
     */
    public void remove(ResourceType type, Object name) {
        throw new UnsupportedOperationException();
    }

    /**
     *
     * @param type child resource type
     * @param name child resource name
     * @return UnsupportedOperationException
     */
    public Resource get(ResourceType type, Object name) {
        throw new UnsupportedOperationException();
    }

    /**
     *
     * @param type child resource type
     * @return UnsupportedOperationException
     */
    public List<Resource> get(ResourceType type) {
        throw new UnsupportedOperationException();
    }

    /**
     * save user to the database
     */
    public void save() {
        EntityManager em = ResourceUtils.getEntityManager();
        em.getTransaction().begin();
        Users user = new Users();
        user.setUser_name(userName);
        user.setPassword(password);
        em.merge(user);
        em.getTransaction().commit();
        em.close();
    }

    /**
     *
     * @param type child resource type
     * @param name child resource name
     * @return UnsupportedOperationException
     */
    public boolean isExists(ResourceType type, Object name) {
        throw new UnsupportedOperationException();
    }

    /**
     *
     * @return project resource
     */
    public ProjectResource getProjectResource() {
        return projectResource;
    }

    /**
     *
     * @param projectResource project resource
     */
    public void setProjectResource(ProjectResource projectResource) {
        this.projectResource = projectResource;
    }

    /**
     *
     * @return  password
     */
    public String getPassword() {
        return password;
    }

    /**
     *
     * @param password  password
     */
    public void setPassword(String password) {
        this.password = password;
    }
}
