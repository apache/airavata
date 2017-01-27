package org.apache.airavata.testsuite.multitenantedairavata.utils;

/**
 * Created by Ajinkya on 12/14/16.
 */
public class ComputeResourceProperties {
    private String computeResourceId;
    private String jobSubmissionId;

    public String getComputeResourceId() {
        return computeResourceId;
    }

    public void setComputeResourceId(String computeResourceId) {
        this.computeResourceId = computeResourceId;
    }

    public String getJobSubmissionId() {
        return jobSubmissionId;
    }

    public void setJobSubmissionId(String jobSubmissionId) {
        this.jobSubmissionId = jobSubmissionId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ComputeResourceProperties{");
        sb.append("computeResourceId='").append(computeResourceId).append('\'');
        sb.append(", jobSubmissionId='").append(jobSubmissionId).append('\'');
        sb.append('}');
        return sb.toString();
    }
}

