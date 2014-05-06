package org.apache.airavata.model.util;

import org.apache.airavata.model.workspace.Project;

public class ProjectModelUtil {
    public static Project createProject (String projectName, String owner,String description){
        Project project = new Project();
        project.setName(projectName);
        project.setOwner(owner);
        project.setDescription(description);
        return project;
    }

}
