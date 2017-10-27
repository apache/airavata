package org.apache.airavata.k8s.api.server.model.task;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
@Entity
@Table(name = "TASK_STATUS")
public class TaskStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private TaskState state; // required
    private long timeOfStateChange; // optional
    private String reason; // optional

    @ManyToOne
    private TaskModel taskModel;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public TaskState getState() {
        return state;
    }

    public void setState(TaskState state) {
        this.state = state;
    }

    public long getTimeOfStateChange() {
        return timeOfStateChange;
    }

    public void setTimeOfStateChange(long timeOfStateChange) {
        this.timeOfStateChange = timeOfStateChange;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public TaskModel getTaskModel() {
        return taskModel;
    }

    public TaskStatus setTaskModel(TaskModel taskModel) {
        this.taskModel = taskModel;
        return this;
    }

    public enum TaskState {
        CREATED(0),
        SCHEDULED(1),
        EXECUTING(2),
        COMPLETED(3),
        FAILED(4),
        CANCELED(5);

        private final int value;

        private TaskState(int value) {
            this.value = value;
        }

        private static Map<Integer, TaskState> map = new HashMap<>();

        static {
            for (TaskState state : TaskState.values()) {
                map.put(state.value, state);
            }
        }

        public static TaskState valueOf(int taskState) {
            return map.get(taskState);
        }

        /**
         * Get the integer value of this enum value, as defined in the Thrift IDL.
         */
        public int getValue() {
            return value;
        }
    }
}
