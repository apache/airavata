package org.apache.airavata.api;

import jakarta.validation.Valid;
import java.util.List;
import org.apache.airavata.application.dto.template.ApplicationTemplateRequestDto;
import org.apache.airavata.application.dto.template.ApplicationTemplateResponseDto;
import org.apache.airavata.application.service.ApplicationTemplateService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/application-templates")
public class ApplicationTemplateController {

    private final ApplicationTemplateService applicationTemplateService;

    public ApplicationTemplateController(ApplicationTemplateService applicationTemplateService) {
        this.applicationTemplateService = applicationTemplateService;
    }

    @GetMapping
    public List<ApplicationTemplateResponseDto> getAllTemplates() {
        return applicationTemplateService.getAllTemplates();
    }

    @GetMapping("/{templateId}")
    public ApplicationTemplateResponseDto getTemplate(@PathVariable String templateId) {
        return applicationTemplateService.getTemplate(templateId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApplicationTemplateResponseDto createTemplate(@Valid @RequestBody ApplicationTemplateRequestDto request) {
        return applicationTemplateService.createTemplate(request);
    }

    @PutMapping("/{templateId}")
    public ApplicationTemplateResponseDto updateTemplate(
            @PathVariable String templateId, @Valid @RequestBody ApplicationTemplateRequestDto request) {
        return applicationTemplateService.updateTemplate(templateId, request);
    }

    @DeleteMapping("/{templateId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTemplate(@PathVariable String templateId) {
        applicationTemplateService.deleteTemplate(templateId);
    }
}
