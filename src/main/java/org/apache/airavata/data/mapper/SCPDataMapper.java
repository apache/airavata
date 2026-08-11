package org.apache.airavata.data.mapper;

import org.apache.airavata.data.dto.SCPDataRequestDto;
import org.apache.airavata.data.dto.SCPDataResponseDto;
import org.apache.airavata.data.model.SCPDataEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

/**
 * Entity/DTO conversion for SCP data registrations.
 *
 * <p>The Slurm cluster credential and the owner are associations resolved by
 * {@code SCPDataService} from the request DTO's id and the caller's access token, so
 * neither is ever mapped straight off the request. {@code provisionStatus} is lifecycle
 * state the service manages directly, so it is ignored here too.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SCPDataMapper {

    @Mapping(target = "slurmClusterCredentialId", source = "slurmClusterCredential.clusterCredentialId")
    @Mapping(target = "ownerId", source = "owner.userId")
    SCPDataResponseDto toResponseDto(SCPDataEntity entity);

    @Mapping(target = "dataId", ignore = true)
    @Mapping(target = "slurmClusterCredential", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "provisionStatus", ignore = true)
    SCPDataEntity toEntity(SCPDataRequestDto dto);

    @Mapping(target = "dataId", ignore = true)
    @Mapping(target = "slurmClusterCredential", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "provisionStatus", ignore = true)
    void updateEntity(SCPDataRequestDto dto, @MappingTarget SCPDataEntity entity);
}
