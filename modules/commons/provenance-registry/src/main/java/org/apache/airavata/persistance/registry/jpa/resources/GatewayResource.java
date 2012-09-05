package org.apache.airavata.persistance.registry.jpa.resources;

import org.apache.airavata.persistance.registry.jpa.Resource;
import org.apache.airavata.persistance.registry.jpa.ResourceType;
import org.apache.airavata.persistance.registry.jpa.model.Application_Descriptor;
import org.apache.airavata.persistance.registry.jpa.model.Gateway;
import org.apache.airavata.persistance.registry.jpa.model.Host_Descriptor;
import org.apache.airavata.persistance.registry.jpa.model.Project;
import org.apache.airavata.persistance.registry.jpa.model.Published_Workflow;
import org.apache.airavata.persistance.registry.jpa.model.Service_Descriptor;
import org.apache.airavata.persistance.registry.jpa.model.Users;

import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;

public class GatewayResource extends AbstractResource {
    private int gatewayID = -1;
    private String name;

    public Resource create(ResourceType type) {
        if (type == ResourceType.PROJECT) {
            ProjectResource projectResource = new ProjectResource();
            projectResource.setGatewayID(gatewayID);
            return projectResource;
        } else if (type == ResourceType.USER) {
            UserResource userResource = new UserResource();
            userResource.setGatewayID(gatewayID);
            return userResource;
        } else if (type == ResourceType.PUBLISHED_WORKFLOW) {
            PublishWorkflowResource publishWorkflowResource = new PublishWorkflowResource();
            publishWorkflowResource.setGatewayID(gatewayID);
            return publishWorkflowResource;
        } else if (type == ResourceType.HOST_DESCRIPTOR) {
            HostDescriptorResource hostDescriptorResource = new HostDescriptorResource();
            hostDescriptorResource.setGatewayID(gatewayID);
            return hostDescriptorResource;
        } else if (type == ResourceType.SERVICE_DESCRIPTOR) {
            ServiceDescriptorResource serviceDescriptorResource = new ServiceDescriptorResource();
            serviceDescriptorResource.setGatewayID(gatewayID);
            return serviceDescriptorResource;
        } else if (type == ResourceType.APPLICATION_DESCRIPTOR) {
            ApplicationDescriptorResource applicationDescriptorResource = new ApplicationDescriptorResource();
            applicationDescriptorResource.setGatewayID(gatewayID);
            return applicationDescriptorResource;
        } else {
            return null;
        }
    }

    public void remove(ResourceType type, Object name) {
        begin();
        if (type == ResourceType.PROJECT) {
            Query q = em.createQuery("Delete p FROM Project p WHERE p.project_name = :proj_name and p.gateway_ID = :gate_ID");
            q.setParameter("proj_name", name);
            q.setParameter("gate_ID", gatewayID);
            q.executeUpdate();
        } else if (type == ResourceType.USER) {
            Query q = em.createQuery("Delete p FROM Users p WHERE p.user_name = :usr_name and p.gateway_ID = :gate_ID");
            q.setParameter("usr_name", name);
            q.setParameter("gate_ID", gatewayID);
            q.executeUpdate();
        } else if (type == ResourceType.PUBLISHED_WORKFLOW) {
            Query q = em.createQuery("Delete p FROM Published_Workflow p WHERE p.publish_workflow_name = :pub_workflow_id and p.gateway_ID = :gate_ID");
            q.setParameter("pub_workflow_id", name);
            q.setParameter("gate_ID", gatewayID);
            q.executeUpdate();
            end();
        } else if (type == ResourceType.HOST_DESCRIPTOR) {
            begin();
            Query q = em.createQuery("Delete p FROM Host_Descriptor p WHERE p.host_descriptor_ID = :host_desc_id and p.gateway_ID = :gate_ID");
            q.setParameter("host_desc_id", name);
            q.setParameter("gate_ID", gatewayID);
            q.executeUpdate();
        } else if (type == ResourceType.SERVICE_DESCRIPTOR) {
            Query q = em.createQuery("Delete p FROM Service_Descriptor p WHERE p.service_descriptor_ID = :service_desc_id and p.gateway_ID = :gate_ID");
            q.setParameter("service_desc_id", name);
            q.setParameter("gate_ID", gatewayID);
            q.executeUpdate();
        } else if (type == ResourceType.APPLICATION_DESCRIPTOR) {
            Query q = em.createQuery("Delete p FROM Application_Descriptor p WHERE p.application_descriptor_ID = :app_desc_id and p.gateway_ID = :gate_ID");
            q.setParameter("app_desc_id", name);
            q.setParameter("gate_ID", gatewayID);
            q.executeUpdate();
        }
        end();


    }

    public Resource get(ResourceType type, Object name) {
        begin();
        if (type == ResourceType.PROJECT) {
            Query q = em.createQuery("SELECT p FROM Project p WHERE p.project_name = :proj_name and p.gateway_ID =:gate_ID");
            q.setParameter("proj_name", name);
            q.setParameter("gate_ID", gatewayID);
            Project eproject = (Project) q.getSingleResult();
            ProjectResource projectResource = new ProjectResource(eproject.getProject_ID());
            projectResource.setName(eproject.getProject_name());
            end();
            return projectResource;
        } else if (type == ResourceType.USER) {
            Query q = em.createQuery("SELECT p FROM Users p WHERE p.user_name = :username and p.gateway_ID =:gate_ID");
            q.setParameter("username", name);
            q.setParameter("gate_ID", gatewayID);
            Users eUser = (Users) q.getSingleResult();
            UserResource userResource = new UserResource(eUser.getUser_ID());
            userResource.setUserName(eUser.getUser_name());
            userResource.setGatewayID(gatewayID);
            end();
            return userResource;
        } else if (type == ResourceType.PUBLISHED_WORKFLOW) {
            Query q = em.createQuery("SELECT p FROM Published_Workflow p WHERE p.publish_workflow_name = :pub_workflow_name and p.gateway_ID =:gate_ID");
            q.setParameter("pub_workflow_name", name);
            q.setParameter("gate_ID", gatewayID);
            Published_Workflow ePub_workflow = (Published_Workflow) q.getSingleResult();
            PublishWorkflowResource publishWorkflowResource = new PublishWorkflowResource(ePub_workflow.getPublish_workflow_name());
            publishWorkflowResource.setContent(ePub_workflow.getWorkflow_content());
            publishWorkflowResource.setPublishedDate(ePub_workflow.getPublished_date());
            publishWorkflowResource.setVersion(ePub_workflow.getVersion());
            end();
            return publishWorkflowResource;
        } else if (type == ResourceType.SERVICE_DESCRIPTOR) {
            Query q = em.createQuery("SELECT p FROM Service_Descriptor p WHERE p.service_descriptor_ID = :service_desc_id and p.gateway_ID =:gate_ID");
            q.setParameter("service_desc_id", name);
            q.setParameter("gate_ID", gatewayID);
            Service_Descriptor eServiceDesc = (Service_Descriptor) q.getSingleResult();
            ServiceDescriptorResource serviceDescriptorResource = new ServiceDescriptorResource(eServiceDesc.getService_descriptor_ID());
            serviceDescriptorResource.setGatewayID(eServiceDesc.getGateway().getGateway_ID());
            serviceDescriptorResource.setContent(eServiceDesc.getService_descriptor_xml());
            end();
            return serviceDescriptorResource;
        } else if (type == ResourceType.HOST_DESCRIPTOR) {
            Query q = em.createQuery("SELECT p FROM Host_Descriptor p WHERE p.host_descriptor_ID = :host_desc_id and p.gateway_ID =:gate_ID");
            q.setParameter("host_desc_id", name);
            q.setParameter("gate_ID", gatewayID);
            Host_Descriptor eHostDesc = (Host_Descriptor) q.getSingleResult();
            HostDescriptorResource hostDescriptorResource = new HostDescriptorResource(eHostDesc.getHost_descriptor_ID());
            hostDescriptorResource.setGatewayID(eHostDesc.getGateway().getGateway_ID());
            hostDescriptorResource.setContent(eHostDesc.getHost_descriptor_ID());
            end();
            return hostDescriptorResource;
        } else if (type == ResourceType.APPLICATION_DESCRIPTOR) {
            Query q = em.createQuery("SELECT p FROM Application_Descriptor p WHERE p.application_descriptor_ID = :app_desc_id and p.gateway_ID =:gate_ID");
            q.setParameter("app_desc_id", name);
            q.setParameter("gate_ID", gatewayID);
            Application_Descriptor eappDesc = (Application_Descriptor) q.getSingleResult();
            ApplicationDescriptorResource applicationDescriptorResource = new ApplicationDescriptorResource(eappDesc.getApplication_descriptor_ID());
            applicationDescriptorResource.setGatewayID(eappDesc.getGateway().getGateway_ID());
            applicationDescriptorResource.setContent(eappDesc.getApplication_descriptor_xml());
            applicationDescriptorResource.setHostDescName(eappDesc.getHost_descriptor().getHost_descriptor_ID());
            applicationDescriptorResource.setServiceDescName(eappDesc.getService_descriptor().getService_descriptor_ID());
            end();
            return applicationDescriptorResource;
        } else {
            return null;
        }

    }

    public List<Resource> get(ResourceType type) {
        List<Resource> resourceList = new ArrayList<Resource>();
        begin();
        if (type == ResourceType.PROJECT) {
            Query q = em.createQuery("SELECT p FROM Project p WHERE p.gateway_ID =:gate_ID");
            q.setParameter("gate_ID", gatewayID);
            List results = q.getResultList();
            if (results.size() != 0) {
                for (Object result : results) {
                    Project project = (Project) result;
                    ProjectResource projectResource = new ProjectResource(project.getProject_ID());
                    projectResource.setGatewayID(gatewayID);
                    projectResource.setName(project.getProject_name());
                    resourceList.add(projectResource);
                }
            }
        } else if (type == ResourceType.USER) {
            Query q = em.createQuery("SELECT p FROM Users p WHERE p.gateway_ID =:gate_ID");
            q.setParameter("gate_ID", gatewayID);
            List results = q.getResultList();
            if (results.size() != 0) {
                for (Object result : results) {
                    Users user = (Users) result;
                    UserResource userResource = new UserResource(user.getUser_ID());
                    userResource.setGatewayID(gatewayID);
                    userResource.setUserName(user.getUser_name());
                    resourceList.add(userResource);
                }
            }
        } else if (type == ResourceType.PUBLISHED_WORKFLOW) {
            Query q = em.createQuery("SELECT p FROM Published_Workflow p WHERE p.gateway_ID =:gate_ID");
            q.setParameter("gate_ID", gatewayID);
            List results = q.getResultList();
            if (results.size() != 0) {
                for (Object result : results) {
                    Published_Workflow publishedWorkflow = (Published_Workflow) result;
                    PublishWorkflowResource publishWorkflowResource = new PublishWorkflowResource(publishedWorkflow.getPublish_workflow_name());
                    publishWorkflowResource.setGatewayID(gatewayID);
                    publishWorkflowResource.setContent(publishedWorkflow.getWorkflow_content());
                    publishWorkflowResource.setPublishedDate(publishedWorkflow.getPublished_date());
                    publishWorkflowResource.setVersion(publishedWorkflow.getVersion());
                    resourceList.add(publishWorkflowResource);
                }
            }
        } else if (type == ResourceType.HOST_DESCRIPTOR) {
            Query q = em.createQuery("SELECT p FROM Host_Descriptor p WHERE p.gateway_ID =:gate_ID");
            q.setParameter("gate_ID", gatewayID);
            List results = q.getResultList();
            if (results.size() != 0) {
                for (Object result : results) {
                    Host_Descriptor hostDescriptor = (Host_Descriptor) result;
                    HostDescriptorResource hostDescriptorResource = new HostDescriptorResource(hostDescriptor.getHost_descriptor_ID());
                    hostDescriptorResource.setGatewayID(gatewayID);
                    hostDescriptorResource.setContent(hostDescriptor.getHost_descriptor_xml());
                    resourceList.add(hostDescriptorResource);
                }
            }
        } else if (type == ResourceType.SERVICE_DESCRIPTOR) {
            Query q = em.createQuery("SELECT p FROM Service_Descriptor p WHERE p.gateway_ID =:gate_ID");
            q.setParameter("gate_ID", gatewayID);
            List results = q.getResultList();
            if (results.size() != 0) {
                for (Object result : results) {
                    Service_Descriptor serviceDescriptor = (Service_Descriptor) result;
                    ServiceDescriptorResource serviceDescriptorResource = new ServiceDescriptorResource(serviceDescriptor.getService_descriptor_ID());
                    serviceDescriptorResource.setGatewayID(gatewayID);
                    serviceDescriptorResource.setContent(serviceDescriptor.getService_descriptor_xml());
                    resourceList.add(serviceDescriptorResource);
                }
            }
        } else if (type == ResourceType.APPLICATION_DESCRIPTOR) {
            Query q = em.createQuery("SELECT p FROM Application_Descriptor p WHERE p.gateway_ID =:gate_ID");
            q.setParameter("gate_ID", gatewayID);
            List results = q.getResultList();
            if (results.size() != 0) {
                for (Object result : results) {
                    Application_Descriptor applicationDescriptor = (Application_Descriptor) result;
                    ApplicationDescriptorResource applicationDescriptorResource = new ApplicationDescriptorResource(applicationDescriptor.getApplication_descriptor_ID());
                    applicationDescriptorResource.setGatewayID(gatewayID);
                    applicationDescriptorResource.setContent(applicationDescriptor.getApplication_descriptor_xml());
                    applicationDescriptorResource.setHostDescName(applicationDescriptor.getHost_descriptor().getHost_descriptor_ID());
                    applicationDescriptorResource.setServiceDescName(applicationDescriptor.getService_descriptor().getService_descriptor_ID());
                    resourceList.add(applicationDescriptorResource);
                }
            }
        }
        end();
        return resourceList;
    }

    public void save() {
        // save me..
        begin();
        Gateway gateway = new Gateway();
        gateway.setGateway_name(name);
        if (gatewayID != -1) {
            gateway.setGateway_ID(gatewayID);
        }
        em.persist(gateway);
        end();
    }

    public boolean isExists(ResourceType type, Object name) {
        begin();
        if(type == ResourceType.PROJECT){
            Query q = em.createQuery("SELECT p FROM Project p WHERE p.gateway_ID =:gate_ID and p.project_name =:proj_name");
            q.setParameter("gate_ID", gatewayID);
            q.setParameter("proj_name",name);
            Project project = (Project)q.getSingleResult();
            if(project != null){
                return true;
            }else {
                return false;
            }
        } else if (type == ResourceType.USER){
            Query q = em.createQuery("SELECT p FROM Users p WHERE p.gateway_ID =:gate_ID and p.user_name =:usr_name");
            q.setParameter("gate_ID", gatewayID);
            q.setParameter("usr_name",name);
            Users users = (Users)q.getSingleResult();
            if(users != null){
                return true;
            }else {
                return false;
            }
        }  else if (type == ResourceType.PUBLISHED_WORKFLOW){
                Query q = em.createQuery("SELECT p FROM Published_Workflow p WHERE p.gateway_ID =:gate_ID and p.Published_Workflow =:pub_wf_name");
            q.setParameter("gate_ID", gatewayID);
            q.setParameter("pub_wf_name",name);
            Published_Workflow publishedWrkflow = (Published_Workflow)q.getSingleResult();
            if(publishedWrkflow != null){
                return true;
            }else {
                return false;
            }
        }  else if (type == ResourceType.HOST_DESCRIPTOR){
            Query q = em.createQuery("SELECT p FROM Host_Descriptor p WHERE p.gateway_ID =:gate_ID and p.host_descriptor_ID =:host_desc_name");
            q.setParameter("gate_ID", gatewayID);
            q.setParameter("host_desc_name",name);
            Host_Descriptor hostDescriptor = (Host_Descriptor)q.getSingleResult();
            if(hostDescriptor != null){
                return true;
            }else {
                return false;
            }
        }else if (type == ResourceType.SERVICE_DESCRIPTOR){
            Query q = em.createQuery("SELECT p FROM Service_Descriptor p WHERE p.gateway_ID =:gate_ID and p.service_descriptor_ID =:service_desc_name");
            q.setParameter("gate_ID", gatewayID);
            q.setParameter("service_desc_name",name);
            Service_Descriptor serviceDescriptor = (Service_Descriptor)q.getSingleResult();
            if(serviceDescriptor != null){
                return true;
            }else {
                return false;
            }
        }   else if (type == ResourceType.APPLICATION_DESCRIPTOR){
            Query q = em.createQuery("SELECT p FROM Application_Descriptor p WHERE p.gateway_ID =:gate_ID and p.application_descriptor_ID =:app_desc_name");
            q.setParameter("gate_ID", gatewayID);
            q.setParameter("app_desc_name",name);
            Application_Descriptor applicationDescriptor = (Application_Descriptor)q.getSingleResult();
            if(applicationDescriptor != null){
                return true;
            }else {
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
}
