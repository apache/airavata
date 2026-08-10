package org.apache.airavata.api;

import jakarta.validation.Valid;
import java.util.List;
import org.apache.airavata.application.dto.deployment.BatchApplicationDeploymentRequestDto;
import org.apache.airavata.application.dto.deployment.BatchApplicationDeploymentResponseDto;
import org.apache.airavata.application.service.BatchApplicationDeploymentService;
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
public class BatchApplicationDeploymentController {

    private final BatchApplicationDeploymentService batchApplicationDeploymentService;

    public BatchApplicationDeploymentController(BatchApplicationDeploymentService batchApplicationDeploymentService) {
        this.batchApplicationDeploymentService = batchApplicationDeploymentService;
    }

    /** Lists every deployment, or only those of {@code templateId} when supplied. */
    @GetMapping
    public List<BatchApplicationDeploymentResponseDto> getAllDeployments(
            @RequestParam(required = false) String templateId) {
        return templateId == null
                ? batchApplicationDeploymentService.getAllDeployments()
                : batchApplicationDeploymentService.getDeploymentsByTemplate(templateId);
    }

    @GetMapping("/{deploymentId}")
    public BatchApplicationDeploymentResponseDto getDeployment(@PathVariable String deploymentId) {
        return batchApplicationDeploymentService.getDeployment(deploymentId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BatchApplicationDeploymentResponseDto createDeployment(
            @Valid @RequestBody BatchApplicationDeploymentRequestDto request) {
        return batchApplicationDeploymentService.createDeployment(request);
    }

    @PutMapping("/{deploymentId}")
    public BatchApplicationDeploymentResponseDto updateDeployment(
            @PathVariable String deploymentId, @Valid @RequestBody BatchApplicationDeploymentRequestDto request) {
        return batchApplicationDeploymentService.updateDeployment(deploymentId, request);
    }

    @DeleteMapping("/{deploymentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDeployment(@PathVariable String deploymentId) {
        batchApplicationDeploymentService.deleteDeployment(deploymentId);
    }
}
