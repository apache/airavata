package org.apache.airavata.process.mapper;

import org.apache.airavata.application.mapper.BatchJobConfigMapper;
import org.apache.airavata.process.dto.BatchJobProcessRequestDto;
import org.apache.airavata.process.dto.BatchJobProcessResponseDto;
import org.apache.airavata.process.model.BatchJobProcess;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

/**
 * Entity/DTO conversion for batch job processes.
 *
 * <p>The deployment and the user are resolved by {@code BatchJobProcessService} from the
 * request DTO's ids and the caller's access token, so neither is ever mapped straight off
 * the request. {@code batchJobConfig} is different: it is owned outright by the process (no
 * id to resolve), so it maps directly via {@link BatchJobConfigMapper} — on update,
 * MapStruct updates the existing nested entity in place rather than replacing it, preserving
 * its row across edits.
 */
@Mapper(
        componentModel = "spring",
        uses = BatchJobConfigMapper.class,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BatchJobProcessMapper {

    @Mapping(target = "deploymentId", source = "batchApplicationDeployment.deploymentId")
    @Mapping(target = "userId", source = "user.userId")
    @Mapping(target = "batchJobConfig", source = "batchJobConfigs")
    BatchJobProcessResponseDto toResponseDto(BatchJobProcess entity);

    @Mapping(target = "processId", ignore = true)
    @Mapping(target = "batchApplicationDeployment", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "batchJobConfigs", source = "batchJobConfig")
    BatchJobProcess toEntity(BatchJobProcessRequestDto dto);

    @Mapping(target = "processId", ignore = true)
    @Mapping(target = "batchApplicationDeployment", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "batchJobConfigs", source = "batchJobConfig")
    void updateEntity(BatchJobProcessRequestDto dto, @MappingTarget BatchJobProcess entity);
}
