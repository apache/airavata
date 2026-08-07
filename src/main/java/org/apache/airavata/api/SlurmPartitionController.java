package org.apache.airavata.api;

import jakarta.validation.Valid;
import java.util.List;
import org.apache.airavata.compute.dto.SlurmPartitionRequestDto;
import org.apache.airavata.compute.dto.SlurmPartitionResponseDto;
import org.apache.airavata.compute.service.SlurmPartitionService;
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
@RequestMapping("/api/v1/slurm-clusters/{clusterId}/partitions")
public class SlurmPartitionController {

    private final SlurmPartitionService slurmPartitionService;

    public SlurmPartitionController(SlurmPartitionService slurmPartitionService) {
        this.slurmPartitionService = slurmPartitionService;
    }

    @GetMapping
    public List<SlurmPartitionResponseDto> getPartitions(@PathVariable String clusterId) {
        return slurmPartitionService.getPartitions(clusterId);
    }

    @GetMapping("/{partitionId}")
    public SlurmPartitionResponseDto getPartition(@PathVariable String clusterId, @PathVariable String partitionId) {
        return slurmPartitionService.getPartition(clusterId, partitionId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SlurmPartitionResponseDto createPartition(
            @PathVariable String clusterId, @Valid @RequestBody SlurmPartitionRequestDto request) {
        return slurmPartitionService.createPartition(clusterId, request);
    }

    @PutMapping("/{partitionId}")
    public SlurmPartitionResponseDto updatePartition(
            @PathVariable String clusterId,
            @PathVariable String partitionId,
            @Valid @RequestBody SlurmPartitionRequestDto request) {
        return slurmPartitionService.updatePartition(clusterId, partitionId, request);
    }

    @DeleteMapping("/{partitionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePartition(@PathVariable String clusterId, @PathVariable String partitionId) {
        slurmPartitionService.deletePartition(clusterId, partitionId);
    }
}
