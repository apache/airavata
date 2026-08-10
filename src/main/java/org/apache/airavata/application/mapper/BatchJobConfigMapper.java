package org.apache.airavata.application.mapper;

import org.apache.airavata.application.dto.deployment.BatchJobConfigRequestDto;
import org.apache.airavata.application.dto.deployment.BatchJobConfigResponseDto;
import org.apache.airavata.application.model.deployment.BatchJobConfigs;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

/**
 * Entity/DTO conversions for {@link BatchJobConfigs}. Unlike the deployment's other
 * associations (template, cluster, credential), this one is owned outright — there is no
 * id to resolve, so it is mapped directly rather than by the service.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BatchJobConfigMapper {

    BatchJobConfigResponseDto toResponseDto(BatchJobConfigs entity);

    @Mapping(target = "batchJobConfigId", ignore = true)
    BatchJobConfigs toEntity(BatchJobConfigRequestDto dto);

    @Mapping(target = "batchJobConfigId", ignore = true)
    void updateEntity(BatchJobConfigRequestDto dto, @MappingTarget BatchJobConfigs entity);
}
