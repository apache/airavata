package org.apache.airavata.persistance.registry.jpa.resources;

import org.apache.airavata.persistance.registry.jpa.Resource;
import org.apache.airavata.persistance.registry.jpa.ResourceType;
import org.apache.airavata.persistance.registry.jpa.model.Project;
import org.apache.airavata.persistance.registry.jpa.model.User_Workflow;
import org.apache.airavata.persistance.registry.jpa.model.Users;

import java.sql.Date;
import java.util.List;

public class UserWorkflowResource extends AbstractResource {
    private int projectID;
    private int userID;
    private String name;
    private Date lastUpdateDate;
    private String content;

    public UserWorkflowResource() {
    }

    public UserWorkflowResource(int projectID, int userID, String name) {
        this.projectID = projectID;
        this.userID = userID;
        this.name = name;
    }

    public int getProjectID() {
        return projectID;
    }

    public int getUserID() {
        return userID;
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

    public void setProjectID(int projectID) {
        this.projectID = projectID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Resource create(ResourceType type) {
        return null;
    }

    public void remove(ResourceType type, Object name) {

    }

    public Resource get(ResourceType type, Object name) {
        return null;
    }

    public List<Resource> get(ResourceType type) {
        return null;
    }

    public void save() {
        begin();
        User_Workflow userWorkflow = new User_Workflow();
        userWorkflow.setUser_workflow_name(name);
        userWorkflow.setLast_update_date(lastUpdateDate);
        userWorkflow.setWorkflow_content(content);
        Project project = new Project();
        project.setProject_ID(projectID);
        userWorkflow.setProject(project);
        Users user = new Users();
        user.setUser_ID(userID);
        userWorkflow.setUsers(user);
        em.persist(userWorkflow);
        end();
    }

    public boolean isExists(ResourceType type, Object name) {
        return false;
    }
}
