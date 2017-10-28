package org.apache.airavata.k8s.api.server.model.job;

import javax.persistence.*;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
@Entity
@Table(name = "JOB_STATUS")
public class JobStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private JobState jobState;
    private long timeOfStateChange;
    private String reason;
    
    @ManyToOne
    private JobModel jobModel;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public JobState getJobState() {
        return jobState;
    }

    public void setJobState(JobState jobState) {
        this.jobState = jobState;
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

    public JobModel getJobModel() {
        return jobModel;
    }

    public JobStatus setJobModel(JobModel jobModel) {
        this.jobModel = jobModel;
        return this;
    }

    public enum JobState {
        SUBMITTED(0),
        QUEUED(1),
        ACTIVE(2),
        COMPLETE(3),
        CANCELED(4),
        FAILED(5),
        SUSPENDED(6),
        UNKNOWN(7);

        private final int value;

        private JobState(int value) {
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
