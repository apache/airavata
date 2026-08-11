package org.apache.airavata.compute.service;

import java.util.List;
import org.apache.airavata.compute.dto.ClusterRequestDto;
import org.apache.airavata.compute.dto.ClusterResponseDto;
import org.apache.airavata.compute.mapper.ClusterMapper;
import org.apache.airavata.compute.model.ClusterEntity;
import org.apache.airavata.compute.repository.ClusterRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * CRUD for Slurm clusters.
 *
 * <p>Reads run inside a transaction so the lazily loaded partition collection can be
 * mapped to DTOs before the session closes — {@code spring.jpa.open-in-view=false} means
 * it cannot be walked from the serialization layer.
 *
 * <p>Deleting a cluster cascades to its partitions, which are owned by it.
 */
@Service
public class ClusterService {

    private final ClusterRepository clusterRepository;
    private final ClusterMapper mapper;

    public ClusterService(ClusterRepository clusterRepository, ClusterMapper mapper) {
        this.clusterRepository = clusterRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<ClusterResponseDto> getAllClusters() {
        return clusterRepository.findAll().stream().map(mapper::toResponseDto).toList();
    }

    @Transactional(readOnly = true)
    public ClusterResponseDto getCluster(String clusterId) {
        return mapper.toResponseDto(findOrThrow(clusterId));
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPER_ADMIN')")
    @Transactional
    public ClusterResponseDto createCluster(ClusterRequestDto request) {
        ClusterEntity entity = mapper.toEntity(request);
        return mapper.toResponseDto(clusterRepository.save(entity));
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPER_ADMIN')")
    @Transactional
    public ClusterResponseDto updateCluster(String clusterId, ClusterRequestDto request) {
        ClusterEntity entity = findOrThrow(clusterId);
        mapper.updateEntity(request, entity);
        return mapper.toResponseDto(clusterRepository.save(entity));
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPER_ADMIN')")
    @Transactional
    public void deleteCluster(String clusterId) {
        clusterRepository.delete(findOrThrow(clusterId));
    }

    private ClusterEntity findOrThrow(String clusterId) {
        return clusterRepository
                .findById(clusterId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cluster not found: " + clusterId));
    }
}
