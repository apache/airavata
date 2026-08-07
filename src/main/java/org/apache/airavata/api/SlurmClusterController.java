package org.apache.airavata.api;

import jakarta.validation.Valid;
import java.util.List;
import org.apache.airavata.compute.dto.SlurmClusterRequestDto;
import org.apache.airavata.compute.dto.SlurmClusterResponseDto;
import org.apache.airavata.compute.service.SlurmClusterService;
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
@RequestMapping("/api/v1/slurm-clusters")
public class SlurmClusterController {

    private final SlurmClusterService slurmClusterService;

    public SlurmClusterController(SlurmClusterService slurmClusterService) {
        this.slurmClusterService = slurmClusterService;
    }

    @GetMapping
    public List<SlurmClusterResponseDto> getAllClusters() {
        return slurmClusterService.getAllClusters();
    }

    @GetMapping("/{clusterId}")
    public SlurmClusterResponseDto getCluster(@PathVariable String clusterId) {
        return slurmClusterService.getCluster(clusterId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SlurmClusterResponseDto createCluster(@Valid @RequestBody SlurmClusterRequestDto request) {
        return slurmClusterService.createCluster(request);
    }

    @PutMapping("/{clusterId}")
    public SlurmClusterResponseDto updateCluster(
            @PathVariable String clusterId, @Valid @RequestBody SlurmClusterRequestDto request) {
        return slurmClusterService.updateCluster(clusterId, request);
    }

    /** Deleting a cluster also deletes its partitions. */
    @DeleteMapping("/{clusterId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCluster(@PathVariable String clusterId) {
        slurmClusterService.deleteCluster(clusterId);
    }
}
