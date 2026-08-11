package org.apache.airavata.api;

import jakarta.validation.Valid;
import java.util.List;
import org.apache.airavata.compute.dto.ClusterPartitionRequestDto;
import org.apache.airavata.compute.dto.ClusterPartitionResponseDto;
import org.apache.airavata.compute.service.ClusterPartitionService;
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

/** Partitions are a sub-resource of their cluster; every path is scoped by cluster id. */
@RestController
@RequestMapping("/api/v1/clusters/{clusterId}/partitions")
public class ClusterPartitionController {

    private final ClusterPartitionService clusterPartitionService;

    public ClusterPartitionController(ClusterPartitionService clusterPartitionService) {
        this.clusterPartitionService = clusterPartitionService;
    }

    @GetMapping
    public List<ClusterPartitionResponseDto> getPartitions(@PathVariable String clusterId) {
        return clusterPartitionService.getPartitions(clusterId);
    }

    @GetMapping("/{partitionId}")
    public ClusterPartitionResponseDto getPartition(
            @PathVariable String clusterId, @PathVariable String partitionId) {
        return clusterPartitionService.getPartition(clusterId, partitionId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClusterPartitionResponseDto createPartition(
            @PathVariable String clusterId, @Valid @RequestBody ClusterPartitionRequestDto request) {
        return clusterPartitionService.createPartition(clusterId, request);
    }

    @PutMapping("/{partitionId}")
    public ClusterPartitionResponseDto updatePartition(
            @PathVariable String clusterId,
            @PathVariable String partitionId,
            @Valid @RequestBody ClusterPartitionRequestDto request) {
        return clusterPartitionService.updatePartition(clusterId, partitionId, request);
    }

    @DeleteMapping("/{partitionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePartition(@PathVariable String clusterId, @PathVariable String partitionId) {
        clusterPartitionService.deletePartition(clusterId, partitionId);
    }
}
