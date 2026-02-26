package org.apache.airavata.compute.provider.slurm;

import java.util.List;
import org.apache.airavata.compute.resource.model.ResourceJobManagerType;

public class ResourceConfig {
    private ResourceJobManagerType jobManagerType;
    private String emailParser;
    private List<String> resourceEmailAddresses;

    public ResourceJobManagerType getJobManagerType() {
        return jobManagerType;
    }

    public void setJobManagerType(ResourceJobManagerType jobManagerType) {
        this.jobManagerType = jobManagerType;
    }

    public String getEmailParser() {
        return emailParser;
    }

    public void setEmailParser(String emailParser) {
        this.emailParser = emailParser;
    }

    public List<String> getResourceEmailAddresses() {
        return resourceEmailAddresses;
    }

    public void setResourceEmailAddresses(List<String> resourceEmailAddresses) {
        this.resourceEmailAddresses = resourceEmailAddresses;
    }
}
