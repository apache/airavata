package org.apache.airavata.compute.mapper;

import org.apache.airavata.compute.dto.ClusterRequestDto;
import org.apache.airavata.compute.dto.ClusterResponseDto;
import org.apache.airavata.compute.model.ClusterEntity;
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
        uses = ClusterPartitionMapper.class,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ClusterMapper {

    ClusterResponseDto toResponseDto(ClusterEntity entity);

    @Mapping(target = "clusterId", ignore = true)
    @Mapping(target = "partitions", ignore = true)
    ClusterEntity toEntity(ClusterRequestDto dto);

    @Mapping(target = "clusterId", ignore = true)
    @Mapping(target = "partitions", ignore = true)
    void updateEntity(ClusterRequestDto dto, @MappingTarget ClusterEntity entity);
}
