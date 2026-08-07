package org.apache.airavata.credentials.mapper;

import org.apache.airavata.credentials.dto.SSHUserCredentialRequestDto;
import org.apache.airavata.credentials.dto.SSHUserCredentialResponseDto;
import org.apache.airavata.credentials.model.SSHUserCredential;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

/**
 * Entity/DTO conversions for SSH user credentials. The key association is resolved by the
 * service from {@code sshKeyId}, so it is never mapped off the request body.
 */
@Mapper(
        componentModel = "spring",
        uses = SSHKeyMapper.class,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SSHUserCredentialMapper {

    SSHUserCredentialResponseDto toResponseDto(SSHUserCredential entity);

    @Mapping(target = "sshCredentialId", ignore = true)
    @Mapping(target = "sshKey", ignore = true)
    SSHUserCredential toEntity(SSHUserCredentialRequestDto dto);

    @Mapping(target = "sshCredentialId", ignore = true)
    @Mapping(target = "sshKey", ignore = true)
    void updateEntity(SSHUserCredentialRequestDto dto, @MappingTarget SSHUserCredential entity);
}
