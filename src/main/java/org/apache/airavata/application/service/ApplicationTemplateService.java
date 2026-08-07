package org.apache.airavata.application.service;

import java.util.List;
import org.apache.airavata.application.dto.template.ApplicationTemplateInputDto;
import org.apache.airavata.application.dto.template.ApplicationTemplateOutputDto;
import org.apache.airavata.application.dto.template.ApplicationTemplateRequestDto;
import org.apache.airavata.application.dto.template.ApplicationTemplateResponseDto;
import org.apache.airavata.application.mapper.ApplicationTemplateMapper;
import org.apache.airavata.application.model.template.ApplicationTemplateEntity;
import org.apache.airavata.application.model.template.ApplicationTemplateInputEntity;
import org.apache.airavata.application.model.template.ApplicationTemplateOutputEntity;
import org.apache.airavata.application.repository.ApplicationTemplateRepository;
import org.apache.airavata.application.repository.SlurmApplicationDeploymentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * CRUD for application templates and their owned input/output declarations.
 *
 * <p>Reads run inside a transaction so the lazily loaded input/output collections can be
 * mapped to DTOs before the session closes — {@code spring.jpa.open-in-view=false} means
 * they cannot be walked from the serialization layer.
 */
@Service
public class ApplicationTemplateService {

    private final ApplicationTemplateRepository templateRepository;
    private final SlurmApplicationDeploymentRepository deploymentRepository;
    private final ApplicationTemplateMapper mapper;

    public ApplicationTemplateService(
            ApplicationTemplateRepository templateRepository,
            SlurmApplicationDeploymentRepository deploymentRepository,
            ApplicationTemplateMapper mapper) {
        this.templateRepository = templateRepository;
        this.deploymentRepository = deploymentRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<ApplicationTemplateResponseDto> getAllTemplates() {
        return templateRepository.findAll().stream()
                .map(mapper::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public ApplicationTemplateResponseDto getTemplate(String templateId) {
        return mapper.toResponseDto(findOrThrow(templateId));
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPER_ADMIN')")
    @Transactional
    public ApplicationTemplateResponseDto createTemplate(ApplicationTemplateRequestDto request) {
        ApplicationTemplateEntity entity = new ApplicationTemplateEntity();
        entity.setTemplateName(request.getTemplateName());
        entity.setTemplateDescription(request.getTemplateDescription());
        replaceChildren(entity, request);
        return mapper.toResponseDto(templateRepository.save(entity));
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPER_ADMIN')")
    @Transactional
    public ApplicationTemplateResponseDto updateTemplate(String templateId, ApplicationTemplateRequestDto request) {
        ApplicationTemplateEntity entity = findOrThrow(templateId);
        entity.setTemplateName(request.getTemplateName());
        entity.setTemplateDescription(request.getTemplateDescription());
        replaceChildren(entity, request);
        return mapper.toResponseDto(templateRepository.save(entity));
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPER_ADMIN')")
    @Transactional
    public void deleteTemplate(String templateId) {
        ApplicationTemplateEntity entity = findOrThrow(templateId);
        if (deploymentRepository.existsByApplicationTemplate_TemplateId(templateId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Template has deployments and cannot be deleted: " + templateId);
        }
        templateRepository.delete(entity);
    }

    private ApplicationTemplateEntity findOrThrow(String templateId) {
        return templateRepository
                .findById(templateId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Template not found: " + templateId));
    }

    /**
     * Rewrites the owned inputs/outputs in place. The existing collections are mutated
     * rather than reassigned — swapping the list instance out from under an
     * orphan-removing association makes Hibernate throw.
     */
    private void replaceChildren(ApplicationTemplateEntity entity, ApplicationTemplateRequestDto request) {
        entity.getInputs().clear();
        if (request.getInputs() != null) {
            for (ApplicationTemplateInputDto dto : request.getInputs()) {
                ApplicationTemplateInputEntity input = mapper.toInputEntity(dto);
                input.setApplicationTemplate(entity);
                entity.getInputs().add(input);
            }
        }

        entity.getOutputs().clear();
        if (request.getOutputs() != null) {
            for (ApplicationTemplateOutputDto dto : request.getOutputs()) {
                ApplicationTemplateOutputEntity output = mapper.toOutputEntity(dto);
                output.setApplicationTemplate(entity);
                entity.getOutputs().add(output);
            }
        }
    }
}
