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
import org.apache.airavata.persistance.registry.jpa.model.Experiment;
import org.apache.airavata.persistance.registry.jpa.model.Project;
import org.apache.airavata.persistance.registry.jpa.model.Users;

import javax.persistence.Query;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class ExperimentResource extends AbstractResource {
    private int projectID;
    private String userName;
    private String expID;
    private Date submittedDate;
    private String gatewayName;

    public ExperimentResource() {
    }

    public ExperimentResource(String expID) {
        this.expID = expID;
    }

    public int getProjectID() {
        return projectID;
    }

    public String getExpID() {
        return expID;
    }

    public Date getSubmittedDate() {
        return submittedDate;
    }

    public void setProjectID(int projectID) {
        this.projectID = projectID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setSubmittedDate(Date submittedDate) {
        this.submittedDate = submittedDate;
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

    public void removeMe(Object[] keys) {

    }

    public Resource get(ResourceType type, Object name) {
        throw new UnsupportedOperationException();
    }

    /**
     * key should be the experiment ID
     * @param keys
     * @return
     */
    public List<Resource> getMe(Object[] keys) {
        List<Resource> list = new ArrayList<Resource>();
        begin();
        Query q = em.createQuery("SELECT p FROM Experiment p WHERE p.experiment_ID = :exp_ID");
        q.setParameter("exp_ID", keys[0]);
        Experiment experiment = (Experiment)q.getSingleResult();
        ExperimentResource experimentResource = new ExperimentResource(experiment.getExperiment_ID());
        experimentResource.setUserName(experiment.getUser().getUser_name());
        experimentResource.setProjectID(experiment.getProject().getProject_ID());
        experimentResource.setSubmittedDate(experiment.getSubmitted_date());
        end();
        list.add(experimentResource);
        return list;

    }

    public List<Resource> get(ResourceType type) {
        throw new UnsupportedOperationException();
    }

    public void save() {
        begin();
        Experiment experiment = new Experiment();
        experiment.setExperiment_ID(expID);
        Project project = new Project();
        project.setProject_ID(projectID);
        experiment.setProject(project);
        Users user = new Users();
        user.setUser_name(userName);
        experiment.setUser(user);
        experiment.setSubmitted_date(submittedDate);
        em.persist(experiment);
        end();


    }

    public void save(boolean isAppendable) {

    }

    public boolean isExists(ResourceType type, Object name) {
        throw new UnsupportedOperationException();
    }
}
