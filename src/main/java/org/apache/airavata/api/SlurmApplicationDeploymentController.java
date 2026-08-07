package org.apache.airavata.api;

import jakarta.validation.Valid;
import java.util.List;
import org.apache.airavata.application.dto.deployment.SlurmApplicationDeploymentRequestDto;
import org.apache.airavata.application.dto.deployment.SlurmApplicationDeploymentResponseDto;
import org.apache.airavata.application.service.SlurmApplicationDeploymentService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/slurm-deployments")
public class SlurmApplicationDeploymentController {

    private final SlurmApplicationDeploymentService slurmApplicationDeploymentService;

    public SlurmApplicationDeploymentController(SlurmApplicationDeploymentService slurmApplicationDeploymentService) {
        this.slurmApplicationDeploymentService = slurmApplicationDeploymentService;
    }

    /** Lists every deployment, or only those of {@code templateId} when supplied. */
    @GetMapping
    public List<SlurmApplicationDeploymentResponseDto> getAllDeployments(
            @RequestParam(required = false) String templateId) {
        return templateId == null
                ? slurmApplicationDeploymentService.getAllDeployments()
                : slurmApplicationDeploymentService.getDeploymentsByTemplate(templateId);
    }

    @GetMapping("/{deploymentId}")
    public SlurmApplicationDeploymentResponseDto getDeployment(@PathVariable String deploymentId) {
        return slurmApplicationDeploymentService.getDeployment(deploymentId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SlurmApplicationDeploymentResponseDto createDeployment(
            @Valid @RequestBody SlurmApplicationDeploymentRequestDto request) {
        return slurmApplicationDeploymentService.createDeployment(request);
    }

    @PutMapping("/{deploymentId}")
    public SlurmApplicationDeploymentResponseDto updateDeployment(
            @PathVariable String deploymentId, @Valid @RequestBody SlurmApplicationDeploymentRequestDto request) {
        return slurmApplicationDeploymentService.updateDeployment(deploymentId, request);
    }

    @DeleteMapping("/{deploymentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDeployment(@PathVariable String deploymentId) {
        slurmApplicationDeploymentService.deleteDeployment(deploymentId);
    }
}
