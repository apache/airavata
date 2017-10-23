package org.apache.airavata.k8s.api.server.service;

import org.apache.airavata.k8s.api.server.model.application.ComputeResourceModel;
import org.apache.airavata.k8s.api.server.repository.ComputeRepository;
import org.apache.airavata.k8s.api.server.resources.compute.ComputeResource;
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
public class ComputeResourceService {
    private ComputeRepository computeRepository;

    @Autowired
    public ComputeResourceService(ComputeRepository computeRepository) {
        this.computeRepository = computeRepository;
    }

    public long create(ComputeResource resource) {
        ComputeResourceModel model = new ComputeResourceModel();
        model.setName(resource.getName());
        ComputeResourceModel saved = computeRepository.save(model);
        return saved.getId();
    }

    public Optional<ComputeResource> findById(long id) {
        return ToResourceUtil.toResource(computeRepository.findById(id).get());
    }
}
