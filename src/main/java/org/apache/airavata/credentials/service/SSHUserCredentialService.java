package org.apache.airavata.credentials.service;

import java.util.List;
import org.apache.airavata.credentials.dto.SSHUserCredentialRequestDto;
import org.apache.airavata.credentials.dto.SSHUserCredentialResponseDto;
import org.apache.airavata.credentials.mapper.SSHUserCredentialMapper;
import org.apache.airavata.credentials.model.SSHKeyEntity;
import org.apache.airavata.credentials.model.SSHUserCredential;
import org.apache.airavata.credentials.repository.SSHKeyRepository;
import org.apache.airavata.credentials.repository.SSHUserCredentialRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** CRUD for SSH user credentials, each pairing a username with a registered SSH key. */
@Service
public class SSHUserCredentialService {

    private final SSHUserCredentialRepository credentialRepository;
    private final SSHKeyRepository keyRepository;
    private final SSHUserCredentialMapper mapper;

    public SSHUserCredentialService(
            SSHUserCredentialRepository credentialRepository,
            SSHKeyRepository keyRepository,
            SSHUserCredentialMapper mapper) {
        this.credentialRepository = credentialRepository;
        this.keyRepository = keyRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<SSHUserCredentialResponseDto> getAllCredentials() {
        return credentialRepository.findAll().stream().map(mapper::toResponseDto).toList();
    }

    @Transactional(readOnly = true)
    public List<SSHUserCredentialResponseDto> getCredentialsByKey(String sshKeyId) {
        return credentialRepository.findBySshKey_SshKeyId(sshKeyId).stream()
                .map(mapper::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public SSHUserCredentialResponseDto getCredential(String sshCredentialId) {
        return mapper.toResponseDto(findOrThrow(sshCredentialId));
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPER_ADMIN')")
    @Transactional
    public SSHUserCredentialResponseDto createCredential(SSHUserCredentialRequestDto request) {
        SSHUserCredential entity = mapper.toEntity(request);
        entity.setSshKey(resolveKey(request.getSshKeyId()));
        return mapper.toResponseDto(credentialRepository.save(entity));
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPER_ADMIN')")
    @Transactional
    public SSHUserCredentialResponseDto updateCredential(
            String sshCredentialId, SSHUserCredentialRequestDto request) {
        SSHUserCredential entity = findOrThrow(sshCredentialId);
        mapper.updateEntity(request, entity);
        entity.setSshKey(resolveKey(request.getSshKeyId()));
        return mapper.toResponseDto(credentialRepository.save(entity));
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPER_ADMIN')")
    @Transactional
    public void deleteCredential(String sshCredentialId) {
        credentialRepository.delete(findOrThrow(sshCredentialId));
    }

    private SSHUserCredential findOrThrow(String sshCredentialId) {
        return credentialRepository
                .findById(sshCredentialId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "SSH credential not found: " + sshCredentialId));
    }

    private SSHKeyEntity resolveKey(String sshKeyId) {
        return keyRepository
                .findById(sshKeyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "SSH key not found: " + sshKeyId));
    }
}
