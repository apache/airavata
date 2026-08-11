package org.apache.airavata.compute.service;

import java.util.List;
import org.apache.airavata.compute.dto.ClusterPartitionRequestDto;
import org.apache.airavata.compute.dto.ClusterPartitionResponseDto;
import org.apache.airavata.compute.mapper.ClusterPartitionMapper;
import org.apache.airavata.compute.model.ClusterEntity;
import org.apache.airavata.compute.model.ClusterPartitionEntity;
import org.apache.airavata.compute.repository.ClusterPartitionRepository;
import org.apache.airavata.compute.repository.ClusterRepository;
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
 * {@link ClusterEntity#getPartitions()}: the association is orphan-removing, so
 * touching that collection here would risk deleting rows this service never intended
 * to remove.
 */
@Service
public class ClusterPartitionService {

    private final ClusterPartitionRepository partitionRepository;
    private final ClusterRepository clusterRepository;
    private final ClusterPartitionMapper mapper;

    public ClusterPartitionService(
            ClusterPartitionRepository partitionRepository,
            ClusterRepository clusterRepository,
            ClusterPartitionMapper mapper) {
        this.partitionRepository = partitionRepository;
        this.clusterRepository = clusterRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<ClusterPartitionResponseDto> getPartitions(String clusterId) {
        requireCluster(clusterId);
        return partitionRepository.findBySlurmCluster_ClusterId(clusterId).stream()
                .map(mapper::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public ClusterPartitionResponseDto getPartition(String clusterId, String partitionId) {
        return mapper.toResponseDto(findOrThrow(clusterId, partitionId));
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPER_ADMIN')")
    @Transactional
    public ClusterPartitionResponseDto createPartition(String clusterId, ClusterPartitionRequestDto request) {
        ClusterPartitionEntity entity = mapper.toEntity(request);
        entity.setSlurmCluster(requireCluster(clusterId));
        return mapper.toResponseDto(partitionRepository.save(entity));
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPER_ADMIN')")
    @Transactional
    public ClusterPartitionResponseDto updatePartition(
            String clusterId, String partitionId, ClusterPartitionRequestDto request) {
        ClusterPartitionEntity entity = findOrThrow(clusterId, partitionId);
        mapper.updateEntity(request, entity);
        return mapper.toResponseDto(partitionRepository.save(entity));
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPER_ADMIN')")
    @Transactional
    public void deletePartition(String clusterId, String partitionId) {
        partitionRepository.delete(findOrThrow(clusterId, partitionId));
    }

    private ClusterEntity requireCluster(String clusterId) {
        return clusterRepository
                .findById(clusterId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cluster not found: " + clusterId));
    }

    private ClusterPartitionEntity findOrThrow(String clusterId, String partitionId) {
        return partitionRepository
                .findByPartitionIdAndSlurmCluster_ClusterId(partitionId, clusterId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Partition not found: " + partitionId + " in cluster " + clusterId));
    }
}
