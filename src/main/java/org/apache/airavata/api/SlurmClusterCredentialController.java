package org.apache.airavata.api;

import jakarta.validation.Valid;
import java.util.List;
import org.apache.airavata.compute.dto.SlurmClusterCredentialRequestDto;
import org.apache.airavata.compute.dto.SlurmClusterCredentialResponseDto;
import org.apache.airavata.compute.service.SlurmClusterCredentialService;
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

/** The owning user is never a request parameter — see {@code SlurmClusterCredentialService.createCredential}. */
@RestController
@RequestMapping("/api/v1/slurm-cluster-credentials")
public class SlurmClusterCredentialController {

    private final SlurmClusterCredentialService slurmClusterCredentialService;

    public SlurmClusterCredentialController(SlurmClusterCredentialService slurmClusterCredentialService) {
        this.slurmClusterCredentialService = slurmClusterCredentialService;
    }

    /** Every binding across every user — admin only. Optionally scoped to {@code clusterId}. */
    @GetMapping
    public List<SlurmClusterCredentialResponseDto> getAllCredentials(
            @RequestParam(required = false) String clusterId) {
        return slurmClusterCredentialService.getAllCredentials(clusterId);
    }

    /** The caller's own bindings. Optionally scoped to {@code clusterId}. */
    @GetMapping("/me")
    public List<SlurmClusterCredentialResponseDto> getMyCredentials(
            @RequestParam(required = false) String clusterId) {
        return slurmClusterCredentialService.getMyCredentials(clusterId);
    }

    @GetMapping("/{id}")
    public SlurmClusterCredentialResponseDto getCredential(@PathVariable String id) {
        return slurmClusterCredentialService.getCredential(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SlurmClusterCredentialResponseDto createCredential(
            @Valid @RequestBody SlurmClusterCredentialRequestDto request) {
        return slurmClusterCredentialService.createCredential(request);
    }

    @PutMapping("/{id}")
    public SlurmClusterCredentialResponseDto updateCredential(
            @PathVariable String id, @Valid @RequestBody SlurmClusterCredentialRequestDto request) {
        return slurmClusterCredentialService.updateCredential(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCredential(@PathVariable String id) {
        slurmClusterCredentialService.deleteCredential(id);
    }
}
