package org.apache.airavata.application.mapper;

import org.apache.airavata.application.dto.deployment.SlurmApplicationDeploymentRequestDto;
import org.apache.airavata.application.dto.deployment.SlurmApplicationDeploymentResponseDto;
import org.apache.airavata.application.model.deployment.SlurmApplicationDeploymentEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

/**
 * Entity/DTO conversions for Slurm deployments. Both associations — the application
 * template and the Slurm cluster — are resolved by the service from their ids, so
 * neither is ever mapped straight off the request.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SlurmApplicationDeploymentMapper {

    @Mapping(target = "templateId", source = "applicationTemplate.templateId")
    @Mapping(target = "slurmClusterId", source = "slurmCluster.clusterId")
    SlurmApplicationDeploymentResponseDto toResponseDto(SlurmApplicationDeploymentEntity entity);

    @Mapping(target = "deploymentId", ignore = true)
    @Mapping(target = "applicationTemplate", ignore = true)
    @Mapping(target = "slurmCluster", ignore = true)
    SlurmApplicationDeploymentEntity toEntity(SlurmApplicationDeploymentRequestDto dto);

    @Mapping(target = "deploymentId", ignore = true)
    @Mapping(target = "applicationTemplate", ignore = true)
    @Mapping(target = "slurmCluster", ignore = true)
    void updateEntity(SlurmApplicationDeploymentRequestDto dto, @MappingTarget SlurmApplicationDeploymentEntity entity);
}
