package org.apache.airavata.compute.service;

import java.util.List;
import org.apache.airavata.compute.dto.SlurmPartitionRequestDto;
import org.apache.airavata.compute.dto.SlurmPartitionResponseDto;
import org.apache.airavata.compute.mapper.SlurmPartitionMapper;
import org.apache.airavata.compute.model.SlurmClusterEntity;
import org.apache.airavata.compute.model.SlurmPartitionEntity;
import org.apache.airavata.compute.repository.SlurmClusterRepository;
import org.apache.airavata.compute.repository.SlurmPartitionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * CRUD for the partitions of a Slurm cluster.
 *
 * <p>Every operation is scoped to a cluster id taken from the request path, so a
 * partition belonging to one cluster is a 404 under another cluster's path rather than
 * an accidental cross-cluster edit.
 *
 * <p>Partitions are persisted through their own repository rather than by mutating
 * {@link SlurmClusterEntity#getPartitions()}: the association is orphan-removing, so
 * touching that collection here would risk deleting rows this service never intended
 * to remove.
 */
@Service
public class SlurmPartitionService {

    private final SlurmPartitionRepository partitionRepository;
    private final SlurmClusterRepository clusterRepository;
    private final SlurmPartitionMapper mapper;

    public SlurmPartitionService(
            SlurmPartitionRepository partitionRepository,
            SlurmClusterRepository clusterRepository,
            SlurmPartitionMapper mapper) {
        this.partitionRepository = partitionRepository;
        this.clusterRepository = clusterRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<SlurmPartitionResponseDto> getPartitions(String clusterId) {
        requireCluster(clusterId);
        return partitionRepository.findBySlurmCluster_ClusterId(clusterId).stream()
                .map(mapper::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public SlurmPartitionResponseDto getPartition(String clusterId, String partitionId) {
        return mapper.toResponseDto(findOrThrow(clusterId, partitionId));
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPER_ADMIN')")
    @Transactional
    public SlurmPartitionResponseDto createPartition(String clusterId, SlurmPartitionRequestDto request) {
        SlurmPartitionEntity entity = mapper.toEntity(request);
        entity.setSlurmCluster(requireCluster(clusterId));
        return mapper.toResponseDto(partitionRepository.save(entity));
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPER_ADMIN')")
    @Transactional
    public SlurmPartitionResponseDto updatePartition(
            String clusterId, String partitionId, SlurmPartitionRequestDto request) {
        SlurmPartitionEntity entity = findOrThrow(clusterId, partitionId);
        mapper.updateEntity(request, entity);
        return mapper.toResponseDto(partitionRepository.save(entity));
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPER_ADMIN')")
    @Transactional
    public void deletePartition(String clusterId, String partitionId) {
        partitionRepository.delete(findOrThrow(clusterId, partitionId));
    }

    private SlurmClusterEntity requireCluster(String clusterId) {
        return clusterRepository
                .findById(clusterId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cluster not found: " + clusterId));
    }

    private SlurmPartitionEntity findOrThrow(String clusterId, String partitionId) {
        return partitionRepository
                .findByPartitionIdAndSlurmCluster_ClusterId(partitionId, clusterId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Partition not found: " + partitionId + " in cluster " + clusterId));
    }
}
