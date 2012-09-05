package org.apache.airavata.persistance.registry.jpa.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import java.sql.Date;

@Entity
@IdClass(User_Workflow_PK.class)
public class User_Workflow {
    @Id
    private String user_workflow_name;

    @Id
	@OneToMany()
	@JoinColumn(name = "user_ID")
    private Users users;

    @Id
	@ManyToMany()
	@JoinColumn(name = "project_ID")
    private Project project;

    private Date last_update_date;
    private String workflow_content;

    public String getUser_workflow_name() {
        return user_workflow_name;
    }

    public Users getUsers() {
        return users;
    }

    public Project getProject() {
        return project;
    }

    public Date getLast_update_date() {
        return last_update_date;
    }

    public String getWorkflow_content() {
        return workflow_content;
    }

    public void setUser_workflow_name(String user_workflow_name) {
        this.user_workflow_name = user_workflow_name;
    }

    public void setUsers(Users users) {
        this.users = users;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public void setLast_update_date(Date last_update_date) {
        this.last_update_date = last_update_date;
    }

    public void setWorkflow_content(String workflow_content) {
        this.workflow_content = workflow_content;
    }
}

class User_Workflow_PK{
    private String user_workflow_name;
    private int project_ID;
    private int user_ID;

    User_Workflow_PK() {
    }

    public String getUser_workflow_name() {
        return user_workflow_name;
    }

    public int getProject_ID() {
        return project_ID;
    }

    public int getUser_ID() {
        return user_ID;
    }

    public void setUser_workflow_name(String user_workflow_name) {
        this.user_workflow_name = user_workflow_name;
    }

    public void setProject_ID(int project_ID) {
        this.project_ID = project_ID;
    }

    public void setUser_ID(int user_ID) {
        this.user_ID = user_ID;
    }
}
