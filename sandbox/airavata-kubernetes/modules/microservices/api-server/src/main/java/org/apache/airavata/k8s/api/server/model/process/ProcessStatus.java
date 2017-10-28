package org.apache.airavata.k8s.api.server.model.process;

import javax.persistence.*;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
@Entity
@Table(name = "PROCESS_STATUS")
public class ProcessStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @ManyToOne
    private ProcessModel processModel;
    private ProcessState state; // required
    private long timeOfStateChange; // optional
    private String reason; // optional

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public ProcessModel getProcessModel() {
        return processModel;
    }

    public void setProcessModel(ProcessModel processModel) {
        this.processModel = processModel;
    }

    public ProcessState getState() {
        return state;
    }

    public void setState(ProcessState state) {
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

    public enum ProcessState {
        CREATED(0),
        VALIDATED(1),
        STARTED(2),
        PRE_PROCESSING(3),
        CONFIGURING_WORKSPACE(4),
        INPUT_DATA_STAGING(5),
        EXECUTING(6),
        MONITORING(7),
        OUTPUT_DATA_STAGING(8),
        POST_PROCESSING(9),
        COMPLETED(10),
        FAILED(11),
        CANCELLING(12),
        CANCELED(13);

        private final int value;

        private ProcessState(int value) {
            this.value = value;
        }
    }
}
