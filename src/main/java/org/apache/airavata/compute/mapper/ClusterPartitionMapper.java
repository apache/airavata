package org.apache.airavata.compute.mapper;

import org.apache.airavata.compute.dto.ClusterPartitionRequestDto;
import org.apache.airavata.compute.dto.ClusterPartitionResponseDto;
import org.apache.airavata.compute.model.ClusterPartitionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

/**
 * Entity/DTO conversions for Slurm partitions. The owning cluster is resolved by the
 * service from the path, so it is never mapped off the request body.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ClusterPartitionMapper {

    @Mapping(target = "clusterId", source = "slurmCluster.clusterId")
    ClusterPartitionResponseDto toResponseDto(ClusterPartitionEntity entity);

    @Mapping(target = "partitionId", ignore = true)
    @Mapping(target = "slurmCluster", ignore = true)
    ClusterPartitionEntity toEntity(ClusterPartitionRequestDto dto);

    @Mapping(target = "partitionId", ignore = true)
    @Mapping(target = "slurmCluster", ignore = true)
    void updateEntity(ClusterPartitionRequestDto dto, @MappingTarget ClusterPartitionEntity entity);
}
