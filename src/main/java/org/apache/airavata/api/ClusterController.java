package org.apache.airavata.api;

import jakarta.validation.Valid;
import java.util.List;
import org.apache.airavata.compute.dto.ClusterRequestDto;
import org.apache.airavata.compute.dto.ClusterResponseDto;
import org.apache.airavata.compute.service.ClusterService;
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
@RequestMapping("/api/v1/clusters")
public class ClusterController {

    private final ClusterService clusterService;

    public ClusterController(ClusterService clusterService) {
        this.clusterService = clusterService;
    }

    @GetMapping
    public List<ClusterResponseDto> getAllClusters() {
        return clusterService.getAllClusters();
    }

    @GetMapping("/{clusterId}")
    public ClusterResponseDto getCluster(@PathVariable String clusterId) {
        return clusterService.getCluster(clusterId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClusterResponseDto createCluster(@Valid @RequestBody ClusterRequestDto request) {
        return clusterService.createCluster(request);
    }

    @PutMapping("/{clusterId}")
    public ClusterResponseDto updateCluster(
            @PathVariable String clusterId, @Valid @RequestBody ClusterRequestDto request) {
        return clusterService.updateCluster(clusterId, request);
    }

    /** Deleting a cluster also deletes its partitions. */
    @DeleteMapping("/{clusterId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCluster(@PathVariable String clusterId) {
        clusterService.deleteCluster(clusterId);
    }
}
