package org.apache.airavata.research.service;

import org.apache.airavata.research.service.enums.PrivacyEnum;
import org.apache.airavata.research.service.enums.StatusEnum;
import org.apache.airavata.research.service.model.entity.DatasetResource;
import org.apache.airavata.research.service.model.entity.Project;
import org.apache.airavata.research.service.model.entity.RepositoryResource;
import org.apache.airavata.research.service.model.entity.User;
import org.apache.airavata.research.service.model.repo.ProjectRepository;
import org.apache.airavata.research.service.model.repo.ResourceRepository;
import org.apache.airavata.research.service.model.repo.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ResourceRepository resourceRepository;

    public DataInitializer(UserRepository userRepository, ProjectRepository projectRepository, ResourceRepository resourceRepository) {
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.resourceRepository = resourceRepository;
    }

    @Override
    public void run(String... args) {
        cleanup();

        User user = new User("airavata@apache.org", "airavata", "admin", "airavata@apache.org");
        userRepository.save(user);

        RepositoryResource repositoryResource = new RepositoryResource();
        repositoryResource.setName("BMTK Repository");
        repositoryResource.setDescription("Repository for the BMTK workshop project");
        repositoryResource.setHeaderImage("header_image.png");
        repositoryResource.setRepositoryUrl("https://github.com/AllenInstitute/bmtk-workshop.git");
        repositoryResource.setStatus(StatusEnum.VERIFIED);
        repositoryResource.setPrivacy(PrivacyEnum.PUBLIC);
        repositoryResource = resourceRepository.save(repositoryResource);

        DatasetResource datasetResource = new DatasetResource();
        datasetResource.setName("BMTK Dataset");
        datasetResource.setDescription("Dataset for the BMTK workshop project");
        datasetResource.setHeaderImage("header_image.png");
        datasetResource.setDatasetUrl("bmtk");
        datasetResource.setStatus(StatusEnum.VERIFIED);
        datasetResource.setPrivacy(PrivacyEnum.PUBLIC);
        datasetResource = resourceRepository.save(datasetResource);

        Project project = new Project();
        project.setRepositoryResource(repositoryResource);
        project.getDatasetResources().add(datasetResource);

        projectRepository.save(project);

        System.out.println("Initialized Project with id: " + project.getId());
    }

    public void cleanup() {
        userRepository.deleteAll();
        projectRepository.deleteAll();
        resourceRepository.deleteAll();
    }
}