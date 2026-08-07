package org.apache.airavata.credentials.service;

import java.util.List;
import org.apache.airavata.credentials.dto.SSHKeyRequestDto;
import org.apache.airavata.credentials.dto.SSHKeyResponseDto;
import org.apache.airavata.credentials.mapper.SSHKeyMapper;
import org.apache.airavata.credentials.model.SSHKeyEntity;
import org.apache.airavata.credentials.repository.SSHKeyRepository;
import org.apache.airavata.credentials.repository.SSHUserCredentialRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * CRUD for SSH key pairs.
 *
 * <p>The private key and passphrase are write-only across this whole layer: they can be
 * set and rotated, but no read path returns them.
 */
@Service
public class SSHKeyService {

    private final SSHKeyRepository keyRepository;
    private final SSHUserCredentialRepository credentialRepository;
    private final SSHKeyMapper mapper;

    public SSHKeyService(
            SSHKeyRepository keyRepository,
            SSHUserCredentialRepository credentialRepository,
            SSHKeyMapper mapper) {
        this.keyRepository = keyRepository;
        this.credentialRepository = credentialRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<SSHKeyResponseDto> getAllKeys() {
        return keyRepository.findAll().stream().map(mapper::toResponseDto).toList();
    }

    @Transactional(readOnly = true)
    public SSHKeyResponseDto getKey(String sshKeyId) {
        return mapper.toResponseDto(findOrThrow(sshKeyId));
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPER_ADMIN')")
    @Transactional
    public SSHKeyResponseDto createKey(SSHKeyRequestDto request) {
        // Required by the column but not by the request DTO, since update treats it as
        // optional — so create has to enforce it here rather than through validation.
        if (request.getPrivateKey() == null || request.getPrivateKey().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Private key is required when creating a key");
        }
        SSHKeyEntity entity = mapper.toEntity(request);
        return mapper.toResponseDto(keyRepository.save(entity));
    }

    /** Blank {@code privateKey}/{@code passphrase} leave the stored secrets untouched. */
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPER_ADMIN')")
    @Transactional
    public SSHKeyResponseDto updateKey(String sshKeyId, SSHKeyRequestDto request) {
        SSHKeyEntity entity = findOrThrow(sshKeyId);
        // MapStruct only skips nulls; normalize blanks to null so an empty string from a
        // form post is treated as "unchanged" too, not as a wiped secret.
        if (request.getPrivateKey() != null && request.getPrivateKey().isBlank()) {
            request.setPrivateKey(null);
        }
        if (request.getPassphrase() != null && request.getPassphrase().isBlank()) {
            request.setPassphrase(null);
        }
        mapper.updateEntity(request, entity);
        return mapper.toResponseDto(keyRepository.save(entity));
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPER_ADMIN')")
    @Transactional
    public void deleteKey(String sshKeyId) {
        SSHKeyEntity entity = findOrThrow(sshKeyId);
        if (credentialRepository.existsBySshKey_SshKeyId(sshKeyId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Key is in use by a credential and cannot be deleted: " + sshKeyId);
        }
        keyRepository.delete(entity);
    }

    private SSHKeyEntity findOrThrow(String sshKeyId) {
        return keyRepository
                .findById(sshKeyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "SSH key not found: " + sshKeyId));
    }
}
