package org.apache.airavata.k8s.api.server.model.data;

import org.apache.airavata.k8s.api.server.model.experiment.ExperimentInputData;
import org.apache.airavata.k8s.api.server.model.experiment.ExperimentOutputData;
import org.apache.airavata.k8s.api.server.model.task.TaskModel;

import javax.persistence.*;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
@Entity
@Table(name = "DATA_STORE")
public class DataStoreModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Lob
    @Column(length = 1000000, name = "CONTENT")
    @Basic(fetch = FetchType.LAZY)
    private byte[] content;

    @ManyToOne
    private ExperimentOutputData experimentOutputData;

    @ManyToOne
    private TaskModel taskModel;

    public long getId() {
        return id;
    }

    public DataStoreModel setId(long id) {
        this.id = id;
        return this;
    }

    public byte[] getContent() {
        return content;
    }

    public DataStoreModel setContent(byte[] content) {
        this.content = content;
        return this;
    }

    public ExperimentOutputData getExperimentOutputData() {
        return experimentOutputData;
    }

    public DataStoreModel setExperimentOutputData(ExperimentOutputData experimentOutputData) {
        this.experimentOutputData = experimentOutputData;
        return this;
    }

    public TaskModel getTaskModel() {
        return taskModel;
    }

    public DataStoreModel setTaskModel(TaskModel taskModel) {
        this.taskModel = taskModel;
        return this;
    }
}
