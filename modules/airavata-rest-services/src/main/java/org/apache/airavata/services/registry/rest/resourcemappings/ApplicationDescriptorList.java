package org.apache.airavata.services.registry.rest.resourcemappings;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ApplicationDescriptorList {
    private ApplicationDescriptor[] applicationDescriptors = new ApplicationDescriptor[]{};

    public ApplicationDescriptorList() {
    }

    public ApplicationDescriptor[] getApplicationDescriptors() {
        return applicationDescriptors;
    }

    public void setApplicationDescriptors(ApplicationDescriptor[] applicationDescriptors) {
        this.applicationDescriptors = applicationDescriptors;
    }
}

