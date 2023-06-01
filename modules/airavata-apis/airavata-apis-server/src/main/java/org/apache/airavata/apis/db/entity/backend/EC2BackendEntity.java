package org.apache.airavata.apis.db.entity.backend;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
public class EC2BackendEntity extends ComputeBackendEntity {

    @Column
    String flavor;
    @Column
    String region;

    @Column
    String awsCredentialId;

    public String getFlavor() {
        return flavor;
    }

    public void setFlavor(String flavor) {
        this.flavor = flavor;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getAwsCredentialId() {
        return awsCredentialId;
    }

    public void setAwsCredentialId(String awsCredentialId) {
        this.awsCredentialId = awsCredentialId;
    }

}
