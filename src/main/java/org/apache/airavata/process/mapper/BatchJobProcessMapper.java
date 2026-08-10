package org.apache.airavata.process.mapper;

import org.apache.airavata.application.mapper.BatchJobConfigMapper;
import org.apache.airavata.process.dto.BatchJobProcessResponseDto;
import org.apache.airavata.process.model.BatchJobProcess;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * Entity/DTO conversion for batch job processes.
 *
 * <p>There is no {@code toEntity}/{@code updateEntity} here: every field on
 * {@link BatchJobProcess} is an association (deployment, user, credential, batch job
 * config) and every one of them is resolved by {@code BatchJobProcessService} — from the
 * request DTO's ids, from the deployment, or from the caller's access token — so there is
 * nothing left for a mapper to write onto the entity.
 */
@Mapper(
        componentModel = "spring",
        uses = BatchJobConfigMapper.class,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BatchJobProcessMapper {

    @Mapping(target = "deploymentId", source = "batchApplicationDeployment.deploymentId")
    @Mapping(target = "userId", source = "user.userId")
    @Mapping(target = "sshCredentialId", source = "sshUserCredential.sshCredentialId")
    @Mapping(target = "batchJobConfig", source = "batchJobConfigs")
    BatchJobProcessResponseDto toResponseDto(BatchJobProcess entity);
}
