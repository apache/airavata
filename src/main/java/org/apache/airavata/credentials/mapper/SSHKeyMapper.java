package org.apache.airavata.credentials.mapper;

import org.apache.airavata.credentials.dto.SSHKeyRequestDto;
import org.apache.airavata.credentials.dto.SSHKeyResponseDto;
import org.apache.airavata.credentials.model.SSHKeyEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

/**
 * Entity/DTO conversions for SSH keys.
 *
 * <p>{@code toResponseDto} cannot leak the private key or passphrase because
 * {@link SSHKeyResponseDto} has nowhere to put them.
 *
 * <p>{@code updateEntity} ignores null incoming secrets ({@code IGNORE} rather than the
 * default {@code SET_TO_NULL}), so a client that round-trips a response — which never
 * carries those fields — cannot blank out a stored key by omission.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SSHKeyMapper {

    SSHKeyResponseDto toResponseDto(SSHKeyEntity entity);

    @Mapping(target = "sshKeyId", ignore = true)
    SSHKeyEntity toEntity(SSHKeyRequestDto dto);

    @Mapping(target = "sshKeyId", ignore = true)
    @Mapping(
            target = "privateKey",
            source = "privateKey",
            nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(
            target = "passphrase",
            source = "passphrase",
            nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(SSHKeyRequestDto dto, @MappingTarget SSHKeyEntity entity);
}
