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
import org.apache.airavata.persistance.registry.jpa.model.Gateway;
import org.apache.airavata.persistance.registry.jpa.model.Project;
import org.apache.airavata.persistance.registry.jpa.model.User_Workflow;

import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;

public class ProjectResource extends AbstractResource {

    private String name;
    private int id = -1;
    private int gatewayID;
    private int userID;
    private UserResource userResource;
    private Gateway gateway;

    public ProjectResource() {
    }

    public ProjectResource(int id) {
        this.id = id;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public ProjectResource(UserResource userResource, int gatewayID, int id) {
        this.userResource = userResource;
        this.gatewayID = gatewayID;
        this.id = id;
    }

    public Resource create(ResourceType type) {
        if (type == ResourceType.USER_WORKFLOW) {
            UserWorkflowResource userWorkflowResource = new UserWorkflowResource();
            userWorkflowResource.setProjectID(id);
            userWorkflowResource.setUserID(userResource.getId());
            return userWorkflowResource;
        } else if (type == ResourceType.EXPERIMENT) {
            ExperimentResource experimentResource = new ExperimentResource();
            experimentResource.setProjectID(id);
            experimentResource.setUserID(userResource.getId());
            return experimentResource;
        } else {
            return null;
        }
    }

    public void remove(ResourceType type, Object name) {
        begin();
        if (type == ResourceType.USER_WORKFLOW) {
            Query q = em.createQuery("Delete p FROM User_Workflow p WHERE p.project_ID = :proj_id and p.user_ID = :user_id and p.user_workflow_name = :usrwf_name");
            q.setParameter("proj_id", id);
            q.setParameter("user_id", userResource.getId());
            q.setParameter("usrwf_name", name);
            q.executeUpdate();
        } else if (type == ResourceType.EXPERIMENT) {
            Query q = em.createQuery("Delete p FROM Experiment p WHERE p.project_ID = :proj_id and p.user_ID = :user_id and p.experiment_ID = :ex_name");
            q.setParameter("proj_id", id);
            q.setParameter("user_id", userResource.getId());
            q.setParameter("ex_name", name);
            q.executeUpdate();
        }
        end();
    }

    public Resource get(ResourceType type, Object name) {
        begin();
        if (type == ResourceType.USER_WORKFLOW) {
            Query q = em.createQuery("SELECT p FROM User_Workflow p WHERE p.project_ID = :proj_id and p.user_ID = :user_id and p.user_workflow_name = :usrwf_name");
            q.setParameter("proj_id", id);
            q.setParameter("user_id", userResource.getId());
            q.setParameter("usrwf_name", name);
            User_Workflow userWorkflow = (User_Workflow) q.getSingleResult();
            UserWorkflowResource userWorkflowResource = new UserWorkflowResource(id, userResource.getId(), userWorkflow.getUser_workflow_name());
            userWorkflowResource.setContent(userWorkflow.getWorkflow_content());
            userWorkflowResource.setLastUpdateDate(userWorkflow.getLast_update_date());
            end();
            return userWorkflowResource;
        } else if (type == ResourceType.EXPERIMENT) {
            Query q = em.createQuery("SELECT p FROM Experiment p WHERE p.project_ID = :proj_id and p.user_ID = :user_id and p.experiment_ID = :ex_name");
            q.setParameter("proj_id", id);
            q.setParameter("user_id", userResource.getId());
            q.setParameter("ex_name", name);
            Experiment experiment = (Experiment) q.getSingleResult();
            ExperimentResource experimentResource = new ExperimentResource(experiment.getExperiment_ID());
            experimentResource.setProjectID(experiment.getProject().getProject_ID());
            experiment.setSubmitted_date(experiment.getSubmitted_date());
            experiment.setUser(experiment.getUser());
            end();
            return experimentResource;
        }
        return null;
    }

    public List<Resource> get(ResourceType type) {
        List<Resource> resourceList = new ArrayList<Resource>();
        begin();
        if (type == ResourceType.USER_WORKFLOW) {
            Query q = em.createQuery("SELECT p FROM User_Workflow p WHERE p.project_ID =:proj_ID");
            q.setParameter("proj_ID", id);
            List results = q.getResultList();
            if (results.size() != 0) {
                for (Object result : results) {
                    User_Workflow userWorkflow = (User_Workflow) result;
                    UserWorkflowResource userWorkflowResource = new UserWorkflowResource(userWorkflow.getProject_ID(), userWorkflow.getUser_ID(), userWorkflow.getUser_workflow_name());
                    userWorkflowResource.setContent(userWorkflow.getWorkflow_content());
                    userWorkflowResource.setLastUpdateDate(userWorkflow.getLast_update_date());
                    resourceList.add(userWorkflowResource);
                }
            }
        } else if (type == ResourceType.EXPERIMENT) {
            Query q = em.createQuery("SELECT p FROM Experiment p WHERE p.project_ID =:proj_ID");
            q.setParameter("proj_ID", id);
            List results = q.getResultList();
            if (results.size() != 0) {
                for (Object result : results) {
                    Experiment experiment = (Experiment) result;
                    ExperimentResource experimentResource = new ExperimentResource(experiment.getExperiment_ID());
                    experimentResource.setProjectID(experiment.getProject().getProject_ID());
                    experimentResource.setUserID(experiment.getUser().getUser_ID());
                    experimentResource.setSubmittedDate(experiment.getSubmitted_date());
                    resourceList.add(experimentResource);
                }
            }
        }
        end();
        return resourceList;
    }

    public void save() {
        begin();
        Project project = new Project();
        project.setProject_name(name);
        Gateway gateway = new Gateway();
        gateway.setGateway_ID(gatewayID);
        project.setGateway(gateway);

        if (id != -1) {
            project.setProject_ID(id);
        }



        em.persist(project);
        end();

    }

    public boolean isExists(ResourceType type, Object name) {
        begin();
        if (type == ResourceType.USER_WORKFLOW) {
            Query q = em.createQuery("SELECT p FROM User_Workflow p WHERE p.gateway_ID =:gate_ID and p.user_ID =:userID and p.project_ID =:projectID");
            q.setParameter("gate_ID", gatewayID);
            q.setParameter("userID", userResource.getId());
            q.setParameter("projectID", id);
            User_Workflow userWorkflow = (User_Workflow) q.getSingleResult();
            if (userWorkflow != null) {
                return true;
            } else {
                return false;
            }
        } else if (type == ResourceType.EXPERIMENT) {
            Query q = em.createQuery("SELECT p FROM Experiment p WHERE p.gateway_ID =:gate_ID and p.user_ID =:userID and p.project_ID =:projectID");
            q.setParameter("gate_ID", gatewayID);
            q.setParameter("userID", userResource.getId());
            q.setParameter("projectID", id);
            Experiment experiment = (Experiment) q.getSingleResult();
            if (experiment != null) {
                return true;
            } else {
                return false;
            }
        }
        end();
        return false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    void setGatewayID(int gatewayID) {
        this.gatewayID = gatewayID;
    }
}
