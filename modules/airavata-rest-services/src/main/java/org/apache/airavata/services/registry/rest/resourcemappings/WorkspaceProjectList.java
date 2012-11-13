package org.apache.airavata.services.registry.rest.resourcemappings;


import org.apache.airavata.registry.api.WorkspaceProject;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class WorkspaceProjectList {
    private WorkspaceProject[] workspaceProjects = new WorkspaceProject[]{};

    public WorkspaceProjectList() {
    }

    public WorkspaceProject[] getWorkspaceProjects() {
        return workspaceProjects;
    }

    public void setWorkspaceProjects(WorkspaceProject[] workspaceProjects) {
        this.workspaceProjects = workspaceProjects;
    }
}
