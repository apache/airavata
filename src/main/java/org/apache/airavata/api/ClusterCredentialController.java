package org.apache.airavata.api;

import jakarta.validation.Valid;
import java.util.List;
import org.apache.airavata.compute.dto.ClusterCredentialRequestDto;
import org.apache.airavata.compute.dto.ClusterCredentialResponseDto;
import org.apache.airavata.compute.service.ClusterCredentialService;
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

/** The owning user is never a request parameter — see {@code ClusterCredentialService.createCredential}. */
@RestController
@RequestMapping("/api/v1/cluster-credentials")
public class ClusterCredentialController {

    private final ClusterCredentialService clusterCredentialService;

    public ClusterCredentialController(ClusterCredentialService clusterCredentialService) {
        this.clusterCredentialService = clusterCredentialService;
    }

    /** Every binding across every user — admin only. Optionally scoped to {@code clusterId}. */
    @GetMapping
    public List<ClusterCredentialResponseDto> getAllCredentials(@RequestParam(required = false) String clusterId) {
        return clusterCredentialService.getAllCredentials(clusterId);
    }

    /** The caller's own bindings. Optionally scoped to {@code clusterId}. */
    @GetMapping("/me")
    public List<ClusterCredentialResponseDto> getMyCredentials(@RequestParam(required = false) String clusterId) {
        return clusterCredentialService.getMyCredentials(clusterId);
    }

    @GetMapping("/{id}")
    public ClusterCredentialResponseDto getCredential(@PathVariable String id) {
        return clusterCredentialService.getCredential(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClusterCredentialResponseDto createCredential(@Valid @RequestBody ClusterCredentialRequestDto request) {
        return clusterCredentialService.createCredential(request);
    }

    @PutMapping("/{id}")
    public ClusterCredentialResponseDto updateCredential(
            @PathVariable String id, @Valid @RequestBody ClusterCredentialRequestDto request) {
        return clusterCredentialService.updateCredential(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCredential(@PathVariable String id) {
        clusterCredentialService.deleteCredential(id);
    }
}
