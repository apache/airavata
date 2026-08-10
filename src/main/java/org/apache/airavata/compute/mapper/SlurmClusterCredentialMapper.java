package org.apache.airavata.compute.mapper;

import org.apache.airavata.compute.dto.SlurmClusterCredentialResponseDto;
import org.apache.airavata.compute.model.SlurmClusterCredentialEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * Entity/DTO conversion for Slurm cluster credential bindings.
 *
 * <p>There is no {@code toEntity}/{@code updateEntity} here: every field on
 * {@link SlurmClusterCredentialEntity} is an association (cluster, credential, user) and
 * every one of them is resolved by {@code SlurmClusterCredentialService} — from the
 * request DTO's ids, or from the caller's access token — so there is nothing left for a
 * mapper to write onto the entity.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SlurmClusterCredentialMapper {

    @Mapping(target = "clusterId", source = "slurmCluster.clusterId")
    @Mapping(target = "sshCredentialId", source = "sshUserCredential.sshCredentialId")
    @Mapping(target = "userId", source = "user.userId")
    SlurmClusterCredentialResponseDto toResponseDto(SlurmClusterCredentialEntity entity);
}
