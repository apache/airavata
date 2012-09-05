package org.apache.airavata.persistance.registry.jpa.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import java.sql.Date;

@Entity
public class Experiment {
    @Id
    private String experiment_ID;
    private Date submitted_date;

    @OneToMany
    @JoinColumn(name="user_ID")
    private Users user;

    @OneToMany
    @JoinColumn(name="project_ID")
    private Project project;

    public String getExperiment_ID() {
        return experiment_ID;
    }

    public Date getSubmitted_date() {
        return submitted_date;
    }

    public Users getUser() {
        return user;
    }

    public Project getProject() {
        return project;
    }

    public void setExperiment_ID(String experiment_ID) {
        this.experiment_ID = experiment_ID;
    }

    public void setSubmitted_date(Date submitted_date) {
        this.submitted_date = submitted_date;
    }

    public void setUser(Users user) {
        this.user = user;
    }

    public void setProject(Project project) {
        this.project = project;
    }
}
