package org.apache.airavata.persistance.registry.jpa.resources;

import org.apache.airavata.persistance.registry.jpa.Resource;
import org.apache.airavata.persistance.registry.jpa.ResourceType;
import org.apache.airavata.persistance.registry.jpa.model.Experiment;
import org.apache.airavata.persistance.registry.jpa.model.Project;
import org.apache.airavata.persistance.registry.jpa.model.Users;

import java.sql.Date;
import java.util.List;

public class ExperimentResource extends AbstractResource {
    private int projectID;
    private int userID;
    private String expID;
    private Date submittedDate;

    public ExperimentResource() {
    }

    public ExperimentResource(String expID) {
        this.expID = expID;
    }

    public int getProjectID() {
        return projectID;
    }

    public int getUserID() {
        return userID;
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

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public void setSubmittedDate(Date submittedDate) {
        this.submittedDate = submittedDate;
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
        Experiment experiment = new Experiment();
        experiment.setExperiment_ID(expID);
        Project project = new Project();
        project.setProject_ID(projectID);
        experiment.setProject(project);
        Users user = new Users();
        user.setUser_ID(userID);
        experiment.setUser(user);
        experiment.setSubmitted_date(submittedDate);
        em.persist(experiment);
        end();


    }

    public boolean isExists(ResourceType type, Object name) {
        throw new UnsupportedOperationException();
    }
}
