package org.apache.airavata.compute.mapper;

import org.apache.airavata.compute.dto.SlurmPartitionRequestDto;
import org.apache.airavata.compute.dto.SlurmPartitionResponseDto;
import org.apache.airavata.compute.model.SlurmPartitionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

/**
 * Entity/DTO conversions for Slurm partitions. The owning cluster is resolved by the
 * service from the path, so it is never mapped off the request body.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SlurmPartitionMapper {

    @Mapping(target = "clusterId", source = "slurmCluster.clusterId")
    SlurmPartitionResponseDto toResponseDto(SlurmPartitionEntity entity);

    @Mapping(target = "partitionId", ignore = true)
    @Mapping(target = "slurmCluster", ignore = true)
    SlurmPartitionEntity toEntity(SlurmPartitionRequestDto dto);

    @Mapping(target = "partitionId", ignore = true)
    @Mapping(target = "slurmCluster", ignore = true)
    void updateEntity(SlurmPartitionRequestDto dto, @MappingTarget SlurmPartitionEntity entity);
}
