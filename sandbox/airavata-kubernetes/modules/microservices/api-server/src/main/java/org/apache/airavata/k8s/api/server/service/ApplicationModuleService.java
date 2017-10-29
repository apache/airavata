package org.apache.airavata.k8s.api.server.service;

import org.apache.airavata.k8s.api.server.model.application.ApplicationModule;
import org.apache.airavata.k8s.api.server.repository.ApplicationModuleRepository;
import org.apache.airavata.k8s.api.resources.application.ApplicationModuleResource;
import org.apache.airavata.k8s.api.server.service.util.ToResourceUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
@Service
public class ApplicationModuleService {

    private ApplicationModuleRepository applicationModuleRepository;

    @Autowired
    public ApplicationModuleService(ApplicationModuleRepository applicationModuleRepository) {
        this.applicationModuleRepository = applicationModuleRepository;
    }

    public long create(ApplicationModuleResource resource) {
        ApplicationModule module = new ApplicationModule();
        module.setName(resource.getName());
        module.setVersion(resource.getVersion());
        module.setDescription(resource.getDescription());
        ApplicationModule saved = this.applicationModuleRepository.save(module);
        return saved.getId();
    }

    public Optional<ApplicationModuleResource> findById(long id) {
        return ToResourceUtil.toResource(applicationModuleRepository.findById(id).get());
    }

    public List<ApplicationModuleResource> getAll() {
        List<ApplicationModuleResource> computeList = new ArrayList<>();
        Optional.ofNullable(applicationModuleRepository.findAll())
                .ifPresent(computes ->
                        computes.forEach(compute -> computeList.add(ToResourceUtil.toResource(compute).get())));
        return computeList;
    }
}
