package org.apache.airavata.k8s.api.server.model.commons;

import org.apache.airavata.k8s.api.server.model.task.TaskModel;

import javax.persistence.*;
import java.util.List;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
@Entity
@Table(name = "ERROR_MODEL")
public class ErrorModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private long creationTime;
    private String actualErrorMessage;
    private String userFriendlyMessage;
    private boolean transientOrPersistent;

    @ManyToOne
    private TaskModel taskModel;

    @OneToMany
    private List<ErrorModel> rootCauseErrorList;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    public String getActualErrorMessage() {
        return actualErrorMessage;
    }

    public void setActualErrorMessage(String actualErrorMessage) {
        this.actualErrorMessage = actualErrorMessage;
    }

    public String getUserFriendlyMessage() {
        return userFriendlyMessage;
    }

    public void setUserFriendlyMessage(String userFriendlyMessage) {
        this.userFriendlyMessage = userFriendlyMessage;
    }

    public boolean isTransientOrPersistent() {
        return transientOrPersistent;
    }

    public void setTransientOrPersistent(boolean transientOrPersistent) {
        this.transientOrPersistent = transientOrPersistent;
    }

    public List<ErrorModel> getRootCauseErrorList() {
        return rootCauseErrorList;
    }

    public void setRootCauseErrorList(List<ErrorModel> rootCauseErrorList) {
        this.rootCauseErrorList = rootCauseErrorList;
    }

    public TaskModel getTaskModel() {
        return taskModel;
    }

    public ErrorModel setTaskModel(TaskModel taskModel) {
        this.taskModel = taskModel;
        return this;
    }
}
