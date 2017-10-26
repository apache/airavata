package org.apache.airavata.k8s.api.server.model.task;

import javax.persistence.*;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
@Entity
@Table(name = "TASK_PARAM")
public class TaskParam {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(name = "PARAM_KEY")
    private String key;

    @Column(name = "PARAM_VALUE")
    private String value;

    @ManyToOne
    private TaskModel taskModel;

    public long getId() {
        return id;
    }

    public TaskParam setId(long id) {
        this.id = id;
        return this;
    }

    public String getKey() {
        return key;
    }

    public TaskParam setKey(String key) {
        this.key = key;
        return this;
    }

    public String getValue() {
        return value;
    }

    public TaskParam setValue(String value) {
        this.value = value;
        return this;
    }

    public TaskModel getTaskModel() {
        return taskModel;
    }

    public TaskParam setTaskModel(TaskModel taskModel) {
        this.taskModel = taskModel;
        return this;
    }
}
