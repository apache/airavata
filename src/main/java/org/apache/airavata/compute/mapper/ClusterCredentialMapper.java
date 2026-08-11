package org.apache.airavata.compute.mapper;

import org.apache.airavata.compute.dto.ClusterCredentialResponseDto;
import org.apache.airavata.compute.model.ClusterCredentialEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * Entity/DTO conversion for Slurm cluster credential bindings.
 *
 * <p>There is no {@code toEntity}/{@code updateEntity} here: every field on
 * {@link ClusterCredentialEntity} is an association (cluster, credential, user) and
 * every one of them is resolved by {@code ClusterCredentialService} — from the
 * request DTO's ids, or from the caller's access token — so there is nothing left for a
 * mapper to write onto the entity.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ClusterCredentialMapper {

    @Mapping(target = "clusterId", source = "slurmCluster.clusterId")
    @Mapping(target = "sshCredentialId", source = "sshUserCredential.sshCredentialId")
    @Mapping(target = "userId", source = "user.userId")
    ClusterCredentialResponseDto toResponseDto(ClusterCredentialEntity entity);
}
