package org.apache.airavata.application.service;

import java.util.List;
import org.apache.airavata.application.dto.deployment.BatchApplicationDeploymentRequestDto;
import org.apache.airavata.application.dto.deployment.BatchApplicationDeploymentResponseDto;
import org.apache.airavata.application.mapper.BatchApplicationDeploymentMapper;
import org.apache.airavata.application.model.deployment.BatchApplicationDeploymentEntity;
import org.apache.airavata.application.model.template.ApplicationTemplateEntity;
import org.apache.airavata.application.repository.ApplicationTemplateRepository;
import org.apache.airavata.application.repository.BatchApplicationDeploymentRepository;
import org.apache.airavata.compute.model.SlurmClusterEntity;
import org.apache.airavata.compute.repository.SlurmClusterRepository;
import org.apache.airavata.credentials.model.SSHUserCredential;
import org.apache.airavata.credentials.repository.SSHUserCredentialRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** CRUD for batch application deployments of an application template. */
@Service
public class BatchApplicationDeploymentService {

    private final BatchApplicationDeploymentRepository deploymentRepository;
    private final ApplicationTemplateRepository templateRepository;
    private final SlurmClusterRepository clusterRepository;
    private final SSHUserCredentialRepository credentialRepository;
    private final BatchApplicationDeploymentMapper mapper;

    public BatchApplicationDeploymentService(
            BatchApplicationDeploymentRepository deploymentRepository,
            ApplicationTemplateRepository templateRepository,
            SlurmClusterRepository clusterRepository,
            SSHUserCredentialRepository credentialRepository,
            BatchApplicationDeploymentMapper mapper) {
        this.deploymentRepository = deploymentRepository;
        this.templateRepository = templateRepository;
        this.clusterRepository = clusterRepository;
        this.credentialRepository = credentialRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<BatchApplicationDeploymentResponseDto> getAllDeployments() {
        return deploymentRepository.findAll().stream().map(mapper::toResponseDto).toList();
    }

    @Transactional(readOnly = true)
    public List<BatchApplicationDeploymentResponseDto> getDeploymentsByTemplate(String templateId) {
        return deploymentRepository.findByApplicationTemplate_TemplateId(templateId).stream()
                .map(mapper::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public BatchApplicationDeploymentResponseDto getDeployment(String deploymentId) {
        return mapper.toResponseDto(findOrThrow(deploymentId));
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPER_ADMIN')")
    @Transactional
    public BatchApplicationDeploymentResponseDto createDeployment(BatchApplicationDeploymentRequestDto request) {
        BatchApplicationDeploymentEntity entity = mapper.toEntity(request);
        entity.setApplicationTemplate(resolveTemplate(request.getTemplateId()));
        entity.setSlurmCluster(resolveCluster(request.getSlurmClusterId()));
        entity.setDefaultSubmissionCredential(resolveCredential(request.getDefaultSubmissionCredentialId()));
        return mapper.toResponseDto(deploymentRepository.save(entity));
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPER_ADMIN')")
    @Transactional
    public BatchApplicationDeploymentResponseDto updateDeployment(
            String deploymentId, BatchApplicationDeploymentRequestDto request) {
        BatchApplicationDeploymentEntity entity = findOrThrow(deploymentId);
        mapper.updateEntity(request, entity);
        entity.setApplicationTemplate(resolveTemplate(request.getTemplateId()));
        entity.setSlurmCluster(resolveCluster(request.getSlurmClusterId()));
        entity.setDefaultSubmissionCredential(resolveCredential(request.getDefaultSubmissionCredentialId()));
        return mapper.toResponseDto(deploymentRepository.save(entity));
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPER_ADMIN')")
    @Transactional
    public void deleteDeployment(String deploymentId) {
        deploymentRepository.delete(findOrThrow(deploymentId));
    }

    private BatchApplicationDeploymentEntity findOrThrow(String deploymentId) {
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

    /**
     * The cluster reference is optional — the column is nullable, so an absent id leaves
     * the deployment unattached rather than failing. A supplied id must resolve.
     */
    private SlurmClusterEntity resolveCluster(String clusterId) {
        if (clusterId == null || clusterId.isBlank()) {
            return null;
        }
        return clusterRepository
                .findById(clusterId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cluster not found: " + clusterId));
    }

    private SSHUserCredential resolveCredential(String credentialId) {
        return credentialRepository
                .findById(credentialId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "SSH credential not found: " + credentialId));
    }
}
