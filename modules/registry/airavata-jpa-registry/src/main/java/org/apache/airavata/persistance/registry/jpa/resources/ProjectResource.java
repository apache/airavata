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
    private String gatewayName;
    private String userName;
    private UserResource userResource;
    private Gateway gateway;

    public ProjectResource() {
    }

    public ProjectResource(int id) {
        this.id = id;
    }

    public ProjectResource(UserResource userResource, String gateway, int id) {
        this.userResource = userResource;
        this.gatewayName = gateway;
        this.id = id;
    }

    public String getGatewayName() {
        return gatewayName;
    }

    public String getUserName() {
        return userName;
    }

    public void setGatewayName(String gatewayName) {
        this.gatewayName = gatewayName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Resource create(ResourceType type) {
        if (type == ResourceType.USER_WORKFLOW) {
            UserWorkflowResource userWorkflowResource = new UserWorkflowResource();
            userWorkflowResource.setGatewayname(gatewayName);
            userWorkflowResource.setUserName(userWorkflowResource.getUserName());
            return userWorkflowResource;
        } else if (type == ResourceType.EXPERIMENT) {
            ExperimentResource experimentResource = new ExperimentResource();
            experimentResource.setProjectID(id);
            experimentResource.setUserName(userResource.getUserName());
            return experimentResource;
        } else {
            return null;
        }
    }

    public void remove(ResourceType type, Object name) {
        begin();
        if (type == ResourceType.USER_WORKFLOW) {
            Query q = em.createQuery("Delete p FROM User_Workflow p WHERE p.project_ID = :proj_id and p.user_name = :usr_name and p.user_workflow_name = :usrwf_name");
            q.setParameter("proj_id", id);
            q.setParameter("usr_name", userResource.getUserName());
            q.setParameter("usrwf_name", name);
            q.executeUpdate();
        } else if (type == ResourceType.EXPERIMENT) {
            Query q = em.createQuery("Delete p FROM Experiment p WHERE p.project_ID = :proj_id and p.user_name = :usr_name and p.experiment_ID = :ex_name");
            q.setParameter("proj_id", id);
            q.setParameter("usr_name", userResource.getUserName());
            q.setParameter("ex_name", name);
            q.executeUpdate();
        }
        end();
    }

    public void removeMe(Object[] keys) {

    }

    public Resource get(ResourceType type, Object name) {
        begin();
        if (type == ResourceType.USER_WORKFLOW) {
            Query q = em.createQuery("SELECT p FROM User_Workflow p WHERE p.project_ID = :proj_id and p.user_name = :usr_name and p.user_workflow_name = :usrwf_name");
            q.setParameter("proj_id", id);
            q.setParameter("usr_name", userResource.getUserName());
            q.setParameter("usrwf_name", name);
            User_Workflow userWorkflow = (User_Workflow) q.getSingleResult();
            UserWorkflowResource userWorkflowResource = new UserWorkflowResource(gatewayName, userResource.getUserName(), userWorkflow.getTemplate_name());
            userWorkflowResource.setContent(userWorkflow.getWorkflow_graph());
            userWorkflowResource.setLastUpdateDate(userWorkflow.getLast_updated_date());
            end();
            return userWorkflowResource;
        } else if (type == ResourceType.EXPERIMENT) {
            Query q = em.createQuery("SELECT p FROM Experiment p WHERE p.project_ID = :proj_id and p.user_name = :usr_name and p.experiment_ID = :ex_name");
            q.setParameter("proj_id", id);
            q.setParameter("usr_name", userResource.getUserName());
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

    public List<Resource> getMe(Object[] keys) {
        List<Resource> list = new ArrayList<Resource>();
        begin();
        Query q = em.createQuery("SELECT p FROM Project p WHERE p.project_name = :proj_name");
        q.setParameter("proj_name", keys[0]);
        List resultList = q.getResultList();
        if (resultList.size() != 0) {
            for (Object result : resultList) {
                Project project = (Project) result;
                ProjectResource projectResource = new ProjectResource();
                projectResource.setGatewayName(project.getGateway().getGateway_name());
                projectResource.setUserName(project.getUsers().getUser_name());
                list.add(projectResource);
            }
        }
        end();
        return list;
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
                    UserWorkflowResource userWorkflowResource = new UserWorkflowResource(userWorkflow.getGateway().getGateway_name(), userWorkflow.getUser().getUser_name(), userWorkflow.getTemplate_name());
                    userWorkflowResource.setContent(userWorkflow.getWorkflow_graph());
                    userWorkflowResource.setLastUpdateDate(userWorkflow.getLast_updated_date());
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
                    experimentResource.setUserName(experiment.getUser().getUser_name());
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
        gateway.setGateway_name(gatewayName);
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
            Query q = em.createQuery("SELECT p FROM User_Workflow p WHERE p.gateway_name =:gate_name and p.user_name = :usr_name and p.project_ID =:projectID");
            q.setParameter("gate_name", gatewayName);
            q.setParameter("usr_name", userResource.getUserName());
            q.setParameter("projectID", id);
            User_Workflow userWorkflow = (User_Workflow) q.getSingleResult();
            if (userWorkflow != null) {
                return true;
            } else {
                return false;
            }
        } else if (type == ResourceType.EXPERIMENT) {
            Query q = em.createQuery("SELECT p FROM Experiment p WHERE p.gateway_name =:gate_name and p.user_name = :usr_name and p.project_ID =:projectID");
            q.setParameter("gate_name", gatewayName);
            q.setParameter("usr_name", userResource.getUserName());
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


}
