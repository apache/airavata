package org.apache.airavata.k8s.api.server.service;

import org.apache.airavata.k8s.api.server.ServerRuntimeException;
import org.apache.airavata.k8s.api.server.model.application.ApplicationDeployment;
import org.apache.airavata.k8s.api.server.repository.ApplicationDeploymentRepository;
import org.apache.airavata.k8s.api.server.repository.ApplicationModuleRepository;
import org.apache.airavata.k8s.api.server.repository.ComputeRepository;
import org.apache.airavata.k8s.api.resources.application.ApplicationDeploymentResource;
import org.apache.airavata.k8s.api.server.service.util.ToResourceUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
@Service
public class ApplicationDeploymentService {

    private ApplicationDeploymentRepository applicationDeploymentRepository;
    private ComputeRepository computeRepository;
    private ApplicationModuleRepository applicationModuleRepository;

    @Autowired
    public ApplicationDeploymentService(ApplicationDeploymentRepository applicationDeploymentRepository,
                                        ComputeRepository computeRepository,
                                        ApplicationModuleRepository applicationModuleRepository) {
        this.applicationDeploymentRepository = applicationDeploymentRepository;
        this.computeRepository = computeRepository;
        this.applicationModuleRepository = applicationModuleRepository;
    }

    public long create(ApplicationDeploymentResource resource) {
        ApplicationDeployment deployment = new ApplicationDeployment();
        deployment.setPreJobCommand(resource.getPreJobCommand());
        deployment.setPostJobCommand(resource.getPostJobCommand());
        deployment.setExecutablePath(resource.getExecutablePath());
        deployment.setComputeResource(computeRepository
                .findById(resource.getComputeResourceId())
                .orElseThrow(() -> new ServerRuntimeException("Can not find a compute resource with id " +
                        resource.getComputeResourceId())));
        deployment.setApplicationModule(applicationModuleRepository
                .findById(resource.getApplicationModuleId())
                .orElseThrow(() -> new ServerRuntimeException("Can not find an app module with id "
                        + resource.getApplicationModuleId())));
        ApplicationDeployment saved = applicationDeploymentRepository.save(deployment);
        return saved.getId();
    }

    public Optional<ApplicationDeploymentResource> findById(long id) {
        return ToResourceUtil.toResource(applicationDeploymentRepository.findById(id).get());
    }
}
