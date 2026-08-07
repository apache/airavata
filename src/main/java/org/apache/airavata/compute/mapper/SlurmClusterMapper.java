package org.apache.airavata.compute.mapper;

import org.apache.airavata.compute.dto.SlurmClusterRequestDto;
import org.apache.airavata.compute.dto.SlurmClusterResponseDto;
import org.apache.airavata.compute.model.SlurmClusterEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

/**
 * Entity/DTO conversions for Slurm clusters. Partitions are read-only on this side:
 * they are mapped out to the response but never written from a cluster request.
 */
@Mapper(
        componentModel = "spring",
        uses = SlurmPartitionMapper.class,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SlurmClusterMapper {

    SlurmClusterResponseDto toResponseDto(SlurmClusterEntity entity);

    @Mapping(target = "clusterId", ignore = true)
    @Mapping(target = "partitions", ignore = true)
    SlurmClusterEntity toEntity(SlurmClusterRequestDto dto);

    @Mapping(target = "clusterId", ignore = true)
    @Mapping(target = "partitions", ignore = true)
    void updateEntity(SlurmClusterRequestDto dto, @MappingTarget SlurmClusterEntity entity);
}
