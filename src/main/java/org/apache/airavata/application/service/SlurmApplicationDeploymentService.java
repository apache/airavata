package org.apache.airavata.application.service;

import java.util.List;
import org.apache.airavata.application.dto.deployment.SlurmApplicationDeploymentRequestDto;
import org.apache.airavata.application.dto.deployment.SlurmApplicationDeploymentResponseDto;
import org.apache.airavata.application.mapper.SlurmApplicationDeploymentMapper;
import org.apache.airavata.application.model.deployment.SlurmApplicationDeploymentEntity;
import org.apache.airavata.application.model.template.ApplicationTemplateEntity;
import org.apache.airavata.application.repository.ApplicationTemplateRepository;
import org.apache.airavata.application.repository.SlurmApplicationDeploymentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** CRUD for Slurm deployments of an application template. */
@Service
public class SlurmApplicationDeploymentService {

    private final SlurmApplicationDeploymentRepository deploymentRepository;
    private final ApplicationTemplateRepository templateRepository;
    private final SlurmApplicationDeploymentMapper mapper;

    public SlurmApplicationDeploymentService(
            SlurmApplicationDeploymentRepository deploymentRepository,
            ApplicationTemplateRepository templateRepository,
            SlurmApplicationDeploymentMapper mapper) {
        this.deploymentRepository = deploymentRepository;
        this.templateRepository = templateRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<SlurmApplicationDeploymentResponseDto> getAllDeployments() {
        return deploymentRepository.findAll().stream().map(mapper::toResponseDto).toList();
    }

    @Transactional(readOnly = true)
    public List<SlurmApplicationDeploymentResponseDto> getDeploymentsByTemplate(String templateId) {
        return deploymentRepository.findByApplicationTemplate_TemplateId(templateId).stream()
                .map(mapper::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public SlurmApplicationDeploymentResponseDto getDeployment(String deploymentId) {
        return mapper.toResponseDto(findOrThrow(deploymentId));
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPER_ADMIN')")
    @Transactional
    public SlurmApplicationDeploymentResponseDto createDeployment(SlurmApplicationDeploymentRequestDto request) {
        SlurmApplicationDeploymentEntity entity = mapper.toEntity(request);
        entity.setApplicationTemplate(resolveTemplate(request.getTemplateId()));
        return mapper.toResponseDto(deploymentRepository.save(entity));
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPER_ADMIN')")
    @Transactional
    public SlurmApplicationDeploymentResponseDto updateDeployment(
            String deploymentId, SlurmApplicationDeploymentRequestDto request) {
        SlurmApplicationDeploymentEntity entity = findOrThrow(deploymentId);
        mapper.updateEntity(request, entity);
        entity.setApplicationTemplate(resolveTemplate(request.getTemplateId()));
        return mapper.toResponseDto(deploymentRepository.save(entity));
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPER_ADMIN')")
    @Transactional
    public void deleteDeployment(String deploymentId) {
        deploymentRepository.delete(findOrThrow(deploymentId));
    }

    private SlurmApplicationDeploymentEntity findOrThrow(String deploymentId) {
        return deploymentRepository
                .findById(deploymentId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Deployment not found: " + deploymentId));
    }

    private ApplicationTemplateEntity resolveTemplate(String templateId) {
        return templateRepository
                .findById(templateId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Template not found: " + templateId));
    }
}
