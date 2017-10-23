package org.apache.airavata.k8s.api.server.model.task;

import javax.persistence.*;

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

    public enum TaskState {
        CREATED(0),
        EXECUTING(1),
        COMPLETED(2),
        FAILED(3),
        CANCELED(4);

        private final int value;

        private TaskState(int value) {
            this.value = value;
        }

        /**
         * Get the integer value of this enum value, as defined in the Thrift IDL.
         */
        public int getValue() {
            return value;
        }
    }
}
