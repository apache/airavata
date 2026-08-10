package org.apache.airavata.application.mapper;

import org.apache.airavata.application.dto.deployment.BatchApplicationDeploymentRequestDto;
import org.apache.airavata.application.dto.deployment.BatchApplicationDeploymentResponseDto;
import org.apache.airavata.application.model.deployment.BatchApplicationDeploymentEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

/**
 * Entity/DTO conversions for batch application deployments.
 *
 * <p>The application template, the Slurm cluster and the default submission credential
 * are shared aggregates resolved by the service from their ids, so none of them is ever
 * mapped straight off the request. {@code batchJobConfig} is different: it is owned
 * outright by the deployment (no id to resolve), so it maps directly via
 * {@link BatchJobConfigMapper} — on update, MapStruct updates the existing nested entity
 * in place rather than replacing it, preserving its row across edits.
 */
@Mapper(
        componentModel = "spring",
        uses = BatchJobConfigMapper.class,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BatchApplicationDeploymentMapper {

    @Mapping(target = "templateId", source = "applicationTemplate.templateId")
    @Mapping(target = "slurmClusterId", source = "slurmCluster.clusterId")
    @Mapping(target = "defaultSubmissionCredentialId", source = "defaultSubmissionCredential.sshCredentialId")
    BatchApplicationDeploymentResponseDto toResponseDto(BatchApplicationDeploymentEntity entity);

    @Mapping(target = "deploymentId", ignore = true)
    @Mapping(target = "applicationTemplate", ignore = true)
    @Mapping(target = "slurmCluster", ignore = true)
    @Mapping(target = "defaultSubmissionCredential", ignore = true)
    BatchApplicationDeploymentEntity toEntity(BatchApplicationDeploymentRequestDto dto);

    @Mapping(target = "deploymentId", ignore = true)
    @Mapping(target = "applicationTemplate", ignore = true)
    @Mapping(target = "slurmCluster", ignore = true)
    @Mapping(target = "defaultSubmissionCredential", ignore = true)
    void updateEntity(
            BatchApplicationDeploymentRequestDto dto, @MappingTarget BatchApplicationDeploymentEntity entity);
}
