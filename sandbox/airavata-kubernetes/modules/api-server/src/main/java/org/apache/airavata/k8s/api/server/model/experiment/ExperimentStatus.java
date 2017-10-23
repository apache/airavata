package org.apache.airavata.k8s.api.server.model.experiment;

import javax.persistence.*;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
@Entity
@Table(name = "EXPERIMENT_STATUS")
public class ExperimentStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private ExperimentState state; // required
    private long timeOfStateChange; // optional
    private String reason; // optional

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public ExperimentState getState() {
        return state;
    }

    public void setState(ExperimentState state) {
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

    public enum ExperimentState {
        CREATED(0),
        VALIDATED(1),
        SCHEDULED(2),
        LAUNCHED(3),
        EXECUTING(4),
        CANCELING(5),
        CANCELED(6),
        COMPLETED(7),
        FAILED(8);

        private final int value;

        private ExperimentState(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }
}
